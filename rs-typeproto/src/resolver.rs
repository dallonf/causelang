use std::collections::HashMap;

use crate::analyzer::{AnalyzedNode, NodeTag};
use crate::ast::{AstNode, Breadcrumbs, FileNode};
use crate::types::*;

#[derive(Debug, Clone)]
pub struct ExternalFileDescriptor {
    pub exports: HashMap<String, ValueLangType>,
}

#[derive(Debug, Clone)]
pub struct FileResolverInput {
    path: String,
    file_node: AstNode<FileNode>,
    analyzed: AnalyzedNode,
    other_files: HashMap<String, ExternalFileDescriptor>,
}
pub fn resolve_for_file(input: FileResolverInput) {
    let FileResolverInput {
        path,
        file_node,
        analyzed,
        other_files,
    } = input;
    let AnalyzedNode { node_tags, .. } = analyzed;

    let mut resolved_types = HashMap::<Breadcrumbs, ValueLangType>::new();

    // start by resolving all expressions and top-level declarations
    for (breadcrumbs, tags) in node_tags.iter() {
        if tags.iter().any(|it| match it {
            NodeTag::TopLevelDeclaration | NodeTag::Expression => true,
            _ => false,
        }) {
            resolved_types.insert(breadcrumbs.to_owned(), ValueLangType::Pending);
        }
    }

    loop {
        let mut iteration_resolved_references = Vec::<(Breadcrumbs, ValueLangType)>::new();

        let pending_references = resolved_types.iter().filter_map(|it| match it.1 {
            ValueLangType::Pending => Some(it.0),
            _ => None,
        });
        for pending in pending_references {
            if let Some(tags) = node_tags.get(pending) {
                for tag in tags {
                    match tag {
                        NodeTag::ValueComesFrom(comes_from) => {
                            match resolved_types.get(comes_from) {
                                Some(ValueLangType::Pending) => (),
                                Some(ValueLangType::Resolved(resolved_with)) => {
                                    iteration_resolved_references.push((
                                        pending.to_owned(),
                                        ValueLangType::Resolved(resolved_with.to_owned()),
                                    ));
                                }
                                Some(ValueLangType::Error(_)) => {
                                    iteration_resolved_references.push((
                                        pending.to_owned(),
                                        ValueLangType::Error(LangTypeError::ProxyError {
                                            caused_by: ErrorSourcePosition::SameFile {
                                                path: path.to_owned(),
                                                breadcrumbs: comes_from.to_owned(),
                                            },
                                        }),
                                    ));
                                }
                                None => iteration_resolved_references
                                    .push((comes_from.to_owned(), ValueLangType::Pending)),
                            }
                        }

                        NodeTag::Calls(calls) => {
                            match resolved_types.get(calls) {
                                Some(ValueLangType::Pending) => (),
                                Some(ValueLangType::Resolved(ResolvedValueLangType::Function(
                                    function,
                                ))) => iteration_resolved_references.push((
                                    pending.to_owned(),
                                    function.return_type.as_ref().to_owned(),
                                )),
                                Some(ValueLangType::Resolved(_)) => iteration_resolved_references
                                    .push((
                                        pending.to_owned(),
                                        ValueLangType::Error(LangTypeError::NotCallable),
                                    )),
                                Some(ValueLangType::Error(_)) => {
                                    iteration_resolved_references.push((
                                        pending.to_owned(),
                                        ValueLangType::Error(LangTypeError::ProxyError {
                                            caused_by: ErrorSourcePosition::SameFile {
                                                path: path.to_owned(),
                                                breadcrumbs: calls.to_owned(),
                                            },
                                        }),
                                    ));
                                }
                                None => iteration_resolved_references
                                    .push((calls.to_owned(), ValueLangType::Pending)),
                            };
                        }

                        NodeTag::IsPrimitiveValue(primitive) => {
                            iteration_resolved_references.push((
                                pending.to_owned(),
                                ValueLangType::Resolved(ResolvedValueLangType::Primitive(
                                    primitive.to_owned(),
                                )),
                            ))
                        }

                        NodeTag::ReferenceNotInScope => iteration_resolved_references.push((
                            pending.to_owned(),
                            ValueLangType::Error(LangTypeError::NotInScope),
                        )),

                        _ => (),
                    }
                }
            }
        }

        let resolved = iteration_resolved_references.len();
        for (breadcrumbs, new_reference) in iteration_resolved_references {
            if let Some(ValueLangType::Resolved(old_type)) = resolved_types.get(&breadcrumbs) {
                panic!("Accidentally clobbered a resolved reference ({breadcrumbs:?} = {old_type:?}) with {new_reference:?}");
            }
            resolved_types.insert(breadcrumbs, new_reference);
        }
        if resolved == 0 {
            break;
        }
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
              cause Print("Hello World")
          }
        "#;

        let ast_node = parse::parse(script).unwrap();
        let analyzed_file = analyzer::analyze_file(&ast_node);

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
