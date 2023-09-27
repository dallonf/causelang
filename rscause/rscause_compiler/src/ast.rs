use crate::breadcrumbs::{BreadcrumbEntry, BreadcrumbName, Breadcrumbs, HasBreadcrumbs};
use std::{collections::HashMap, sync::Arc};

include!("gen/ast_nodes.rs");

pub enum BreadcrumbTreeNode {
    Node(Option<AnyAstNode>),
    List(Vec<BreadcrumbTreeNode>),
}
impl BreadcrumbTreeNode {
    pub fn descendants(&self) -> Vec<AnyAstNode> {
        match self {
            BreadcrumbTreeNode::Node(None) => vec![],
            BreadcrumbTreeNode::Node(Some(node)) => node
                .children()
                .into_iter()
                .flat_map(|(_, node)| node.descendants())
                .collect(),
            BreadcrumbTreeNode::List(nodes) => {
                nodes.iter().flat_map(|node| node.descendants()).collect()
            }
        }
    }
}

impl AstNode for AnyAstNode {
    fn children(&self) -> HashMap<BreadcrumbEntry, BreadcrumbTreeNode> {
        match self {
            _ => todo!(),
        }
    }
}

pub trait AstNode {
    fn children(&self) -> HashMap<BreadcrumbEntry, BreadcrumbTreeNode>;
}

impl AstNode for FileNode {
    fn children(&self) -> HashMap<BreadcrumbEntry, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("declarations").into(),
            (&self.declarations).into(),
        );
        result
    }
}
impl From<DeclarationNode> for AnyAstNode {
    fn from(node: DeclarationNode) -> Self {
        match node {
            DeclarationNode::Import(node) => AnyAstNode::Import(node),
            DeclarationNode::Function(node) => AnyAstNode::Function(node),
        }
    }
}

impl<T> From<&Vec<T>> for BreadcrumbTreeNode
where
    T: Into<AnyAstNode> + Clone,
{
    fn from(nodes: &Vec<T>) -> Self {
        Self::List(
            nodes
                .into_iter()
                .map(|x| BreadcrumbTreeNode::Node(Some(x.clone().into())))
                .collect(),
        )
    }
}
