use std::sync::Arc;

#[derive(Debug, Clone, Eq, PartialEq)]
pub enum InferredType<T> {
    Known(T),
    Error,
}

pub type InferredLangType = InferredType<Arc<LangType>>;

#[derive(Debug, Clone, Eq, PartialEq)]
pub enum LangType {
    Action,
    Function(FunctionLangType),
    Primitive(PrimitiveLangType),
    TypeReference(InferredLangType),
}

#[derive(Debug, Clone, Eq, PartialEq)]
pub struct FunctionLangType {
    pub name: Arc<String>,
    // TODO: pub params
    pub return_type: InferredLangType,
}

#[derive(Debug, Clone, Eq, PartialEq)]
pub enum PrimitiveLangType {
    Text,
}
