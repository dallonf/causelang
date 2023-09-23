use std::sync::Arc;

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub enum TypeReferenceNode {
    Identifier(IdentifierTypeReferenceNode),
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub enum DeclarationNode {
    Import(ImportNode),
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
    pub text: Arc<String>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct IdentifierTypeReferenceNode {
    pub identifier: IdentifierNode,
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
    pub path: ImportPathNode,
    pub mappings: Vec<ImportMappingNode>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct ImportPathNode {
    pub path: Arc<String>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct ImportMappingNode {
    pub source_name: IdentifierNode,
    pub rename: Option<IdentifierNode>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct FunctionNode {
    pub name: IdentifierNode,
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
    pub identifier: IdentifierNode,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct StringLiteralExpressionNode {
    pub text: Arc<String>,
}

