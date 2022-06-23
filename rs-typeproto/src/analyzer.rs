use std::collections::HashMap;

use crate::ast::{self, AstNode, Breadcrumbs};
use crate::types::LangPrimitiveType;

#[derive(Debug, Clone, PartialEq, Eq)]
enum TypeReference {
    Pending {
        waiting_on_position: SourcePosition,
        pending_type: PendingType,
    },
    Resolved(ResolvedTypeReference),
    Error(TypeError),
}

#[derive(Debug, Copy, Clone, PartialEq, Eq)]
enum PendingType {
    /// This node is the same type as the node it's waiting on
    Verbatim,
    /// This node is waiting on the type of a function; it will be the function's return type.
    Call,
}

#[derive(Debug, Clone, PartialEq, Eq)]
enum ResolvedTypeReference {
    Function(FunctionTypeReference),
    Primitive(LangPrimitiveType),
}

#[derive(Debug, Clone, PartialEq, Eq)]
enum TypeError {
    NotInScope,
    ProxyError { caused_by: SourcePosition },
    NotCallable,
}

#[derive(Debug, Clone, PartialEq, Eq)]
struct FunctionTypeReference {
    name: Option<String>,
    return_type: Box<TypeReference>,
    // TODO: params
}

#[derive(Debug, Clone, PartialEq, Eq)]
struct SourcePosition {
    file: String,
    breadcrumbs: Breadcrumbs,
}

#[derive(Debug, Clone)]
struct ScopeItem {
    origin: Breadcrumbs,
}

#[derive(Debug, Clone)]
struct Scope(HashMap<String, ScopeItem>);

impl Scope {
    fn new() -> Self {
        Scope(HashMap::new())
    }

    fn extend(&self) -> Self {
        self.clone()
    }
}

#[derive(Debug)]
struct AnalyzerContext<'a> {
    current_scope: Scope,
    type_resolutions: &'a mut HashMap<Breadcrumbs, TypeReference>,
    file_path: &'a str,
}

impl AnalyzerContext<'_> {
    fn with_new_scope(&mut self, f: impl FnOnce(&mut AnalyzerContext) -> ()) {
        let mut new = AnalyzerContext {
            current_scope: self.current_scope.extend(),
            type_resolutions: self.type_resolutions,
            file_path: self.file_path,
        };
        f(&mut new);
    }

    fn mark_pending_type<NodeType>(
        &mut self,
        node: &AstNode<NodeType>,
        waiting_on_breadcrumbs: Breadcrumbs,
        pending_type: PendingType,
    ) {
        self.type_resolutions.insert(
            node.breadcrumbs.clone(),
            TypeReference::Pending {
                waiting_on_position: SourcePosition {
                    file: self.file_path.to_string(),
                    breadcrumbs: waiting_on_breadcrumbs,
                },
                pending_type,
            },
        );
    }
}

