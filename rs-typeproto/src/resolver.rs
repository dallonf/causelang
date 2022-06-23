use std::collections::hash_map::Entry;
use std::collections::HashMap;
use std::future::pending;
use std::iter;

use crate::analyzer::{AnalyzedNode, NodeTag};
use crate::ast::{AstNode, Breadcrumbs, FileNode};
use crate::types::LangPrimitiveType;

#[derive(Debug, Clone, PartialEq, Eq)]
enum SourcePosition {
    SameFile(Breadcrumbs),
    FileImport { path: String, export_name: String },
}

#[derive(Debug, Clone, PartialEq, Eq)]
enum TypeReference {
    Pending,
    Resolved(ResolvedTypeReference),
    Error(TypeError),
}

#[derive(Debug, Clone, PartialEq, Eq)]
enum TypeError {
    NotInScope,
    ProxyError { caused_by: SourcePosition },
    NotCallable,
}

#[derive(Debug, Clone, PartialEq, Eq)]
enum ResolvedTypeReference {
    Function(FunctionTypeReference),
    Primitive(LangPrimitiveType),
}

#[derive(Debug, Clone, PartialEq, Eq)]
struct FunctionTypeReference {
    name: Option<String>,
    return_type: Box<TypeReference>,
    // TODO: params
}

#[derive(Debug, Clone)]
pub struct FileResolverInput {
    file_node: AstNode<FileNode>,
    analyzed: AnalyzedNode,
}
pub fn resolve_for_file(input: FileResolverInput) {
    let FileResolverInput {
        file_node,
        analyzed,
    } = input;
    let AnalyzedNode { node_tags, .. } = analyzed;

    let mut resolved_types = HashMap::<Breadcrumbs, TypeReference>::new();

    // start by resolving all expressions and top-level declarations
    for (breadcrumbs, tags) in node_tags.iter() {
        if tags.iter().any(|it| match it {
            NodeTag::TopLevelDeclaration | NodeTag::Expression => true,
            _ => false,
        }) {
            resolved_types.insert(breadcrumbs.to_owned(), TypeReference::Pending);
        }
    }

    loop {
        let mut iteration_resolved_references = Vec::<(Breadcrumbs, TypeReference)>::new();

        let pending_references = resolved_types.iter().filter_map(|it| match it.1 {
            TypeReference::Pending => Some(it.0),
            _ => None,
        });
        for pending in pending_references {
            if let Some(tags) = node_tags.get(pending) {
                for tag in tags {
                    match tag {
                        NodeTag::ValueComesFrom(comes_from) => {
                            match resolved_types.get(comes_from) {
                                Some(TypeReference::Pending) => (),
                                Some(TypeReference::Resolved(resolved_with)) => {
                                    iteration_resolved_references.push((
                                        pending.to_owned(),
                                        TypeReference::Resolved(resolved_with.to_owned()),
                                    ));
                                }
                                Some(TypeReference::Error(_)) => {
                                    iteration_resolved_references.push((
                                        pending.to_owned(),
                                        TypeReference::Error(TypeError::ProxyError {
                                            caused_by: SourcePosition::SameFile(
                                                comes_from.to_owned(),
                                            ),
                                        }),
                                    ));
                                }
                                None => iteration_resolved_references
                                    .push((comes_from.to_owned(), TypeReference::Pending)),
                            }
                        }

                        NodeTag::Calls(calls) => {
                            match resolved_types.get(calls) {
                                Some(TypeReference::Pending) => (),
                                Some(TypeReference::Resolved(ResolvedTypeReference::Function(
                                    function,
                                ))) => iteration_resolved_references.push((
                                    pending.to_owned(),
                                    function.return_type.as_ref().to_owned(),
                                )),
                                Some(TypeReference::Resolved(_)) => iteration_resolved_references
                                    .push((
                                        pending.to_owned(),
                                        TypeReference::Error(TypeError::NotCallable),
                                    )),
                                Some(TypeReference::Error(_)) => {
                                    iteration_resolved_references.push((
                                        pending.to_owned(),
                                        TypeReference::Error(TypeError::ProxyError {
                                            caused_by: SourcePosition::SameFile(calls.to_owned()),
                                        }),
                                    ));
                                }
                                None => iteration_resolved_references
                                    .push((calls.to_owned(), TypeReference::Pending)),
                            };
                        }

                        NodeTag::IsPrimitiveValue(primitive) => {
                            iteration_resolved_references.push((
                                pending.to_owned(),
                                TypeReference::Resolved(ResolvedTypeReference::Primitive(
                                    primitive.to_owned(),
                                )),
                            ))
                        }

                        NodeTag::ReferenceNotInScope => iteration_resolved_references.push((
                            pending.to_owned(),
                            TypeReference::Error(TypeError::NotInScope),
                        )),

                        _ => (),
                    }
                }
            }
        }

        let resolved = iteration_resolved_references.len();
        for (breadcrumbs, new_reference) in iteration_resolved_references {
            if let Some(TypeReference::Resolved(old_type)) = resolved_types.get(&breadcrumbs) {
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
          // import langtest/effects { Print }

          fn main() {
              cause Print("Hello World")
          }
        "#;

        let ast_node = parse::parse(script).unwrap();
        let analyzed_file = analyzer::analyze_file(&ast_node);

        resolve_for_file(FileResolverInput {
            file_node: ast_node,
            analyzed: analyzed_file,
        });
    }
}
