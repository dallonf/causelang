use crate::prelude::*;
use std::{str::FromStr, sync::Arc};

use anyhow::anyhow;
use serde::{
    de::{self, value},
    Deserialize, Serialize,
};

use crate::error_types::{ConstraintUsedAsValueError, LangError};

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub enum InferredType<T> {
    Known(T),
    Error(Arc<LangError>),
    InferenceVariable(usize),
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
    #[inline]
    pub fn to_result(self) -> Result<T, Arc<LangError>> {
        match self {
            InferredType::Known(t) => Ok(t),
            InferredType::Error(err) => Err(err),
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

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub enum LangType {
    TypeReference(AnyInferredLangType),
    Action,
    Instance(InstanceLangType),
    Function(FunctionLangType),
    Primitive(PrimitiveLangType),
    Anything,
    OneOf(OneOfLangType),
    NeverContinues,
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

    pub fn is_assignable_to(&self, other_type: &LangType) -> bool {
        if self == &LangType::NeverContinues {
            return true;
        }

        match other_type {
            // Type references aren't assignable to other type references
            // at least until generics become a thing
            LangType::TypeReference(_other_type_reference) => false,

            LangType::Action => self == &LangType::Action,
            LangType::Instance(other_instance) => {
                if let LangType::Instance(self_instance) = self {
                    self_instance.type_id == other_instance.type_id
                } else {
                    // TODO: support unique types
                    false
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
                                self_param == other_param
                            });

                    let return_type_matches = {
                        // also don't allow any variance for now
                        self_function.return_type == other_function.return_type
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
            LangType::OneOf(other_one_of) => other_one_of.is_superset_of(self),

            LangType::NeverContinues => self == &LangType::NeverContinues,
        }
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub struct FunctionLangType {
    pub name: Arc<String>,
    pub params: Vec<LangParameter>,
    pub return_type: AnyInferredLangType,
}
impl From<FunctionLangType> for LangType {
    fn from(value: FunctionLangType) -> Self {
        Self::Function(value)
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub struct LangParameter {
    pub name: Arc<String>,
    pub value_type: AnyInferredLangType,
}

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub enum PrimitiveLangType {
    Text,
    Number,
}
impl From<PrimitiveLangType> for LangType {
    fn from(value: PrimitiveLangType) -> Self {
        Self::Primitive(value)
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
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
        let possible_values = match pattern_type {
            LangType::OneOf(pattern_one_of) => pattern_one_of.simplify().options.clone(),
            other => vec![other.clone().into()],
        };

        let remaining_options = self
            .options
            .iter()
            .cloned()
            .filter(|option| {
                if let InferredType::Known(option) = option {
                    !possible_values.iter().any(|possible_value| {
                        if let InferredType::Known(possible_value) = possible_value {
                            option.is_assignable_to(&possible_value)
                        } else {
                            // don't count error or pending
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

    pub fn expand(&self, pattern_type: &LangType) -> OneOfLangType {
        let new_values = vec![self.options.clone(), vec![pattern_type.clone().into()]].concat();
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
                                &possible_type.clone().into(),
                                existing_type,
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

#[derive(Debug, Clone, Eq, PartialEq, Hash)]
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

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub enum CanonicalLangTypeCategory {
    Object,
    Signal,
}

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
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

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub struct ObjectCanonicalLangType {
    type_id: CanonicalLangTypeId,
    fields: Vec<CanonicalTypeField>,
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

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub struct SignalCanonicalLangType {
    type_id: CanonicalLangTypeId,
    fields: Vec<CanonicalTypeField>,
    result: AnyInferredLangType,
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

#[derive(Debug, Clone, Eq, PartialEq, Serialize, Deserialize)]
pub struct CanonicalTypeField {
    pub name: Arc<String>,
    pub value_type: AnyInferredLangType,
}
