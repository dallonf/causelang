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
    /// This type should become NeverContinues if the linked type is also NeverContinues
    UnreachableIfNeverContinues(ResolvingLangTypeLink),
    // The result of calling the linked function
    CallResult(ResolvingLangTypeLink),
    // The result of causing the linked signal
    CauseResult(ResolvingLangTypeLink),
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
