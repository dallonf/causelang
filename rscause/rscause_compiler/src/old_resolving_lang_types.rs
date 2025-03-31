use crate::{
    error_types::ValueUsedAsConstraintError,
    lang_types::{
        self, CanonicalLangTypeCategory, CanonicalLangTypeId, LangTypeResult, PrimitiveLangType,
    },
    prelude::*,
};
use std::{
    hash::{Hash, Hasher},
    sync::Arc,
};

use serde::{Deserialize, Serialize};
use strum::EnumTryAs;

use crate::error_types::{ConstraintUsedAsValueError, LangError};

include!("gen/old_resolving_lang_types.rs");

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize, EnumTryAs)]
pub enum OldResolvingType<T> {
    Known(T),
    Error(Arc<LangError>),
    InferenceVariable(u64),
}
impl<T> OldResolvingType<T> {
    #[inline]
    pub fn map<U, F: FnOnce(T) -> U>(self, op: F) -> OldResolvingType<U> {
        self.and_then(|it| OldResolvingType::Known(op(it)))
    }
    #[inline]
    pub fn and_then<U, F: FnOnce(T) -> OldResolvingType<U>>(self, op: F) -> OldResolvingType<U> {
        match self {
            OldResolvingType::Known(t) => op(t),
            OldResolvingType::Error(err) => OldResolvingType::Error(err),
            OldResolvingType::InferenceVariable(var) => OldResolvingType::InferenceVariable(var),
        }
    }
    // Will be a NeverResolved error if it's an inference variable
    #[inline]
    pub fn to_result_assuming_inferred(self) -> LangTypeResult<T> {
        match self {
            OldResolvingType::Known(t) => Ok(t),
            OldResolvingType::Error(err) => Err(err),
            OldResolvingType::InferenceVariable(_) => Err(LangError::NeverResolved.into()),
        }
    }
    // Will be a NeverResolved error if it's an inference variable
    #[inline]
    pub fn to_result_assuming_inferred_ref(&self) -> LangTypeResult<&T> {
        match self {
            OldResolvingType::Known(t) => Ok(t),
            OldResolvingType::Error(err) => Err(err.clone()),
            OldResolvingType::InferenceVariable(_) => Err(LangError::NeverResolved.into()),
        }
    }

    #[inline]
    pub fn map_err<F: FnOnce(Arc<LangError>) -> Arc<LangError>>(
        self,
        op: F,
    ) -> OldResolvingType<T> {
        match self {
            OldResolvingType::Known(t) => OldResolvingType::Known(t),
            OldResolvingType::Error(err) => OldResolvingType::Error(op(err)),
            OldResolvingType::InferenceVariable(var) => OldResolvingType::InferenceVariable(var),
        }
    }

    pub fn as_known(&self) -> Option<&T> {
        if let OldResolvingType::Known(known) = self {
            Some(known)
        } else {
            None
        }
    }
}

pub type AnyOldResolvingLangTypeResult = LangTypeResult<Arc<OldResolvingLangType>>;

impl<T> From<LangError> for OldResolvingType<T> {
    fn from(value: LangError) -> Self {
        Self::Error(Arc::new(value))
    }
}
impl<T> From<T> for AnyOldResolvingLangType
where
    T: Into<OldResolvingLangType>,
{
    fn from(value: T) -> Self {
        Self::Known(Arc::new(value.into()))
    }
}
impl From<AnyOldResolvingLangType> for lang_types::FallibleLangType {
    fn from(value: AnyOldResolvingLangType) -> Self {
        match value {
            OldResolvingType::Known(known) => Ok(Arc::new(known.as_ref().to_owned().into())),
            OldResolvingType::Error(lang_error) => Err(lang_error),
            OldResolvingType::InferenceVariable(_) => Err(Arc::new(LangError::NeverResolved)),
        }
    }
}
impl From<Arc<OldResolvingLangType>> for AnyOldResolvingLangType {
    fn from(value: Arc<OldResolvingLangType>) -> Self {
        Self::Known(value)
    }
}

