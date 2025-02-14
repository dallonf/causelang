pub static BREADCRUMB_NAMES: &[&str] = &[
    "text",
    "identifier",
    "name",
    "type_reference",
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
    "name",
    "type_annotation",
    "value",
    "is_variable",
    "name",
    "fields",
    "name",
    "fields",
    "result",
    "name",
    "type_annotation",
    "name",
    "options",
    "statements",
    "statement",
    "expression",
    "declaration",
    "block",
    "pattern",
    "body",
    "identifier",
    "expression",
    "with_value",
    "branches",
    "condition",
    "body",
    "pattern",
    "body",
    "body",
    "body",
    "signal",
    "callee",
    "parameters",
    "object_expression",
    "member_identifier",
    "identifier",
    "text",
    "value",
    "value",
    "with_value",
];

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize, EnumTryAs)]
pub enum AnyAstNode {
    Identifier(Arc<IdentifierNode>),
    IdentifierTypeReference(Arc<IdentifierTypeReferenceNode>),
    Pattern(Arc<PatternNode>),
    FunctionSignatureParameter(Arc<FunctionSignatureParameterNode>),
    FunctionCallParameter(Arc<FunctionCallParameterNode>),
    File(Arc<FileNode>),
    Import(Arc<ImportNode>),
    ImportPath(Arc<ImportPathNode>),
    ImportMapping(Arc<ImportMappingNode>),
    Function(Arc<FunctionNode>),
    NamedValue(Arc<NamedValueNode>),
    ObjectType(Arc<ObjectTypeNode>),
    SignalType(Arc<SignalTypeNode>),
    ObjectField(Arc<ObjectFieldNode>),
    OneOfType(Arc<OneOfTypeNode>),
    BlockBody(Arc<BlockBodyNode>),
    SingleStatementBody(Arc<SingleStatementBodyNode>),
    ExpressionStatement(Arc<ExpressionStatementNode>),
    DeclarationStatement(Arc<DeclarationStatementNode>),
    BlockExpression(Arc<BlockExpressionNode>),
    EffectStatement(Arc<EffectStatementNode>),
    SetStatement(Arc<SetStatementNode>),
    BranchExpression(Arc<BranchExpressionNode>),
    IfBranchOption(Arc<IfBranchOptionNode>),
    IsBranchOption(Arc<IsBranchOptionNode>),
    ElseBranchOption(Arc<ElseBranchOptionNode>),
    LoopExpression(Arc<LoopExpressionNode>),
    CauseExpression(Arc<CauseExpressionNode>),
    CallExpression(Arc<CallExpressionNode>),
    MemberExpression(Arc<MemberExpressionNode>),
    IdentifierExpression(Arc<IdentifierExpressionNode>),
    StringLiteralExpression(Arc<StringLiteralExpressionNode>),
    NumberLiteralExpression(Arc<NumberLiteralExpressionNode>),
    ReturnExpression(Arc<ReturnExpressionNode>),
    BreakExpression(Arc<BreakExpressionNode>),
}
impl AstNode for AnyAstNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        match self {
            AnyAstNode::Identifier(node) => node.children(),
            AnyAstNode::IdentifierTypeReference(node) => node.children(),
            AnyAstNode::Pattern(node) => node.children(),
            AnyAstNode::FunctionSignatureParameter(node) => node.children(),
            AnyAstNode::FunctionCallParameter(node) => node.children(),
            AnyAstNode::File(node) => node.children(),
            AnyAstNode::Import(node) => node.children(),
            AnyAstNode::ImportPath(node) => node.children(),
            AnyAstNode::ImportMapping(node) => node.children(),
            AnyAstNode::Function(node) => node.children(),
            AnyAstNode::NamedValue(node) => node.children(),
            AnyAstNode::ObjectType(node) => node.children(),
            AnyAstNode::SignalType(node) => node.children(),
            AnyAstNode::ObjectField(node) => node.children(),
            AnyAstNode::OneOfType(node) => node.children(),
            AnyAstNode::BlockBody(node) => node.children(),
            AnyAstNode::SingleStatementBody(node) => node.children(),
            AnyAstNode::ExpressionStatement(node) => node.children(),
            AnyAstNode::DeclarationStatement(node) => node.children(),
            AnyAstNode::BlockExpression(node) => node.children(),
            AnyAstNode::EffectStatement(node) => node.children(),
            AnyAstNode::SetStatement(node) => node.children(),
            AnyAstNode::BranchExpression(node) => node.children(),
            AnyAstNode::IfBranchOption(node) => node.children(),
            AnyAstNode::IsBranchOption(node) => node.children(),
            AnyAstNode::ElseBranchOption(node) => node.children(),
            AnyAstNode::LoopExpression(node) => node.children(),
            AnyAstNode::CauseExpression(node) => node.children(),
            AnyAstNode::CallExpression(node) => node.children(),
            AnyAstNode::MemberExpression(node) => node.children(),
            AnyAstNode::IdentifierExpression(node) => node.children(),
            AnyAstNode::StringLiteralExpression(node) => node.children(),
            AnyAstNode::NumberLiteralExpression(node) => node.children(),
            AnyAstNode::ReturnExpression(node) => node.children(),
            AnyAstNode::BreakExpression(node) => node.children(),
        }
    }
    fn info(&self) -> &NodeInfo {
        match self {
            AnyAstNode::Identifier(node) => node.info(),
            AnyAstNode::IdentifierTypeReference(node) => node.info(),
            AnyAstNode::Pattern(node) => node.info(),
            AnyAstNode::FunctionSignatureParameter(node) => node.info(),
            AnyAstNode::FunctionCallParameter(node) => node.info(),
            AnyAstNode::File(node) => node.info(),
            AnyAstNode::Import(node) => node.info(),
            AnyAstNode::ImportPath(node) => node.info(),
            AnyAstNode::ImportMapping(node) => node.info(),
            AnyAstNode::Function(node) => node.info(),
            AnyAstNode::NamedValue(node) => node.info(),
            AnyAstNode::ObjectType(node) => node.info(),
            AnyAstNode::SignalType(node) => node.info(),
            AnyAstNode::ObjectField(node) => node.info(),
            AnyAstNode::OneOfType(node) => node.info(),
            AnyAstNode::BlockBody(node) => node.info(),
            AnyAstNode::SingleStatementBody(node) => node.info(),
            AnyAstNode::ExpressionStatement(node) => node.info(),
            AnyAstNode::DeclarationStatement(node) => node.info(),
            AnyAstNode::BlockExpression(node) => node.info(),
            AnyAstNode::EffectStatement(node) => node.info(),
            AnyAstNode::SetStatement(node) => node.info(),
            AnyAstNode::BranchExpression(node) => node.info(),
            AnyAstNode::IfBranchOption(node) => node.info(),
            AnyAstNode::IsBranchOption(node) => node.info(),
            AnyAstNode::ElseBranchOption(node) => node.info(),
            AnyAstNode::LoopExpression(node) => node.info(),
            AnyAstNode::CauseExpression(node) => node.info(),
            AnyAstNode::CallExpression(node) => node.info(),
            AnyAstNode::MemberExpression(node) => node.info(),
            AnyAstNode::IdentifierExpression(node) => node.info(),
            AnyAstNode::StringLiteralExpression(node) => node.info(),
            AnyAstNode::NumberLiteralExpression(node) => node.info(),
            AnyAstNode::ReturnExpression(node) => node.info(),
            AnyAstNode::BreakExpression(node) => node.info(),
        }
    }
}
impl HasBreadcrumbs for AnyAstNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        match self {
            AnyAstNode::Identifier(node) => node.breadcrumbs(),
            AnyAstNode::IdentifierTypeReference(node) => node.breadcrumbs(),
            AnyAstNode::Pattern(node) => node.breadcrumbs(),
            AnyAstNode::FunctionSignatureParameter(node) => node.breadcrumbs(),
            AnyAstNode::FunctionCallParameter(node) => node.breadcrumbs(),
            AnyAstNode::File(node) => node.breadcrumbs(),
            AnyAstNode::Import(node) => node.breadcrumbs(),
            AnyAstNode::ImportPath(node) => node.breadcrumbs(),
            AnyAstNode::ImportMapping(node) => node.breadcrumbs(),
            AnyAstNode::Function(node) => node.breadcrumbs(),
            AnyAstNode::NamedValue(node) => node.breadcrumbs(),
            AnyAstNode::ObjectType(node) => node.breadcrumbs(),
            AnyAstNode::SignalType(node) => node.breadcrumbs(),
            AnyAstNode::ObjectField(node) => node.breadcrumbs(),
            AnyAstNode::OneOfType(node) => node.breadcrumbs(),
            AnyAstNode::BlockBody(node) => node.breadcrumbs(),
            AnyAstNode::SingleStatementBody(node) => node.breadcrumbs(),
            AnyAstNode::ExpressionStatement(node) => node.breadcrumbs(),
            AnyAstNode::DeclarationStatement(node) => node.breadcrumbs(),
            AnyAstNode::BlockExpression(node) => node.breadcrumbs(),
            AnyAstNode::EffectStatement(node) => node.breadcrumbs(),
            AnyAstNode::SetStatement(node) => node.breadcrumbs(),
            AnyAstNode::BranchExpression(node) => node.breadcrumbs(),
            AnyAstNode::IfBranchOption(node) => node.breadcrumbs(),
            AnyAstNode::IsBranchOption(node) => node.breadcrumbs(),
            AnyAstNode::ElseBranchOption(node) => node.breadcrumbs(),
            AnyAstNode::LoopExpression(node) => node.breadcrumbs(),
            AnyAstNode::CauseExpression(node) => node.breadcrumbs(),
            AnyAstNode::CallExpression(node) => node.breadcrumbs(),
            AnyAstNode::MemberExpression(node) => node.breadcrumbs(),
            AnyAstNode::IdentifierExpression(node) => node.breadcrumbs(),
            AnyAstNode::StringLiteralExpression(node) => node.breadcrumbs(),
            AnyAstNode::NumberLiteralExpression(node) => node.breadcrumbs(),
            AnyAstNode::ReturnExpression(node) => node.breadcrumbs(),
            AnyAstNode::BreakExpression(node) => node.breadcrumbs(),
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
        value.clone().into()
    }
}
impl From<TypeReferenceNode> for AnyAstNode {
    fn from(value: TypeReferenceNode) -> Self {
        match value {
            TypeReferenceNode::Identifier(node) => AnyAstNode::IdentifierTypeReference(node),
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
    NamedValue(Arc<NamedValueNode>),
    ObjectType(Arc<ObjectTypeNode>),
    SignalType(Arc<SignalTypeNode>),
    OneOfType(Arc<OneOfTypeNode>),
}
impl AstNode for DeclarationNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        match self {
            DeclarationNode::Import(node) => node.children(),
            DeclarationNode::Function(node) => node.children(),
            DeclarationNode::NamedValue(node) => node.children(),
            DeclarationNode::ObjectType(node) => node.children(),
            DeclarationNode::SignalType(node) => node.children(),
            DeclarationNode::OneOfType(node) => node.children(),
        }
    }
    fn info(&self) -> &NodeInfo {
        match self {
            DeclarationNode::Import(node) => node.info(),
            DeclarationNode::Function(node) => node.info(),
            DeclarationNode::NamedValue(node) => node.info(),
            DeclarationNode::ObjectType(node) => node.info(),
            DeclarationNode::SignalType(node) => node.info(),
            DeclarationNode::OneOfType(node) => node.info(),
        }
    }
}
impl From<&DeclarationNode> for AnyAstNode {
    fn from(value: &DeclarationNode) -> Self {
        value.clone().into()
    }
}
impl From<DeclarationNode> for AnyAstNode {
    fn from(value: DeclarationNode) -> Self {
        match value {
            DeclarationNode::Import(node) => AnyAstNode::Import(node),
            DeclarationNode::Function(node) => AnyAstNode::Function(node),
            DeclarationNode::NamedValue(node) => AnyAstNode::NamedValue(node),
            DeclarationNode::ObjectType(node) => AnyAstNode::ObjectType(node),
            DeclarationNode::SignalType(node) => AnyAstNode::SignalType(node),
            DeclarationNode::OneOfType(node) => AnyAstNode::OneOfType(node),
        }
    }
}
impl HasBreadcrumbs for DeclarationNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        match self {
            DeclarationNode::Import(node) => node.breadcrumbs(),
            DeclarationNode::Function(node) => node.breadcrumbs(),
            DeclarationNode::NamedValue(node) => node.breadcrumbs(),
            DeclarationNode::ObjectType(node) => node.breadcrumbs(),
            DeclarationNode::SignalType(node) => node.breadcrumbs(),
            DeclarationNode::OneOfType(node) => node.breadcrumbs(),
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub enum BodyNode {
    Block(Arc<BlockBodyNode>),
    SingleStatement(Arc<SingleStatementBodyNode>),
}
impl AstNode for BodyNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        match self {
            BodyNode::Block(node) => node.children(),
            BodyNode::SingleStatement(node) => node.children(),
        }
    }
    fn info(&self) -> &NodeInfo {
        match self {
            BodyNode::Block(node) => node.info(),
            BodyNode::SingleStatement(node) => node.info(),
        }
    }
}
impl From<&BodyNode> for AnyAstNode {
    fn from(value: &BodyNode) -> Self {
        value.clone().into()
    }
}
impl From<BodyNode> for AnyAstNode {
    fn from(value: BodyNode) -> Self {
        match value {
            BodyNode::Block(node) => AnyAstNode::BlockBody(node),
            BodyNode::SingleStatement(node) => AnyAstNode::SingleStatementBody(node),
        }
    }
}
impl HasBreadcrumbs for BodyNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        match self {
            BodyNode::Block(node) => node.breadcrumbs(),
            BodyNode::SingleStatement(node) => node.breadcrumbs(),
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub enum StatementNode {
    Expression(Arc<ExpressionStatementNode>),
    Declaration(Arc<DeclarationStatementNode>),
    Effect(Arc<EffectStatementNode>),
    Set(Arc<SetStatementNode>),
}
impl AstNode for StatementNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        match self {
            StatementNode::Expression(node) => node.children(),
            StatementNode::Declaration(node) => node.children(),
            StatementNode::Effect(node) => node.children(),
            StatementNode::Set(node) => node.children(),
        }
    }
    fn info(&self) -> &NodeInfo {
        match self {
            StatementNode::Expression(node) => node.info(),
            StatementNode::Declaration(node) => node.info(),
            StatementNode::Effect(node) => node.info(),
            StatementNode::Set(node) => node.info(),
        }
    }
}
impl From<&StatementNode> for AnyAstNode {
    fn from(value: &StatementNode) -> Self {
        value.clone().into()
    }
}
impl From<StatementNode> for AnyAstNode {
    fn from(value: StatementNode) -> Self {
        match value {
            StatementNode::Expression(node) => AnyAstNode::ExpressionStatement(node),
            StatementNode::Declaration(node) => AnyAstNode::DeclarationStatement(node),
            StatementNode::Effect(node) => AnyAstNode::EffectStatement(node),
            StatementNode::Set(node) => AnyAstNode::SetStatement(node),
        }
    }
}
impl HasBreadcrumbs for StatementNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        match self {
            StatementNode::Expression(node) => node.breadcrumbs(),
            StatementNode::Declaration(node) => node.breadcrumbs(),
            StatementNode::Effect(node) => node.breadcrumbs(),
            StatementNode::Set(node) => node.breadcrumbs(),
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub enum ExpressionNode {
    Block(Arc<BlockExpressionNode>),
    Branch(Arc<BranchExpressionNode>),
    Loop(Arc<LoopExpressionNode>),
    Cause(Arc<CauseExpressionNode>),
    Call(Arc<CallExpressionNode>),
    Member(Arc<MemberExpressionNode>),
    Identifier(Arc<IdentifierExpressionNode>),
    StringLiteral(Arc<StringLiteralExpressionNode>),
    NumberLiteral(Arc<NumberLiteralExpressionNode>),
    Return(Arc<ReturnExpressionNode>),
    Break(Arc<BreakExpressionNode>),
}
impl AstNode for ExpressionNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        match self {
            ExpressionNode::Block(node) => node.children(),
            ExpressionNode::Branch(node) => node.children(),
            ExpressionNode::Loop(node) => node.children(),
            ExpressionNode::Cause(node) => node.children(),
            ExpressionNode::Call(node) => node.children(),
            ExpressionNode::Member(node) => node.children(),
            ExpressionNode::Identifier(node) => node.children(),
            ExpressionNode::StringLiteral(node) => node.children(),
            ExpressionNode::NumberLiteral(node) => node.children(),
            ExpressionNode::Return(node) => node.children(),
            ExpressionNode::Break(node) => node.children(),
        }
    }
    fn info(&self) -> &NodeInfo {
        match self {
            ExpressionNode::Block(node) => node.info(),
            ExpressionNode::Branch(node) => node.info(),
            ExpressionNode::Loop(node) => node.info(),
            ExpressionNode::Cause(node) => node.info(),
            ExpressionNode::Call(node) => node.info(),
            ExpressionNode::Member(node) => node.info(),
            ExpressionNode::Identifier(node) => node.info(),
            ExpressionNode::StringLiteral(node) => node.info(),
            ExpressionNode::NumberLiteral(node) => node.info(),
            ExpressionNode::Return(node) => node.info(),
            ExpressionNode::Break(node) => node.info(),
        }
    }
}
impl From<&ExpressionNode> for AnyAstNode {
    fn from(value: &ExpressionNode) -> Self {
        value.clone().into()
    }
}
impl From<ExpressionNode> for AnyAstNode {
    fn from(value: ExpressionNode) -> Self {
        match value {
            ExpressionNode::Block(node) => AnyAstNode::BlockExpression(node),
            ExpressionNode::Branch(node) => AnyAstNode::BranchExpression(node),
            ExpressionNode::Loop(node) => AnyAstNode::LoopExpression(node),
            ExpressionNode::Cause(node) => AnyAstNode::CauseExpression(node),
            ExpressionNode::Call(node) => AnyAstNode::CallExpression(node),
            ExpressionNode::Member(node) => AnyAstNode::MemberExpression(node),
            ExpressionNode::Identifier(node) => AnyAstNode::IdentifierExpression(node),
            ExpressionNode::StringLiteral(node) => AnyAstNode::StringLiteralExpression(node),
            ExpressionNode::NumberLiteral(node) => AnyAstNode::NumberLiteralExpression(node),
            ExpressionNode::Return(node) => AnyAstNode::ReturnExpression(node),
            ExpressionNode::Break(node) => AnyAstNode::BreakExpression(node),
        }
    }
}
impl HasBreadcrumbs for ExpressionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        match self {
            ExpressionNode::Block(node) => node.breadcrumbs(),
            ExpressionNode::Branch(node) => node.breadcrumbs(),
            ExpressionNode::Loop(node) => node.breadcrumbs(),
            ExpressionNode::Cause(node) => node.breadcrumbs(),
            ExpressionNode::Call(node) => node.breadcrumbs(),
            ExpressionNode::Member(node) => node.breadcrumbs(),
            ExpressionNode::Identifier(node) => node.breadcrumbs(),
            ExpressionNode::StringLiteral(node) => node.breadcrumbs(),
            ExpressionNode::NumberLiteral(node) => node.breadcrumbs(),
            ExpressionNode::Return(node) => node.breadcrumbs(),
            ExpressionNode::Break(node) => node.breadcrumbs(),
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub enum BranchOptionNode {
    If(Arc<IfBranchOptionNode>),
    Is(Arc<IsBranchOptionNode>),
    Else(Arc<ElseBranchOptionNode>),
}
impl AstNode for BranchOptionNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        match self {
            BranchOptionNode::If(node) => node.children(),
            BranchOptionNode::Is(node) => node.children(),
            BranchOptionNode::Else(node) => node.children(),
        }
    }
    fn info(&self) -> &NodeInfo {
        match self {
            BranchOptionNode::If(node) => node.info(),
            BranchOptionNode::Is(node) => node.info(),
            BranchOptionNode::Else(node) => node.info(),
        }
    }
}
impl From<&BranchOptionNode> for AnyAstNode {
    fn from(value: &BranchOptionNode) -> Self {
        value.clone().into()
    }
}
impl From<BranchOptionNode> for AnyAstNode {
    fn from(value: BranchOptionNode) -> Self {
        match value {
            BranchOptionNode::If(node) => AnyAstNode::IfBranchOption(node),
            BranchOptionNode::Is(node) => AnyAstNode::IsBranchOption(node),
            BranchOptionNode::Else(node) => AnyAstNode::ElseBranchOption(node),
        }
    }
}
impl HasBreadcrumbs for BranchOptionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        match self {
            BranchOptionNode::If(node) => node.breadcrumbs(),
            BranchOptionNode::Is(node) => node.breadcrumbs(),
            BranchOptionNode::Else(node) => node.breadcrumbs(),
        }
    }
}


