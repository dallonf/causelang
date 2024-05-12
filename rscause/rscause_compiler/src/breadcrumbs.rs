use std::collections::hash_map::DefaultHasher;
use std::collections::HashMap;
use std::fmt::{Debug, Display};
use std::hash::{Hash, Hasher};
use std::string;

use serde::de::{self, Visitor};
use serde::{Deserialize, Serialize};

use crate::ast::BREADCRUMB_NAMES;

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

#[derive(Clone, Eq, PartialEq, Hash, Default)]
pub struct Breadcrumbs {
    pub entries: Vec<BreadcrumbEntry>,
}
impl Breadcrumbs {
    pub fn new() -> Self {
        Self::default()
    }

    pub fn pop_start(&self) -> Breadcrumbs {
        let new_entries = self.entries[1..].to_vec();
        Self {
            entries: new_entries,
        }
    }

    pub fn push(&self, entry: BreadcrumbEntry) -> Breadcrumbs {
        let mut new_entries = self.entries.clone();
        new_entries.push(entry);
        Self {
            entries: new_entries,
        }
    }
}

impl Serialize for Breadcrumbs {
    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: serde::Serializer,
    {
        let breadcrumb_string = self
            .entries
            .iter()
            .map(|entry| match entry {
                BreadcrumbEntry::Index(index) => index.to_string(),
                BreadcrumbEntry::Name(name) => name.name.to_string(),
            })
            .collect::<Vec<_>>()
            .join(".");
        serializer.serialize_str(&breadcrumb_string)
    }
}

impl<'de> Deserialize<'de> for Breadcrumbs {
    fn deserialize<D>(deserializer: D) -> Result<Self, D::Error>
    where
        D: de::Deserializer<'de>,
    {
        let breadcrumb_string = String::deserialize(deserializer)?;
        let string_entries = breadcrumb_string.split('.').collect::<Vec<_>>();
        if string_entries.eq(&vec![""]) {
            return Ok(Self::default());
        }
        let entries: Vec<BreadcrumbEntry> = string_entries
            .into_iter()
            .map(|segment| match segment.parse::<usize>() {
                Ok(index) => Ok(BreadcrumbEntry::Index(index)),
                Err(_) => {
                    let index = BREADCRUMB_NAMES
                        .iter()
                        .position(|it| it == &segment)
                        .ok_or(de::Error::custom(format!(
                            "Unknown breadcrumb name {}",
                            segment
                        )))?;
                    Ok(BreadcrumbEntry::Name(BreadcrumbName::new(
                        BREADCRUMB_NAMES[index],
                    )))
                }
            })
            .collect::<Result<Vec<_>, D::Error>>()?;
        Ok(Self { entries })
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

#[cfg(test)]
mod test {
    use super::*;

    #[test]
    fn serialize_breadcrumbs() {
        let breadcrumbs = Breadcrumbs::new().push(BreadcrumbName::new("value").into());
        let serialized = serde_json::to_string(&breadcrumbs).unwrap();
        let deserialized: Breadcrumbs = serde_json::from_str(&serialized).unwrap();
        assert_eq!(deserialized, breadcrumbs);
    }
}
