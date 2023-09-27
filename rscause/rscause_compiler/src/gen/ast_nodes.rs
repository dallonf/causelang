use std::sync::Arc;
use crate::breadcrumbs::{Breadcrumbs, HasBreadcrumbs};

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub enum AnyAstNode {
    Identifier(IdentifierNode),
    IdentifierTypeReference(IdentifierTypeReferenceNode),
    FunctionSignatureParameter(FunctionSignatureParameterNode),
    FunctionCallParameter(FunctionCallParameterNode),
    File(FileNode),
    Import(ImportNode),
    ImportPath(ImportPathNode),
    ImportMapping(ImportMappingNode),
    Function(FunctionNode),
    BlockBody(BlockBodyNode),
    ExpressionStatement(ExpressionStatementNode),
    CauseExpression(CauseExpressionNode),
    CallExpression(CallExpressionNode),
    IdentifierExpression(IdentifierExpressionNode),
    StringLiteralExpression(StringLiteralExpressionNode),
}
impl HasBreadcrumbs for AnyAstNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        match self {
            AnyAstNode::Identifier(node) => &node.breadcrumbs,
            AnyAstNode::IdentifierTypeReference(node) => &node.breadcrumbs,
            AnyAstNode::FunctionSignatureParameter(node) => &node.breadcrumbs,
            AnyAstNode::FunctionCallParameter(node) => &node.breadcrumbs,
            AnyAstNode::File(node) => &node.breadcrumbs,
            AnyAstNode::Import(node) => &node.breadcrumbs,
            AnyAstNode::ImportPath(node) => &node.breadcrumbs,
            AnyAstNode::ImportMapping(node) => &node.breadcrumbs,
            AnyAstNode::Function(node) => &node.breadcrumbs,
            AnyAstNode::BlockBody(node) => &node.breadcrumbs,
            AnyAstNode::ExpressionStatement(node) => &node.breadcrumbs,
            AnyAstNode::CauseExpression(node) => &node.breadcrumbs,
            AnyAstNode::CallExpression(node) => &node.breadcrumbs,
            AnyAstNode::IdentifierExpression(node) => &node.breadcrumbs,
            AnyAstNode::StringLiteralExpression(node) => &node.breadcrumbs,
        }
    }
}

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
impl From<IdentifierNode> for AnyAstNode {
    fn from(node: IdentifierNode) -> Self {
        AnyAstNode::Identifier(node)
    }
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
impl From<IdentifierTypeReferenceNode> for AnyAstNode {
    fn from(node: IdentifierTypeReferenceNode) -> Self {
        AnyAstNode::IdentifierTypeReference(node)
    }
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
impl From<FunctionSignatureParameterNode> for AnyAstNode {
    fn from(node: FunctionSignatureParameterNode) -> Self {
        AnyAstNode::FunctionSignatureParameter(node)
    }
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
impl From<FunctionCallParameterNode> for AnyAstNode {
    fn from(node: FunctionCallParameterNode) -> Self {
        AnyAstNode::FunctionCallParameter(node)
    }
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
impl From<FileNode> for AnyAstNode {
    fn from(node: FileNode) -> Self {
        AnyAstNode::File(node)
    }
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
impl From<ImportNode> for AnyAstNode {
    fn from(node: ImportNode) -> Self {
        AnyAstNode::Import(node)
    }
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
impl From<ImportPathNode> for AnyAstNode {
    fn from(node: ImportPathNode) -> Self {
        AnyAstNode::ImportPath(node)
    }
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
impl From<ImportMappingNode> for AnyAstNode {
    fn from(node: ImportMappingNode) -> Self {
        AnyAstNode::ImportMapping(node)
    }
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
impl From<FunctionNode> for AnyAstNode {
    fn from(node: FunctionNode) -> Self {
        AnyAstNode::Function(node)
    }
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
impl From<BlockBodyNode> for AnyAstNode {
    fn from(node: BlockBodyNode) -> Self {
        AnyAstNode::BlockBody(node)
    }
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
impl From<ExpressionStatementNode> for AnyAstNode {
    fn from(node: ExpressionStatementNode) -> Self {
        AnyAstNode::ExpressionStatement(node)
    }
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
impl From<CauseExpressionNode> for AnyAstNode {
    fn from(node: CauseExpressionNode) -> Self {
        AnyAstNode::CauseExpression(node)
    }
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
impl From<CallExpressionNode> for AnyAstNode {
    fn from(node: CallExpressionNode) -> Self {
        AnyAstNode::CallExpression(node)
    }
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
impl From<IdentifierExpressionNode> for AnyAstNode {
    fn from(node: IdentifierExpressionNode) -> Self {
        AnyAstNode::IdentifierExpression(node)
    }
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
impl From<StringLiteralExpressionNode> for AnyAstNode {
    fn from(node: StringLiteralExpressionNode) -> Self {
        AnyAstNode::StringLiteralExpression(node)
    }
}
impl HasBreadcrumbs for StringLiteralExpressionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.breadcrumbs
    }
}