pub fn analyze_file(ast_node: &AstNode<ast::FileNode>, path: impl Into<String>) {
    let path = path.into();
    // declarations in the file root scope are hoisted
    let mut root_scope = Scope::new();
    for declaration in ast_node.node.declarations.iter() {
        match &declaration.node {
            ast::DeclarationNode::Function(function_node) => {
                root_scope.0.insert(
                    function_node.name.node.0.clone(),
                    ScopeItem {
                        origin: declaration.breadcrumbs.clone(),
                    },
                );
            }
            ast::DeclarationNode::Import(import_node) => {
                let filepath = &import_node.path.node.0;
                for mapping in import_node.mappings.iter() {
                    let source_name = &mapping.node.source_name.node.0;
                    let rename = mapping.node.rename.as_ref().map(|it| &it.node.0);
                    todo!()
                }
            }
        };
    }

    let mut type_resolutions = HashMap::new();

    let mut ctx = AnalyzerContext {
        current_scope: root_scope,
        type_resolutions: &mut type_resolutions,
        file_path: &path,
    };

    // now let's establish a mapping of types
    for declaration in ast_node.node.declarations.iter() {
        analyze_declaration(declaration, &mut ctx)
    }

    // infer until stable
    fn count_pending(type_resolutions: &HashMap<Breadcrumbs, TypeReference>) -> (usize, usize) {
        type_resolutions.iter().fold(
            (0, 0),
            |(prev_pending, prev_resolved), (_, next)| match next {
                TypeReference::Pending { .. } => (prev_pending + 1, prev_resolved),
                TypeReference::Resolved(_) | TypeReference::Error(_) => {
                    (prev_pending, prev_resolved + 1)
                }
            },
        )
    }

    let mut pending_count = count_pending(&type_resolutions);
    while pending_count.0 > 0 {
        let pending_references = type_resolutions.iter().filter_map(|it| match it.1 {
            TypeReference::Pending {
                waiting_on_position,
                pending_type,
            } => Some((it.0, waiting_on_position, pending_type)),
            _ => None,
        });

        let mut iteration_resolved_references = Vec::<(Breadcrumbs, TypeReference)>::new();
        for (breadcrumbs, waiting_on_position, pending_type) in pending_references {
            if waiting_on_position.file != path {
                // only support intra-file inference, for now
                break;
            }

            match type_resolutions.get(&waiting_on_position.breadcrumbs) {
                Some(TypeReference::Pending { .. }) => (),
                Some(TypeReference::Resolved(resolved_type_reference)) => match pending_type {
                    PendingType::Verbatim => {
                        iteration_resolved_references.push((
                            breadcrumbs.clone(),
                            TypeReference::Resolved(resolved_type_reference.clone()),
                        ));
                    }
                    PendingType::Call => match resolved_type_reference {
                        ResolvedTypeReference::Function(function_type) => {
                            iteration_resolved_references.push((
                                breadcrumbs.clone(),
                                function_type.return_type.as_ref().clone(),
                            ));
                        }
                        _ => iteration_resolved_references.push((
                            breadcrumbs.clone(),
                            TypeReference::Error(TypeError::NotCallable),
                        )),
                    },
                },
                Some(TypeReference::Error(_)) => {
                    iteration_resolved_references.push((
                        breadcrumbs.clone(),
                        TypeReference::Error(TypeError::ProxyError {
                            caused_by: SourcePosition {
                                file: path.to_string(),
                                breadcrumbs: waiting_on_position.breadcrumbs.clone(),
                            },
                        }),
                    ));
                }
                None => (),
            }
        }

        let resolved_len = iteration_resolved_references.len();
        for (breadcrumbs, new_reference) in iteration_resolved_references.into_iter() {
            type_resolutions.insert(breadcrumbs, new_reference);
        }

        let new_count = count_pending(&type_resolutions);
        if new_count == pending_count && resolved_len == 0 {
            // we haven't gotten anywhere this iteration
            break;
        } else {
            pending_count = new_count;
        }
    }

    println!("type resolutions: {:#?}", type_resolutions);
}

fn analyze_declaration(ast_node: &AstNode<ast::DeclarationNode>, ctx: &mut AnalyzerContext) {
    match &ast_node.node {
        ast::DeclarationNode::Function(function_declaration_node) => {
            analze_function_declaration(
                &ast_node.with_node(function_declaration_node.clone()),
                ctx,
            );
        }
        ast::DeclarationNode::Import(import_declaration_node) => {
            analyze_import_declaration(&ast_node.with_node(import_declaration_node.clone()), ctx)
        }
    }
}

fn analyze_import_declaration(
    ast_node: &AstNode<ast::ImportDeclarationNode>,
    ctx: &mut AnalyzerContext,
) {
    // not much to do here, actually
    // most of this is handled during hoisting
}

fn analze_function_declaration(
    ast_node: &AstNode<ast::FunctionDeclarationNode>,
    ctx: &mut AnalyzerContext,
) {
    ctx.with_new_scope(|ctx| {
        ctx.current_scope.0.insert(
            ast_node.node.name.node.0.clone(),
            ScopeItem {
                origin: ast_node.breadcrumbs.clone(),
            },
        );

        ctx.type_resolutions.insert(
            ast_node.breadcrumbs.clone(),
            TypeReference::Resolved(ResolvedTypeReference::Function(FunctionTypeReference {
                name: Some(ast_node.node.name.node.0.clone()),
                return_type: Box::new(TypeReference::Pending {
                    waiting_on_position: SourcePosition {
                        file: ctx.file_path.to_string(),
                        breadcrumbs: ast_node.breadcrumbs.append_name("body"),
                    },
                    pending_type: PendingType::Verbatim,
                }),
            })),
        );

        analyze_body(&ast_node.node.body, ctx);
    });
}

