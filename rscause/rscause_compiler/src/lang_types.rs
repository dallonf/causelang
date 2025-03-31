use crate::error_types::LangError;
use anyhow::anyhow;
use serde::{
    de::{self},
    Deserialize, Serialize,
};
use std::sync::Arc;
use std::{
    hash::{Hash, Hasher},
    str::FromStr,
};
use strum::EnumTryAs;

include!("gen/lang_types.rs");

pub type LangTypeResult<T> = Result<T, Arc<LangError>>;

pub type FallibleLangType = LangTypeResult<Arc<LangType>>;

#[derive(Debug, Clone, Copy, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub enum PrimitiveLangType {
    Text,
    Number,
}

impl OneOfLangType {
    pub fn simplify(&self) -> OneOfLangType {
        // TODO
        return self.clone();
    }
}
impl Hash for OneOfLangType {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.simplify().options.hash(state);
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
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

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct CanonicalTypeField {
    pub name: Arc<String>,
    pub value_type: FallibleLangType,
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

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct ObjectCanonicalLangType {
    pub type_id: CanonicalLangTypeId,
    pub fields: Vec<CanonicalTypeField>,
}

impl ObjectCanonicalLangType {
    pub fn new(type_id: CanonicalLangTypeId, fields: Vec<CanonicalTypeField>) -> Self {
        if type_id.category != CanonicalLangTypeCategory::Object {
            panic!("ObjectCanonicalLangType::new called with non-object type_id");
        }
        assert_uniqueness_matches("ObjectCanonicalLangType", &type_id, &fields);
        Self { type_id, fields }
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct SignalCanonicalLangType {
    pub type_id: CanonicalLangTypeId,
    pub fields: Vec<CanonicalTypeField>,
    pub result: FallibleLangType,
}

impl SignalCanonicalLangType {
    pub fn new(
        type_id: CanonicalLangTypeId,
        fields: Vec<CanonicalTypeField>,
        result: FallibleLangType,
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
    pub fn result(&self) -> &FallibleLangType {
        &self.result
    }
}
