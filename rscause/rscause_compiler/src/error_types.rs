use std::sync::Arc;

use crate::{
    ast::DocumentRange,
    breadcrumbs::Breadcrumbs,
    lang_types::{AnyInferredLangType, LangType, OneOfLangType},
};

include!("gen/error_types.rs");

pub struct SourcePosition {
    path: Arc<String>,
    breadcrumbs: Breadcrumbs,
    position: DocumentRange,
}

pub enum ErrorPosition {
    Source(SourcePosition),
}