fn analyze_body(ast_node: &AstNode<ast::BodyNode>, ctx: &mut AnalyzerContext) {
    match &ast_node.node {
        ast::BodyNode::BlockBody(block_body_node) => {
            if block_body_node.statements.len() == 0 {
                // if there are no statements, the block can only be Action-typed.
                // avoids issues with `statements.len() - 1` below
                ctx.type_resolutions.insert(
                    ast_node.breadcrumbs.clone(),
                    TypeReference::Resolved(ResolvedTypeReference::Primitive(
                        LangPrimitiveType::Action,
                    )),
                );
            } else {
                // a block's return type is the last expression... or the type of any returns? hmmmmmm
                ctx.mark_pending_type(
                    ast_node,
                    ast_node
                        .breadcrumbs
                        .append_name("statements")
                        .append_index(block_body_node.statements.len() - 1),
                    PendingType::Verbatim,
                );
            }

            for statement_node in block_body_node.statements.iter() {
                match &statement_node.node {
                    ast::StatementNode::ExpressionStatement(expression_statement_node) => {
                        ctx.mark_pending_type(
                            statement_node,
                            statement_node.breadcrumbs.append_name("expression"),
                            PendingType::Verbatim,
                        );

                        analyze_expression(&expression_statement_node.expression, ctx);
                    }
                }
            }
        }
    }
}

fn analyze_expression(ast_node: &AstNode<ast::ExpressionNode>, ctx: &mut AnalyzerContext) {
    match &ast_node.node {
        ast::ExpressionNode::IdentifierExpression(identifier_expression_node) => {
            analyze_identifier_expression(
                &ast_node.with_node(identifier_expression_node.clone()),
                ctx,
            );
        }
        ast::ExpressionNode::CauseExpression(cause_expression_node) => {
            analyze_cause_expression(&ast_node.with_node(cause_expression_node.clone()), ctx)
        }
        ast::ExpressionNode::CallExpression(call_expression_node) => {
            analyze_call_expression(&ast_node.with_node(call_expression_node.clone()), ctx);
        }
        ast::ExpressionNode::StringLiteralExpression(_) => {
            ctx.type_resolutions.insert(
                ast_node.breadcrumbs.clone(),
                TypeReference::Resolved(ResolvedTypeReference::Primitive(
                    LangPrimitiveType::String,
                )),
            );
        }
    }
}

fn analyze_identifier_expression(
    ast_node: &AstNode<ast::IdentifierExpressionNode>,
    ctx: &mut AnalyzerContext,
) {
    let identifier_string = &ast_node.node.identifier.node.0;
    let found_item = ctx.current_scope.0.get(identifier_string);

    if let Some(found_item) = found_item {
        let breadcrumbs = found_item.origin.clone();
        ctx.mark_pending_type(ast_node, breadcrumbs, PendingType::Verbatim);
    } else {
        ctx.type_resolutions.insert(
            ast_node.breadcrumbs.clone(),
            TypeReference::Error(TypeError::NotInScope),
        );
    }
}

fn analyze_call_expression(ast_node: &AstNode<ast::CallExpressionNode>, ctx: &mut AnalyzerContext) {
    for argument in ast_node.node.arguments.iter() {
        analyze_expression(&argument.node.value, ctx);
    }

    analyze_expression(&ast_node.node.callee, ctx);

    ctx.mark_pending_type(
        &ast_node,
        ast_node.breadcrumbs.append_name("callee"),
        PendingType::Call,
    );
}

fn analyze_cause_expression(
    ast_node: &AstNode<ast::CauseExpressionNode>,
    ctx: &mut AnalyzerContext,
) {
    // TODO: somehow need to resolve the type of this

    analyze_expression(&ast_node.node.argument, ctx);
}

#[cfg(test)]
mod test {
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
        analyze_file(&ast_node, "test.cau");
    }
}
