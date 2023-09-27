#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub enum AnyAstNode {
    Identifier(Arc<IdentifierNode>),
    IdentifierTypeReference(Arc<IdentifierTypeReferenceNode>),
    FunctionSignatureParameter(Arc<FunctionSignatureParameterNode>),
    FunctionCallParameter(Arc<FunctionCallParameterNode>),
    File(Arc<FileNode>),
    Import(Arc<ImportNode>),
    ImportPath(Arc<ImportPathNode>),
    ImportMapping(Arc<ImportMappingNode>),
    Function(Arc<FunctionNode>),
    BlockBody(Arc<BlockBodyNode>),
    ExpressionStatement(Arc<ExpressionStatementNode>),
    CauseExpression(Arc<CauseExpressionNode>),
    CallExpression(Arc<CallExpressionNode>),
    IdentifierExpression(Arc<IdentifierExpressionNode>),
    StringLiteralExpression(Arc<StringLiteralExpressionNode>),
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
    Identifier(Arc<IdentifierTypeReferenceNode>),
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
    Import(Arc<ImportNode>),
    Function(Arc<FunctionNode>),
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
    Block(Arc<BlockBodyNode>),
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
    Expression(Arc<ExpressionStatementNode>),
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
    Cause(Arc<CauseExpressionNode>),
    Call(Arc<CallExpressionNode>),
    Identifier(Arc<IdentifierExpressionNode>),
    StringLiteral(Arc<StringLiteralExpressionNode>),
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
impl From<Arc<IdentifierNode>> for AnyAstNode {
    fn from(value: Arc<IdentifierNode>) -> Self {
        AnyAstNode::Identifier(value.clone())
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
    pub identifier: Arc<IdentifierNode>,
}
impl From<Arc<IdentifierTypeReferenceNode>> for AnyAstNode {
    fn from(value: Arc<IdentifierTypeReferenceNode>) -> Self {
        AnyAstNode::IdentifierTypeReference(value.clone())
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
    pub type_reference: Option<TypeReferenceNode>,
}
impl From<Arc<FunctionSignatureParameterNode>> for AnyAstNode {
    fn from(value: Arc<FunctionSignatureParameterNode>) -> Self {
        AnyAstNode::FunctionSignatureParameter(value.clone())
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
    pub value: ExpressionNode,
}
impl From<Arc<FunctionCallParameterNode>> for AnyAstNode {
    fn from(value: Arc<FunctionCallParameterNode>) -> Self {
        AnyAstNode::FunctionCallParameter(value.clone())
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
impl From<Arc<FileNode>> for AnyAstNode {
    fn from(value: Arc<FileNode>) -> Self {
        AnyAstNode::File(value.clone())
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
    pub path: Arc<ImportPathNode>,
    pub mappings: Vec<Arc<ImportMappingNode>>,
}
impl From<Arc<ImportNode>> for AnyAstNode {
    fn from(value: Arc<ImportNode>) -> Self {
        AnyAstNode::Import(value.clone())
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
impl From<Arc<ImportPathNode>> for AnyAstNode {
    fn from(value: Arc<ImportPathNode>) -> Self {
        AnyAstNode::ImportPath(value.clone())
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
    pub source_name: Arc<IdentifierNode>,
    pub rename: Option<Arc<IdentifierNode>>,
}
impl From<Arc<ImportMappingNode>> for AnyAstNode {
    fn from(value: Arc<ImportMappingNode>) -> Self {
        AnyAstNode::ImportMapping(value.clone())
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
    pub name: Arc<IdentifierNode>,
    pub params: Vec<Arc<FunctionSignatureParameterNode>>,
    pub body: BodyNode,
    pub return_type: Option<TypeReferenceNode>,
}
impl From<Arc<FunctionNode>> for AnyAstNode {
    fn from(value: Arc<FunctionNode>) -> Self {
        AnyAstNode::Function(value.clone())
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
impl From<Arc<BlockBodyNode>> for AnyAstNode {
    fn from(value: Arc<BlockBodyNode>) -> Self {
        AnyAstNode::BlockBody(value.clone())
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
    pub expression: ExpressionNode,
}
impl From<Arc<ExpressionStatementNode>> for AnyAstNode {
    fn from(value: Arc<ExpressionStatementNode>) -> Self {
        AnyAstNode::ExpressionStatement(value.clone())
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
    pub signal: ExpressionNode,
}
impl From<Arc<CauseExpressionNode>> for AnyAstNode {
    fn from(value: Arc<CauseExpressionNode>) -> Self {
        AnyAstNode::CauseExpression(value.clone())
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
    pub callee: ExpressionNode,
    pub parameters: Vec<Arc<FunctionCallParameterNode>>,
}
impl From<Arc<CallExpressionNode>> for AnyAstNode {
    fn from(value: Arc<CallExpressionNode>) -> Self {
        AnyAstNode::CallExpression(value.clone())
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
    pub identifier: Arc<IdentifierNode>,
}
impl From<Arc<IdentifierExpressionNode>> for AnyAstNode {
    fn from(value: Arc<IdentifierExpressionNode>) -> Self {
        AnyAstNode::IdentifierExpression(value.clone())
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
impl From<Arc<StringLiteralExpressionNode>> for AnyAstNode {
    fn from(value: Arc<StringLiteralExpressionNode>) -> Self {
        AnyAstNode::StringLiteralExpression(value.clone())
    }
}
impl HasBreadcrumbs for StringLiteralExpressionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.breadcrumbs
    }
}

