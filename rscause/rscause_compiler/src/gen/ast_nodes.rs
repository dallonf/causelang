pub static BREADCRUMB_NAMES: &[&str] = &[
    "text",
    "identifier",
    "name",
    "type_reference",
    "value",
    "declarations",
    "path",
    "mappings",
    "path",
    "source_name",
    "rename",
    "name",
    "params",
    "body",
    "return_type",
    "statements",
    "expression",
    "signal",
    "callee",
    "parameters",
    "identifier",
    "text",
];

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
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
impl AstNode for AnyAstNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        match self {
            AnyAstNode::Identifier(node) => node.children(),
            AnyAstNode::IdentifierTypeReference(node) => node.children(),
            AnyAstNode::FunctionSignatureParameter(node) => node.children(),
            AnyAstNode::FunctionCallParameter(node) => node.children(),
            AnyAstNode::File(node) => node.children(),
            AnyAstNode::Import(node) => node.children(),
            AnyAstNode::ImportPath(node) => node.children(),
            AnyAstNode::ImportMapping(node) => node.children(),
            AnyAstNode::Function(node) => node.children(),
            AnyAstNode::BlockBody(node) => node.children(),
            AnyAstNode::ExpressionStatement(node) => node.children(),
            AnyAstNode::CauseExpression(node) => node.children(),
            AnyAstNode::CallExpression(node) => node.children(),
            AnyAstNode::IdentifierExpression(node) => node.children(),
            AnyAstNode::StringLiteralExpression(node) => node.children(),
        }
    }
    fn info(&self) -> &NodeInfo {
        match self {
            AnyAstNode::Identifier(node) => node.info(),
            AnyAstNode::IdentifierTypeReference(node) => node.info(),
            AnyAstNode::FunctionSignatureParameter(node) => node.info(),
            AnyAstNode::FunctionCallParameter(node) => node.info(),
            AnyAstNode::File(node) => node.info(),
            AnyAstNode::Import(node) => node.info(),
            AnyAstNode::ImportPath(node) => node.info(),
            AnyAstNode::ImportMapping(node) => node.info(),
            AnyAstNode::Function(node) => node.info(),
            AnyAstNode::BlockBody(node) => node.info(),
            AnyAstNode::ExpressionStatement(node) => node.info(),
            AnyAstNode::CauseExpression(node) => node.info(),
            AnyAstNode::CallExpression(node) => node.info(),
            AnyAstNode::IdentifierExpression(node) => node.info(),
            AnyAstNode::StringLiteralExpression(node) => node.info(),
        }
    }
}
impl HasBreadcrumbs for AnyAstNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        match self {
            AnyAstNode::Identifier(node) => node.breadcrumbs(),
            AnyAstNode::IdentifierTypeReference(node) => node.breadcrumbs(),
            AnyAstNode::FunctionSignatureParameter(node) => node.breadcrumbs(),
            AnyAstNode::FunctionCallParameter(node) => node.breadcrumbs(),
            AnyAstNode::File(node) => node.breadcrumbs(),
            AnyAstNode::Import(node) => node.breadcrumbs(),
            AnyAstNode::ImportPath(node) => node.breadcrumbs(),
            AnyAstNode::ImportMapping(node) => node.breadcrumbs(),
            AnyAstNode::Function(node) => node.breadcrumbs(),
            AnyAstNode::BlockBody(node) => node.breadcrumbs(),
            AnyAstNode::ExpressionStatement(node) => node.breadcrumbs(),
            AnyAstNode::CauseExpression(node) => node.breadcrumbs(),
            AnyAstNode::CallExpression(node) => node.breadcrumbs(),
            AnyAstNode::IdentifierExpression(node) => node.breadcrumbs(),
            AnyAstNode::StringLiteralExpression(node) => node.breadcrumbs(),
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub enum TypeReferenceNode {
    Identifier(Arc<IdentifierTypeReferenceNode>),
}
impl AstNode for TypeReferenceNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        match self {
            TypeReferenceNode::Identifier(node) => node.children(),
        }
    }
    fn info(&self) -> &NodeInfo {
        match self {
            TypeReferenceNode::Identifier(node) => node.info(),
        }
    }
}
impl From<&TypeReferenceNode> for AnyAstNode {
    fn from(value: &TypeReferenceNode) -> Self {
        match value {
            TypeReferenceNode::Identifier(node) => AnyAstNode::IdentifierTypeReference(node.clone()),
        }
    }
}
impl HasBreadcrumbs for TypeReferenceNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        match self {
            TypeReferenceNode::Identifier(node) => node.breadcrumbs(),
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub enum DeclarationNode {
    Import(Arc<ImportNode>),
    Function(Arc<FunctionNode>),
}
impl AstNode for DeclarationNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        match self {
            DeclarationNode::Import(node) => node.children(),
            DeclarationNode::Function(node) => node.children(),
        }
    }
    fn info(&self) -> &NodeInfo {
        match self {
            DeclarationNode::Import(node) => node.info(),
            DeclarationNode::Function(node) => node.info(),
        }
    }
}
impl From<&DeclarationNode> for AnyAstNode {
    fn from(value: &DeclarationNode) -> Self {
        match value {
            DeclarationNode::Import(node) => AnyAstNode::Import(node.clone()),
            DeclarationNode::Function(node) => AnyAstNode::Function(node.clone()),
        }
    }
}
impl HasBreadcrumbs for DeclarationNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        match self {
            DeclarationNode::Import(node) => node.breadcrumbs(),
            DeclarationNode::Function(node) => node.breadcrumbs(),
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub enum BodyNode {
    Block(Arc<BlockBodyNode>),
}
impl AstNode for BodyNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        match self {
            BodyNode::Block(node) => node.children(),
        }
    }
    fn info(&self) -> &NodeInfo {
        match self {
            BodyNode::Block(node) => node.info(),
        }
    }
}
impl From<&BodyNode> for AnyAstNode {
    fn from(value: &BodyNode) -> Self {
        match value {
            BodyNode::Block(node) => AnyAstNode::BlockBody(node.clone()),
        }
    }
}
impl HasBreadcrumbs for BodyNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        match self {
            BodyNode::Block(node) => node.breadcrumbs(),
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub enum StatementNode {
    Expression(Arc<ExpressionStatementNode>),
}
impl AstNode for StatementNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        match self {
            StatementNode::Expression(node) => node.children(),
        }
    }
    fn info(&self) -> &NodeInfo {
        match self {
            StatementNode::Expression(node) => node.info(),
        }
    }
}
impl From<&StatementNode> for AnyAstNode {
    fn from(value: &StatementNode) -> Self {
        match value {
            StatementNode::Expression(node) => AnyAstNode::ExpressionStatement(node.clone()),
        }
    }
}
impl HasBreadcrumbs for StatementNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        match self {
            StatementNode::Expression(node) => node.breadcrumbs(),
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub enum ExpressionNode {
    Cause(Arc<CauseExpressionNode>),
    Call(Arc<CallExpressionNode>),
    Identifier(Arc<IdentifierExpressionNode>),
    StringLiteral(Arc<StringLiteralExpressionNode>),
}
impl AstNode for ExpressionNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        match self {
            ExpressionNode::Cause(node) => node.children(),
            ExpressionNode::Call(node) => node.children(),
            ExpressionNode::Identifier(node) => node.children(),
            ExpressionNode::StringLiteral(node) => node.children(),
        }
    }
    fn info(&self) -> &NodeInfo {
        match self {
            ExpressionNode::Cause(node) => node.info(),
            ExpressionNode::Call(node) => node.info(),
            ExpressionNode::Identifier(node) => node.info(),
            ExpressionNode::StringLiteral(node) => node.info(),
        }
    }
}
impl From<&ExpressionNode> for AnyAstNode {
    fn from(value: &ExpressionNode) -> Self {
        match value {
            ExpressionNode::Cause(node) => AnyAstNode::CauseExpression(node.clone()),
            ExpressionNode::Call(node) => AnyAstNode::CallExpression(node.clone()),
            ExpressionNode::Identifier(node) => AnyAstNode::IdentifierExpression(node.clone()),
            ExpressionNode::StringLiteral(node) => AnyAstNode::StringLiteralExpression(node.clone()),
        }
    }
}
impl HasBreadcrumbs for ExpressionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        match self {
            ExpressionNode::Cause(node) => node.breadcrumbs(),
            ExpressionNode::Call(node) => node.breadcrumbs(),
            ExpressionNode::Identifier(node) => node.breadcrumbs(),
            ExpressionNode::StringLiteral(node) => node.breadcrumbs(),
        }
    }
}


