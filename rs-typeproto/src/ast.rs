#[derive(Debug, Clone, Copy, PartialEq, Eq, Default)]
pub struct DocumentPosition {
    pub line: usize,
    pub col: usize,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Default)]
pub struct DocumentRange {
    pub start: DocumentPosition,
    pub end: DocumentPosition,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
pub enum BreadcrumbEntry {
    Index(usize),
    Name(&'static str),
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct Breadcrumbs(pub Vec<BreadcrumbEntry>);

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct AstNode<T> {
    pub position: DocumentRange,
    pub breadcrumbs: Breadcrumbs,
    pub node: T,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct Identifier(pub String);

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum TypeReferenceNode {
    Identifier(Identifier),
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct FileNode {
    pub declarations: Vec<AstNode<DeclarationNode>>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum DeclarationNode {
    Function(FunctionDeclarationNode),
}

#[derive(Debug, Clone, PartialEq, Eq)]

pub struct FunctionDeclarationNode {
    pub name: AstNode<Identifier>,
    // TODO: params
    pub return_type: Option<AstNode<TypeReferenceNode>>,
    pub body: AstNode<BodyNode>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum BodyNode {
    BlockBody(BlockBodyNode),
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct BlockBodyNode {
    pub statements: Vec<AstNode<StatementNode>>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum StatementNode {
    ExpressionStatementNode(ExpressionStatementNode),
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ExpressionStatementNode {
    pub expression: AstNode<ExpressionNode>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum ExpressionNode {
    IdentifierExpression(IdentifierExpression),
    CauseExpression(CauseExpressionNode),
    CallExpression(CallExpressionNode),
    StringLiteralExpression(StringLiteralExpressionNode),
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct IdentifierExpression {
    pub identifier: AstNode<Identifier>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct CauseExpressionNode {
    pub keyword: DocumentRange,
    pub param: Box<AstNode<ExpressionNode>>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct CallExpressionNode {
    callee: Box<AstNode<ExpressionNode>>,
    arguments: Vec<CallExpressionArgumentNode>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct CallExpressionArgumentNode {
    name: Option<AstNode<Identifier>>,
    value: AstNode<ExpressionNode>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct StringLiteralExpressionNode {
    text_range: DocumentRange,
    text: String,
}
