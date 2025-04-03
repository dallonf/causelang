use super::resolving_lang_types::ResolvingLangTypeLink;

#[derive(Debug, Clone)]
pub enum Hint {
    EqualTo(ResolvingLangTypeLink),
    ReferencedType(ResolvingLangTypeLink),
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
