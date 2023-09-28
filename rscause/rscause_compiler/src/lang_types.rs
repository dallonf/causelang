use std::sync::Arc;

#[derive(Debug, Clone, Eq, PartialEq)]
pub enum InferredType<T> {
    Known(T),
    Error,
}
impl<T> InferredType<T> {
    #[inline]
    pub fn map<U, F: FnOnce(T) -> U>(self, op: F) -> InferredType<U> {
        self.and_then(|it| InferredType::Known(op(it)))
    }
    #[inline]
    pub fn and_then<U, F: FnOnce(T) -> InferredType<U>>(self, op: F) -> InferredType<U> {
        match self {
            InferredType::Known(t) => op(t),
            InferredType::Error => InferredType::Error,
        }
    }
}
impl<T> From<T> for InferredLangType
where
    T: Into<LangType>,
{
    fn from(value: T) -> Self {
        Self::Known(Arc::new(value.into()))
    }
}
impl From<Arc<LangType>> for InferredLangType {
    fn from(value: Arc<LangType>) -> Self {
        Self::Known(value)
    }
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
impl From<FunctionLangType> for LangType {
    fn from(value: FunctionLangType) -> Self {
        Self::Function(value)
    }
}

#[derive(Debug, Clone, Eq, PartialEq)]
pub enum PrimitiveLangType {
    Text,
}
impl From<PrimitiveLangType> for LangType {
    fn from(value: PrimitiveLangType) -> Self {
        Self::Primitive(value)
    }
}
