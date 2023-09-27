use std::collections::hash_map::DefaultHasher;
use std::fmt::Display;
use std::hash::{Hash, Hasher};

#[derive(Debug, Clone, Eq, PartialEq, Hash)]
pub enum BreadcrumbEntry {
    Index(usize),
    Name(BreadcrumbName),
}

#[derive(Debug, Clone, Eq, PartialEq)]
pub struct BreadcrumbName {
    pub name: &'static str,
    precomputed_hash: u64,
}
impl BreadcrumbName {
    pub fn new(name: &'static str) -> Self {
        let mut hasher = DefaultHasher::new();
        name.hash(&mut hasher);
        Self {
            name,
            precomputed_hash: hasher.finish(),
        }
    }
}
impl Hash for BreadcrumbName {
    fn hash<H: std::hash::Hasher>(&self, state: &mut H) {
        self.precomputed_hash.hash(state);
    }
}
impl From<BreadcrumbName> for BreadcrumbEntry {
    fn from(name: BreadcrumbName) -> Self {
        Self::Name(name)
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Hash)]
pub struct Breadcrumbs {
    pub entries: Vec<BreadcrumbEntry>,
}

impl Display for Breadcrumbs {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        let segments: Vec<String> = self
            .entries
            .iter()
            .map(|entry| match entry {
                BreadcrumbEntry::Index(index) => index.to_string(),
                BreadcrumbEntry::Name(name) => name.name.to_string(),
            })
            .collect();
        write!(f, "{}", segments.join("."))
    }
}

pub trait HasBreadcrumbs {
    fn breadcrumbs(&self) -> &Breadcrumbs;
}
