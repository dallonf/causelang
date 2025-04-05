use crate::{
    breadcrumbs::Breadcrumbs,
    error_types::LangError,
    lang_types::{
        self, CanonicalLangTypeCategory, CanonicalLangTypeId, FallibleLangType, LangTypeResult,
        PrimitiveLangType,
    },
    prelude::*,
};
use anyhow::anyhow;
use std::{
    cell::RefCell,
    collections::HashMap,
    hash::Hash,
    rc::{Rc, Weak},
    sync::Arc,
};
use strum::EnumTryAs;

use super::hints::TrackedHint;

include!("../gen/resolving_lang_types.rs");

// hierarchy, in order:
// ResolvingLangTypeLink - used to represent a connection in the type graph. Weak reference
// LinkedResolvingLangType - owned version of ResolvingLangTypeLink
// LinkedResolvingLangTypeVariable - variant representing a variable whose value can change during resolution, and its source
// ResolvingLangTypeValue - the actual value that can change
// LangTypeResult<ResolvingLangType> - A known type value

/// Owns strong references to all types in the graph.
/// Must be in scope and not dropped while working with ResolvingLangTypes.
pub struct ResolvingLangTypesContext {
    values_by_source: HashMap<ResolvingLangTypeSource, Rc<LinkedResolvingLangType>>,
    all_values: Vec<Rc<LinkedResolvingLangType>>,
    next_source_id: u64,
}
impl ResolvingLangTypesContext {
    pub fn new() -> Self {
        Self {
            values_by_source: Default::default(),
            all_values: Default::default(),
            next_source_id: 0,
        }
    }

    pub fn get_variable(
        &mut self,
        source: &ResolvingLangTypeSource,
    ) -> Option<Rc<LinkedResolvingLangType>> {
        self.values_by_source.get(source).cloned()
    }

    /// Fails if already tracking a linked type for the same source
    pub fn add_variable(
        &mut self,
        source: ResolvingLangTypeSource,
        value: ResolvingLangTypeValue,
    ) -> anyhow::Result<Rc<LinkedResolvingLangType>> {
        let new_link = LinkedResolvingLangType::Variable(LinkedResolvingLangTypeVariable {
            source: source.clone(),
            value: RefCell::new(value),
        })
        .pipe(Rc::new);

        self.all_values.push(new_link.clone());
        if self.values_by_source.contains_key(&source) {
            return Err(anyhow!("Already tracking a linked type for {source:?}"));
        } else {
            self.values_by_source
                .insert(source.to_owned(), new_link.clone());
        }

        Ok(new_link)
    }

    pub fn link_lang_type(
        &mut self,
        lang_type: LangTypeResult<ResolvingLangType>,
    ) -> Rc<LinkedResolvingLangType> {
        let new_link = LinkedResolvingLangType::Constant(lang_type).pipe(Rc::new);
        new_link
    }

    pub fn create_id_variable(
        &mut self,
        hints: Vec<TrackedHint>,
    ) -> anyhow::Result<(u64, Rc<LinkedResolvingLangType>)> {
        let id = self.next_source_id;
        self.next_source_id += 1;
        let new_link = self.add_variable(
            ResolvingLangTypeSource::Id(id),
            ResolvingLangTypeValue::Hints(hints),
        )?;
        Ok((id, new_link))
    }
}

#[derive(Debug, Clone)]
pub struct ResolvingLangTypeLink(Weak<LinkedResolvingLangType>);
impl ResolvingLangTypeLink {
    pub fn import_type(
        ctx: &mut ResolvingLangTypesContext,
        lang_type: FallibleLangType,
    ) -> anyhow::Result<Self> {
        let value = match lang_type {
            Ok(lang_type) => Ok(ResolvingLangType::import_type(
                ctx,
                lang_type.as_ref().to_owned(),
            )?),
            Err(err) => Err(err),
        };
        let tracked = ctx.link_lang_type(value);

        Ok(ResolvingLangTypeLink(Rc::downgrade(&tracked)))
    }

