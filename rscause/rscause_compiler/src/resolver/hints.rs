use std::rc::Rc;

use strum::EnumTryAs;

use crate::breadcrumbs::Breadcrumbs;

use super::resolving_lang_types::ResolvingLangTypeLink;

#[derive(Debug, Clone, EnumTryAs)]
pub enum Hint {
    EqualTo(ResolvingLangTypeLink),
    ReferencedType(ResolvingLangTypeLink),
    TypeReference(ResolvingLangTypeLink),
    OneOf(Rc<Vec<OneOfOptionHint>>),
}

#[derive(Debug, Clone)]
pub struct OneOfOptionHint {
    pub source_breadcrumbs: Breadcrumbs,
    pub value: ResolvingLangTypeLink,
}

#[derive(Debug, Clone)]
pub struct TrackedHint {
    pub hint: Hint,
    pub reason: String,
    pub inferred_from: Option<Vec<TrackedHint>>,
}
impl TrackedHint {
    pub fn new(
        hint: Hint,
        reason: impl Into<String>,
        inferred_from: Option<Vec<TrackedHint>>,
    ) -> Self {
        Self {
            hint,
            reason: reason.into(),
            inferred_from,
        }
    }
}
