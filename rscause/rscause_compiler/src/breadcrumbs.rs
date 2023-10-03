use std::collections::hash_map::DefaultHasher;
use std::fmt::{Debug, Display};
use std::hash::{Hash, Hasher};

use serde::de::{self, Visitor};
use serde::{Deserialize, Serialize};

use crate::ast::BREADCRUMB_NAMES;

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
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
impl Serialize for BreadcrumbName {
    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: serde::Serializer,
    {
        serializer.serialize_newtype_struct("BreadcrumbName", self.name)
    }
}
impl<'de> Deserialize<'de> for BreadcrumbName {
    fn deserialize<D>(deserializer: D) -> Result<Self, D::Error>
    where
        D: serde::Deserializer<'de>,
    {
        struct BreadcrumbNameVisitor;
        impl<'de> Visitor<'de> for BreadcrumbNameVisitor {
            type Value = &'static str;

            fn expecting(&self, formatter: &mut std::fmt::Formatter) -> std::fmt::Result {
                formatter.write_str("a string matching one of the known breadcrumb names")
            }

            fn visit_newtype_struct<D>(self, deserializer: D) -> Result<Self::Value, D::Error>
            where
                D: serde::Deserializer<'de>,
            {
                deserializer.deserialize_str(BreadcrumbNameVisitor)
            }

            fn visit_str<E>(self, v: &str) -> Result<Self::Value, E>
            where
                E: serde::de::Error,
            {
                let index = BREADCRUMB_NAMES
                    .iter()
                    .position(|it| it == &v)
                    .ok_or(de::Error::custom(format!("Unknown breadcrumb name {}", v)))?;
                Ok(BREADCRUMB_NAMES[index])
            }
        }
        let name =
            deserializer.deserialize_newtype_struct("BreadcrumbName", BreadcrumbNameVisitor)?;
        Ok(BreadcrumbName::new(name))
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

#[cfg(test)]
mod test {
    use super::BreadcrumbName;

    #[test]
    fn serialize_breadcrumb_name() {
        let name = BreadcrumbName::new("value");
        let serialized = serde_lexpr::to_string(&name).unwrap();
        let deserialized: BreadcrumbName = serde_lexpr::from_str(&serialized).unwrap();
        assert_eq!(deserialized, name);
    }
}
