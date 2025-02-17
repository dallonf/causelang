use crate::{error_types::ValueUsedAsConstraintError, prelude::*};
use std::{
    hash::{Hash, Hasher},
    str::FromStr,
    sync::Arc,
};

use anyhow::anyhow;
use serde::{
    de::{self},
    Deserialize, Serialize,
};
use strum::EnumTryAs;

use crate::error_types::{ConstraintUsedAsValueError, LangError};

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize, EnumTryAs)]
pub enum InferredType<T> {
    Known(T),
    Error(Arc<LangError>),
    InferenceVariable(u64),
}
impl<T> InferredType<T> {
    #[inline]
    pub fn map<U, F: FnOnce(T) -> U>(self, op: F) -> InferredType<U> {
        self.and_then(|it| InferredType::Known(op(it)))
    }
    #[inline]
    pub fn and_then<U, F: FnOnce(T) -> InferredType<U>>(self, op: F) -> InferredType<U> {
        match self {
            InferredType::Known(t) => op(t),
            InferredType::Error(err) => InferredType::Error(err),
            InferredType::InferenceVariable(var) => InferredType::InferenceVariable(var),
        }
    }
    // Will be a NeverResolved error if it's an inference variable
    #[inline]
    pub fn to_result_assuming_inferred(self) -> LangTypeResult<T> {
        match self {
            InferredType::Known(t) => Ok(t),
            InferredType::Error(err) => Err(err),
            InferredType::InferenceVariable(_) => Err(LangError::NeverResolved.into()),
        }
    }
    // Will be a NeverResolved error if it's an inference variable
    #[inline]
    pub fn to_result_assuming_inferred_ref(&self) -> LangTypeResult<&T> {
        match self {
            InferredType::Known(t) => Ok(t),
            InferredType::Error(err) => Err(err.clone()),
            InferredType::InferenceVariable(_) => Err(LangError::NeverResolved.into()),
        }
    }

    #[inline]
    pub fn map_err<F: FnOnce(Arc<LangError>) -> Arc<LangError>>(self, op: F) -> InferredType<T> {
        match self {
            InferredType::Known(t) => InferredType::Known(t),
            InferredType::Error(err) => InferredType::Error(op(err)),
            InferredType::InferenceVariable(var) => InferredType::InferenceVariable(var),
        }
    }

    pub fn as_known(&self) -> Option<&T> {
        if let InferredType::Known(known) = self {
            Some(known)
        } else {
            None
        }
    }
}

pub type LangTypeResult<T> = Result<T, Arc<LangError>>;
pub type AnyLangTypeResult = LangTypeResult<Arc<LangType>>;

impl<T> From<LangError> for InferredType<T> {
    fn from(value: LangError) -> Self {
        Self::Error(Arc::new(value))
    }
}
impl<T> From<T> for AnyInferredLangType
where
    T: Into<LangType>,
{
    fn from(value: T) -> Self {
        Self::Known(Arc::new(value.into()))
    }
}
impl From<Arc<LangType>> for AnyInferredLangType {
    fn from(value: Arc<LangType>) -> Self {
        Self::Known(value)
    }
}

impl From<Result<Arc<LangType>, Arc<LangError>>> for AnyInferredLangType {
    fn from(value: Result<Arc<LangType>, Arc<LangError>>) -> Self {
        match value {
            Ok(value) => Self::Known(value),
            Err(err) => Self::Error(err),
        }
    }
}

pub type AnyInferredLangType = InferredType<Arc<LangType>>;
impl AnyInferredLangType {
    /// Assuming this is a LangType::TypeReference and that
    /// any type inference has already been run, return the inner value.
    pub fn try_get_referenced_type(&self) -> AnyLangTypeResult {
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

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize, EnumTryAs)]
pub enum LangType {
    TypeReference(AnyInferredLangType),
    Action,
    Instance(InstanceLangType),
    Function(FunctionLangType),
    Primitive(PrimitiveLangType),
    Anything,
    AnySignal,
    OneOf(OneOfLangType),
    NeverContinues,
    StopgapDictionary,
    StopgapList,
    // BadValue should only exist at runtime
    BadValue,
}

