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
    Number,
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
    pub category: CanonicalLangTypeCategory,
    pub is_unique: bool,
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub enum CanonicalLangTypeCategory {
    Object,
    Signal,
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

fn assert_uniqueness_matches(
    struct_name: &str,
    type_id: &CanonicalLangTypeId,
    fields: &[CanonicalTypeField],
) {
    let is_unique = fields.is_empty();
    if type_id.is_unique != is_unique {
        panic!(
            "Tried to create {struct_name} with type_id.is_unique={} but fields are {}",
            is_unique,
            if fields.is_empty() {
                "empty"
            } else {
                "not empty"
            }
        );
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub struct ObjectCanonicalLangType {
    type_id: CanonicalLangTypeId,
    fields: Vec<CanonicalTypeField>,
}

impl ObjectCanonicalLangType {
    pub fn new(type_id: CanonicalLangTypeId, fields: Vec<CanonicalTypeField>) -> Self {
        if type_id.category != CanonicalLangTypeCategory::Object {
            panic!("ObjectCanonicalLangType::new called with non-object type_id");
        }
        assert_uniqueness_matches("ObjectCanonicalLangType", &type_id, &fields);
        Self { type_id, fields }
    }

    pub fn type_id(&self) -> &CanonicalLangTypeId {
        &self.type_id
    }

    pub fn fields(&self) -> &[CanonicalTypeField] {
        &self.fields
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub struct SignalCanonicalLangType {
    type_id: CanonicalLangTypeId,
    fields: Vec<CanonicalTypeField>,
    result: AnyInferredLangType,
}

impl SignalCanonicalLangType {
    pub fn new(
        type_id: CanonicalLangTypeId,
        fields: Vec<CanonicalTypeField>,
        result: AnyInferredLangType,
    ) -> Self {
        if type_id.category != CanonicalLangTypeCategory::Signal {
            panic!("SignalCanonicalLangType::new called with non-signal type_id");
        }
        assert_uniqueness_matches("SignalCanonicalLangType", &type_id, &fields);
        Self {
            type_id,
            fields,
            result,
        }
    }
    pub fn type_id(&self) -> &CanonicalLangTypeId {
        &self.type_id
    }
    pub fn fields(&self) -> &[CanonicalTypeField] {
        &self.fields
    }
    pub fn result(&self) -> &AnyInferredLangType {
        &self.result
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub struct CanonicalTypeField {
    pub name: Arc<String>,
    pub value_type: AnyInferredLangType,
}
