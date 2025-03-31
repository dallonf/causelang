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
