use std::sync::Arc;

use serde::{Deserialize, Serialize};

use crate::{
    ast::DocumentRange,
    breadcrumbs::Breadcrumbs,
    lang_types::{AnyInferredLangType, LangType, OneOfLangType},
};

include!("gen/error_types.rs");

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct SourcePosition {
    path: Arc<String>,
    breadcrumbs: Breadcrumbs,
    position: DocumentRange,
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub enum ErrorPosition {
    Source(SourcePosition),
}

pub fn compiler_error(description: impl Into<String>) -> LangError {
    LangError::CompilerBug(CompilerBugError {
        description: description.into(),
    })
}
