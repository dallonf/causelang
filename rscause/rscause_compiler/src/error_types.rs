use std::sync::Arc;

use serde::{Deserialize, Serialize};

use crate::{ast::DocumentRange, breadcrumbs::Breadcrumbs, lang_types};

include!("gen/error_types.rs");

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct SourcePosition {
    pub path: Arc<String>,
    pub breadcrumbs: Breadcrumbs,
    pub position: DocumentRange,
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
