use std::collections::hash_map::DefaultHasher;
use std::fmt::{Debug, Display};
use std::hash::{Hash, Hasher};

use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub enum BreadcrumbEntry {
    Index(usize),
    Name(BreadcrumbName),
}

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
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

#[derive(Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct Breadcrumbs {
    pub entries: Vec<BreadcrumbEntry>,
}
impl Breadcrumbs {
    pub fn pop_start(&self) -> Breadcrumbs {
        let new_entries = self.entries[1..].to_vec();
        Self {
            entries: new_entries,
        }
    }
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

impl Debug for Breadcrumbs {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.debug_tuple("Breadcrumbs")
            .field(&self.to_string())
            .finish()
    }
}

pub trait HasBreadcrumbs {
    fn breadcrumbs(&self) -> &Breadcrumbs;
}
