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
                            BreadcrumbEntry::Name("path"),
                            to_breadcrumb_walk_node(&import.path),
                        ),
                        (
                            BreadcrumbEntry::Name("mappings"),
                            to_any_node_list(&import.mappings),
                        ),
                    ]
                }
                DeclarationNode::Function(function) => {
                    vec![
                        (
                            BreadcrumbEntry::Name("body"),
                            to_breadcrumb_walk_node(&function.body),
                        ),
                        (
                            BreadcrumbEntry::Name("name"),
                            to_breadcrumb_walk_node(&function.name),
                        ),
                    ]
                }
                DeclarationNode::NamedValue(named_value) => {
                    // name, type_annotation, value
                    let mut result = vec![
                        (
                            BreadcrumbEntry::Name("name"),
                            to_breadcrumb_walk_node(&named_value.name),
                        ),
                        (
                            BreadcrumbEntry::Name("value"),
                            to_breadcrumb_walk_node(&named_value.value),
                        ),
                    ];
                    if let Some(type_annotation) = &named_value.type_annotation {
                        result.push((
                            BreadcrumbEntry::Name("type_annotation"),
                            to_breadcrumb_walk_node(type_annotation),
                        ));
                    }
                    result
                }
            },
            AnyNode::Body(body) => match body {
                BodyNode::BlockBody(block) => {
                    vec![(
                        BreadcrumbEntry::Name("statements"),
                        to_any_node_list(&block.statements),
                    )]
                }
            },
            AnyNode::Statement(statement) => match statement {
                StatementNode::ExpressionStatement(expression) => vec![(
                    BreadcrumbEntry::Name("expression"),
                    to_breadcrumb_walk_node(&expression.expression),
                )],
                StatementNode::DeclarationStatement(declaration) => vec![(
                    BreadcrumbEntry::Name("declaration"),
                    to_breadcrumb_walk_node(&declaration.declaration),
                )],
            },
            AnyNode::Expression(expression) => match expression {
                ExpressionNode::IdentifierExpression(identifier) => {
                    vec![(
                        BreadcrumbEntry::Name("identifier"),
                        to_breadcrumb_walk_node(&identifier.identifier),
                    )]
                }
                ExpressionNode::CauseExpression(cause) => {
                    vec![(
                        BreadcrumbEntry::Name("argument"),
                        to_breadcrumb_walk_node(&cause.argument),
                    )]
                }
                ExpressionNode::CallExpression(call) => {
                    // arguments, callee
                    vec![
                        (
                            BreadcrumbEntry::Name("arguments"),
                            to_any_node_list(&call.arguments),
                        ),
                        (
                            BreadcrumbEntry::Name("callee"),
                            to_breadcrumb_walk_node(&call.callee),
                        ),
                    ]
                }
                ExpressionNode::StringLiteralExpression(_) => vec![],
                ExpressionNode::IntegerLiteralExpression(_) => vec![],
            },
            AnyNode::ImportMapping(mapping) => {
                let mut result = vec![(
                    BreadcrumbEntry::Name("source_name"),
                    to_breadcrumb_walk_node(&mapping.source_name),
                )];
                if let Some(rename) = &mapping.rename {
                    result.push((
                        BreadcrumbEntry::Name("rename"),
                        to_breadcrumb_walk_node(rename),
                    ));
                }
                result
            }
            AnyNode::ImportPath(_) => vec![],

            AnyNode::CallExpressionArgument(call_argument) => {
                let mut result = vec![(
                    BreadcrumbEntry::Name("value"),
                    to_breadcrumb_walk_node(&call_argument.value),
                )];
                if let Some(name) = &call_argument.name {
                    result.push((BreadcrumbEntry::Name("name"), to_breadcrumb_walk_node(name)));
                }
                result
            }
        }
    }
}

impl BreadcrumbWalk for FileNode {
    fn child_nodes(&self) -> Vec<(BreadcrumbEntry, BreadcrumbWalkChild)> {
        vec![(
            BreadcrumbEntry::Name("declarations"),
            to_any_node_list(&self.declarations),
        )]
    }
}

// impl<'a, ListType, NodeType> From<ListType> for BreadcrumbWalkChild
// where
//     ListType: IntoIterator<Item = &'a AstNode<NodeType>>,
//     NodeType: Into<AnyNode> + 'a + Clone,
// {
//     fn from(list: ListType) -> Self {
//         BreadcrumbWalkChild::List(
//             list.into_iter()
//                 .map(|list_item| list_item.to_owned().map(|it| it.into()))
//                 .collect(),
//         )
//     }
// }

// impl<NodeType> From<AstNode<NodeType>> for BreadcrumbWalkChild
// where
//     NodeType: Into<AnyNode>,
// {
//     fn from(node: AstNode<NodeType>) -> Self {
//         BreadcrumbWalkChild::Node(node.map(|it| it.into()))
//     }
// }
// impl<NodeType> From<&AstNode<NodeType>> for BreadcrumbWalkChild
// where
//     NodeType: Into<AnyNode>,
// {
//     fn from(node: &AstNode<NodeType>) -> Self {
//         node.to_owned().into()
//     }
// }

// impl BreadcrumbWalk for AnyNode {
//     fn child_nodes(&self) -> Vec<(BreadcrumbEntry, BreadcrumbWalkChild)> {
//         match self {
//             AnyNode::Identifier(identifier) => identifier.child_nodes(),
//             AnyNode::TypeReference(type_reference) => type_reference.child_nodes(),
//             AnyNode::File(file) => file.child_nodes(),
//             AnyNode::Declaration(declaration) => declaration.child_nodes(),
//             AnyNode::ImportPathNode(import_path) => import_path.child_nodes(),
//             AnyNode::ImportMappingNode(import_mapping) => import_mapping.child_nodes(),
//         }
//     }
// }

// impl BreadcrumbWalk for Identifier {
//     fn child_nodes(&self) -> Vec<(BreadcrumbEntry, BreadcrumbWalkChild)> {
//         vec![]
//     }
// }

// impl BreadcrumbWalk for TypeReferenceNode {
//     fn child_nodes(&self) -> Vec<(BreadcrumbEntry, BreadcrumbWalkChild)> {
//         match self {
//             TypeReferenceNode::Identifier(identifier) => identifier.child_nodes(),
//         }
//     }
// }

// impl BreadcrumbWalk for FileNode {
//     fn child_nodes(&self) -> Vec<(BreadcrumbEntry, BreadcrumbWalkChild)> {
//         vec![(
//             BreadcrumbEntry::Name("declarations"),
//             self.declarations.as_slice().into(),
//         )]
//     }
// }

// impl BreadcrumbWalk for DeclarationNode {
//     fn child_nodes(&self) -> Vec<(BreadcrumbEntry, BreadcrumbWalkChild)> {
//         match self {
//             DeclarationNode::Import(import) => import.child_nodes(),
//             DeclarationNode::Function(function) => function.child_nodes(),
//             DeclarationNode::NamedValue(value) => value.child_nodes(),
//         }
//     }
// }

// impl BreadcrumbWalk for ImportDeclarationNode {
//     fn child_nodes(&self) -> Vec<(BreadcrumbEntry, BreadcrumbWalkChild)> {
//         vec![
//             (BreadcrumbEntry::Name("mappings"), self.mappings.into()),
//             (BreadcrumbEntry::Name("path"), self.path.into()),
//         ]
//     }
// }

// impl BreadcrumbWalk for ImportMappingNode {
//     fn child_nodes(&self) -> Vec<(BreadcrumbEntry, BreadcrumbWalkChild)> {
//         todo!()
//     }
// }
