use super::resolving_lang_types::ResolvingLangTypeLink;

#[derive(Debug, Clone)]
pub enum Hint {
    EqualTo(ResolvingLangTypeLink),
}

#[derive(Debug, Clone)]
pub struct TrackedHint {
    hint: Hint,
    inferred_from: Option<Vec<Hint>>,
}
