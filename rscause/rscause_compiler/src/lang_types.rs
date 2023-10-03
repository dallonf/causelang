use std::sync::Arc;

use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
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
    #[inline]
    pub fn to_result(self) -> Result<T, ()> {
        match self {
            InferredType::Known(t) => Ok(t),
            InferredType::Error => Err(()),
        }
    }
}
impl<T> From<T> for AnyInferredLangType
where
    T: Into<LangType>,
{
    fn from(value: T) -> Self {
        Self::Known(Arc::new(value.into()))
    }
}
impl From<Arc<LangType>> for AnyInferredLangType {
    fn from(value: Arc<LangType>) -> Self {
        Self::Known(value)
    }
}

pub type AnyInferredLangType = InferredType<Arc<LangType>>;

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub enum LangType {
    TypeReference(AnyInferredLangType),
    Action,
    Instance(InstanceLangType),
    Function(FunctionLangType),
    Primitive(PrimitiveLangType),
    Anything,
}

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub struct FunctionLangType {
    pub name: Arc<String>,
    // TODO: pub params
    pub return_type: AnyInferredLangType,
}
impl From<FunctionLangType> for LangType {
    fn from(value: FunctionLangType) -> Self {
        Self::Function(value)
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub enum PrimitiveLangType {
    Text,
}
impl From<PrimitiveLangType> for LangType {
    fn from(value: PrimitiveLangType) -> Self {
        Self::Primitive(value)
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub struct InstanceLangType {
    pub type_id: Arc<CanonicalLangTypeId>,
}
impl From<InstanceLangType> for LangType {
    fn from(value: InstanceLangType) -> Self {
        Self::Instance(value)
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct CanonicalLangTypeId {
    pub path: Arc<String>,
    pub parent_name: Option<Arc<String>>,
    pub name: Option<Arc<String>>,
    pub number: u32,
}

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub enum CanonicalLangType {
    Object(ObjectCanonicalLangType),
    Signal(SignalCanonicalLangType),
}
impl CanonicalLangType {
    pub fn type_id(&self) -> CanonicalLangTypeId {
        match self {
            Self::Object(object) => object.type_id.clone(),
            Self::Signal(signal) => signal.type_id.clone(),
        }
    }
    pub fn fields(&self) -> Vec<CanonicalTypeField> {
        match self {
            Self::Object(object) => object.fields.clone(),
            Self::Signal(signal) => signal.fields.clone(),
        }
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub struct ObjectCanonicalLangType {
    pub type_id: CanonicalLangTypeId,
    pub fields: Vec<CanonicalTypeField>,
}

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub struct SignalCanonicalLangType {
    pub type_id: CanonicalLangTypeId,
    pub fields: Vec<CanonicalTypeField>,
    pub result: AnyInferredLangType,
}

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub struct CanonicalTypeField {
    pub name: Arc<String>,
    pub value_type: AnyInferredLangType,
}
