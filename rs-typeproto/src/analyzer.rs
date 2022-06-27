use std::collections::hash_map::Entry;
use std::collections::{HashMap, HashSet};

use crate::ast::{self, AstNode, Breadcrumbs, DeclarationNode};
use crate::types::PrimitiveLangType;

#[derive(Debug, Default, Clone)]
pub struct AnalyzedNode {
    pub node_tags: HashMap<Breadcrumbs, Vec<NodeTag>>,
    pub files_referenced: HashSet<String>,
}

impl AnalyzedNode {
    fn merge(&self, other: &AnalyzedNode) -> Self {
        let mut new = self.clone();
        for (breadcrumbs, tags) in other.node_tags.iter() {
            match new.node_tags.entry(breadcrumbs.to_owned()) {
                Entry::Occupied(mut entry) => {
                    entry.get_mut().extend(tags.iter().cloned());
                }
                Entry::Vacant(entry) => {
                    entry.insert(tags.to_owned());
                }
            }
        }
        new.files_referenced
            .extend(other.files_referenced.iter().cloned());
        new
    }

    fn add_tag_without_inverse(&mut self, breadcrumbs: Breadcrumbs, tag: NodeTag) {
        match self.node_tags.entry(breadcrumbs) {
            Entry::Occupied(mut tags) => {
                tags.get_mut().push(tag);
            }
            Entry::Vacant(tags) => {
                tags.insert(vec![tag]);
            }
        }
    }

    fn add_tag(&mut self, breadcrumbs: Breadcrumbs, tag: NodeTag) {
        let inverse = tag.inverse(&breadcrumbs);
        self.add_tag_without_inverse(breadcrumbs, tag);
        if let Some((inverse_breadcrumbs, inverse_tag)) = inverse {
            self.add_tag_without_inverse(inverse_breadcrumbs, inverse_tag);
        }
    }

    fn add_value_flow_tag(&mut self, comes_from: Breadcrumbs, goes_to: Breadcrumbs) {
        self.add_tag(comes_from, NodeTag::ValueGoesTo(goes_to));
    }

    fn add_file_reference(&mut self, path: String) {
        self.files_referenced.insert(path);
    }
}

#[derive(Debug, Clone)]
pub enum NodeTag {
    ReferencesFile {
        path: String,
        export_name: Option<String>,
    },
    ValueComesFrom(Breadcrumbs),
    ValueGoesTo(Breadcrumbs),
    Calls(Breadcrumbs),
    CalledBy(Breadcrumbs),
    Causes(Breadcrumbs),
    CausedBy(Breadcrumbs),
    IsPrimitiveValue(PrimitiveLangType),
    IsFunction {
        name: Option<String>,
    },
    FunctionCanReturnTypeOf(Breadcrumbs),
    ReferenceNotInScope,
    DeclarationForScope(Breadcrumbs),
    ScopeContainsDeclaration(Breadcrumbs),
    Expression,
    NamedValue {
        name: String,
        type_declaration: Option<Breadcrumbs>,
        value: Breadcrumbs,
    },
    BasicConstraint {
        type_annotation: Breadcrumbs,
    },
}