impl From<Result<Arc<OldResolvingLangType>, Arc<LangError>>> for AnyOldResolvingLangType {
    fn from(value: Result<Arc<OldResolvingLangType>, Arc<LangError>>) -> Self {
        match value {
            Ok(value) => Self::Known(value),
            Err(err) => Self::Error(err),
        }
    }
}

pub type AnyOldResolvingLangType = OldResolvingType<Arc<OldResolvingLangType>>;
impl AnyOldResolvingLangType {
    /// Assuming this is a LangType::TypeReference and that
    /// any type inference has already been run, return the inner value.
    pub fn try_get_referenced_type(&self) -> AnyOldResolvingLangTypeResult {
        let self_result = self.clone().to_result_assuming_inferred()?;
        let instance = self_result
            .try_as_type_reference_ref()
            .ok_or(
                LangError::ValueUsedAsConstraint(ValueUsedAsConstraintError {
                    r#type: self.clone(),
                })
                .pipe(Arc::new),
            )?
            .to_owned();

        return instance.to_result_assuming_inferred();
    }
}

pub trait HasInference {
    fn recursive_inferred_types(&self) -> Vec<AnyOldResolvingLangType>;
    // TODO: this is going to be horrifically slow to do for every single
    // found type
    fn fill_variable(&self, id: u64, value: AnyOldResolvingLangType) -> Self;

    fn has_pending(&self) -> bool {
        self.recursive_inferred_types()
            .iter()
            .any(|it| matches!(it, OldResolvingType::InferenceVariable(_)))
    }
}

impl HasInference for AnyOldResolvingLangType {
    fn recursive_inferred_types(&self) -> Vec<AnyOldResolvingLangType> {
        match self {
            OldResolvingType::Known(known) => known.recursive_inferred_types(),
            OldResolvingType::Error(_) => vec![self.clone()],
            OldResolvingType::InferenceVariable(_) => vec![self.clone()],
        }
    }

    fn fill_variable(&self, id: u64, value: AnyOldResolvingLangType) -> Self {
        match self {
            OldResolvingType::Known(known) => {
                OldResolvingType::Known(known.fill_variable(id, value).into())
            }
            OldResolvingType::Error(error) => OldResolvingType::Error(error.clone()),
            &OldResolvingType::InferenceVariable(current_id) => {
                if current_id == id {
                    value
                } else {
                    OldResolvingType::InferenceVariable(current_id)
                }
            }
        }
    }
}

impl OldResolvingLangType {
    /// If this is a TypeReference, extract the value type
    pub fn get_referenced_value_type(&self) -> AnyOldResolvingLangType {
        match self {
            OldResolvingLangType::TypeReference(value_type) => value_type.clone(),
            _ => LangError::ConstraintUsedAsValue(ConstraintUsedAsValueError {
                r#type: self.to_owned(),
            })
            .into(),
        }
    }

    /// Includes special handling for unique types
    pub fn get_canonical_id_for_instance(&self) -> Option<Arc<CanonicalLangTypeId>> {
        match self {
            OldResolvingLangType::Instance(instance_type) => Some(instance_type.type_id.clone()),
            OldResolvingLangType::TypeReference(referenced_type) => {
                let referenced_type = referenced_type.to_result_assuming_inferred_ref().ok()?;
                let instance = referenced_type.try_as_instance_ref()?;
                if instance.type_id.is_unique {
                    Some(instance.type_id.clone())
                } else {
                    None
                }
            }
            _ => None,
        }
    }

