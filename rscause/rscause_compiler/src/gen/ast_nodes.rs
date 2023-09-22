use std::sync::Arc;

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub enum TypeReferenceNode {
    Identifier(IdentifierTypeReferenceNode),
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub enum DeclarationNode {
    Function(FunctionNode),
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub enum BodyNode {
    Block(BlockBodyNode),
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub enum StatementNode {
    Expression(ExpressionStatementNode),
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub enum ExpressionNode {
    Cause(CauseExpressionNode),
    Call(CallExpressionNode),
    Identifier(IdentifierExpressionNode),
    StringLiteral(StringLiteralExpressionNode),
}


#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct IdentifierNode {
    pub value: Arc<String>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct IdentifierTypeReferenceNode {
    pub identifier: Box<IdentifierNode>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct FunctionSignatureParameterNode {
    pub name: Arc<String>,
    pub type_reference: Option<Box<TypeReferenceNode>>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct FunctionCallParameterNode {
    pub value: Box<ExpressionNode>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct FileNode {
    pub declarations: Vec<DeclarationNode>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct ImportNode {
    pub path: Box<ImportPathNode>,
    pub mappings: Vec<ImportMappingNode>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct ImportPathNode {
    pub path: Arc<String>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct ImportMappingNode {
    pub source_name: Box<IdentifierNode>,
    pub rename: Option<Box<IdentifierNode>>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct FunctionNode {
    pub name: Arc<String>,
    pub params: Vec<FunctionSignatureParameterNode>,
    pub body: Box<BodyNode>,
    pub return_type: Option<Box<TypeReferenceNode>>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct BlockBodyNode {
    pub statements: Vec<StatementNode>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct ExpressionStatementNode {
    pub expression: Box<ExpressionNode>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct CauseExpressionNode {
    pub signal: Box<ExpressionNode>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct CallExpressionNode {
    pub callee: Box<ExpressionNode>,
    pub parameters: Vec<FunctionCallParameterNode>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct IdentifierExpressionNode {
    pub identifier: Box<IdentifierNode>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct StringLiteralExpressionNode {
    pub text: Arc<String>,
}