impl NodeTag {
    fn inverse(&self, breadcrumbs: &Breadcrumbs) -> Option<(Breadcrumbs, NodeTag)> {
        match self {
            NodeTag::ReferencesFile { .. } => None,
            NodeTag::ValueComesFrom(comes_from) => Some((
                comes_from.to_owned(),
                NodeTag::ValueGoesTo(breadcrumbs.to_owned()),
            )),
            NodeTag::ValueGoesTo(goes_to) => Some((
                goes_to.to_owned(),
                NodeTag::ValueComesFrom(breadcrumbs.to_owned()),
            )),
            NodeTag::Calls(calls) => {
                Some((calls.to_owned(), NodeTag::CalledBy(breadcrumbs.to_owned())))
            }
            NodeTag::CalledBy(called_by) => Some((
                called_by.to_owned(),
                NodeTag::CalledBy(breadcrumbs.to_owned()),
            )),
            NodeTag::Causes(causes) => {
                Some((causes.to_owned(), NodeTag::CausedBy(breadcrumbs.to_owned())))
            }
            NodeTag::CausedBy(caused_by) => Some((
                caused_by.to_owned(),
                NodeTag::Causes(breadcrumbs.to_owned()),
            )),
            NodeTag::IsPrimitiveValue(_) => None,
            NodeTag::IsFunction { .. } => None,
            NodeTag::FunctionCanReturnTypeOf(_) => None,
            NodeTag::ReferenceNotInScope => None,
            NodeTag::DeclarationForScope(scope) => Some((
                scope.to_owned(),
                NodeTag::ScopeContainsDeclaration(breadcrumbs.to_owned()),
            )),
            NodeTag::ScopeContainsDeclaration(declaration) => Some((
                declaration.to_owned(),
                NodeTag::DeclarationForScope(breadcrumbs.to_owned()),
            )),
            NodeTag::Expression => None,
            NodeTag::NamedValue { .. } => None,
            NodeTag::BasicConstraint { .. } => None,
        }
    }
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
        self.to_owned()
    }
}

#[derive(Debug, Clone)]
struct AnalyzerContext {
    current_scope: Scope,
    current_scope_position: Breadcrumbs,
}

impl AnalyzerContext {
    fn with_new_scope(
        &mut self,
        breadcrumbs: Breadcrumbs,
        f: impl FnOnce(&mut AnalyzerContext) -> (),
    ) {
        let mut new = AnalyzerContext {
            current_scope: self.current_scope.extend(),
            current_scope_position: breadcrumbs,
        };
        f(&mut new);
    }
}

pub fn analyze_file(ast_node: &AstNode<ast::FileNode>) -> AnalyzedNode {
    let result = AnalyzedNode::default();
    let mut root_scope = Scope::new();
    // loop over top-level declarations to hoist them into file scope
    for declaration in ast_node.node.declarations.iter() {
        add_declaration_to_scope(declaration, &mut root_scope);
        match &declaration.node {
            ast::DeclarationNode::Function(function_node) => {
                root_scope.0.insert(
                    function_node.name.node.0.to_owned(),
                    ScopeItem {
                        origin: declaration.breadcrumbs.to_owned(),
                    },
                );
            }
            ast::DeclarationNode::Import(import_node) => {
                for mapping in import_node.mappings.iter() {
                    let source_name = &mapping.node.source_name.node.0;
                    let rename = mapping.node.rename.as_ref().map(|it| &it.node.0);

                    root_scope.0.insert(
                        rename.unwrap_or(source_name).to_owned(),
                        ScopeItem {
                            origin: mapping.breadcrumbs.to_owned(),
                        },
                    );
                }
            }
            ast::DeclarationNode::NamedValue(named_value_node) => {
                root_scope.0.insert(
                    named_value_node.name.node.0.to_owned(),
                    ScopeItem {
                        origin: declaration.breadcrumbs.to_owned(),
                    },
                );
            }
        };
    }

    let mut ctx = AnalyzerContext {
        current_scope: root_scope,
        current_scope_position: ast_node.breadcrumbs.to_owned(),
    };

    let result = ast_node
        .node
        .declarations
        .iter()
        .map(|it| analyze_declaration(it, &mut ctx))
        .fold(result, |prev, next| prev.merge(&next));

    result
}