    pub fn is_assignable_to(&self, other_type: &OldResolvingLangType) -> bool {
        if self == &OldResolvingLangType::NeverContinues {
            return true;
        }
        if let OldResolvingLangType::OneOf(one_of) = self {
            // special case: self is an unsimplified OneOfLangType
            let assignable_to_simplified = one_of
                .simplify_to_value()
                .try_as_known()
                // if it simplifies still to a OneOf, then the rest of the
                // function is still correct
                .filter(|it| it.try_as_one_of_ref().is_none())
                .map(|it| it.is_assignable_to(other_type))
                .unwrap_or(false);
            if assignable_to_simplified {
                return true;
            }
        }

        match other_type {
            // Type references aren't assignable to other type references
            // at least until generics become a thing.
            OldResolvingLangType::TypeReference(_other_type_reference) => false,

            OldResolvingLangType::Action => {
                self == &OldResolvingLangType::Action
                    // Action is a sort of unique type; a "reference" to it is equivalent
                    // to an Action value
                    || self == &OldResolvingLangType::TypeReference(OldResolvingLangType::Action.into())
            }
            OldResolvingLangType::Instance(other_instance) => {
                let self_canonical_id = self.get_canonical_id_for_instance();
                match self_canonical_id {
                    Some(self_canonical_id) => self_canonical_id == other_instance.type_id,
                    None => false,
                }
            }
            OldResolvingLangType::Function(other_function) => {
                if let OldResolvingLangType::Function(self_function) = self {
                    let params_match = self_function.params.len() == other_function.params.len()
                        && self_function
                            .params
                            .iter()
                            .zip(other_function.params.iter())
                            .all(|(self_param, other_param)| {
                                // names can be different, but types can't be
                                // at least until we work out variance
                                // or named arguments
                                &self_param.value_type == &other_param.value_type
                            });

                    let return_type_matches = {
                        if let (
                            OldResolvingType::Known(self_function_return),
                            OldResolvingType::Known(other_function_return),
                        ) = (&self_function.return_type, &other_function.return_type)
                        {
                            // I have a hunch this isn't sound variance,
                            // but it works for now
                            self_function_return.is_assignable_to(other_function_return)
                        } else {
                            false
                        }
                    };

                    params_match && return_type_matches
                } else {
                    false
                }
            }
            OldResolvingLangType::Primitive(other_primitive) => {
                if let OldResolvingLangType::Primitive(self_primitive) = self {
                    self_primitive == other_primitive
                } else {
                    false
                }
            }
            OldResolvingLangType::Anything => true,
            OldResolvingLangType::AnySignal => {
                if self == &OldResolvingLangType::AnySignal {
                    true
                } else {
                    let self_canonical_id = self.get_canonical_id_for_instance();
                    match self_canonical_id {
                        Some(self_canonical_id) => {
                            self_canonical_id.category == CanonicalLangTypeCategory::Signal
                        }
                        None => false,
                    }
                }
            }
            OldResolvingLangType::OneOf(other_one_of) => other_one_of.is_superset_of(self),

            OldResolvingLangType::NeverContinues => self == &OldResolvingLangType::NeverContinues,

            OldResolvingLangType::StopgapDictionary => {
                self == &OldResolvingLangType::StopgapDictionary
            }
            OldResolvingLangType::StopgapList => self == &OldResolvingLangType::StopgapList,

            OldResolvingLangType::BadValue => self == &OldResolvingLangType::BadValue,
        }
    }
}

impl From<PrimitiveLangType> for OldResolvingLangType {
    fn from(value: PrimitiveLangType) -> Self {
        Self::Primitive(value)
    }
}

impl OneOfOldResolvingLangType {
    pub fn new(options: Vec<AnyOldResolvingLangType>) -> Self {
        Self { options }
    }

    pub fn new_with_one(option: AnyOldResolvingLangType) -> Self {
        Self {
            options: vec![option],
        }
    }

    pub fn is_empty(&self) -> bool {
        self.options.is_empty()
    }

