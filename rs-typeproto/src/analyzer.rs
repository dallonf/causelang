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
    InferenceFailed { caused_by: SourcePosition },
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
    // declarations in the file root scope are hoisted
    let mut root_scope = Scope::new();
    for declaration in ast_node.node.declarations.iter() {
        match &declaration.node {
            ast::DeclarationNode::Function(function_node) => root_scope.0.insert(
                function_node.name.node.0.clone(),
                ScopeItem {
                    origin: declaration.breadcrumbs.clone(),
                },
            ),
        };
    }

    let mut type_resolutions = HashMap::new();

    let mut ctx = AnalyzerContext {
        current_scope: root_scope,
        type_resolutions: &mut type_resolutions,
        file_path: &path.into(),
    };

    // now let's try to resolve all types
    for declaration in ast_node.node.declarations.iter() {
        analyze_declaration(declaration, &mut ctx)
    }

    println!("type resolutions: {:#?}", ctx.type_resolutions);
}

fn analyze_declaration(ast_node: &AstNode<ast::DeclarationNode>, ctx: &mut AnalyzerContext) {
    match &ast_node.node {
        ast::DeclarationNode::Function(function_declaration_node) => {
            ctx.with_new_scope(|ctx| {
                ctx.current_scope.0.insert(
                    function_declaration_node.name.node.0.clone(),
                    ScopeItem {
                        origin: ast_node.breadcrumbs.clone(),
                    },
                );

                ctx.type_resolutions.insert(
                    ast_node.breadcrumbs.clone(),
                    TypeReference::Resolved(ResolvedTypeReference::Function(
                        FunctionTypeReference {
                            name: Some(function_declaration_node.name.node.0.clone()),
                            return_type: Box::new(TypeReference::Pending {
                                waiting_on_position: SourcePosition {
                                    file: ctx.file_path.to_string(),
                                    breadcrumbs: ast_node.breadcrumbs.append_name("body"),
                                },
                                pending_type: PendingType::Verbatim,
                            }),
                        },
                    )),
                );

                analyze_body(&function_declaration_node.body, ctx);
            });
        }
    }
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
          fn main() {
              cause Print("Hello World")
          }
        "#;

        let ast_node = parse::parse(script).unwrap();
        analyze_file(&ast_node, "test.cau");
    }
}
