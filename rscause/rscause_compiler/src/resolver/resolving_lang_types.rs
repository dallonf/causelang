use crate::{
    lang_types::{self, CanonicalLangTypeCategory, CanonicalLangTypeId, PrimitiveLangType},
    prelude::*,
};
use serde::{Deserialize, Serialize};
use std::{
    hash::{Hash, Hasher},
    rc::{Rc, Weak},
    sync::Arc,
};
use strum::EnumTryAs;

include!("../gen/resolving_lang_types.rs");

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct LinkedResolvingLangType(Rc<ResolvingLangTypeValue>);
#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct ResolvingLangTypeValue {
    // TODO
    // source (id/breadcrumb/unknown [maybe import?])
    // value
    // - rc (weak?)
    // - refcell
    // - one of:
    //   - known
    //   - list of constraints
}
impl From<LinkedResolvingLangType> for lang_types::FallibleLangType {
    fn from(value: LinkedResolvingLangType) -> Self {
        todo!()
    }
}
impl From<lang_types::FallibleLangType> for LinkedResolvingLangType {
    fn from(value: lang_types::FallibleLangType) -> Self {
        todo!()
    }
}

impl OneOfResolvingLangType {
    pub fn new(options: Vec<LinkedResolvingLangType>) -> Self {
        Self { options }
    }

    pub fn new_with_one(option: LinkedResolvingLangType) -> Self {
        Self {
            options: vec![option],
        }
    }

    pub fn simplify(&self) -> OneOfResolvingLangType {
        // TODO
        return self.clone();
    }
}
impl Hash for OneOfResolvingLangType {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.simplify().options.hash(state);
    }
}