    pub fn is_superset_of(&self, pattern_type: &OldResolvingLangType) -> bool {
        let possible_values = match pattern_type {
            OldResolvingLangType::OneOf(pattern_one_of) => {
                pattern_one_of.simplify().options.clone()
            }
            other => vec![other.clone().into()],
        };

        let mut has_pending_values = false;
        let result = possible_values
            .into_iter()
            .all(|possible_value| match possible_value {
                OldResolvingType::InferenceVariable(_) => {
                    has_pending_values = true;
                    true
                }
                OldResolvingType::Error(_) => false,
                OldResolvingType::Known(possible_value) => self.options.iter().any(|option| {
                    if let OldResolvingType::Known(option) = option {
                        possible_value.is_assignable_to(&option)
                    } else {
                        false
                    }
                }),
            });

        if result && has_pending_values {
            panic!("Value is pending; can't tell if it was truly assignable");
        }

        return result;
    }

    pub fn narrow(&self, pattern_type: &OldResolvingLangType) -> OneOfOldResolvingLangType {
        let options = self.simplify().options;
        let possible_values = match pattern_type {
            OldResolvingLangType::OneOf(pattern_one_of) => {
                pattern_one_of.simplify().options.clone()
            }
            other => vec![other.clone().into()],
        };

        let remaining_options = options
            .iter()
            .cloned()
            .filter(|option| {
                if let OldResolvingType::Known(option) = option {
                    !possible_values.iter().any(|possible_value| {
                        if let OldResolvingType::Known(possible_value) = possible_value {
                            option.is_assignable_to(&possible_value)
                        } else {
                            // don't narrow with error or pending
                            false
                        }
                    })
                } else {
                    // keep error and pending options around
                    true
                }
            })
            .collect();

        return OneOfOldResolvingLangType::new(remaining_options);
    }

    pub fn expand(&self, pattern_type: &AnyOldResolvingLangType) -> OneOfOldResolvingLangType {
        let new_values = vec![self.options.clone(), vec![pattern_type.clone()]].concat();
        return OneOfOldResolvingLangType::new(new_values).simplify();
    }

    pub fn simplify(&self) -> OneOfOldResolvingLangType {
        let all_possible_types = self
            .options
            .iter()
            .flat_map(|it| {
                if let OldResolvingType::Known(known) = it {
                    if let OldResolvingLangType::OneOf(one_of) = known.as_ref() {
                        return one_of.simplify().options.clone();
                    }
                }
                return vec![it.clone()];
            })
            .collect_vec();

        let all_possible_types = if all_possible_types.len() > 1 {
            let mut not_duplicated = vec![];
            for possible_type in &all_possible_types {
                let possible_type = if let OldResolvingType::Known(known) = possible_type {
                    known
                } else {
                    not_duplicated.push(possible_type.clone());
                    continue;
                };

                // first check if the the type is already covered by the options in `not_duplicated`
                let is_already_covered = not_duplicated.iter().any(|existing_type| {
                    OneOfOldResolvingLangType::is_mergeable(
                        &possible_type.clone().into(),
                        existing_type,
                    )
                });
                if !is_already_covered {
                    // make sure none of the existing options in `not_duplicated``
                    // would be made redundant by adding `possible_type`
                    // (i.e. they are more specific than `possible_type`)
                    not_duplicated = not_duplicated
                        .into_iter()
                        .filter(|existing_type| {
                            !OneOfOldResolvingLangType::is_mergeable(
                                existing_type,
                                &possible_type.clone().into(),
                            )
                        })
                        .collect();
                    not_duplicated.push(possible_type.clone().into());
                }
            }
            not_duplicated
        } else {
            all_possible_types
        };

        OneOfOldResolvingLangType::new(all_possible_types)
    }

    pub fn simplify_to_value(&self) -> AnyOldResolvingLangType {
        let simplified = self.simplify();
        if simplified.options.len() == 1 {
            simplified.options[0].clone()
        } else if simplified.options.len() == 0 {
            OldResolvingLangType::NeverContinues.into()
        } else {
            simplified.into()
        }
    }