fn add_declaration_to_scope(declaration: &AstNode<DeclarationNode>, scope: &mut Scope) -> bool {
    match &declaration.node {
        ast::DeclarationNode::Function(function_node) => {
            scope.0.insert(
                function_node.name.node.0.to_owned(),
                ScopeItem {
                    origin: declaration.breadcrumbs.to_owned(),
                },
            );
            true
        }
        ast::DeclarationNode::Import(import_node) => {
            for mapping in import_node.mappings.iter() {
                let source_name = &mapping.node.source_name.node.0;
                let rename = mapping.node.rename.as_ref().map(|it| &it.node.0);

                scope.0.insert(
                    rename.unwrap_or(source_name).to_owned(),
                    ScopeItem {
                        origin: mapping.breadcrumbs.to_owned(),
                    },
                );
            }
            true
        }
        ast::DeclarationNode::NamedValue(named_value_node) => {
            scope.0.insert(
                named_value_node.name.node.0.to_owned(),
                ScopeItem {
                    origin: declaration.breadcrumbs.to_owned(),
                },
            );
            true
        }
    }
}

fn analyze_type_reference(
    type_reference: &AstNode<ast::TypeReferenceNode>,
    ctx: &mut AnalyzerContext,
) -> AnalyzedNode {
    let mut result = AnalyzedNode::default();
    match &type_reference.node {
        ast::TypeReferenceNode::Identifier(identifier) => {
            if let Some(type_in_scope) = ctx.current_scope.0.get(&identifier.0) {
                result.add_tag(
                    type_reference.breadcrumbs.to_owned(),
                    NodeTag::ValueComesFrom(type_in_scope.origin.to_owned()),
                );
            } else {
                result.add_tag(
                    type_reference.breadcrumbs.to_owned(),
                    NodeTag::ReferenceNotInScope,
                );
            }
        }
    }
    result
}

fn analyze_declaration(
    ast_node: &AstNode<ast::DeclarationNode>,
    ctx: &mut AnalyzerContext,
) -> AnalyzedNode {
    let mut result = match &ast_node.node {
        ast::DeclarationNode::Import(import_declaration_node) => {
            analyze_import_declaration(&ast_node.with_node(import_declaration_node.to_owned()), ctx)
        }
        ast::DeclarationNode::Function(function_declaration_node) => analyze_function_declaration(
            &ast_node.with_node(function_declaration_node.to_owned()),
            ctx,
        ),
        ast::DeclarationNode::NamedValue(named_value_node) => {
            analyze_named_value_declaration(&ast_node.with_node(named_value_node.to_owned()), ctx)
        }
    };
    result.add_tag(
        ast_node.breadcrumbs.to_owned(),
        NodeTag::DeclarationForScope(ctx.current_scope_position.to_owned()),
    );
    result
}

fn analyze_import_declaration(
    ast_node: &AstNode<ast::ImportDeclarationNode>,
    _ctx: &mut AnalyzerContext,
) -> AnalyzedNode {
    let mut result = AnalyzedNode::default();

    let path = ast_node.node.path.node.0.to_owned();
    result.add_file_reference(path.to_owned());
    result.add_tag_without_inverse(
        ast_node.node.path.breadcrumbs.to_owned(),
        NodeTag::ReferencesFile {
            path: path.to_owned(),
            export_name: None,
        },
    );

    for mapping_node in ast_node.node.mappings.iter() {
        let source_name = mapping_node.node.source_name.node.0.to_owned();
        result.add_tag_without_inverse(
            mapping_node.breadcrumbs.to_owned(),
            NodeTag::ReferencesFile {
                path: path.to_owned(),
                export_name: Some(source_name),
            },
        )
    }

    result
}

fn analyze_function_declaration(
    ast_node: &AstNode<ast::FunctionDeclarationNode>,
    ctx: &mut AnalyzerContext,
) -> AnalyzedNode {
    let mut result = AnalyzedNode::default();

    ctx.with_new_scope(ast_node.breadcrumbs.to_owned(), |ctx| {
        let name = &ast_node.node.name.node.0;
        ctx.current_scope.0.insert(
            name.clone(),
            ScopeItem {
                origin: ast_node.breadcrumbs.to_owned(),
            },
        );

        result.add_tag_without_inverse(
            ast_node.breadcrumbs.to_owned(),
            NodeTag::IsFunction {
                name: Some(name.to_owned()),
            },
        );

        result.add_tag_without_inverse(
            ast_node.breadcrumbs.to_owned(),
            NodeTag::FunctionCanReturnTypeOf(ast_node.breadcrumbs.append_name("body")),
        );

        result = result.merge(&analyze_body(&ast_node.node.body, ctx));
    });
    result
}

