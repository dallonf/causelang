use crate::breadcrumbs::{Breadcrumbs, HasBreadcrumbs};
use std::sync::Arc;

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub enum TypeReferenceNode {
    Identifier(IdentifierTypeReferenceNode),
}
impl HasBreadcrumbs for TypeReferenceNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        match self {
            TypeReferenceNode::Identifier(node) => &node.breadcrumbs,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub enum DeclarationNode {
    Import(ImportNode),
    Function(FunctionNode),
}
impl HasBreadcrumbs for DeclarationNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        match self {
            DeclarationNode::Import(node) => &node.breadcrumbs,
            DeclarationNode::Function(node) => &node.breadcrumbs,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub enum BodyNode {
    Block(BlockBodyNode),
}
impl HasBreadcrumbs for BodyNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        match self {
            BodyNode::Block(node) => &node.breadcrumbs,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub enum StatementNode {
    Expression(ExpressionStatementNode),
}
impl HasBreadcrumbs for StatementNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        match self {
            StatementNode::Expression(node) => &node.breadcrumbs,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub enum ExpressionNode {
    Cause(CauseExpressionNode),
    Call(CallExpressionNode),
    Identifier(IdentifierExpressionNode),
    StringLiteral(StringLiteralExpressionNode),
}
impl HasBreadcrumbs for ExpressionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        match self {
            ExpressionNode::Cause(node) => &node.breadcrumbs,
            ExpressionNode::Call(node) => &node.breadcrumbs,
            ExpressionNode::Identifier(node) => &node.breadcrumbs,
            ExpressionNode::StringLiteral(node) => &node.breadcrumbs,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct IdentifierNode {
    pub breadcrumbs: Breadcrumbs,
    pub text: Arc<String>,
}
impl HasBreadcrumbs for IdentifierNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct IdentifierTypeReferenceNode {
    pub breadcrumbs: Breadcrumbs,
    pub identifier: IdentifierNode,
}
impl HasBreadcrumbs for IdentifierTypeReferenceNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct FunctionSignatureParameterNode {
    pub breadcrumbs: Breadcrumbs,
    pub name: Arc<String>,
    pub type_reference: Option<Box<TypeReferenceNode>>,
}
impl HasBreadcrumbs for FunctionSignatureParameterNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct FunctionCallParameterNode {
    pub breadcrumbs: Breadcrumbs,
    pub value: Box<ExpressionNode>,
}
impl HasBreadcrumbs for FunctionCallParameterNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct FileNode {
    pub breadcrumbs: Breadcrumbs,
    pub declarations: Vec<DeclarationNode>,
}
impl HasBreadcrumbs for FileNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct ImportNode {
    pub breadcrumbs: Breadcrumbs,
    pub path: ImportPathNode,
    pub mappings: Vec<ImportMappingNode>,
}
impl HasBreadcrumbs for ImportNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct ImportPathNode {
    pub breadcrumbs: Breadcrumbs,
    pub path: Arc<String>,
}
impl HasBreadcrumbs for ImportPathNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct ImportMappingNode {
    pub breadcrumbs: Breadcrumbs,
    pub source_name: IdentifierNode,
    pub rename: Option<IdentifierNode>,
}
impl HasBreadcrumbs for ImportMappingNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct FunctionNode {
    pub breadcrumbs: Breadcrumbs,
    pub name: IdentifierNode,
    pub params: Vec<FunctionSignatureParameterNode>,
    pub body: Box<BodyNode>,
    pub return_type: Option<Box<TypeReferenceNode>>,
}
impl HasBreadcrumbs for FunctionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct BlockBodyNode {
    pub breadcrumbs: Breadcrumbs,
    pub statements: Vec<StatementNode>,
}
impl HasBreadcrumbs for BlockBodyNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct ExpressionStatementNode {
    pub breadcrumbs: Breadcrumbs,
    pub expression: Box<ExpressionNode>,
}
impl HasBreadcrumbs for ExpressionStatementNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct CauseExpressionNode {
    pub breadcrumbs: Breadcrumbs,
    pub signal: Box<ExpressionNode>,
}
impl HasBreadcrumbs for CauseExpressionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct CallExpressionNode {
    pub breadcrumbs: Breadcrumbs,
    pub callee: Box<ExpressionNode>,
    pub parameters: Vec<FunctionCallParameterNode>,
}
impl HasBreadcrumbs for CallExpressionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct IdentifierExpressionNode {
    pub breadcrumbs: Breadcrumbs,
    pub identifier: IdentifierNode,
}
impl HasBreadcrumbs for IdentifierExpressionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct StringLiteralExpressionNode {
    pub breadcrumbs: Breadcrumbs,
    pub text: Arc<String>,
}
impl HasBreadcrumbs for StringLiteralExpressionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.breadcrumbs
    }
}
