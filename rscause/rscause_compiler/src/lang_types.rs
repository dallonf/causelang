use crate::error_types::LangError;
use serde::{Deserialize, Serialize};
use std::sync::Arc;

pub type LangTypeResult<T> = Result<T, Arc<LangError>>;

#[derive(Debug, Clone, Copy, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub enum PrimitiveLangType {
    Text,
    Number,
}