#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct IdentifierNode {
    pub info: NodeInfo,
    pub text: Arc<String>,
}
impl From<&Arc<IdentifierNode>> for AnyAstNode {
    fn from(value: &Arc<IdentifierNode>) -> Self {
        AnyAstNode::Identifier(value.clone())
    }
}
impl AstNode for IdentifierNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        HashMap::new()
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for IdentifierNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct IdentifierTypeReferenceNode {
    pub info: NodeInfo,
    pub identifier: Arc<IdentifierNode>,
}
impl From<&Arc<IdentifierTypeReferenceNode>> for AnyAstNode {
    fn from(value: &Arc<IdentifierTypeReferenceNode>) -> Self {
        AnyAstNode::IdentifierTypeReference(value.clone())
    }
}
impl AstNode for IdentifierTypeReferenceNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("identifier"),
            (&self.identifier).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for IdentifierTypeReferenceNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct FunctionSignatureParameterNode {
    pub info: NodeInfo,
    pub name: Arc<IdentifierNode>,
    pub type_reference: Option<TypeReferenceNode>,
}
impl From<&Arc<FunctionSignatureParameterNode>> for AnyAstNode {
    fn from(value: &Arc<FunctionSignatureParameterNode>) -> Self {
        AnyAstNode::FunctionSignatureParameter(value.clone())
    }
}
impl AstNode for FunctionSignatureParameterNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("name"),
            (&self.name).into(),
        );
        result.insert(
            BreadcrumbName::new("type_reference"),
            (&self.type_reference).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for FunctionSignatureParameterNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct FunctionCallParameterNode {
    pub info: NodeInfo,
    pub value: ExpressionNode,
}
impl From<&Arc<FunctionCallParameterNode>> for AnyAstNode {
    fn from(value: &Arc<FunctionCallParameterNode>) -> Self {
        AnyAstNode::FunctionCallParameter(value.clone())
    }
}
impl AstNode for FunctionCallParameterNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("value"),
            (&self.value).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for FunctionCallParameterNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct FileNode {
    pub info: NodeInfo,
    pub declarations: Vec<DeclarationNode>,
}
impl From<&Arc<FileNode>> for AnyAstNode {
    fn from(value: &Arc<FileNode>) -> Self {
        AnyAstNode::File(value.clone())
    }
}
impl AstNode for FileNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("declarations"),
            (&self.declarations).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for FileNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct ImportNode {
    pub info: NodeInfo,
    pub path: Arc<ImportPathNode>,
    pub mappings: Vec<Arc<ImportMappingNode>>,
}
impl From<&Arc<ImportNode>> for AnyAstNode {
    fn from(value: &Arc<ImportNode>) -> Self {
        AnyAstNode::Import(value.clone())
    }
}
impl AstNode for ImportNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("path"),
            (&self.path).into(),
        );
        result.insert(
            BreadcrumbName::new("mappings"),
            (&self.mappings).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for ImportNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct ImportPathNode {
    pub info: NodeInfo,
    pub path: Arc<String>,
}
impl From<&Arc<ImportPathNode>> for AnyAstNode {
    fn from(value: &Arc<ImportPathNode>) -> Self {
        AnyAstNode::ImportPath(value.clone())
    }
}
impl AstNode for ImportPathNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        HashMap::new()
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for ImportPathNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct ImportMappingNode {
    pub info: NodeInfo,
    pub source_name: Arc<IdentifierNode>,
    pub rename: Option<Arc<IdentifierNode>>,
}
impl From<&Arc<ImportMappingNode>> for AnyAstNode {
    fn from(value: &Arc<ImportMappingNode>) -> Self {
        AnyAstNode::ImportMapping(value.clone())
    }
}
impl AstNode for ImportMappingNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("source_name"),
            (&self.source_name).into(),
        );
        result.insert(
            BreadcrumbName::new("rename"),
            (&self.rename).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for ImportMappingNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct FunctionNode {
    pub info: NodeInfo,
    pub name: Arc<IdentifierNode>,
    pub params: Vec<Arc<FunctionSignatureParameterNode>>,
    pub body: BodyNode,
    pub return_type: Option<TypeReferenceNode>,
}
impl From<&Arc<FunctionNode>> for AnyAstNode {
    fn from(value: &Arc<FunctionNode>) -> Self {
        AnyAstNode::Function(value.clone())
    }
}
impl AstNode for FunctionNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("name"),
            (&self.name).into(),
        );
        result.insert(
            BreadcrumbName::new("params"),
            (&self.params).into(),
        );
        result.insert(
            BreadcrumbName::new("body"),
            (&self.body).into(),
        );
        result.insert(
            BreadcrumbName::new("return_type"),
            (&self.return_type).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for FunctionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct BlockBodyNode {
    pub info: NodeInfo,
    pub statements: Vec<StatementNode>,
}
impl From<&Arc<BlockBodyNode>> for AnyAstNode {
    fn from(value: &Arc<BlockBodyNode>) -> Self {
        AnyAstNode::BlockBody(value.clone())
    }
}
impl AstNode for BlockBodyNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("statements"),
            (&self.statements).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for BlockBodyNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct ExpressionStatementNode {
    pub info: NodeInfo,
    pub expression: ExpressionNode,
}
impl From<&Arc<ExpressionStatementNode>> for AnyAstNode {
    fn from(value: &Arc<ExpressionStatementNode>) -> Self {
        AnyAstNode::ExpressionStatement(value.clone())
    }
}
impl AstNode for ExpressionStatementNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("expression"),
            (&self.expression).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for ExpressionStatementNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct CauseExpressionNode {
    pub info: NodeInfo,
    pub signal: ExpressionNode,
}
impl From<&Arc<CauseExpressionNode>> for AnyAstNode {
    fn from(value: &Arc<CauseExpressionNode>) -> Self {
        AnyAstNode::CauseExpression(value.clone())
    }
}
impl AstNode for CauseExpressionNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("signal"),
            (&self.signal).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for CauseExpressionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct CallExpressionNode {
    pub info: NodeInfo,
    pub callee: ExpressionNode,
    pub parameters: Vec<Arc<FunctionCallParameterNode>>,
}
impl From<&Arc<CallExpressionNode>> for AnyAstNode {
    fn from(value: &Arc<CallExpressionNode>) -> Self {
        AnyAstNode::CallExpression(value.clone())
    }
}
impl AstNode for CallExpressionNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("callee"),
            (&self.callee).into(),
        );
        result.insert(
            BreadcrumbName::new("parameters"),
            (&self.parameters).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for CallExpressionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct IdentifierExpressionNode {
    pub info: NodeInfo,
    pub identifier: Arc<IdentifierNode>,
}
impl From<&Arc<IdentifierExpressionNode>> for AnyAstNode {
    fn from(value: &Arc<IdentifierExpressionNode>) -> Self {
        AnyAstNode::IdentifierExpression(value.clone())
    }
}
impl AstNode for IdentifierExpressionNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("identifier"),
            (&self.identifier).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for IdentifierExpressionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct StringLiteralExpressionNode {
    pub info: NodeInfo,
    pub text: Arc<String>,
}
impl From<&Arc<StringLiteralExpressionNode>> for AnyAstNode {
    fn from(value: &Arc<StringLiteralExpressionNode>) -> Self {
        AnyAstNode::StringLiteralExpression(value.clone())
    }
}
impl AstNode for StringLiteralExpressionNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        HashMap::new()
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for StringLiteralExpressionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

