use std::collections::HashMap;

use crate::analyzer::{AnalyzedNode, NodeTag};
use crate::ast::{AstNode, Breadcrumbs, FileNode};
use crate::core_globals::core_global_file;
use crate::types::*;

#[derive(Debug, Clone)]
pub struct ExternalFileDescriptor {
    pub exports: HashMap<String, ValueLangType>,
}

#[derive(Debug, Clone)]
pub struct FileResolverInput {
    pub path: String,
    pub file_node: AstNode<FileNode>,
    pub analyzed: AnalyzedNode,
    pub other_files: HashMap<String, ExternalFileDescriptor>,
}
pub fn resolve_for_file(input: FileResolverInput) {
    let FileResolverInput {
        path,
        file_node,
        analyzed,
        other_files,
    } = input;

    let mut other_files = other_files.clone();
    let core_global_file = core_global_file();
    other_files.insert(core_global_file.0, core_global_file.1);

    let AnalyzedNode { node_tags, .. } = analyzed;

    #[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
    enum ResolutionType {
        Inferred,
        Constraint,
    }

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

                if let None = found {
                    iteration_resolved_references.push((
                        (ResolutionType::Inferred, $x.to_owned()),
                        ValueLangType::Pending,
                    ));
                }
                found
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
                for tag in pending_tags {
                    match &pending_key.0 {
                        ResolutionType::Inferred => match tag {
                            NodeTag::ValueComesFrom(comes_from) => {
                                match get_resolved_type_of!(comes_from) {
                                    Some(ValueLangType::Pending) => (),
                                    Some(ValueLangType::Resolved(resolved_with)) => {
                                        iteration_resolved_references.push((
                                            pending_key.to_owned(),
                                            ValueLangType::Resolved(resolved_with.to_owned()),
                                        ));
                                    }
                                    Some(ValueLangType::Error(_)) => {
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
                                    None => {}
                                }
                            }

                            NodeTag::Calls(calls) => {
                                match get_resolved_type_of!(calls) {
                                    Some(ValueLangType::Pending) => (),
                                    Some(ValueLangType::Resolved(
                                        ResolvedValueLangType::Function(function),
                                    )) => iteration_resolved_references.push((
                                        pending_key.to_owned(),
                                        function.return_type.as_ref().to_owned(),
                                    )),
                                    Some(ValueLangType::Resolved(
                                        ResolvedValueLangType::Reference(type_id),
                                    )) => {
                                        let lang_type = get_type!(type_id);

                                        match lang_type {
                                            CanonicalLangType::Signal(signal) => {
                                                iteration_resolved_references.push((
                                                    pending_key.to_owned(),
                                                    ValueLangType::Resolved(
                                                        ResolvedValueLangType::Instance(
                                                            signal.id.to_owned(),
                                                        ),
                                                    ),
                                                ));
                                            }
                                        }
                                    }
                                    Some(ValueLangType::Resolved(_)) => {
                                        iteration_resolved_references.push((
                                            pending_key.to_owned(),
                                            ValueLangType::Error(LangTypeError::NotCallable),
                                        ))
                                    }
                                    Some(ValueLangType::Error(_)) => {
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
                                    None => {}
                                };
                            }

                            NodeTag::Causes(causes) => match get_resolved_type_of!(causes) {
                                Some(ValueLangType::Pending) => (),
                                Some(ValueLangType::Resolved(ResolvedValueLangType::Instance(
                                    type_id,
                                ))) => {
                                    let instance_type = get_type!(type_id);
                                    match instance_type {
                                        CanonicalLangType::Signal(signal) => {
                                            iteration_resolved_references.push((
                                                pending_key.to_owned(),
                                                signal.result.as_ref().to_owned(),
                                            ))
                                        }
                                    }
                                }
                                Some(ValueLangType::Resolved(_)) => iteration_resolved_references
                                    .push((
                                        pending_key.to_owned(),
                                        ValueLangType::Error(LangTypeError::NotCausable),
                                    )),
                                Some(ValueLangType::Error(_)) => {
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
                                None => {}
                            },

                            NodeTag::NamedValue {
                                type_declaration,
                                value,
                                ..
                            } => {
                                if let Some(type_declaration_breadcrumbs) = type_declaration {
                                    match get_resolved_type_of!(type_declaration_breadcrumbs) {
                                        Some(ValueLangType::Pending) => (),
                                        Some(ValueLangType::Resolved(resolved_with)) => {
                                            match resolved_with {
                                                ResolvedValueLangType::FunctionType(_) => todo!(),
                                                ResolvedValueLangType::PrimitiveType(primitive) => {
                                                    iteration_resolved_references.push((
                                                        pending_key.to_owned(),
                                                        ValueLangType::Resolved(
                                                            ResolvedValueLangType::Primitive(
                                                                *primitive,
                                                            ),
                                                        ),
                                                    ))
                                                }
                                                ResolvedValueLangType::Reference(_) => todo!(),
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
                                            }
                                        }
                                        Some(ValueLangType::Error(_)) => {
                                            iteration_resolved_references.push((
                                                pending_key.to_owned(),
                                                ValueLangType::Error(LangTypeError::ProxyError {
                                                    caused_by: ErrorSourcePosition::SameFile {
                                                        path: path.to_owned(),
                                                        breadcrumbs: type_declaration_breadcrumbs
                                                            .to_owned(),
                                                    },
                                                }),
                                            ))
                                        }
                                        None => {}
                                    }
                                } else {
                                    match get_resolved_type_of!(value) {
                                        Some(ValueLangType::Pending) => (),
                                        Some(ValueLangType::Resolved(resolved_with)) => {
                                            iteration_resolved_references.push((
                                                pending_key.to_owned(),
                                                ValueLangType::Resolved(resolved_with.to_owned()),
                                            ));
                                        }
                                        Some(ValueLangType::Error(_)) => {
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
                                        None => {}
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
                                        if let Some(return_type) =
                                            get_resolved_type_of!(return_type_breadcrumbs)
                                        {
                                            return_type.to_owned()
                                        } else {
                                            ValueLangType::Pending
                                        }
                                    }
                                    _ => ValueLangType::Error(LangTypeError::ImplementationTodo { description: "Can't infer a function that can return from multiple locations".into() })
                                };

                                iteration_resolved_references.push((
                                    pending_key.to_owned(),
                                    ValueLangType::Resolved(ResolvedValueLangType::Function(
                                        FunctionValueLangType {
                                            name: name.to_owned(),
                                            return_type: Box::new(return_type),
                                        },
                                    )),
                                ))
                            }

                            NodeTag::ReferencesFile {
                                path,
                                export_name: Some(export_name),
                            } => {
                                if let Some(file) = other_files.get(path) {
                                    if let Some(export) = file.exports.get(export_name) {
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

                            NodeTag::ReferenceNotInScope => iteration_resolved_references.push((
                                pending_key.to_owned(),
                                ValueLangType::Error(LangTypeError::NotInScope),
                            )),

                            _ => (),
                        },
                        ResolutionType::Constraint => match tag {
                            NodeTag::BasicConstraint { type_annotation } => {
                                match get_resolved_type_of!(type_annotation) {
                                    Some(ValueLangType::Pending) => {}
                                    Some(ValueLangType::Resolved(type_annotation)) => {
                                        resolve_with!(ValueLangType::Resolved(
                                            type_annotation.to_owned()
                                        ));
                                    }
                                    Some(ValueLangType::Error(_)) => {
                                        resolve_with!(ValueLangType::Error(
                                            LangTypeError::ProxyError {
                                                caused_by: ErrorSourcePosition::SameFile {
                                                    path: path.to_owned(),
                                                    breadcrumbs: type_annotation.to_owned()
                                                }
                                            }
                                        ))
                                    }
                                    None => {}
                                }
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
                        ValueLangType::Resolved(ResolvedValueLangType::Reference(id.to_owned())),
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
            ValueLangType::Resolved(expected_type_reference) => {
                match expected_type_reference.get_instance_type() {
                    Ok(expected_type) => Some(expected_type),
                    Err(_) => None,
                }
            }
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
            if expected_type != actual_type {
                constraint_modified_references.push((
                    (ResolutionType::Inferred, constrained_breadcrumbs.to_owned()),
                    ValueLangType::Error(LangTypeError::MismatchedType {
                        expected: expected_type,
                        actual: actual_type,
                    }),
                ))
            }
        }
    }
    for modified in constraint_modified_references {
        resolved_types.insert(modified.0, modified.1);
    }

    println!("{resolved_types:#?}");
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

          fn main() {
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
            file_node: ast_node,
            analyzed: analyzed_file,
            other_files,
        });
    }
}