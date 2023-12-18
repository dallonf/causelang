use crate::breadcrumbs::{BreadcrumbEntry, BreadcrumbName, Breadcrumbs, HasBreadcrumbs};
use anyhow::{anyhow, Result};
use serde::{Deserialize, Serialize};
use std::{collections::HashMap, sync::Arc};

include!("gen/ast_nodes.rs");

#[derive(Debug, Clone)]
pub enum BreadcrumbTreeNode {
    Node(Option<AnyAstNode>),
    List(Vec<BreadcrumbTreeNode>),
}
impl BreadcrumbTreeNode {
    pub fn descendants(&self) -> Vec<AnyAstNode> {
        match self {
            BreadcrumbTreeNode::Node(None) => vec![],
            BreadcrumbTreeNode::Node(Some(node)) => {
                let mut result = vec![node.to_owned()];
                node.children()
                    .into_iter()
                    .flat_map(|(_, node)| node.descendants())
                    .for_each(|node| result.push(node.to_owned()));
                result
            }
            BreadcrumbTreeNode::List(nodes) => {
                nodes.iter().flat_map(|node| node.descendants()).collect()
            }
        }
    }

    pub fn at_path(&self, breadcrumbs: &Breadcrumbs) -> Result<Self> {
        let entry = if let Some(entry) = breadcrumbs.entries.first() {
            entry
        } else {
            return Ok(self.clone());
        };

        match (self, entry) {
            (BreadcrumbTreeNode::Node(node), BreadcrumbEntry::Name(name)) => {
                let children = node
                    .clone()
                    .ok_or_else(|| anyhow!("Tried to get child {:?} of empty node", name))?
                    .children();
                let child = children
                    .get(name)
                    .cloned()
                    .ok_or_else(|| anyhow!("No child named {:?} on {:?}", name, node));
                child?.at_path(&breadcrumbs.pop_start())
            }
            (BreadcrumbTreeNode::List(list), &BreadcrumbEntry::Index(index)) => {
                let child = list
                    .get(index)
                    .ok_or_else(|| anyhow!("No child at index {:?} on {:?}", index, list));
                child?.at_path(&breadcrumbs.pop_start())
            }
            _ => Err(anyhow!(
                "Invalid breadcrumb entry {:?} for node {:?}",
                entry,
                self
            )),
        }
    }
}

pub trait AstNode: HasBreadcrumbs {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode>;
    fn info(&self) -> &NodeInfo;
}

impl AnyAstNode {
    pub fn node_at_path(&self, breadcrumbs: &Breadcrumbs) -> Result<AnyAstNode> {
        let breadcrumb_node = BreadcrumbTreeNode::from(self.clone());
        let node = breadcrumb_node.at_path(breadcrumbs)?;
        match node {
            BreadcrumbTreeNode::Node(Some(node)) => Ok(node.clone()),
            BreadcrumbTreeNode::Node(None) => Err(anyhow!("Empty node at path {:?}", breadcrumbs)),
            BreadcrumbTreeNode::List(_) => Err(anyhow!("List node at path {:?}", breadcrumbs)),
        }
    }
}

impl From<&AnyAstNode> for AnyAstNode {
    fn from(value: &AnyAstNode) -> Self {
        value.clone()
    }
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

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq, Hash)]
pub struct NodeInfo {
    pub position: DocumentRange,
    pub breadcrumbs: Breadcrumbs,
}

#[derive(Debug, Copy, Clone, Serialize, Deserialize, PartialEq, Eq, Hash)]
pub struct DocumentRange {
    pub start: DocumentPosition,
    pub end: DocumentPosition,
}

#[derive(Debug, Copy, Clone, Serialize, Deserialize, PartialEq, Eq, Hash)]
pub struct DocumentPosition {
    pub line: u32,
    pub column: u32,
}
