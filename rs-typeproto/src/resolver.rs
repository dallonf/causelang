use std::collections::HashMap;
use std::fmt::Write;
use std::iter;

use serde::{Serialize, Deserialize};

use crate::analyzer::{AnalyzedNode, ArgumentTag, NodeTag};
use crate::ast::{AstNode, Breadcrumbs, FileNode};
use crate::breadcrumb_walk::BreadcrumbWalk;
use crate::core_builtin::core_builtin_file;
use crate::types::*;

#[derive(Debug, Clone)]
pub struct ExternalFileDescriptor {
    pub exports: HashMap<String, ValueLangType>,
}

#[derive(Debug, Clone)]
pub struct FileResolverInput<'a> {
    pub path: &'a str,
    pub file_node: &'a AstNode<FileNode>,
    pub analyzed: &'a AnalyzedNode,
    pub other_files: HashMap<String, ExternalFileDescriptor>,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub enum ResolutionType {
    Inferred,
    Constraint,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ResolvedFile {
    pub path: String,
    pub resolved_types: HashMap<(ResolutionType, Breadcrumbs), ValueLangType>,
    pub canonical_types: HashMap<CanonicalLangTypeId, CanonicalLangType>,
}

pub fn resolve_for_file(input: FileResolverInput) -> ResolvedFile {
    let FileResolverInput {
        path,
        file_node,
        analyzed,
        other_files,
    } = input;

    let mut other_files = other_files.clone();
    let core_global_file = core_builtin_file();
    other_files.insert(core_global_file.0, core_global_file.1);

    let AnalyzedNode { node_tags, .. } = analyzed;

    let mut resolved_types = HashMap::<(ResolutionType, Breadcrumbs), ValueLangType>::new();
    let mut known_canonical_types = HashMap::<CanonicalLangTypeId, CanonicalLangType>::new();

    // start by resolving all expressions, top-level declarations, and constraints
    for (node_breadcrumbs, tags) in node_tags.iter() {
        for tag in tags.iter() {
            match tag {
                NodeTag::Expression => {
                    resolved_types.insert(
                        (ResolutionType::Inferred, node_breadcrumbs.to_owned()),
                        ValueLangType::Pending,
                    );
                }
                NodeTag::BasicConstraint { .. } => {
                    resolved_types.insert(
                        (ResolutionType::Constraint, node_breadcrumbs.to_owned()),
                        ValueLangType::Pending,
                    );
                    resolved_types.insert(
                        (ResolutionType::Inferred, node_breadcrumbs.to_owned()),
                        ValueLangType::Pending,
                    );
                }
                NodeTag::ArgumentForCall(_) => {
                    resolved_types.insert(
                        (ResolutionType::Constraint, node_breadcrumbs.to_owned()),
                        ValueLangType::Pending,
                    );
                    resolved_types.insert(
                        (ResolutionType::Inferred, node_breadcrumbs.to_owned()),
                        ValueLangType::Pending,
                    );
                }
                _ => {}
            }
        }
    }
    if let Some(file_tags) = node_tags.get(&file_node.breadcrumbs) {
        for file_tag in file_tags.iter() {
            match file_tag {
                NodeTag::ScopeContainsDeclaration(declaration) => {
                    resolved_types.insert(
                        (ResolutionType::Inferred, declaration.to_owned()),
                        ValueLangType::Pending,
                    );
                }
                _ => {}
            }
        }
    }

    macro_rules! get_type {
        ( $x:expr ) => {
            known_canonical_types
                .get($x)
                .expect("If a reference exists, the type should be known")
        };
    }

    // try to resolve all references

    loop {
        let mut iteration_resolved_references =
            Vec::<((ResolutionType, Breadcrumbs), ValueLangType)>::new();

        macro_rules! get_resolved_type_of {
            ( $x:expr ) => {{
                let found = resolved_types
                    .get(&(ResolutionType::Constraint, $x.to_owned()))
                    .or_else(|| resolved_types.get(&(ResolutionType::Inferred, $x.to_owned())));

                if let Some(found) = found {
                    found.to_owned()
                } else {
                    iteration_resolved_references.push((
                        (ResolutionType::Inferred, $x.to_owned()),
                        ValueLangType::Pending,
                    ));
                    ValueLangType::Pending
                }
            }};
        }

        let pending_references =
            resolved_types
                .iter()
                .filter_map(|it| if it.1.is_pending() { Some(it.0) } else { None });
        for pending_key in pending_references {
            macro_rules! resolve_with {
                ( $x: expr ) => {{
                    iteration_resolved_references.push((pending_key.to_owned(), $x));
                }};
            }

            if let Some(pending_tags) = node_tags.get(&pending_key.1) {
                'each_tag: for tag in pending_tags {
                    match &pending_key.0 {
                        ResolutionType::Inferred => match tag {
                            NodeTag::ValueComesFrom(comes_from) => {
                                match get_resolved_type_of!(comes_from) {
                                    ValueLangType::Pending => (),
                                    ValueLangType::Resolved(resolved_with) => {
                                        iteration_resolved_references.push((
                                            pending_key.to_owned(),
                                            ValueLangType::Resolved(resolved_with.to_owned()),
                                        ));
                                    }
                                    ValueLangType::Error(_) => {
                                        iteration_resolved_references.push((
                                            pending_key.to_owned(),
                                            ValueLangType::Error(LangTypeError::ProxyError {
                                                caused_by: ErrorSourcePosition::SameFile {
                                                    path: path.to_owned(),
                                                    breadcrumbs: comes_from.to_owned(),
                                                },
                                            }),
                                        ));
                                    }
                                }
                            }

                            NodeTag::Calls(calls) => {
                                match get_resolved_type_of!(calls) {
                                    ValueLangType::Pending => (),
                                    ValueLangType::Resolved(ResolvedValueLangType::Function(
                                        function,
                                    )) => iteration_resolved_references.push((
                                        pending_key.to_owned(),
                                        function.return_type.as_ref().to_owned(),
                                    )),
                                    ValueLangType::Resolved(
                                        ResolvedValueLangType::TypeReference(type_id),
                                    ) => {
                                        let lang_type = get_type!(&type_id);

                                        match lang_type {
                                            CanonicalLangType::Signal(signal) => {
                                                let mut args: Vec<Option<ValueLangType>> =
                                                    iter::repeat(None)
                                                        .take(signal.params.len())
                                                        .collect();

                                                let argument_tags: Option<Vec<_>> =
                                                    node_tags.get(&pending_key.1).map(|tags| {
                                                        tags.iter()
                                                            .filter_map(|it| match it {
                                                                NodeTag::CallsWithArgument(it) => {
                                                                    Some(it)
                                                                }
                                                                _ => None,
                                                            })
                                                            .collect()
                                                    });
                                                if let Some(argument_tags) = argument_tags {
                                                    for (argument_breadcrumbs, argument_tag) in
                                                        argument_tags
                                                    {
                                                        let argument_type = get_resolved_type_of!(
                                                            argument_breadcrumbs
                                                        );
                                                        if let Some(_) = argument_type.get_error() {
                                                            resolve_with!(ValueLangType::Error(
                                                                LangTypeError::ProxyError {
                                                                    caused_by: ErrorSourcePosition::SameFile { 
                                                                        path: path.to_owned(),
                                                                        breadcrumbs: argument_breadcrumbs.to_owned() 
                                                                    }
                                                                }
                                                            ));
                                                            continue 'each_tag;
                                                        }
                                                        match argument_tag {
                                                            ArgumentTag::Positional(i) => {
                                                                if *i < args.len() {
                                                                    args[*i] = Some(argument_type);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                let mut found_args = Vec::new();
                                                let mut missing_args = Vec::new();
                                                for (i, arg) in args.into_iter().enumerate() {
                                                    if let Some(arg) = arg {
                                                        found_args.push(arg);
                                                    } else {
                                                        missing_args.push(&signal.params[i]);
                                                    }
                                                }

                                                if missing_args.len() > 0 {
                                                    resolve_with!(ValueLangType::Error(
                                                        LangTypeError::MissingArguments(
                                                            missing_args
                                                                .into_iter()
                                                                .map(|it| it.name.to_owned())
                                                                .collect()
                                                        )
                                                    ));
                                                } else if found_args
                                                    .iter()
                                                    .all(|it| !it.is_pending())
                                                {
                                                    resolve_with!(ValueLangType::Resolved(
                                                        ResolvedValueLangType::Instance(
                                                            signal.id.to_owned(),
                                                        ),
                                                    ));
                                                }
                                            }
                                        }
                                    }
                                    ValueLangType::Resolved(_) => iteration_resolved_references
                                        .push((
                                            pending_key.to_owned(),
                                            ValueLangType::Error(LangTypeError::NotCallable),
                                        )),
                                    ValueLangType::Error(_) => {
                                        iteration_resolved_references.push((
                                            pending_key.to_owned(),
                                            ValueLangType::Error(LangTypeError::ProxyError {
                                                caused_by: ErrorSourcePosition::SameFile {
                                                    path: path.to_owned(),
                                                    breadcrumbs: calls.to_owned(),
                                                },
                                            }),
                                        ));
                                    }
                                };
                            }

                            NodeTag::Causes(causes) => match get_resolved_type_of!(causes) {
                                ValueLangType::Pending => (),
                                ValueLangType::Resolved(ResolvedValueLangType::Instance(
                                    type_id,
                                )) => {
                                    let instance_type = get_type!(&type_id);
                                    match instance_type {
                                        CanonicalLangType::Signal(signal) => {
                                            iteration_resolved_references.push((
                                                pending_key.to_owned(),
                                                signal.result.as_ref().to_owned(),
                                            ))
                                        }
                                    }
                                }
                                ValueLangType::Resolved(_) => iteration_resolved_references.push((
                                    pending_key.to_owned(),
                                    ValueLangType::Error(LangTypeError::NotCausable),
                                )),
                                ValueLangType::Error(_) => {
                                    iteration_resolved_references.push((
                                        pending_key.to_owned(),
                                        ValueLangType::Error(LangTypeError::ProxyError {
                                            caused_by: ErrorSourcePosition::SameFile {
                                                path: path.to_owned(),
                                                breadcrumbs: causes.to_owned(),
                                            },
                                        }),
                                    ));
                                }
                            },

                            NodeTag::NamedValue {
                                type_declaration,
                                value,
                                ..
                            } => {
                                if let Some(type_declaration_breadcrumbs) = type_declaration {
                                    match get_resolved_type_of!(type_declaration_breadcrumbs) {
                                        ValueLangType::Pending => (),
                                        ValueLangType::Resolved(resolved_with) => {
                                            match resolved_with {
                                                ResolvedValueLangType::FunctionType(_) => todo!(),
                                                ResolvedValueLangType::PrimitiveType(primitive) => {
                                                    iteration_resolved_references.push((
                                                        pending_key.to_owned(),
                                                        ValueLangType::Resolved(
                                                            ResolvedValueLangType::Primitive(
                                                                primitive,
                                                            ),
                                                        ),
                                                    ))
                                                }
                                                ResolvedValueLangType::TypeReference(_) => todo!(),
                                                ResolvedValueLangType::Canonical(_) => todo!(),
                                                ResolvedValueLangType::Function(_)
                                                | ResolvedValueLangType::Primitive(_)
                                                | ResolvedValueLangType::Instance(_) => {
                                                    iteration_resolved_references.push((
                                                        pending_key.to_owned(),
                                                        ValueLangType::Error(
                                                            LangTypeError::NotATypeReference {
                                                                actual: resolved_with.to_owned(),
                                                            },
                                                        ),
                                                    ))
                                                }
                                                ResolvedValueLangType::BadValue => {
                                                    resolve_with!(ValueLangType::Resolved(
                                                        ResolvedValueLangType::BadValue
                                                    ))
                                                }
                                                ResolvedValueLangType::NeverContinues => {
                                                    resolve_with!(ValueLangType::Resolved(
                                                        ResolvedValueLangType::NeverContinues
                                                    ))
                                                }
                                            }
                                        }
                                        ValueLangType::Error(_) => iteration_resolved_references
                                            .push((
                                                pending_key.to_owned(),
                                                ValueLangType::Error(LangTypeError::ProxyError {
                                                    caused_by: ErrorSourcePosition::SameFile {
                                                        path: path.to_owned(),
                                                        breadcrumbs: type_declaration_breadcrumbs
                                                            .to_owned(),
                                                    },
                                                }),
                                            )),
                                    }
                                } else {
                                    match get_resolved_type_of!(value) {
                                        ValueLangType::Pending => (),
                                        ValueLangType::Resolved(resolved_with) => {
                                            iteration_resolved_references.push((
                                                pending_key.to_owned(),
                                                ValueLangType::Resolved(resolved_with.to_owned()),
                                            ));
                                        }
                                        ValueLangType::Error(_) => {
                                            iteration_resolved_references.push((
                                                pending_key.to_owned(),
                                                ValueLangType::Error(LangTypeError::ProxyError {
                                                    caused_by: ErrorSourcePosition::SameFile {
                                                        path: path.to_owned(),
                                                        breadcrumbs: value.to_owned(),
                                                    },
                                                }),
                                            ));
                                        }
                                    }
                                }
                            }

                            NodeTag::IsPrimitiveValue(primitive) => iteration_resolved_references
                                .push((
                                    pending_key.to_owned(),
                                    ValueLangType::Resolved(ResolvedValueLangType::Primitive(
                                        primitive.to_owned(),
                                    )),
                                )),

                            NodeTag::IsFunction { name } => {
                                let can_return = pending_tags
                                    .iter()
                                    .filter_map(|it| {
                                        if let NodeTag::FunctionCanReturnTypeOf(
                                            return_breadcrumbs,
                                        ) = it
                                        {
                                            Some(return_breadcrumbs)
                                        } else {
                                            None
                                        }
                                    })
                                    .collect::<Vec<_>>();

                                let return_type = match can_return.len() {
                                    0 => ValueLangType::Pending,
                                    1 => {
                                        let return_type_breadcrumbs: &Breadcrumbs =
                                            *can_return.get(0).unwrap();
                                        get_resolved_type_of!(return_type_breadcrumbs)
                                    }
                                    _ => ValueLangType::Error(LangTypeError::ImplementationTodo { description: "Can't infer a function that can return from multiple locations".into() })
                                };

                                iteration_resolved_references.push((
                                    pending_key.to_owned(),
                                    ValueLangType::Resolved(ResolvedValueLangType::Function(
                                        FunctionValueLangType {
                                            name: name.to_owned(),
                                            return_type: Box::new(return_type),
                                            // TODO
                                            params: vec![],
                                        },
                                    )),
                                ))
                            }

                            NodeTag::ReferencesFile {
                                path,
                                export_name: Some(export_name),
                            } => {
                                if let Some(file) = other_files.get(path.as_str()) {
                                    if let Some(export) = file.exports.get(export_name.as_str()) {
                                        iteration_resolved_references
                                            .push((pending_key.to_owned(), export.to_owned()))
                                    } else {
                                        iteration_resolved_references.push((
                                            pending_key.to_owned(),
                                            ValueLangType::Error(LangTypeError::ExportNotFound),
                                        ));
                                    }
                                } else {
                                    iteration_resolved_references.push((
                                        pending_key.to_owned(),
                                        ValueLangType::Error(LangTypeError::FileNotFound),
                                    ));
                                }
                            }

                            NodeTag::ReferencesFile {
                                path: _path,
                                export_name: None,
                            } => {
                                // TODO: I guess eventually we'll probably want to import whole files,
                                // but we don't really have a "file reference" type yet so meh
                                resolve_with!(ValueLangType::Resolved(
                                    ResolvedValueLangType::Primitive(PrimitiveLangType::Action)
                                ))
                            }

                            NodeTag::ReferenceNotInScope => iteration_resolved_references.push((
                                pending_key.to_owned(),
                                ValueLangType::Error(LangTypeError::NotInScope),
                            )),

                            _ => (),
                        },

                        ResolutionType::Constraint => match tag {
                            NodeTag::BasicConstraint { type_annotation } => {
                                match get_resolved_type_of!(type_annotation) {
                                    ValueLangType::Pending => {}
                                    ValueLangType::Resolved(type_annotation) => {
                                        match type_annotation.get_instance_type() {
                                            Ok(expected_type) => resolve_with!(
                                                ValueLangType::Resolved(expected_type.to_owned())
                                            ),
                                            Err(_) => resolve_with!(ValueLangType::Error(
                                                LangTypeError::NotATypeReference {
                                                    actual: type_annotation
                                                }
                                            )),
                                        }
                                    }
                                    ValueLangType::Error(_) => {
                                        resolve_with!(ValueLangType::Error(
                                            LangTypeError::ProxyError {
                                                caused_by: ErrorSourcePosition::SameFile {
                                                    path: path.to_owned(),
                                                    breadcrumbs: type_annotation.to_owned()
                                                }
                                            }
                                        ))
                                    }
                                }
                            }

                            NodeTag::ArgumentForCall((call, argument_tag)) => {
                                let call_type = node_tags
                                    .get(&call)
                                    .expect("Missing tags for call expression")
                                    .iter()
                                    .find_map(|call_tag| match call_tag {
                                        NodeTag::Calls(calls) => Some(get_resolved_type_of!(calls)),
                                        _ => None,
                                    })
                                    .expect("Missing call expression");

                                match call_type {
                                    ValueLangType::Pending => {}
                                    ValueLangType::Resolved(call_type) => {
                                        let params = match call_type {
                                            ResolvedValueLangType::Function(function) => function.params,
                                            ResolvedValueLangType::TypeReference(id) => {
                                                match get_type!(&id) {
                                                    CanonicalLangType::Signal(signal) => signal.params.to_owned(),
                                                }
                                            },
                                            _ => panic!("Call expression not callable (should have been caught by call resolution)")
                                        };

                                        match argument_tag {
                                            ArgumentTag::Positional(i) => {
                                                if *i >= params.len() {
                                                    resolve_with!(ValueLangType::Error(
                                                        LangTypeError::ExcessArgument {
                                                            expected: params.len()
                                                        }
                                                    ))
                                                } else {
                                                    let param = &params[*i];
                                                    resolve_with!(param.value_type.to_owned());
                                                }
                                            }
                                        }
                                    }
                                    ValueLangType::Error(_) => resolve_with!(ValueLangType::Error(
                                        LangTypeError::ProxyError {
                                            caused_by: ErrorSourcePosition::SameFile {
                                                path: path.to_owned(),
                                                breadcrumbs: call.to_owned()
                                            }
                                        }
                                    )),
                                };
                            }
                            _ => (),
                        },
                    }
                }
            }
        }

        let resolved = iteration_resolved_references.len();
        for (breadcrumbs, new_type) in iteration_resolved_references {
            if let Some(old_resolved_type) = resolved_types.get(&breadcrumbs).and_then(|it| {
                if it.is_pending() {
                    None
                } else {
                    Some(it)
                }
            }) {
                panic!("Accidentally clobbered a resolved reference ({breadcrumbs:?} = {old_resolved_type:?}) with {new_type:?}")
            }
            match new_type {
                ValueLangType::Resolved(ResolvedValueLangType::Canonical(canonical)) => {
                    let id = canonical.id();
                    known_canonical_types.insert(id.to_owned(), canonical.to_owned());
                    resolved_types.insert(
                        breadcrumbs,
                        ValueLangType::Resolved(ResolvedValueLangType::TypeReference(
                            id.to_owned(),
                        )),
                    );
                }
                _ => {
                    resolved_types.insert(breadcrumbs, new_type);
                }
            }
        }
        if resolved == 0 {
            break;
        }
    }

    // handle constraints
    let mut constraint_modified_references =
        Vec::<((ResolutionType, Breadcrumbs), ValueLangType)>::new();

    let constraints = resolved_types.iter().filter(|it| match it {
        ((ResolutionType::Constraint, _), _) => true,
        _ => false,
    });
    for constraint in constraints {
        let ((_, constrained_breadcrumbs), expected_type_reference) = constraint;

        let expected_type = match expected_type_reference {
            ValueLangType::Resolved(expected_type_reference) => Some(expected_type_reference),
            ValueLangType::Pending => None,
            ValueLangType::Error(_) => None,
        };

        let actual_type = match resolved_types
            .get(&(ResolutionType::Inferred, constrained_breadcrumbs.to_owned()))
        {
            Some(ValueLangType::Resolved(resolved)) => Some(resolved.to_owned()),
            Some(_) => None,
            None => None,
        };

        if let (Some(expected_type), Some(actual_type)) = (expected_type, actual_type) {
            if expected_type != &actual_type {
                constraint_modified_references.push((
                    (ResolutionType::Inferred, constrained_breadcrumbs.to_owned()),
                    ValueLangType::Error(LangTypeError::MismatchedType {
                        expected: expected_type.to_owned(),
                        actual: actual_type,
                    }),
                ))
            }
        }
    }
    for modified in constraint_modified_references {
        resolved_types.insert(modified.0, modified.1);
    }

    ResolvedFile {
        path: path.into(),
        resolved_types,
        canonical_types: known_canonical_types,
    }
}

#[derive(Debug, Clone, Serialize)]
pub struct ResolverError {
    pub file_path: String,
    pub location: Breadcrumbs,
    pub error: LangTypeError,
}

impl ResolvedFile {
    pub fn get_errors(&self) -> Vec<ResolverError> {
        self.resolved_types
            .iter()
            .filter_map(|((_, breadcrumbs), resolved_type)| match resolved_type {
                ValueLangType::Resolved(_) => None,
                ValueLangType::Error(LangTypeError::ProxyError { .. }) => None,
                ValueLangType::Error(error) => Some(ResolverError {
                    file_path: self.path.to_owned(),
                    location: breadcrumbs.to_owned(),
                    error: error.to_owned(),
                }),
                ValueLangType::Pending => Some(ResolverError {
                    file_path: self.path.to_owned(),
                    location: breadcrumbs.to_owned(),
                    error: LangTypeError::NeverResolved,
                }),
            })
            .collect()
    }
}

impl ResolverError {
    pub fn format_error(
        &self,
        source: &str,
        ast: &AstNode<FileNode>,
        destination: &mut impl Write,
    ) -> Result<(), std::fmt::Error> {
        writeln!(destination, "{:#?} at", self.error)?;

        let node = ast.node.find_node(&self.location);
        let line = node.position.start.line;
        let start_line = {
            if line > 2 {
                line - 2
            } else {
                0
            }
        };

        for line in source.lines().skip(start_line).take(line - start_line) {
            writeln!(destination, "{line}")?;
        }
        let col = node.position.start.col;
        let prefix_length = if col > 1 { col - 1 } else { 0 };
        let prefix: String = (0..prefix_length).map(|_| '-').collect();
        writeln!(destination, "{prefix}^ line {}, col {}", line, col)?;
        for line in source.lines().skip(line).take(2) {
            writeln!(destination, "{line}")?;
        }

        Ok(())
    }
}

#[cfg(test)]
mod test {
    use crate::analyzer;
    use crate::parse;

    use super::*;

    #[test]
    fn hello_world() {
        let script = r#"
          import langtest/signals { Print }

          function main() {
              cause Print(15)
          }
        "#;

        let ast_node = parse::parse(script).unwrap();
        let analyzed_file = analyzer::analyze_file(&ast_node);

        println!("node tags: {:#?}", analyzed_file.node_tags);

        let other_files = HashMap::from_iter(vec![(
            "langtest/signals".to_string(),
            ExternalFileDescriptor {
                exports: HashMap::from_iter(vec![(
                    "Print".to_string(),
                    ValueLangType::Resolved(ResolvedValueLangType::Canonical(
                        CanonicalLangType::Signal(SignalCanonicalLangType {
                            id: CanonicalLangTypeId {
                                path: "langtest/signals".into(),
                                parent_name: None,
                                name: Some("Print".into()),
                                number: 0,
                            },
                            name: "Print".into(),
                            params: vec![LangParameter {
                                name: "message".into(),
                                value_type: ValueLangType::Resolved(
                                    ResolvedValueLangType::Primitive(PrimitiveLangType::String),
                                ),
                            }],
                            result: Box::new(ValueLangType::Resolved(
                                ResolvedValueLangType::Primitive(PrimitiveLangType::Action),
                            )),
                        }),
                    )),
                )]),
            },
        )]);

        resolve_for_file(FileResolverInput {
            path: "test.cau".into(),
            file_node: &ast_node,
            analyzed: &analyzed_file,
            other_files,
        });
    }
}