    fn is_mergeable(
        less_specific: &AnyOldResolvingLangType,
        more_specific: &AnyOldResolvingLangType,
    ) -> bool {
        if less_specific == more_specific {
            return true;
        }

        let more_specific_type = more_specific.as_known();
        let less_specific_value = less_specific.as_known();

        if let (Some(more_specific_type), Some(less_specific_value)) =
            (more_specific_type, less_specific_value)
        {
            return less_specific_value.is_assignable_to(more_specific_type);
        }

        false
    }
}
impl Hash for OneOfOldResolvingLangType {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.simplify().options.hash(state);
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub enum OldResolvingCanonicalLangType {
    Object(ObjectOldResolvingCanonicalLangType),
    Signal(SignalOldResolvingCanonicalLangType),
}
impl OldResolvingCanonicalLangType {
    pub fn type_id(&self) -> CanonicalLangTypeId {
        match self {
            Self::Object(object) => object.type_id.clone(),
            Self::Signal(signal) => signal.type_id.clone(),
        }
    }
    pub fn fields(&self) -> Vec<OldResolvingCanonicalTypeField> {
        match self {
            Self::Object(object) => object.fields.clone(),
            Self::Signal(signal) => signal.fields.clone(),
        }
    }
}
impl HasInference for OldResolvingCanonicalLangType {
    fn recursive_inferred_types(&self) -> Vec<AnyOldResolvingLangType> {
        match self {
            OldResolvingCanonicalLangType::Object(object) => object.recursive_inferred_types(),
            OldResolvingCanonicalLangType::Signal(signal) => signal.recursive_inferred_types(),
        }
    }

