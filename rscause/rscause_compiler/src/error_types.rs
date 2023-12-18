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

impl LangError {
    pub fn compiler_bug(description: impl Into<String>) -> LangError {
        LangError::CompilerBug(CompilerBugError {
            description: description.into(),
        })
    }

    pub fn proxy_error(original: Arc<LangError>, position: ErrorPosition) -> LangError {
        match original.as_ref() {
            LangError::ProxyError(ProxyErrorError {
                actual_error,
                proxy_chain,
            }) => {
                let mut proxy_chain = proxy_chain.clone();
                proxy_chain.push(position);
                LangError::ProxyError(ProxyErrorError {
                    actual_error: actual_error.clone(),
                    proxy_chain,
                })
            }
            _ => LangError::ProxyError(ProxyErrorError {
                actual_error: original.clone(),
                proxy_chain: vec![position],
            }),
        }
    }
}

/// Deprecated, use `LangError::compiler_bug` instead
pub fn compiler_bug_error(description: impl Into<String>) -> LangError {
    LangError::CompilerBug(CompilerBugError {
        description: description.into(),
    })
}