#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct IdentifierNode {
    pub info: NodeInfo,
    pub text: Arc<String>,
}
impl From<&IdentifierNode> for AnyAstNode {
    fn from(value: &IdentifierNode) -> Self {
        AnyAstNode::Identifier(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<IdentifierNode>> for AnyAstNode {
    fn from(value: &Arc<IdentifierNode>) -> Self {
        AnyAstNode::Identifier(value.clone())
    }
}
impl From<Arc<IdentifierNode>> for AnyAstNode {
    fn from(value: Arc<IdentifierNode>) -> Self {
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
impl From<&IdentifierTypeReferenceNode> for AnyAstNode {
    fn from(value: &IdentifierTypeReferenceNode) -> Self {
        AnyAstNode::IdentifierTypeReference(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<IdentifierTypeReferenceNode>> for AnyAstNode {
    fn from(value: &Arc<IdentifierTypeReferenceNode>) -> Self {
        AnyAstNode::IdentifierTypeReference(value.clone())
    }
}
impl From<Arc<IdentifierTypeReferenceNode>> for AnyAstNode {
    fn from(value: Arc<IdentifierTypeReferenceNode>) -> Self {
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
pub struct PatternNode {
    pub info: NodeInfo,
    pub name: Option<Arc<IdentifierNode>>,
    pub type_reference: TypeReferenceNode,
}
impl From<&PatternNode> for AnyAstNode {
    fn from(value: &PatternNode) -> Self {
        AnyAstNode::Pattern(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<PatternNode>> for AnyAstNode {
    fn from(value: &Arc<PatternNode>) -> Self {
        AnyAstNode::Pattern(value.clone())
    }
}
impl From<Arc<PatternNode>> for AnyAstNode {
    fn from(value: Arc<PatternNode>) -> Self {
        AnyAstNode::Pattern(value.clone())
    }
}
impl AstNode for PatternNode {
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
impl HasBreadcrumbs for PatternNode {
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
impl From<&FunctionSignatureParameterNode> for AnyAstNode {
    fn from(value: &FunctionSignatureParameterNode) -> Self {
        AnyAstNode::FunctionSignatureParameter(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<FunctionSignatureParameterNode>> for AnyAstNode {
    fn from(value: &Arc<FunctionSignatureParameterNode>) -> Self {
        AnyAstNode::FunctionSignatureParameter(value.clone())
    }
}
impl From<Arc<FunctionSignatureParameterNode>> for AnyAstNode {
    fn from(value: Arc<FunctionSignatureParameterNode>) -> Self {
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
impl From<&FunctionCallParameterNode> for AnyAstNode {
    fn from(value: &FunctionCallParameterNode) -> Self {
        AnyAstNode::FunctionCallParameter(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<FunctionCallParameterNode>> for AnyAstNode {
    fn from(value: &Arc<FunctionCallParameterNode>) -> Self {
        AnyAstNode::FunctionCallParameter(value.clone())
    }
}
impl From<Arc<FunctionCallParameterNode>> for AnyAstNode {
    fn from(value: Arc<FunctionCallParameterNode>) -> Self {
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
impl From<&FileNode> for AnyAstNode {
    fn from(value: &FileNode) -> Self {
        AnyAstNode::File(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<FileNode>> for AnyAstNode {
    fn from(value: &Arc<FileNode>) -> Self {
        AnyAstNode::File(value.clone())
    }
}
impl From<Arc<FileNode>> for AnyAstNode {
    fn from(value: Arc<FileNode>) -> Self {
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
impl From<&ImportNode> for AnyAstNode {
    fn from(value: &ImportNode) -> Self {
        AnyAstNode::Import(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<ImportNode>> for AnyAstNode {
    fn from(value: &Arc<ImportNode>) -> Self {
        AnyAstNode::Import(value.clone())
    }
}
impl From<Arc<ImportNode>> for AnyAstNode {
    fn from(value: Arc<ImportNode>) -> Self {
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
impl From<&ImportPathNode> for AnyAstNode {
    fn from(value: &ImportPathNode) -> Self {
        AnyAstNode::ImportPath(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<ImportPathNode>> for AnyAstNode {
    fn from(value: &Arc<ImportPathNode>) -> Self {
        AnyAstNode::ImportPath(value.clone())
    }
}
impl From<Arc<ImportPathNode>> for AnyAstNode {
    fn from(value: Arc<ImportPathNode>) -> Self {
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
impl From<&ImportMappingNode> for AnyAstNode {
    fn from(value: &ImportMappingNode) -> Self {
        AnyAstNode::ImportMapping(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<ImportMappingNode>> for AnyAstNode {
    fn from(value: &Arc<ImportMappingNode>) -> Self {
        AnyAstNode::ImportMapping(value.clone())
    }
}
impl From<Arc<ImportMappingNode>> for AnyAstNode {
    fn from(value: Arc<ImportMappingNode>) -> Self {
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
impl From<&FunctionNode> for AnyAstNode {
    fn from(value: &FunctionNode) -> Self {
        AnyAstNode::Function(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<FunctionNode>> for AnyAstNode {
    fn from(value: &Arc<FunctionNode>) -> Self {
        AnyAstNode::Function(value.clone())
    }
}
impl From<Arc<FunctionNode>> for AnyAstNode {
    fn from(value: Arc<FunctionNode>) -> Self {
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
pub struct NamedValueNode {
    pub info: NodeInfo,
    pub name: Arc<IdentifierNode>,
    pub type_annotation: Option<TypeReferenceNode>,
    pub value: ExpressionNode,
    pub is_variable: bool,
}
impl From<&NamedValueNode> for AnyAstNode {
    fn from(value: &NamedValueNode) -> Self {
        AnyAstNode::NamedValue(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<NamedValueNode>> for AnyAstNode {
    fn from(value: &Arc<NamedValueNode>) -> Self {
        AnyAstNode::NamedValue(value.clone())
    }
}
impl From<Arc<NamedValueNode>> for AnyAstNode {
    fn from(value: Arc<NamedValueNode>) -> Self {
        AnyAstNode::NamedValue(value.clone())
    }
}
impl AstNode for NamedValueNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("name"),
            (&self.name).into(),
        );
        result.insert(
            BreadcrumbName::new("type_annotation"),
            (&self.type_annotation).into(),
        );
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
impl HasBreadcrumbs for NamedValueNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct ObjectTypeNode {
    pub info: NodeInfo,
    pub name: Arc<IdentifierNode>,
    pub fields: Vec<Arc<ObjectFieldNode>>,
}
impl From<&ObjectTypeNode> for AnyAstNode {
    fn from(value: &ObjectTypeNode) -> Self {
        AnyAstNode::ObjectType(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<ObjectTypeNode>> for AnyAstNode {
    fn from(value: &Arc<ObjectTypeNode>) -> Self {
        AnyAstNode::ObjectType(value.clone())
    }
}
impl From<Arc<ObjectTypeNode>> for AnyAstNode {
    fn from(value: Arc<ObjectTypeNode>) -> Self {
        AnyAstNode::ObjectType(value.clone())
    }
}
impl AstNode for ObjectTypeNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("name"),
            (&self.name).into(),
        );
        result.insert(
            BreadcrumbName::new("fields"),
            (&self.fields).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for ObjectTypeNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct SignalTypeNode {
    pub info: NodeInfo,
    pub name: Arc<IdentifierNode>,
    pub fields: Vec<Arc<ObjectFieldNode>>,
    pub result: Option<TypeReferenceNode>,
}
impl From<&SignalTypeNode> for AnyAstNode {
    fn from(value: &SignalTypeNode) -> Self {
        AnyAstNode::SignalType(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<SignalTypeNode>> for AnyAstNode {
    fn from(value: &Arc<SignalTypeNode>) -> Self {
        AnyAstNode::SignalType(value.clone())
    }
}
impl From<Arc<SignalTypeNode>> for AnyAstNode {
    fn from(value: Arc<SignalTypeNode>) -> Self {
        AnyAstNode::SignalType(value.clone())
    }
}
impl AstNode for SignalTypeNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("name"),
            (&self.name).into(),
        );
        result.insert(
            BreadcrumbName::new("fields"),
            (&self.fields).into(),
        );
        result.insert(
            BreadcrumbName::new("result"),
            (&self.result).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for SignalTypeNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct ObjectFieldNode {
    pub info: NodeInfo,
    pub name: Arc<IdentifierNode>,
    pub type_annotation: TypeReferenceNode,
}
impl From<&ObjectFieldNode> for AnyAstNode {
    fn from(value: &ObjectFieldNode) -> Self {
        AnyAstNode::ObjectField(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<ObjectFieldNode>> for AnyAstNode {
    fn from(value: &Arc<ObjectFieldNode>) -> Self {
        AnyAstNode::ObjectField(value.clone())
    }
}
impl From<Arc<ObjectFieldNode>> for AnyAstNode {
    fn from(value: Arc<ObjectFieldNode>) -> Self {
        AnyAstNode::ObjectField(value.clone())
    }
}
impl AstNode for ObjectFieldNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("name"),
            (&self.name).into(),
        );
        result.insert(
            BreadcrumbName::new("type_annotation"),
            (&self.type_annotation).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for ObjectFieldNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct OneOfTypeNode {
    pub info: NodeInfo,
    pub name: Arc<IdentifierNode>,
    pub options: Vec<TypeReferenceNode>,
}
impl From<&OneOfTypeNode> for AnyAstNode {
    fn from(value: &OneOfTypeNode) -> Self {
        AnyAstNode::OneOfType(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<OneOfTypeNode>> for AnyAstNode {
    fn from(value: &Arc<OneOfTypeNode>) -> Self {
        AnyAstNode::OneOfType(value.clone())
    }
}
impl From<Arc<OneOfTypeNode>> for AnyAstNode {
    fn from(value: Arc<OneOfTypeNode>) -> Self {
        AnyAstNode::OneOfType(value.clone())
    }
}
impl AstNode for OneOfTypeNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("name"),
            (&self.name).into(),
        );
        result.insert(
            BreadcrumbName::new("options"),
            (&self.options).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for OneOfTypeNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct BlockBodyNode {
    pub info: NodeInfo,
    pub statements: Vec<StatementNode>,
}
impl From<&BlockBodyNode> for AnyAstNode {
    fn from(value: &BlockBodyNode) -> Self {
        AnyAstNode::BlockBody(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<BlockBodyNode>> for AnyAstNode {
    fn from(value: &Arc<BlockBodyNode>) -> Self {
        AnyAstNode::BlockBody(value.clone())
    }
}
impl From<Arc<BlockBodyNode>> for AnyAstNode {
    fn from(value: Arc<BlockBodyNode>) -> Self {
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
pub struct SingleStatementBodyNode {
    pub info: NodeInfo,
    pub statement: StatementNode,
}
impl From<&SingleStatementBodyNode> for AnyAstNode {
    fn from(value: &SingleStatementBodyNode) -> Self {
        AnyAstNode::SingleStatementBody(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<SingleStatementBodyNode>> for AnyAstNode {
    fn from(value: &Arc<SingleStatementBodyNode>) -> Self {
        AnyAstNode::SingleStatementBody(value.clone())
    }
}
impl From<Arc<SingleStatementBodyNode>> for AnyAstNode {
    fn from(value: Arc<SingleStatementBodyNode>) -> Self {
        AnyAstNode::SingleStatementBody(value.clone())
    }
}
impl AstNode for SingleStatementBodyNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("statement"),
            (&self.statement).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for SingleStatementBodyNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct ExpressionStatementNode {
    pub info: NodeInfo,
    pub expression: ExpressionNode,
}
impl From<&ExpressionStatementNode> for AnyAstNode {
    fn from(value: &ExpressionStatementNode) -> Self {
        AnyAstNode::ExpressionStatement(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<ExpressionStatementNode>> for AnyAstNode {
    fn from(value: &Arc<ExpressionStatementNode>) -> Self {
        AnyAstNode::ExpressionStatement(value.clone())
    }
}
impl From<Arc<ExpressionStatementNode>> for AnyAstNode {
    fn from(value: Arc<ExpressionStatementNode>) -> Self {
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
pub struct DeclarationStatementNode {
    pub info: NodeInfo,
    pub declaration: DeclarationNode,
}
impl From<&DeclarationStatementNode> for AnyAstNode {
    fn from(value: &DeclarationStatementNode) -> Self {
        AnyAstNode::DeclarationStatement(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<DeclarationStatementNode>> for AnyAstNode {
    fn from(value: &Arc<DeclarationStatementNode>) -> Self {
        AnyAstNode::DeclarationStatement(value.clone())
    }
}
impl From<Arc<DeclarationStatementNode>> for AnyAstNode {
    fn from(value: Arc<DeclarationStatementNode>) -> Self {
        AnyAstNode::DeclarationStatement(value.clone())
    }
}
impl AstNode for DeclarationStatementNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("declaration"),
            (&self.declaration).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for DeclarationStatementNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct BlockExpressionNode {
    pub info: NodeInfo,
    pub block: Arc<BlockBodyNode>,
}
impl From<&BlockExpressionNode> for AnyAstNode {
    fn from(value: &BlockExpressionNode) -> Self {
        AnyAstNode::BlockExpression(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<BlockExpressionNode>> for AnyAstNode {
    fn from(value: &Arc<BlockExpressionNode>) -> Self {
        AnyAstNode::BlockExpression(value.clone())
    }
}
impl From<Arc<BlockExpressionNode>> for AnyAstNode {
    fn from(value: Arc<BlockExpressionNode>) -> Self {
        AnyAstNode::BlockExpression(value.clone())
    }
}
impl AstNode for BlockExpressionNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("block"),
            (&self.block).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for BlockExpressionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct EffectStatementNode {
    pub info: NodeInfo,
    pub pattern: Arc<PatternNode>,
    pub body: BodyNode,
}
impl From<&EffectStatementNode> for AnyAstNode {
    fn from(value: &EffectStatementNode) -> Self {
        AnyAstNode::EffectStatement(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<EffectStatementNode>> for AnyAstNode {
    fn from(value: &Arc<EffectStatementNode>) -> Self {
        AnyAstNode::EffectStatement(value.clone())
    }
}
impl From<Arc<EffectStatementNode>> for AnyAstNode {
    fn from(value: Arc<EffectStatementNode>) -> Self {
        AnyAstNode::EffectStatement(value.clone())
    }
}
impl AstNode for EffectStatementNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("pattern"),
            (&self.pattern).into(),
        );
        result.insert(
            BreadcrumbName::new("body"),
            (&self.body).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for EffectStatementNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct SetStatementNode {
    pub info: NodeInfo,
    pub identifier: Arc<IdentifierNode>,
    pub expression: ExpressionNode,
}
impl From<&SetStatementNode> for AnyAstNode {
    fn from(value: &SetStatementNode) -> Self {
        AnyAstNode::SetStatement(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<SetStatementNode>> for AnyAstNode {
    fn from(value: &Arc<SetStatementNode>) -> Self {
        AnyAstNode::SetStatement(value.clone())
    }
}
impl From<Arc<SetStatementNode>> for AnyAstNode {
    fn from(value: Arc<SetStatementNode>) -> Self {
        AnyAstNode::SetStatement(value.clone())
    }
}
impl AstNode for SetStatementNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("identifier"),
            (&self.identifier).into(),
        );
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
impl HasBreadcrumbs for SetStatementNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct BranchExpressionNode {
    pub info: NodeInfo,
    pub with_value: Option<ExpressionNode>,
    pub branches: Vec<BranchOptionNode>,
}
impl From<&BranchExpressionNode> for AnyAstNode {
    fn from(value: &BranchExpressionNode) -> Self {
        AnyAstNode::BranchExpression(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<BranchExpressionNode>> for AnyAstNode {
    fn from(value: &Arc<BranchExpressionNode>) -> Self {
        AnyAstNode::BranchExpression(value.clone())
    }
}
impl From<Arc<BranchExpressionNode>> for AnyAstNode {
    fn from(value: Arc<BranchExpressionNode>) -> Self {
        AnyAstNode::BranchExpression(value.clone())
    }
}
impl AstNode for BranchExpressionNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("with_value"),
            (&self.with_value).into(),
        );
        result.insert(
            BreadcrumbName::new("branches"),
            (&self.branches).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for BranchExpressionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct IfBranchOptionNode {
    pub info: NodeInfo,
    pub condition: ExpressionNode,
    pub body: BodyNode,
}
impl From<&IfBranchOptionNode> for AnyAstNode {
    fn from(value: &IfBranchOptionNode) -> Self {
        AnyAstNode::IfBranchOption(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<IfBranchOptionNode>> for AnyAstNode {
    fn from(value: &Arc<IfBranchOptionNode>) -> Self {
        AnyAstNode::IfBranchOption(value.clone())
    }
}
impl From<Arc<IfBranchOptionNode>> for AnyAstNode {
    fn from(value: Arc<IfBranchOptionNode>) -> Self {
        AnyAstNode::IfBranchOption(value.clone())
    }
}
impl AstNode for IfBranchOptionNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("condition"),
            (&self.condition).into(),
        );
        result.insert(
            BreadcrumbName::new("body"),
            (&self.body).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for IfBranchOptionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct IsBranchOptionNode {
    pub info: NodeInfo,
    pub pattern: Arc<PatternNode>,
    pub body: BodyNode,
}
impl From<&IsBranchOptionNode> for AnyAstNode {
    fn from(value: &IsBranchOptionNode) -> Self {
        AnyAstNode::IsBranchOption(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<IsBranchOptionNode>> for AnyAstNode {
    fn from(value: &Arc<IsBranchOptionNode>) -> Self {
        AnyAstNode::IsBranchOption(value.clone())
    }
}
impl From<Arc<IsBranchOptionNode>> for AnyAstNode {
    fn from(value: Arc<IsBranchOptionNode>) -> Self {
        AnyAstNode::IsBranchOption(value.clone())
    }
}
impl AstNode for IsBranchOptionNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("pattern"),
            (&self.pattern).into(),
        );
        result.insert(
            BreadcrumbName::new("body"),
            (&self.body).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for IsBranchOptionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct ElseBranchOptionNode {
    pub info: NodeInfo,
    pub body: BodyNode,
}
impl From<&ElseBranchOptionNode> for AnyAstNode {
    fn from(value: &ElseBranchOptionNode) -> Self {
        AnyAstNode::ElseBranchOption(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<ElseBranchOptionNode>> for AnyAstNode {
    fn from(value: &Arc<ElseBranchOptionNode>) -> Self {
        AnyAstNode::ElseBranchOption(value.clone())
    }
}
impl From<Arc<ElseBranchOptionNode>> for AnyAstNode {
    fn from(value: Arc<ElseBranchOptionNode>) -> Self {
        AnyAstNode::ElseBranchOption(value.clone())
    }
}
impl AstNode for ElseBranchOptionNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("body"),
            (&self.body).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for ElseBranchOptionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct LoopExpressionNode {
    pub info: NodeInfo,
    pub body: BodyNode,
}
impl From<&LoopExpressionNode> for AnyAstNode {
    fn from(value: &LoopExpressionNode) -> Self {
        AnyAstNode::LoopExpression(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<LoopExpressionNode>> for AnyAstNode {
    fn from(value: &Arc<LoopExpressionNode>) -> Self {
        AnyAstNode::LoopExpression(value.clone())
    }
}
impl From<Arc<LoopExpressionNode>> for AnyAstNode {
    fn from(value: Arc<LoopExpressionNode>) -> Self {
        AnyAstNode::LoopExpression(value.clone())
    }
}
impl AstNode for LoopExpressionNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("body"),
            (&self.body).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for LoopExpressionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct CauseExpressionNode {
    pub info: NodeInfo,
    pub signal: ExpressionNode,
}
impl From<&CauseExpressionNode> for AnyAstNode {
    fn from(value: &CauseExpressionNode) -> Self {
        AnyAstNode::CauseExpression(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<CauseExpressionNode>> for AnyAstNode {
    fn from(value: &Arc<CauseExpressionNode>) -> Self {
        AnyAstNode::CauseExpression(value.clone())
    }
}
impl From<Arc<CauseExpressionNode>> for AnyAstNode {
    fn from(value: Arc<CauseExpressionNode>) -> Self {
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
impl From<&CallExpressionNode> for AnyAstNode {
    fn from(value: &CallExpressionNode) -> Self {
        AnyAstNode::CallExpression(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<CallExpressionNode>> for AnyAstNode {
    fn from(value: &Arc<CallExpressionNode>) -> Self {
        AnyAstNode::CallExpression(value.clone())
    }
}
impl From<Arc<CallExpressionNode>> for AnyAstNode {
    fn from(value: Arc<CallExpressionNode>) -> Self {
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
pub struct MemberExpressionNode {
    pub info: NodeInfo,
    pub object_expression: ExpressionNode,
    pub member_identifier: Arc<IdentifierNode>,
}
impl From<&MemberExpressionNode> for AnyAstNode {
    fn from(value: &MemberExpressionNode) -> Self {
        AnyAstNode::MemberExpression(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<MemberExpressionNode>> for AnyAstNode {
    fn from(value: &Arc<MemberExpressionNode>) -> Self {
        AnyAstNode::MemberExpression(value.clone())
    }
}
impl From<Arc<MemberExpressionNode>> for AnyAstNode {
    fn from(value: Arc<MemberExpressionNode>) -> Self {
        AnyAstNode::MemberExpression(value.clone())
    }
}
impl AstNode for MemberExpressionNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("object_expression"),
            (&self.object_expression).into(),
        );
        result.insert(
            BreadcrumbName::new("member_identifier"),
            (&self.member_identifier).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for MemberExpressionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct IdentifierExpressionNode {
    pub info: NodeInfo,
    pub identifier: Arc<IdentifierNode>,
}
impl From<&IdentifierExpressionNode> for AnyAstNode {
    fn from(value: &IdentifierExpressionNode) -> Self {
        AnyAstNode::IdentifierExpression(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<IdentifierExpressionNode>> for AnyAstNode {
    fn from(value: &Arc<IdentifierExpressionNode>) -> Self {
        AnyAstNode::IdentifierExpression(value.clone())
    }
}
impl From<Arc<IdentifierExpressionNode>> for AnyAstNode {
    fn from(value: Arc<IdentifierExpressionNode>) -> Self {
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
impl From<&StringLiteralExpressionNode> for AnyAstNode {
    fn from(value: &StringLiteralExpressionNode) -> Self {
        AnyAstNode::StringLiteralExpression(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<StringLiteralExpressionNode>> for AnyAstNode {
    fn from(value: &Arc<StringLiteralExpressionNode>) -> Self {
        AnyAstNode::StringLiteralExpression(value.clone())
    }
}
impl From<Arc<StringLiteralExpressionNode>> for AnyAstNode {
    fn from(value: Arc<StringLiteralExpressionNode>) -> Self {
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

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct NumberLiteralExpressionNode {
    pub info: NodeInfo,
    pub value: rust_decimal::Decimal,
}
impl From<&NumberLiteralExpressionNode> for AnyAstNode {
    fn from(value: &NumberLiteralExpressionNode) -> Self {
        AnyAstNode::NumberLiteralExpression(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<NumberLiteralExpressionNode>> for AnyAstNode {
    fn from(value: &Arc<NumberLiteralExpressionNode>) -> Self {
        AnyAstNode::NumberLiteralExpression(value.clone())
    }
}
impl From<Arc<NumberLiteralExpressionNode>> for AnyAstNode {
    fn from(value: Arc<NumberLiteralExpressionNode>) -> Self {
        AnyAstNode::NumberLiteralExpression(value.clone())
    }
}
impl AstNode for NumberLiteralExpressionNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        HashMap::new()
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for NumberLiteralExpressionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct ReturnExpressionNode {
    pub info: NodeInfo,
    pub value: Option<ExpressionNode>,
}
impl From<&ReturnExpressionNode> for AnyAstNode {
    fn from(value: &ReturnExpressionNode) -> Self {
        AnyAstNode::ReturnExpression(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<ReturnExpressionNode>> for AnyAstNode {
    fn from(value: &Arc<ReturnExpressionNode>) -> Self {
        AnyAstNode::ReturnExpression(value.clone())
    }
}
impl From<Arc<ReturnExpressionNode>> for AnyAstNode {
    fn from(value: Arc<ReturnExpressionNode>) -> Self {
        AnyAstNode::ReturnExpression(value.clone())
    }
}
impl AstNode for ReturnExpressionNode {
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
impl HasBreadcrumbs for ReturnExpressionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct BreakExpressionNode {
    pub info: NodeInfo,
    pub with_value: Option<ExpressionNode>,
}
impl From<&BreakExpressionNode> for AnyAstNode {
    fn from(value: &BreakExpressionNode) -> Self {
        AnyAstNode::BreakExpression(Arc::new(value.to_owned()))
    }
}
impl From<&Arc<BreakExpressionNode>> for AnyAstNode {
    fn from(value: &Arc<BreakExpressionNode>) -> Self {
        AnyAstNode::BreakExpression(value.clone())
    }
}
impl From<Arc<BreakExpressionNode>> for AnyAstNode {
    fn from(value: Arc<BreakExpressionNode>) -> Self {
        AnyAstNode::BreakExpression(value.clone())
    }
}
impl AstNode for BreakExpressionNode {
    fn children(&self) -> HashMap<BreadcrumbName, BreadcrumbTreeNode> {
        let mut result = HashMap::new();
        result.insert(
            BreadcrumbName::new("with_value"),
            (&self.with_value).into(),
        );
        result
    }
    fn info(&self) -> &NodeInfo {
        &self.info
    }
}
impl HasBreadcrumbs for BreakExpressionNode {
    fn breadcrumbs(&self) -> &Breadcrumbs {
        &self.info.breadcrumbs
    }
}