fn analyze_named_value_declaration(
    ast_node: &AstNode<ast::NamedValueDeclarationNode>,
    ctx: &mut AnalyzerContext,
) -> AnalyzedNode {
    let mut result = AnalyzedNode::default();
    result.add_tag(
        ast_node.breadcrumbs.to_owned(),
        NodeTag::NamedValue {
            name: ast_node.node.name.node.0.to_owned(),
            type_declaration: ast_node
                .node
                .type_annotation
                .as_ref()
                .map(|it| it.breadcrumbs.to_owned()),
            value: ast_node.node.value.breadcrumbs.to_owned(),
        },
    );

    result = result.merge(&analyze_expression(&ast_node.node.value, ctx));

    if let Some(type_annotation) = &ast_node.node.type_annotation {
        result = result.merge(&analyze_type_reference(&type_annotation, ctx));
        result.add_tag(
            ast_node.node.value.breadcrumbs.to_owned(),
            NodeTag::BasicConstraint {
                type_annotation: type_annotation.breadcrumbs.to_owned(),
            },
        )
    }

    result
}

fn analyze_body(ast_node: &AstNode<ast::BodyNode>, ctx: &mut AnalyzerContext) -> AnalyzedNode {
    let mut result = AnalyzedNode::default();
    match &ast_node.node {
        ast::BodyNode::BlockBody(block_body_node) => {
            if block_body_node.statements.len() == 0 {
                // if there are no statements, the block can only be Action-typed.
                // avoids issues with `statements.len() - 1` below
                result.add_tag_without_inverse(
                    ast_node.breadcrumbs.to_owned(),
                    NodeTag::IsPrimitiveValue(PrimitiveLangType::Action),
                );
            } else {
                // a block's return type is the last expression... or the type of any returns? hmmmmmm
                let last_statement_breadcrumbs = ast_node
                    .breadcrumbs
                    .append_name("statements")
                    .append_index(block_body_node.statements.len() - 1);
                result.add_value_flow_tag(
                    last_statement_breadcrumbs.to_owned(),
                    ast_node.breadcrumbs.to_owned(),
                );
            }

            let mut current_ctx = ctx.clone();
            current_ctx.current_scope = current_ctx.current_scope.extend();
            current_ctx.current_scope_position = ast_node.breadcrumbs.to_owned();

            for statement_node in block_body_node.statements.iter() {
                match &statement_node.node {
                    ast::StatementNode::ExpressionStatement(expression_statement_node) => {
                        result.add_value_flow_tag(
                            statement_node.breadcrumbs.append_name("expression"),
                            statement_node.breadcrumbs.to_owned(),
                        );

                        result = result.merge(&analyze_expression(
                            &expression_statement_node.expression,
                            &mut current_ctx,
                        ));
                    }
                    ast::StatementNode::DeclarationStatement(declaration_statement_node) => {
                        let mut new_scope = current_ctx.current_scope.extend();
                        if add_declaration_to_scope(
                            &declaration_statement_node.declaration,
                            &mut new_scope,
                        ) {
                            current_ctx = current_ctx.clone();
                            current_ctx.current_scope = new_scope;
                        }

                        result = result.merge(&analyze_declaration(
                            &declaration_statement_node.declaration,
                            &mut current_ctx,
                        ));
                    }
                }
            }

            result
        }
    }
}