pub trait HasInference {
    fn recursive_inferred_types(&self) -> Vec<AnyInferredLangType>;
    // TODO: this is going to be horrifically slow to do for every single
    // found type
    fn fill_variable(&self, id: u64, value: AnyInferredLangType) -> Self;

    fn has_pending(&self) -> bool {
        self.recursive_inferred_types()
            .iter()
            .any(|it| matches!(it, InferredType::InferenceVariable(_)))
    }
}

impl HasInference for AnyInferredLangType {
    fn recursive_inferred_types(&self) -> Vec<AnyInferredLangType> {
        match self {
            InferredType::Known(known) => known.recursive_inferred_types(),
            InferredType::Error(_) => vec![self.clone()],
            InferredType::InferenceVariable(_) => vec![self.clone()],
        }
    }

    fn fill_variable(&self, id: u64, value: AnyInferredLangType) -> Self {
        match self {
            InferredType::Known(known) => {
                InferredType::Known(known.fill_variable(id, value).into())
            }
            InferredType::Error(error) => InferredType::Error(error.clone()),
            &InferredType::InferenceVariable(current_id) => {
                if current_id == id {
                    value
                } else {
                    InferredType::InferenceVariable(current_id)
                }
            }
        }
    }
}

impl LangType {
    /// If this is a TypeReference, extract the value type
    pub fn get_referenced_value_type(&self) -> AnyInferredLangType {
        match self {
            LangType::TypeReference(value_type) => value_type.clone(),
            _ => LangError::ConstraintUsedAsValue(ConstraintUsedAsValueError {
                r#type: self.to_owned(),
            })
            .into(),
        }
    }

