use crate::ast::Breadcrumbs;

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct CanonicalLangTypeId {
    pub path: String,
    pub parent_name: Option<String>,
    pub name: Option<String>,
    pub number: u8,
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

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct SignalCanonicalLangType {
    pub id: CanonicalLangTypeId,
    pub name: String,
    pub params: Vec<LangParameter>,
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

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum LangTypeError {
    NotInScope,
    ProxyError { caused_by: ErrorSourcePosition },
    NotCallable,
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

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum ResolvedValueLangType {
    Function(FunctionValueLangType),
    Primitive(PrimitiveLangType),
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
