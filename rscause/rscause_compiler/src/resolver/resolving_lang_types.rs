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

/// Owns strong references to all types in the graph.
/// Must be in scope and not dropped while working with ResolvingLangTypes.
pub struct ResolvingLangTypesContext {
    pub values_by_source: HashMap<ResolvingLangTypeSource, Rc<LinkedResolvingLangType>>,
    all_values: Vec<Rc<LinkedResolvingLangType>>,
}
impl ResolvingLangTypesContext {
    pub fn new() -> Self {
        Self {
            values_by_source: Default::default(),
            all_values: Default::default(),
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

    fn import(
        &mut self,
        value: LangTypeResult<ResolvingLangType>,
    ) -> anyhow::Result<Rc<LinkedResolvingLangType>> {
        let new_link = LinkedResolvingLangType::Imported(value).pipe(Rc::new);
        Ok(new_link)
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
        let tracked = ctx.import(value)?;

        Ok(ResolvingLangTypeLink(Rc::downgrade(&tracked)))
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

        match value.get_snapshot_value() {
            Some(Ok(resolving_lang_type)) => {
                Ok(Arc::new(resolving_lang_type.to_owned().try_into()?))
            }
            Some(Err(lang_error)) => Err(lang_error.clone()),
            None => Err(Arc::new(LangError::NeverResolved)),
        }
        .pipe(Ok)
    }
}
#[derive(Debug, Clone)]
pub enum LinkedResolvingLangType {
    Variable(LinkedResolvingLangTypeVariable),
    Imported(LangTypeResult<ResolvingLangType>),
}
impl LinkedResolvingLangType {
    fn get_snapshot_value(&self) -> Option<LangTypeResult<ResolvingLangType>> {
        match self {
            LinkedResolvingLangType::Variable(variable) => match &*variable.value.borrow() {
                ResolvingLangTypeValue::Known(value) => Some(value.to_owned()),
                ResolvingLangTypeValue::Hints(_) => None,
            },
            LinkedResolvingLangType::Imported(it) => Some(it.to_owned()),
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
    Variable(u64),
    Breadcrumb(Breadcrumbs),
}

#[derive(Debug, Clone, EnumTryAs)]
pub enum ResolvingLangTypeValue {
    Known(LangTypeResult<ResolvingLangType>),
    Hints(Vec<TrackedHint>),
}
impl ResolvingLangTypeValue {
    pub fn from_type(lang_type: impl Into<ResolvingLangType>) -> Self {
        Self::Known(Ok(lang_type.into()))
    }
    pub fn from_error(err: impl Into<Arc<LangError>>) -> Self {
        Self::Known(Err(err.into()))
    }
    pub fn from_link(link: impl Into<ResolvingLangTypeLink>, reason: impl Into<String>) -> Self {
        Self::Hints(vec![TrackedHint {
            hint: super::hints::Hint::EqualTo(link.into()),
            reason: reason.into(),
            inferred_from: None,
        }])
    }
}
impl<T> From<T> for ResolvingLangTypeValue
where
    T: Into<LangTypeResult<ResolvingLangType>>,
{
    fn from(value: T) -> Self {
        Self::Known(value.into())
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
