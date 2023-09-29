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

#[derive(Debug, Clone, Eq, PartialEq)]
pub enum LangType {
    TypeReference(AnyInferredLangType),
    Action,
    Instance(InstanceLangType),
    Function(FunctionLangType),
    Primitive(PrimitiveLangType),
}

#[derive(Debug, Clone, Eq, PartialEq)]
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

#[derive(Debug, Clone, Eq, PartialEq)]
pub enum PrimitiveLangType {
    Text,
}
impl From<PrimitiveLangType> for LangType {
    fn from(value: PrimitiveLangType) -> Self {
        Self::Primitive(value)
    }
}

#[derive(Debug, Clone, Eq, PartialEq)]
pub struct InstanceLangType {
    pub type_id: Arc<String>,
}
impl From<InstanceLangType> for LangType {
    fn from(value: InstanceLangType) -> Self {
        Self::Instance(value)
    }
}

#[derive(Debug, Clone, Eq, PartialEq)]
pub enum CanonicalLangType {
    Object(ObjectCanonicalLangType),
    Signal(SignalCanonicalLangType),
}
impl CanonicalLangType {
    pub fn type_id(&self) -> Arc<String> {
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

#[derive(Debug, Clone, Eq, PartialEq)]
pub struct ObjectCanonicalLangType {
    pub type_id: Arc<String>,
    pub fields: Vec<CanonicalTypeField>,
}

#[derive(Debug, Clone, Eq, PartialEq)]
pub struct SignalCanonicalLangType {
    pub type_id: Arc<String>,
    pub fields: Vec<CanonicalTypeField>,
    pub result: AnyInferredLangType,
}

#[derive(Debug, Clone, Eq, PartialEq)]
pub struct CanonicalTypeField {
    pub name: Arc<String>,
    pub value_type: AnyInferredLangType,
}
