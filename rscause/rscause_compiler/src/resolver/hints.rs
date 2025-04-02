use super::resolving_lang_types::ResolvingLangTypeLink;

#[derive(Debug, Clone)]
pub enum Hint {
    EqualTo(ResolvingLangTypeLink),
}

#[derive(Debug, Clone)]
pub struct TrackedHint {
    pub hint: Hint,
    pub reason: String,
    pub inferred_from: Option<Vec<TrackedHint>>,
}
