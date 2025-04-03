use crate::breadcrumbs::Breadcrumbs;

use super::resolving_lang_types::ResolvingLangTypeLink;

#[derive(Debug, Clone)]
pub enum EdictRule {
    /// The node must be resolve to a TypeReference and not a value type
    MustBeTypeReference,
    /// The node, a ReturnExpression's value, must be assignable to the given function's return type
    ValidReturnForFunction(ResolvingLangTypeLink),
    /// `Action` must be assignable to the given function's return type (the node, a ReturnExpression, is irrelevant here)
    ActionIsValidReturnForFunction(ResolvingLangTypeLink),
}

#[derive(Debug, Clone)]
pub struct Edict {
    pub rule: EdictRule,
    pub breadcrumbs: Breadcrumbs,
    pub reason: String,
}
