use std::fmt::Display;

use crate::ast::Breadcrumbs;

#[derive(Clone, PartialEq, Eq, Hash)]
pub struct CanonicalLangTypeId {
    pub path: String,
    pub parent_name: Option<String>,
    pub name: Option<String>,
    pub number: u8,
}

impl Display for CanonicalLangTypeId {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        let name = self
            .name
            .as_ref()
            .map(|it| it.as_str())
            .unwrap_or_else(|| "<anonymous>");

        match &self.parent_name {
            Some(parent_name) => write!(f, "{}:{}_{}{}", self.path, parent_name, name, self.number),
            None => write!(f, "{}:{}{}", self.path, name, self.number),
        }
    }
}

impl std::fmt::Debug for CanonicalLangTypeId {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.debug_tuple("CanonicalLangTypeId")
            .field(&self.to_string())
            .finish()
    }
}

#[derive(Debug, Copy, Clone, PartialEq, Eq)]
pub enum PrimitiveLangType {
    String,
    Integer,
    Float,
    Action,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum CanonicalLangType {
    Signal(SignalCanonicalLangType),
}

impl CanonicalLangType {
    pub fn id(&self) -> &CanonicalLangTypeId {
        match self {
            CanonicalLangType::Signal(signal) => &signal.id,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct SignalCanonicalLangType {
    pub id: CanonicalLangTypeId,
    pub name: String,
    pub params: Vec<LangParameter>,
    pub result: Box<ValueLangType>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct LangParameter {
    pub name: String,
    pub value_type: ValueLangType,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum ValueLangType {
    Pending,
    Resolved(ResolvedValueLangType),
    Error(LangTypeError),
}

impl ValueLangType {
    pub fn is_pending(&self) -> bool {
        match self {
            ValueLangType::Pending => true,
            ValueLangType::Resolved(ResolvedValueLangType::Function(function)) => {
                function.return_type.is_pending()
            }
            ValueLangType::Resolved(_) => false,
            ValueLangType::Error(_) => false,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum LangTypeError {
    NotInScope,
    FileNotFound,
    ExportNotFound,
    ProxyError { caused_by: ErrorSourcePosition },
    NotCallable,
    NotCausable,
    ImplementationTodo { description: String },
    NotATypeReference,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum ErrorSourcePosition {
    SameFile {
        path: String,
        breadcrumbs: Breadcrumbs,
    },
    FileImport {
        path: String,
        export_name: String,
    },
}

// TODO: more distinction between values and types?

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum ResolvedValueLangType {
    Function(FunctionValueLangType),
    FunctionType(FunctionValueLangType),
    Primitive(PrimitiveLangType),
    PrimitiveType(PrimitiveLangType),
    Reference(CanonicalLangTypeId),
    Instance(CanonicalLangTypeId),
    Canonical(CanonicalLangType),
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct FunctionValueLangType {
    pub name: Option<String>,
    pub return_type: Box<ValueLangType>,
    // TODO: params
}
