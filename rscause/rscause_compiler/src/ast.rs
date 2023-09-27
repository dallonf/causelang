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

pub trait AstNode {
    fn children(&self) -> HashMap<BreadcrumbEntry, BreadcrumbTreeNode>;
}

impl<T> From<T> for BreadcrumbTreeNode
where
    T: Into<AnyAstNode>,
{
    fn from(value: T) -> Self {
        Self::Node(Some(value.into()))
    }
}

impl<T> From<&Option<T>> for BreadcrumbTreeNode
where
    for<'a> &'a T: Into<AnyAstNode>,
{
    fn from(value: &Option<T>) -> Self {
        match value {
            Some(value) => Self::Node(Some(value.into())),
            None => Self::Node(None),
        }
    }
}

impl<T> From<&Vec<T>> for BreadcrumbTreeNode
where
    for<'a> &'a T: Into<AnyAstNode>,
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