fn analyze_expression(
    ast_node: &AstNode<ast::ExpressionNode>,
    ctx: &mut AnalyzerContext,
) -> AnalyzedNode {
    let mut result = match &ast_node.node {
        ast::ExpressionNode::IdentifierExpression(identifier_expression_node) => {
            analyze_identifier_expression(
                &ast_node.with_node(identifier_expression_node.to_owned()),
                ctx,
            )
        }
        ast::ExpressionNode::CauseExpression(cause_expression_node) => {
            analyze_cause_expression(&ast_node.with_node(cause_expression_node.to_owned()), ctx)
        }
        ast::ExpressionNode::CallExpression(call_expression_node) => {
            analyze_call_expression(&ast_node.with_node(call_expression_node.to_owned()), ctx)
        }
        ast::ExpressionNode::StringLiteralExpression(_) => {
            let mut result = AnalyzedNode::default();
            result.add_tag_without_inverse(
                ast_node.breadcrumbs.to_owned(),
                NodeTag::IsPrimitiveValue(PrimitiveLangType::String),
            );
            result
        }
        ast::ExpressionNode::IntegerLiteralExpression(_) => {
            let mut result = AnalyzedNode::default();
            result.add_tag_without_inverse(
                ast_node.breadcrumbs.to_owned(),
                NodeTag::IsPrimitiveValue(PrimitiveLangType::Integer),
            );
            result
        }
    };
    result.add_tag_without_inverse(ast_node.breadcrumbs.to_owned(), NodeTag::Expression);
    result
}

fn analyze_identifier_expression(
    ast_node: &AstNode<ast::IdentifierExpressionNode>,
    ctx: &mut AnalyzerContext,
) -> AnalyzedNode {
    let identifier_string = &ast_node.node.identifier.node.0;
    let found_item = ctx.current_scope.0.get(identifier_string);

    if let Some(found_item) = found_item {
        let breadcrumbs = found_item.origin.to_owned();
        let mut result = AnalyzedNode::default();
        result.add_value_flow_tag(breadcrumbs, ast_node.breadcrumbs.to_owned());
        result
    } else {
        let mut result = AnalyzedNode::default();
        result.add_tag_without_inverse(
            ast_node.breadcrumbs.to_owned(),
            NodeTag::ReferenceNotInScope,
        );
        result
    }
}

fn analyze_cause_expression(
    ast_node: &AstNode<ast::CauseExpressionNode>,
    ctx: &mut AnalyzerContext,
) -> AnalyzedNode {
    let mut result = AnalyzedNode::default();

    result.add_tag_without_inverse(
        ast_node.breadcrumbs.to_owned(),
        NodeTag::Causes(ast_node.node.argument.breadcrumbs.to_owned()),
    );
    result.add_tag_without_inverse(
        ast_node.node.argument.breadcrumbs.to_owned(),
        NodeTag::CausedBy(ast_node.breadcrumbs.to_owned()),
    );

    result.merge(&analyze_expression(&ast_node.node.argument, ctx))
}

fn analyze_call_expression(
    ast_node: &AstNode<ast::CallExpressionNode>,
    ctx: &mut AnalyzerContext,
) -> AnalyzedNode {
    let mut result = AnalyzedNode::default();
    for argument in ast_node.node.arguments.iter() {
        result = result.merge(&analyze_expression(&argument.node.value, ctx));
    }

    result = result.merge(&analyze_expression(&ast_node.node.callee, ctx));
    result.add_tag_without_inverse(
        ast_node.breadcrumbs.to_owned(),
        NodeTag::Calls(ast_node.breadcrumbs.append_name("callee")),
    );
    result.add_tag_without_inverse(
        ast_node.breadcrumbs.append_name("callee"),
        NodeTag::CalledBy(ast_node.breadcrumbs.to_owned()),
    );

    result
}

#[cfg(test)]
mod test {
    use crate::parse;

    use super::*;

    #[test]
    fn hello_world() {
        let script = r#"
          import langtest/effects { Print }

          fn main() {
              cause Print("Hello World")
          }
        "#;

        let ast_node = parse::parse(script).unwrap();
        let result = analyze_file(&ast_node);

        println!("{result:#?}");
    }
}
