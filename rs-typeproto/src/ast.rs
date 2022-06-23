use std::fmt::{Debug, Display};

#[derive(Clone, Copy, PartialEq, Eq, Default)]
pub struct DocumentPosition {
    pub line: usize,
    pub col: usize,
}

impl Debug for DocumentPosition {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.debug_tuple("DocumentPosition")
            .field(&format!("{}:{}", self.line, self.col))
            .finish()
    }
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

#[derive(Clone, PartialEq, Eq, Hash)]
pub struct Breadcrumbs(pub Vec<BreadcrumbEntry>);

impl Breadcrumbs {
    pub fn empty() -> Breadcrumbs {
        Breadcrumbs(vec![])
    }

    pub fn append(&self, new_entry: BreadcrumbEntry) -> Self {
        let mut new_vec = self.0.clone();
        new_vec.push(new_entry);
        Breadcrumbs(new_vec)
    }

    pub fn append_name(&self, name: &'static str) -> Self {
        self.append(BreadcrumbEntry::Name(name))
    }

    pub fn append_index(&self, index: usize) -> Self {
        self.append(BreadcrumbEntry::Index(index))
    }

    pub fn up(&self) -> Self {
        let mut new_vec = self.0.clone();
        new_vec.pop();
        Breadcrumbs(new_vec)
    }

    pub fn is_empty(&self) -> bool {
        self.0.len() == 0
    }
}

impl Debug for Breadcrumbs {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.debug_tuple("Breadcrumbs")
            .field(&self.to_string())
            .finish()
    }
}

impl Display for Breadcrumbs {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        let segments: Vec<String> = self
            .0
            .iter()
            .map(|it| match it {
                BreadcrumbEntry::Index(i) => i.to_string(),
                BreadcrumbEntry::Name(name) => name.to_string(),
            })
            .collect();
        write!(f, "{}", segments.join("."))
    }
}

#[derive(Clone, PartialEq, Eq)]
pub struct AstNode<T> {
    pub position: DocumentRange,
    pub breadcrumbs: Breadcrumbs,
    pub node: T,
}

impl<T> AstNode<T> {
    pub fn new(node: T, position: impl Into<DocumentRange>, breadcrumbs: Breadcrumbs) -> Self {
        AstNode {
            position: position.into(),
            breadcrumbs,
            node,
        }
    }

    pub fn map<Other>(self, f: impl Fn(T) -> Other) -> AstNode<Other> {
        AstNode {
            position: self.position,
            breadcrumbs: self.breadcrumbs,
            node: f(self.node),
        }
    }

    pub fn with_node<Other>(&self, other: Other) -> AstNode<Other> {
        AstNode {
            position: self.position,
            breadcrumbs: self.breadcrumbs.clone(),
            node: other,
        }
    }
}

impl<T: Debug> Debug for AstNode<T> {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        struct MiniDocumentRange(DocumentRange);
        impl Debug for MiniDocumentRange {
            fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
                f.debug_struct("DocumentRange")
                    .field(
                        "start",
                        &format!("{}:{}", self.0.start.line, self.0.start.col),
                    )
                    .field("end", &format!("{}:{}", self.0.end.line, self.0.end.col))
                    .finish()
            }
        }
        f.debug_struct("AstNode")
            .field("position", &MiniDocumentRange(self.position))
            .field("breadcrumbs", &self.breadcrumbs.to_string())
            .field("node", &self.node)
            .finish()
    }
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
    Import(ImportDeclarationNode),
    Function(FunctionDeclarationNode),
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct FunctionDeclarationNode {
    pub name: AstNode<Identifier>,
    // TODO: params
    // TODO: return type
    pub body: AstNode<BodyNode>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ImportDeclarationNode {
    pub path: AstNode<ImportPathNode>,
    pub mappings: Vec<AstNode<ImportMappingNode>>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ImportPathNode(pub String);

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ImportMappingNode {
    pub source_name: AstNode<Identifier>,
    pub rename: Option<AstNode<Identifier>>,
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
    ExpressionStatement(ExpressionStatementNode),
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ExpressionStatementNode {
    pub expression: AstNode<ExpressionNode>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum ExpressionNode {
    IdentifierExpression(IdentifierExpressionNode),
    CauseExpression(CauseExpressionNode),
    CallExpression(CallExpressionNode),
    StringLiteralExpression(StringLiteralExpressionNode),
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct IdentifierExpressionNode {
    pub identifier: AstNode<Identifier>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct CauseExpressionNode {
    pub argument: Box<AstNode<ExpressionNode>>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct CallExpressionNode {
    pub callee: Box<AstNode<ExpressionNode>>,
    pub arguments: Vec<AstNode<CallExpressionArgumentNode>>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct CallExpressionArgumentNode {
    pub name: Option<AstNode<Identifier>>,
    pub value: AstNode<ExpressionNode>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct StringLiteralExpressionNode {
    pub text_range: DocumentRange,
    pub text: String,
}
