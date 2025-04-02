use crate::breadcrumbs::Breadcrumbs;

#[derive(Debug, Clone)]
pub enum EdictRule {
    MustBeTypeReference,
}

#[derive(Debug, Clone)]
pub struct Edict {
    pub rule: EdictRule,
    pub breadcrumbs: Breadcrumbs,
    pub reason: String,
}