    fn fill_variable(&self, id: u64, value: AnyOldResolvingLangType) -> Self {
        match self {
            OldResolvingCanonicalLangType::Object(object) => {
                OldResolvingCanonicalLangType::Object(object.fill_variable(id, value))
            }
            OldResolvingCanonicalLangType::Signal(signal) => {
                OldResolvingCanonicalLangType::Signal(signal.fill_variable(id, value))
            }
        }
    }
}

fn assert_uniqueness_matches(
    struct_name: &str,
    type_id: &CanonicalLangTypeId,
    fields: &[OldResolvingCanonicalTypeField],
) {
    let is_unique = fields.is_empty();
    if type_id.is_unique != is_unique {
        panic!(
            "Tried to create {struct_name} with type_id.is_unique={} but fields are {}",
            is_unique,
            if fields.is_empty() {
                "empty"
            } else {
                "not empty"
            }
        );
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct ObjectOldResolvingCanonicalLangType {
    pub type_id: CanonicalLangTypeId,
    pub fields: Vec<OldResolvingCanonicalTypeField>,
}

impl ObjectOldResolvingCanonicalLangType {
    pub fn new(type_id: CanonicalLangTypeId, fields: Vec<OldResolvingCanonicalTypeField>) -> Self {
        if type_id.category != CanonicalLangTypeCategory::Object {
            panic!("ObjectOldResolvingCanonicalLangType::new called with non-object type_id");
        }
        assert_uniqueness_matches("ObjectOldResolvingCanonicalLangType", &type_id, &fields);
        Self { type_id, fields }
    }

    pub fn type_id(&self) -> &CanonicalLangTypeId {
        &self.type_id
    }

    pub fn fields(&self) -> &[OldResolvingCanonicalTypeField] {
        &self.fields
    }
}
impl HasInference for ObjectOldResolvingCanonicalLangType {
    fn recursive_inferred_types(&self) -> Vec<AnyOldResolvingLangType> {
        return self
            .fields
            .iter()
            .flat_map(|f| f.value_type.recursive_inferred_types())
            .collect();
    }

    fn fill_variable(&self, id: u64, value: AnyOldResolvingLangType) -> Self {
        let fields = self
            .fields
            .iter()
            .map(|it| OldResolvingCanonicalTypeField {
                name: it.name.clone(),
                value_type: it.value_type.fill_variable(id, value.clone()),
            })
            .collect();
        return Self {
            type_id: self.type_id.clone(),
            fields,
        };
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct SignalOldResolvingCanonicalLangType {
    pub type_id: CanonicalLangTypeId,
    pub fields: Vec<OldResolvingCanonicalTypeField>,
    pub result: AnyOldResolvingLangType,
}

impl SignalOldResolvingCanonicalLangType {
    pub fn new(
        type_id: CanonicalLangTypeId,
        fields: Vec<OldResolvingCanonicalTypeField>,
        result: AnyOldResolvingLangType,
    ) -> Self {
        if type_id.category != CanonicalLangTypeCategory::Signal {
            panic!("SignalOldResolvingCanonicalLangType::new called with non-signal type_id");
        }
        assert_uniqueness_matches("SignalOldResolvingCanonicalLangType", &type_id, &fields);
        Self {
            type_id,
            fields,
            result,
        }
    }
    pub fn type_id(&self) -> &CanonicalLangTypeId {
        &self.type_id
    }
    pub fn fields(&self) -> &[OldResolvingCanonicalTypeField] {
        &self.fields
    }
    pub fn result(&self) -> &AnyOldResolvingLangType {
        &self.result
    }
}
impl HasInference for SignalOldResolvingCanonicalLangType {
    fn recursive_inferred_types(&self) -> Vec<AnyOldResolvingLangType> {
        let mut result = vec![];
        result.append(
            &mut self
                .fields
                .iter()
                .flat_map(|f| f.value_type.recursive_inferred_types())
                .collect(),
        );
        result.append(&mut self.result.recursive_inferred_types());
        return result;
    }

    fn fill_variable(&self, id: u64, value: AnyOldResolvingLangType) -> Self {
        let fields = self
            .fields
            .iter()
            .map(|it| OldResolvingCanonicalTypeField {
                name: it.name.clone(),
                value_type: it.value_type.fill_variable(id, value.clone()),
            })
            .collect();
        let result = self.result.fill_variable(id, value);
        return Self {
            type_id: self.type_id.clone(),
            fields,
            result,
        };
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct OldResolvingCanonicalTypeField {
    pub name: Arc<String>,
    pub value_type: AnyOldResolvingLangType,
}

#[cfg(test)]
mod test {
    use super::*;

    mod one_of {
        use super::*;

        #[test]
        fn test_collapse_nevercontinues_first() {
            let one_of = OneOfOldResolvingLangType::new(vec![
                OldResolvingLangType::NeverContinues.into(),
                OldResolvingLangType::Primitive(PrimitiveLangType::Number).into(),
            ]);
            let simplified = one_of.simplify_to_value();
            assert_eq!(
                simplified,
                OldResolvingLangType::Primitive(PrimitiveLangType::Number).into()
            )
        }

        #[test]
        fn test_collapse_nevercontinues_second() {
            let one_of = OneOfOldResolvingLangType::new(vec![
                OldResolvingLangType::Primitive(PrimitiveLangType::Number).into(),
                OldResolvingLangType::NeverContinues.into(),
            ]);
            let simplified = one_of.simplify_to_value();
            assert_eq!(
                simplified,
                OldResolvingLangType::Primitive(PrimitiveLangType::Number).into()
            )
        }

        #[test]
        fn test_single_value_assignable() {
            let one_of = OneOfOldResolvingLangType::new(vec![OldResolvingLangType::Primitive(
                PrimitiveLangType::Number,
            )
            .into()]);
            let primitive = OldResolvingLangType::Primitive(PrimitiveLangType::Number);
            assert!(OldResolvingLangType::OneOf(one_of).is_assignable_to(&primitive));
        }
    }

    mod functions {
        use super::*;

        #[test]
        fn test_function_with_specific_return_type_assignable_to_function_with_anything_return() {
            let function_constraint = FunctionOldResolvingLangType {
                name: None,
                params: vec![],
                return_type: OldResolvingLangType::Anything.into(),
            };
            let actual_function = FunctionOldResolvingLangType {
                name: None,
                params: vec![],
                return_type: OldResolvingLangType::Primitive(PrimitiveLangType::Number).into(),
            };
            assert!(OldResolvingLangType::Function(actual_function)
                .is_assignable_to(&function_constraint.into()));
        }
    }
}