    fn get_snapshot_value(&self) -> anyhow::Result<Option<LangTypeResult<ResolvingLangType>>> {
        let linked = self
            .0
            .upgrade()
            .ok_or(anyhow!("ResolvingLangTypesContext has been dropped",))?;
        linked.get_snapshot_value()
    }
}
impl From<Rc<LinkedResolvingLangType>> for ResolvingLangTypeLink {
    fn from(value: Rc<LinkedResolvingLangType>) -> Self {
        Self(Rc::downgrade(&value))
    }
}
impl TryFrom<ResolvingLangTypeLink> for lang_types::FallibleLangType {
    type Error = anyhow::Error;

    fn try_from(value: ResolvingLangTypeLink) -> Result<Self, Self::Error> {
        let value = value
            .0
            .upgrade()
            .ok_or(anyhow!("ResolvingLangTypesContext has been dropped"))?;

        match value.get_snapshot_value()? {
            Some(Ok(resolving_lang_type)) => {
                Ok(Arc::new(resolving_lang_type.to_owned().try_into()?))
            }
            Some(Err(lang_error)) => Err(lang_error.clone()),
            None => Err(Arc::new(LangError::NeverResolved)),
        }
        .pipe(Ok)
    }
}
#[derive(Debug, Clone, EnumTryAs)]
pub enum LinkedResolvingLangType {
    Variable(LinkedResolvingLangTypeVariable),
    Constant(LangTypeResult<ResolvingLangType>),
}
impl LinkedResolvingLangType {
    fn get_snapshot_value(&self) -> anyhow::Result<Option<LangTypeResult<ResolvingLangType>>> {
        match self {
            LinkedResolvingLangType::Variable(variable) => match &*variable.value.borrow() {
                ResolvingLangTypeValue::Known(value) => value.get_snapshot_value(),
                ResolvingLangTypeValue::Hints(_) => Ok(None),
            },
            LinkedResolvingLangType::Constant(it) => Ok(Some(it.to_owned())),
        }
    }
}
#[derive(Debug, Clone)]
pub struct LinkedResolvingLangTypeVariable {
    pub source: ResolvingLangTypeSource,
    pub value: RefCell<ResolvingLangTypeValue>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, EnumTryAs)]
pub enum ResolvingLangTypeSource {
    Id(u64),
    Breadcrumb(Breadcrumbs),
}

#[derive(Debug, Clone, EnumTryAs)]
pub enum ResolvingLangTypeValue {
    Known(ResolvingLangTypeLink),
    Hints(Vec<TrackedHint>),
}
impl ResolvingLangTypeValue {
    pub fn from_type(
        lang_type: impl Into<ResolvingLangType>,
        ctx: &mut ResolvingLangTypesContext,
    ) -> Self {
        let constant_link = ctx.link_lang_type(Ok(lang_type.into()));
        Self::Known(constant_link.into())
    }
    pub fn from_error(err: impl Into<Arc<LangError>>, ctx: &mut ResolvingLangTypesContext) -> Self {
        let constant_link = ctx.link_lang_type(Err(err.into()));
        Self::Known(constant_link.into())
    }
    pub fn from_link(link: impl Into<ResolvingLangTypeLink>) -> Self {
        Self::Known(link.into())
    }
}

impl From<ResolvingLangType> for LangTypeResult<ResolvingLangType> {
    fn from(value: ResolvingLangType) -> Self {
        Ok(value)
    }
}

impl OneOfResolvingLangType {
    pub fn new(options: Vec<ResolvingLangTypeLink>) -> Self {
        Self { options }
    }

    pub fn new_with_one(option: ResolvingLangTypeLink) -> Self {
        Self {
            options: vec![option],
        }
    }
}

impl ResolvingCanonicalLangType {
    pub fn import(
        ctx: &mut ResolvingLangTypesContext,
        value: lang_types::CanonicalLangType,
    ) -> anyhow::Result<Self> {
        match value {
            lang_types::CanonicalLangType::Object(it) => todo!(),
            lang_types::CanonicalLangType::Signal(it) => todo!(),
        }
    }
}
