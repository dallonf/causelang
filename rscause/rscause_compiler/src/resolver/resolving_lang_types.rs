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

include!("../gen/resolving_lang_types.rs");

/// Owns strong references to all types in the graph.
/// Must be in scope and not dropped while working with ResolvingLangTypes.
pub struct ResolvingLangTypesContext {
    pub values_by_source: HashMap<ResolvingLangTypeSource, Rc<ResolvingLangTypeLink>>,
    all_values: Vec<Rc<ResolvingLangTypeLink>>,
}
impl ResolvingLangTypesContext {
    pub fn new() -> Self {
        Self {
            values_by_source: Default::default(),
            all_values: Default::default(),
        }
    }

    pub fn track_type(
        &mut self,
        source: Option<ResolvingLangTypeSource>,
        value: ResolvingLangTypeValue,
    ) -> anyhow::Result<Rc<ResolvingLangTypeLink>> {
        let new_link = ResolvingLangTypeLink {
            source,
            value: RefCell::new(value),
        }
        .pipe(Rc::new);

        self.all_values.push(new_link.clone());

        if let Some(source) = &new_link.source {
            if self.values_by_source.contains_key(source) {
                return Err(anyhow!("Already tracking a linked type for {source:?}"));
            } else {
                self.values_by_source
                    .insert(source.to_owned(), new_link.clone());
            }
        }

        Ok(new_link)
    }
}

#[derive(Debug, Clone)]
pub struct LinkedResolvingLangType(Weak<ResolvingLangTypeLink>);
#[derive(Debug, Clone)]
pub struct ResolvingLangTypeLink {
    pub source: Option<ResolvingLangTypeSource>,
    pub value: RefCell<ResolvingLangTypeValue>,
}
impl LinkedResolvingLangType {
    pub fn import_type(
        ctx: &mut ResolvingLangTypesContext,
        lang_type: FallibleLangType,
    ) -> anyhow::Result<Self> {
        let value = ResolvingLangTypeValue::Known(match lang_type {
            Ok(lang_type) => Ok(ResolvingLangType::import_type(
                ctx,
                lang_type.as_ref().to_owned(),
            )?),
            Err(err) => Err(err),
        });
        let tracked = ctx.track_type(None, value)?;

        Ok(LinkedResolvingLangType(Rc::downgrade(&tracked)))
    }
}
impl TryFrom<LinkedResolvingLangType> for lang_types::FallibleLangType {
    type Error = anyhow::Error;

    fn try_from(value: LinkedResolvingLangType) -> Result<Self, Self::Error> {
        let value = value
            .0
            .upgrade()
            .ok_or(anyhow!("ResolvingLangTypesContext has been dropped"))?;
        let value = value.value.borrow();

        match &*value {
            ResolvingLangTypeValue::Known(Ok(resolving_lang_type)) => {
                Ok(Arc::new(resolving_lang_type.to_owned().try_into()?))
            }
            ResolvingLangTypeValue::Known(Err(lang_error)) => Err(lang_error.clone()),
            ResolvingLangTypeValue::Hints(_) => Err(Arc::new(LangError::NeverResolved)),
        }
        .pipe(Ok)
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, EnumTryAs)]
pub enum ResolvingLangTypeSource {
    Variable(u64),
    Breadcrumb(Breadcrumbs),
}

#[derive(Debug, Clone, EnumTryAs)]
pub enum ResolvingLangTypeValue {
    Known(LangTypeResult<ResolvingLangType>),
    Hints(Vec<()>),
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
}
