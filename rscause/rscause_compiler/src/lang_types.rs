use std::sync::Arc;

#[derive(Debug, Clone, Eq, PartialEq)]
pub enum InferredType<T> {
    Known(T),
    Error,
}

#[derive(Debug, Clone, Eq, PartialEq)]
pub enum LangType {
    Action,
    Function(FunctionLangType),
    Primitive(PrimitiveLangType),
}

#[derive(Debug, Clone, Eq, PartialEq)]
pub struct FunctionLangType {
    pub name: Arc<String>,
    // TODO: pub params
    pub return_type: Box<LangType>,
}

#[derive(Debug, Clone, Eq, PartialEq)]
pub enum PrimitiveLangType {
    Text,
}