    /// Includes special handling for unique types
    pub fn get_canonical_id_for_instance(&self) -> Option<Arc<CanonicalLangTypeId>> {
        match self {
            LangType::Instance(instance_type) => Some(instance_type.type_id.clone()),
            LangType::TypeReference(referenced_type) => {
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

    pub fn is_assignable_to(&self, other_type: &LangType) -> bool {
        if self == &LangType::NeverContinues {
            return true;
        }
        if let LangType::OneOf(one_of) = self {
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
            LangType::TypeReference(_other_type_reference) => false,

            LangType::Action => {
                self == &LangType::Action
                    // Action is a sort of unique type; a "reference" to it is equivalent
                    // to an Action value
                    || self == &LangType::TypeReference(LangType::Action.into())
            }
            LangType::Instance(other_instance) => {
                let self_canonical_id = self.get_canonical_id_for_instance();
                match self_canonical_id {
                    Some(self_canonical_id) => self_canonical_id == other_instance.type_id,
                    None => false,
                }
            }
            LangType::Function(other_function) => {
                if let LangType::Function(self_function) = self {
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
                            InferredType::Known(self_function_return),
                            InferredType::Known(other_function_return),
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
            LangType::Primitive(other_primitive) => {
                if let LangType::Primitive(self_primitive) = self {
                    self_primitive == other_primitive
                } else {
                    false
                }
            }
            LangType::Anything => true,
            LangType::AnySignal => {
                if self == &LangType::AnySignal {
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
            LangType::OneOf(other_one_of) => other_one_of.is_superset_of(self),

            LangType::NeverContinues => self == &LangType::NeverContinues,

            LangType::StopgapDictionary => self == &LangType::StopgapDictionary,
            LangType::StopgapList => self == &LangType::StopgapList,

            LangType::BadValue => self == &LangType::BadValue,
        }
    }
}

impl HasInference for LangType {
    fn recursive_inferred_types(&self) -> Vec<AnyInferredLangType> {
        match self {
            LangType::TypeReference(inferred_type) => inferred_type.recursive_inferred_types(),
            LangType::Action => vec![],
            LangType::Instance(_) => vec![],
            LangType::Function(function_lang_type) => function_lang_type.recursive_inferred_types(),
            LangType::Primitive(_) => vec![],
            LangType::Anything => vec![],
            LangType::AnySignal => vec![],
            LangType::OneOf(one_of_lang_type) => one_of_lang_type.recursive_inferred_types(),
            LangType::NeverContinues => vec![],
            LangType::StopgapDictionary => vec![],
            LangType::StopgapList => vec![],
            LangType::BadValue => vec![],
        }
    }

    fn fill_variable(&self, id: u64, value: AnyInferredLangType) -> Self {
        match self {
            LangType::TypeReference(inferred_type) => {
                LangType::TypeReference(inferred_type.fill_variable(id, value))
            }
            LangType::Action => LangType::Action,
            LangType::Instance(instance_lang_type) => {
                LangType::Instance(instance_lang_type.clone())
            }
            LangType::Function(function_lang_type) => {
                LangType::Function(function_lang_type.fill_variable(id, value))
            }
            LangType::Primitive(primitive_lang_type) => LangType::Primitive(*primitive_lang_type),
            LangType::Anything => LangType::Anything,
            LangType::AnySignal => LangType::AnySignal,
            LangType::OneOf(one_of_lang_type) => {
                LangType::OneOf(one_of_lang_type.fill_variable(id, value))
            }
            LangType::NeverContinues => LangType::NeverContinues,
            LangType::StopgapDictionary => LangType::StopgapDictionary,
            LangType::StopgapList => LangType::StopgapList,
            LangType::BadValue => LangType::BadValue,
        }
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct FunctionLangType {
    pub name: Option<Arc<String>>,
    pub params: Vec<LangParameter>,
    pub return_type: AnyInferredLangType,
}
impl From<FunctionLangType> for LangType {
    fn from(value: FunctionLangType) -> Self {
        Self::Function(value)
    }
}
impl HasInference for FunctionLangType {
    fn recursive_inferred_types(&self) -> Vec<AnyInferredLangType> {
        let mut result = vec![];
        let mut param_results = self
            .params
            .iter()
            .flat_map(|p| p.value_type.recursive_inferred_types())
            .collect();
        result.append(&mut param_results);
        result.append(&mut self.return_type.recursive_inferred_types());
        return result;
    }

    fn fill_variable(&self, id: u64, value: AnyInferredLangType) -> Self {
        let params = self
            .params
            .iter()
            .map(|it| LangParameter {
                name: it.name.clone(),
                value_type: it.value_type.fill_variable(id, value.clone()),
            })
            .collect_vec();
        let return_type = self.return_type.fill_variable(id, value.clone());
        return Self {
            name: self.name.clone(),
            params,
            return_type,
        };
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct LangParameter {
    pub name: Arc<String>,
    pub value_type: AnyInferredLangType,
}

#[derive(Debug, Clone, Copy, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub enum PrimitiveLangType {
    Text,
    Number,
}
impl From<PrimitiveLangType> for LangType {
    fn from(value: PrimitiveLangType) -> Self {
        Self::Primitive(value)
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct InstanceLangType {
    pub type_id: Arc<CanonicalLangTypeId>,
}
impl From<InstanceLangType> for LangType {
    fn from(value: InstanceLangType) -> Self {
        Self::Instance(value)
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub struct OneOfLangType {
    pub options: Vec<AnyInferredLangType>,
}
impl OneOfLangType {
    pub fn new(options: Vec<AnyInferredLangType>) -> Self {
        Self { options }
    }

    pub fn new_with_one(option: AnyInferredLangType) -> Self {
        Self {
            options: vec![option],
        }
    }

    pub fn is_empty(&self) -> bool {
        self.options.is_empty()
    }

    pub fn is_superset_of(&self, pattern_type: &LangType) -> bool {
        let possible_values = match pattern_type {
            LangType::OneOf(pattern_one_of) => pattern_one_of.simplify().options.clone(),
            other => vec![other.clone().into()],
        };

        let mut has_pending_values = false;
        let result = possible_values
            .into_iter()
            .all(|possible_value| match possible_value {
                InferredType::InferenceVariable(_) => {
                    has_pending_values = true;
                    true
                }
                InferredType::Error(_) => false,
                InferredType::Known(possible_value) => self.options.iter().any(|option| {
                    if let InferredType::Known(option) = option {
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

    pub fn narrow(&self, pattern_type: &LangType) -> OneOfLangType {
        let options = self.simplify().options;
        let possible_values = match pattern_type {
            LangType::OneOf(pattern_one_of) => pattern_one_of.simplify().options.clone(),
            other => vec![other.clone().into()],
        };

        let remaining_options = options
            .iter()
            .cloned()
            .filter(|option| {
                if let InferredType::Known(option) = option {
                    !possible_values.iter().any(|possible_value| {
                        if let InferredType::Known(possible_value) = possible_value {
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

        return OneOfLangType::new(remaining_options);
    }

    pub fn expand(&self, pattern_type: &AnyInferredLangType) -> OneOfLangType {
        let new_values = vec![self.options.clone(), vec![pattern_type.clone()]].concat();
        return OneOfLangType::new(new_values).simplify();
    }

    pub fn simplify(&self) -> OneOfLangType {
        let all_possible_types = self
            .options
            .iter()
            .flat_map(|it| {
                if let InferredType::Known(known) = it {
                    if let LangType::OneOf(one_of) = known.as_ref() {
                        return one_of.simplify().options.clone();
                    }
                }
                return vec![it.clone()];
            })
            .collect_vec();

        let all_possible_types = if all_possible_types.len() > 1 {
            let mut not_duplicated = vec![];
            for possible_type in &all_possible_types {
                let possible_type = if let InferredType::Known(known) = possible_type {
                    known
                } else {
                    not_duplicated.push(possible_type.clone());
                    continue;
                };

                // first check if the the type is already covered by the options in `not_duplicated`
                let is_already_covered = not_duplicated.iter().any(|existing_type| {
                    OneOfLangType::is_mergeable(&possible_type.clone().into(), existing_type)
                });
                if !is_already_covered {
                    // make sure none of the existing options in `not_duplicated``
                    // would be made redundant by adding `possible_type`
                    // (i.e. they are more specific than `possible_type`)
                    not_duplicated = not_duplicated
                        .into_iter()
                        .filter(|existing_type| {
                            !OneOfLangType::is_mergeable(
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

        OneOfLangType::new(all_possible_types)
    }

    pub fn simplify_to_value(&self) -> AnyInferredLangType {
        let simplified = self.simplify();
        if simplified.options.len() == 1 {
            simplified.options[0].clone()
        } else if simplified.options.len() == 0 {
            LangType::NeverContinues.into()
        } else {
            simplified.into()
        }
    }

    fn is_mergeable(
        less_specific: &AnyInferredLangType,
        more_specific: &AnyInferredLangType,
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
impl From<OneOfLangType> for LangType {
    fn from(value: OneOfLangType) -> Self {
        Self::OneOf(value)
    }
}
impl Hash for OneOfLangType {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.simplify().options.hash(state);
    }
}
impl HasInference for OneOfLangType {
    fn recursive_inferred_types(&self) -> Vec<AnyInferredLangType> {
        self.options
            .iter()
            .flat_map(|o| o.recursive_inferred_types())
            .collect()
    }

    fn fill_variable(&self, id: u64, value: AnyInferredLangType) -> Self {
        let options = self
            .simplify()
            .options
            .iter()
            .map(|it| it.fill_variable(id, value.clone()))
            .collect_vec();
        return Self { options };
    }
}

#[derive(Debug, Clone, Eq, PartialEq, PartialOrd, Ord, Hash)]
pub struct CanonicalLangTypeId {
    pub path: Arc<String>,
    pub parent_name: Option<Arc<String>>,
    pub name: Option<Arc<String>>,
    pub number: u32,
    pub category: CanonicalLangTypeCategory,
    pub is_unique: bool,
}
impl ToString for CanonicalLangTypeId {
    fn to_string(&self) -> String {
        let name_with_fallback = self
            .name
            .as_ref()
            .map(|it| it.to_string())
            .unwrap_or_else(|| "$?".to_string());
        let full_name = match &self.parent_name {
            Some(parent_name) => {
                format! {"{}.{}", parent_name, name_with_fallback}
            }
            None => name_with_fallback,
        };

        let number_if_applicable = if self.number == 0 {
            "".into()
        } else {
            format!("_{}", self.number)
        };

        let category = match self.category {
            CanonicalLangTypeCategory::Object => "O",
            CanonicalLangTypeCategory::Signal => "S",
        };

        format!(
            "{}:{}:{}{}{}",
            &self.path,
            category,
            full_name,
            number_if_applicable,
            if self.is_unique { "!" } else { "" }
        )
    }
}
impl FromStr for CanonicalLangTypeId {
    type Err = anyhow::Error;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let original = s;
        let (path, s) = s
            .split_once(':')
            .ok_or(anyhow!("No : in type id: {}", original))?;
        let (category, s) = s
            .split_once(':')
            .ok_or(anyhow!("No : after category in type id: {}", original))?;

        let unique = s.ends_with('!');
        let s = if unique { s.trim_end_matches('!') } else { s };

        let (full_name, number_str) = s
            .split_once('_')
            .map(|(a, b)| (a, Some(b)))
            .unwrap_or((s, None));

        let number = number_str
            .map(|it| it.parse::<u32>())
            .transpose()
            .map_err(|err| anyhow!("Failed to parse number in type id: {}", err))?
            .unwrap_or(0);

        let (parent_name, name_with_fallback) = full_name
            .split_once('.')
            .map(|(a, b)| (Some(a), b))
            .unwrap_or((None, full_name));

        let name = if name_with_fallback == "$?" {
            None
        } else {
            Some(name_with_fallback)
        };

        let category = match category {
            "O" => CanonicalLangTypeCategory::Object,
            "S" => CanonicalLangTypeCategory::Signal,
            _ => {
                return Err(anyhow!(
                    "Invalid category in type id: {} (expected O or S)",
                    category
                ))
            }
        };

        Ok(CanonicalLangTypeId {
            path: path.to_string().into(),
            parent_name: parent_name.map(|it| it.to_string().into()),
            name: name.map(|it| it.to_string().into()),
            number,
            category,
            is_unique: unique,
        })
    }
}
impl Serialize for CanonicalLangTypeId {
    fn serialize<S: serde::Serializer>(&self, serializer: S) -> Result<S::Ok, S::Error> {
        serializer.serialize_str(&self.to_string())
    }
}
impl<'de> Deserialize<'de> for CanonicalLangTypeId {
    fn deserialize<D>(deserializer: D) -> Result<Self, D::Error>
    where
        D: serde::Deserializer<'de>,
    {
        let string = String::deserialize(deserializer)?;
        string
            .parse::<CanonicalLangTypeId>()
            .map_err(de::Error::custom)
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, PartialOrd, Ord, Serialize, Deserialize)]
pub enum CanonicalLangTypeCategory {
    Object,
    Signal,
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub enum CanonicalLangType {
    Object(ObjectCanonicalLangType),
    Signal(SignalCanonicalLangType),
}
impl CanonicalLangType {
    pub fn type_id(&self) -> CanonicalLangTypeId {
        match self {
            Self::Object(object) => object.type_id.clone(),
            Self::Signal(signal) => signal.type_id.clone(),
        }
    }
    pub fn fields(&self) -> Vec<CanonicalTypeField> {
        match self {
            Self::Object(object) => object.fields.clone(),
            Self::Signal(signal) => signal.fields.clone(),
        }
    }
}
impl HasInference for CanonicalLangType {
    fn recursive_inferred_types(&self) -> Vec<AnyInferredLangType> {
        match self {
            CanonicalLangType::Object(object) => object.recursive_inferred_types(),
            CanonicalLangType::Signal(signal) => signal.recursive_inferred_types(),
        }
    }

    fn fill_variable(&self, id: u64, value: AnyInferredLangType) -> Self {
        match self {
            CanonicalLangType::Object(object) => {
                CanonicalLangType::Object(object.fill_variable(id, value))
            }
            CanonicalLangType::Signal(signal) => {
                CanonicalLangType::Signal(signal.fill_variable(id, value))
            }
        }
    }
}

fn assert_uniqueness_matches(
    struct_name: &str,
    type_id: &CanonicalLangTypeId,
    fields: &[CanonicalTypeField],
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
pub struct ObjectCanonicalLangType {
    pub type_id: CanonicalLangTypeId,
    pub fields: Vec<CanonicalTypeField>,
}

impl ObjectCanonicalLangType {
    pub fn new(type_id: CanonicalLangTypeId, fields: Vec<CanonicalTypeField>) -> Self {
        if type_id.category != CanonicalLangTypeCategory::Object {
            panic!("ObjectCanonicalLangType::new called with non-object type_id");
        }
        assert_uniqueness_matches("ObjectCanonicalLangType", &type_id, &fields);
        Self { type_id, fields }
    }

    pub fn type_id(&self) -> &CanonicalLangTypeId {
        &self.type_id
    }

    pub fn fields(&self) -> &[CanonicalTypeField] {
        &self.fields
    }
}
impl HasInference for ObjectCanonicalLangType {
    fn recursive_inferred_types(&self) -> Vec<AnyInferredLangType> {
        return self
            .fields
            .iter()
            .flat_map(|f| f.value_type.recursive_inferred_types())
            .collect();
    }

    fn fill_variable(&self, id: u64, value: AnyInferredLangType) -> Self {
        let fields = self
            .fields
            .iter()
            .map(|it| CanonicalTypeField {
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
pub struct SignalCanonicalLangType {
    pub type_id: CanonicalLangTypeId,
    pub fields: Vec<CanonicalTypeField>,
    pub result: AnyInferredLangType,
}

impl SignalCanonicalLangType {
    pub fn new(
        type_id: CanonicalLangTypeId,
        fields: Vec<CanonicalTypeField>,
        result: AnyInferredLangType,
    ) -> Self {
        if type_id.category != CanonicalLangTypeCategory::Signal {
            panic!("SignalCanonicalLangType::new called with non-signal type_id");
        }
        assert_uniqueness_matches("SignalCanonicalLangType", &type_id, &fields);
        Self {
            type_id,
            fields,
            result,
        }
    }
    pub fn type_id(&self) -> &CanonicalLangTypeId {
        &self.type_id
    }
    pub fn fields(&self) -> &[CanonicalTypeField] {
        &self.fields
    }
    pub fn result(&self) -> &AnyInferredLangType {
        &self.result
    }
}
impl HasInference for SignalCanonicalLangType {
    fn recursive_inferred_types(&self) -> Vec<AnyInferredLangType> {
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

    fn fill_variable(&self, id: u64, value: AnyInferredLangType) -> Self {
        let fields = self
            .fields
            .iter()
            .map(|it| CanonicalTypeField {
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
pub struct CanonicalTypeField {
    pub name: Arc<String>,
    pub value_type: AnyInferredLangType,
}

#[cfg(test)]
mod test {
    use super::*;

    mod one_of {
        use super::*;

        #[test]
        fn test_collapse_nevercontinues_first() {
            let one_of = OneOfLangType::new(vec![
                LangType::NeverContinues.into(),
                LangType::Primitive(PrimitiveLangType::Number).into(),
            ]);
            let simplified = one_of.simplify_to_value();
            assert_eq!(
                simplified,
                LangType::Primitive(PrimitiveLangType::Number).into()
            )
        }

        #[test]
        fn test_collapse_nevercontinues_second() {
            let one_of = OneOfLangType::new(vec![
                LangType::Primitive(PrimitiveLangType::Number).into(),
                LangType::NeverContinues.into(),
            ]);
            let simplified = one_of.simplify_to_value();
            assert_eq!(
                simplified,
                LangType::Primitive(PrimitiveLangType::Number).into()
            )
        }

        #[test]
        fn test_single_value_assignable() {
            let one_of =
                OneOfLangType::new(vec![LangType::Primitive(PrimitiveLangType::Number).into()]);
            let primitive = LangType::Primitive(PrimitiveLangType::Number);
            assert!(LangType::OneOf(one_of).is_assignable_to(&primitive));
        }
    }

    mod functions {
        use super::*;

        #[test]
        fn test_function_with_specific_return_type_assignable_to_function_with_anything_return() {
            let function_constraint = FunctionLangType {
                name: None,
                params: vec![],
                return_type: LangType::Anything.into(),
            };
            let actual_function = FunctionLangType {
                name: None,
                params: vec![],
                return_type: LangType::Primitive(PrimitiveLangType::Number).into(),
            };
            assert!(
                LangType::Function(actual_function).is_assignable_to(&function_constraint.into())
            );
        }
    }
}
