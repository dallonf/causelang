use std::{fmt::Debug, vec};

use crate::ast::*;

#[derive(Debug, Clone, PartialEq)]
pub enum BreadcrumbWalkChild {
    Node(AstNode<AnyNode>),
    List(Vec<AstNode<AnyNode>>),
}

impl BreadcrumbWalk for BreadcrumbWalkChild {
    fn child_nodes(&self) -> Vec<(BreadcrumbEntry, BreadcrumbWalkChild)> {
        match self {
            BreadcrumbWalkChild::Node(node) => node.node.child_nodes(),
            BreadcrumbWalkChild::List(list) => list
                .into_iter()
                .enumerate()
                .map(|(i, node)| {
                    (
                        BreadcrumbEntry::Index(i),
                        BreadcrumbWalkChild::Node(node.to_owned()),
                    )
                })
                .collect(),
        }
    }
}

pub trait BreadcrumbWalk<T = Self>: Debug {
    fn child_nodes(&self) -> Vec<(BreadcrumbEntry, BreadcrumbWalkChild)>;
    fn find_node(&self, breadcrumbs: &Breadcrumbs) -> AstNode<AnyNode> {
        let (entry, remaining_breadcrumbs) = breadcrumbs.pop_start();

        let children = self.child_nodes();

        let found_child =
            children
                .into_iter()
                .find_map(|(key, value)| if &key == entry { Some(value) } else { None });

        if let Some(found_child) = found_child {
            if remaining_breadcrumbs.is_empty() {
                match found_child {
                    BreadcrumbWalkChild::Node(node) => node,
                    BreadcrumbWalkChild::List(_) => {
                        panic!("Can't stop a breadcrumb walk in the middle of a list")
                    }
                }
            } else {
                found_child.find_node(&remaining_breadcrumbs)
            }
        } else {
            panic!("Can't find key {:?} for node: {:#?}", entry, self);
        }
    }
}

fn to_any_node_list(nodes: &[AstNode<impl Into<AnyNode> + Clone>]) -> BreadcrumbWalkChild {
    let vec = nodes
        .into_iter()
        .map(|ast_node| ast_node.to_owned().map(|it| it.into()))
        .collect();
    BreadcrumbWalkChild::List(vec)
}

fn to_breadcrumb_walk_node(node: &AstNode<impl Into<AnyNode> + Clone>) -> BreadcrumbWalkChild {
    BreadcrumbWalkChild::Node(node.to_owned().map(|it| it.into()))
}

impl BreadcrumbWalk for AnyNode {
    fn child_nodes(&self) -> Vec<(BreadcrumbEntry, BreadcrumbWalkChild)> {
        match self {
            AnyNode::Identifier(_) => vec![],
            AnyNode::TypeReference(type_reference) => match type_reference {
                TypeReferenceNode::Identifier(_) => vec![],
            },
            AnyNode::File(file) => file.child_nodes(),
            AnyNode::Declaration(declaration) => match declaration {
                DeclarationNode::Import(import) => {
                    vec![
                        (
                            BreadcrumbEntry::with_name("path"),
                            to_breadcrumb_walk_node(&import.path),
                        ),
                        (
                            BreadcrumbEntry::with_name("mappings"),
                            to_any_node_list(&import.mappings),
                        ),
                    ]
                }
                DeclarationNode::Function(function) => {
                    vec![
                        (
                            BreadcrumbEntry::with_name("body"),
                            to_breadcrumb_walk_node(&function.body),
                        ),
                        (
                            BreadcrumbEntry::with_name("name"),
                            to_breadcrumb_walk_node(&function.name),
                        ),
                    ]
                }
                DeclarationNode::NamedValue(named_value) => {
                    let mut result = vec![
                        (
                            BreadcrumbEntry::with_name("name"),
                            to_breadcrumb_walk_node(&named_value.name),
                        ),
                        (
                            BreadcrumbEntry::with_name("value"),
                            to_breadcrumb_walk_node(&named_value.value),
                        ),
                    ];
                    if let Some(type_annotation) = &named_value.type_annotation {
                        result.push((
                            BreadcrumbEntry::with_name("type_annotation"),
                            to_breadcrumb_walk_node(type_annotation),
                        ));
                    }
                    result
                }
            },
            AnyNode::Body(body) => match body {
                BodyNode::BlockBody(block) => {
                    vec![(
                        BreadcrumbEntry::with_name("statements"),
                        to_any_node_list(&block.statements),
                    )]
                }
            },
            AnyNode::Statement(statement) => match statement {
                StatementNode::ExpressionStatement(expression) => vec![(
                    BreadcrumbEntry::with_name("expression"),
                    to_breadcrumb_walk_node(&expression.expression),
                )],
                StatementNode::DeclarationStatement(declaration) => vec![(
                    BreadcrumbEntry::with_name("declaration"),
                    to_breadcrumb_walk_node(&declaration.declaration),
                )],
            },
            AnyNode::Expression(expression) => match expression {
                ExpressionNode::IdentifierExpression(identifier) => {
                    vec![(
                        BreadcrumbEntry::with_name("identifier"),
                        to_breadcrumb_walk_node(&identifier.identifier),
                    )]
                }
                ExpressionNode::CauseExpression(cause) => {
                    vec![(
                        BreadcrumbEntry::with_name("argument"),
                        to_breadcrumb_walk_node(&cause.argument),
                    )]
                }
                ExpressionNode::CallExpression(call) => {
                    // arguments, callee
                    vec![
                        (
                            BreadcrumbEntry::with_name("arguments"),
                            to_any_node_list(&call.arguments),
                        ),
                        (
                            BreadcrumbEntry::with_name("callee"),
                            to_breadcrumb_walk_node(&call.callee),
                        ),
                    ]
                }
                ExpressionNode::StringLiteralExpression(_) => vec![],
                ExpressionNode::IntegerLiteralExpression(_) => vec![],
            },
            AnyNode::ImportMapping(mapping) => {
                let mut result = vec![(
                    BreadcrumbEntry::with_name("source_name"),
                    to_breadcrumb_walk_node(&mapping.source_name),
                )];
                if let Some(rename) = &mapping.rename {
                    result.push((
                        BreadcrumbEntry::with_name("rename"),
                        to_breadcrumb_walk_node(rename),
                    ));
                }
                result
            }
            AnyNode::ImportPath(_) => vec![],

            AnyNode::CallExpressionArgument(call_argument) => {
                vec![(
                    BreadcrumbEntry::with_name("value"),
                    to_breadcrumb_walk_node(&call_argument.value),
                )]
            }
        }
    }
}

impl BreadcrumbWalk for FileNode {
    fn child_nodes(&self) -> Vec<(BreadcrumbEntry, BreadcrumbWalkChild)> {
        vec![(
            BreadcrumbEntry::with_name("declarations"),
            to_any_node_list(&self.declarations),
        )]
    }
}
