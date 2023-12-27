use std::sync::Arc;

use crate::util::{get_class_name, noisy_log};

use super::{FromJni, IntoJni, JniInto};
use anyhow::{anyhow, Result};
use jni::{
    objects::{JObject, JValueOwned},
    JNIEnv,
};
use rscause_compiler::{
    error_types::LangError,
    lang_types::{
        AnyInferredLangType, CanonicalLangType, CanonicalLangTypeCategory, CanonicalLangTypeId,
        CanonicalTypeField, FunctionLangType, InferredType, InstanceLangType, LangType,
        OneOfLangType, PrimitiveLangType, SignalCanonicalLangType,
    },
};
use tap::prelude::*;

impl FromJni for CanonicalLangTypeId {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        noisy_log(env, "CanonicalLangTypeId::from_jni");
        let path = env
            .call_method(value, "getPath", "()Ljava/lang/String;", &[])?
            .l()?
            .jni_into(env)?;
        let parent_name = env
            .call_method(value, "getParentName", "()Ljava/lang/String;", &[])?
            .l()?
            .jni_into(env)?;
        let name = env
            .call_method(value, "getName", "()Ljava/lang/String;", &[])?
            .l()?
            .jni_into(env)?;
        let number = env
            // gesundheit?
            .call_method(value, "getNumber-pVg5ArA", "()I", &[])?
            .i()?
            .pipe(|jni_number| jni_number as u32);
        let category = env
            .call_method(
                value,
                "getCategory",
                "()Lcom/dallonf/ktcause/types/CanonicalLangTypeId$CanonicalLangTypeIdCategory;",
                &[],
            )?
            .l()?
            .jni_into(env)?;
        let is_unique = env.call_method(value, "isUnique", "()Z", &[])?.z()?.into();
        Ok(Self {
            path,
            parent_name,
            name,
            number,
            category,
            is_unique,
        })
    }
}

impl IntoJni for CanonicalLangTypeId {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com.dallonf.ktcause.types.CanonicalLangTypeId")?;
        let jni_path = self.path.into_jni(env)?;
        let jni_parent_name = self.parent_name.into_jni(env)?;
        let jni_name = self.name.clone().into_jni(env)?;
        let jni_number = self.number.into_jni(env)?;
        let jni_category = self.category.into_jni(env)?;
        let jni_is_unique = self.is_unique.into_jni(env)?;
        let result = env.new_object(class, "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILcom/dallonf/ktcause/types/CanonicalLangTypeId$CanonicalLangTypeIdCategory;ZILkotlin/jvm/internal/DefaultConstructorMarker;)V", &[
            jni_path.borrow(),
            jni_parent_name.borrow(),
            jni_name.borrow(),
            jni_number.borrow(),
            jni_category.borrow(),
            jni_is_unique.borrow(),
        ])?;
        Ok(result.into())
    }
}

impl FromJni for CanonicalLangTypeCategory {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        let value_ordinal = env.call_method(value, "ordinal", "()I", &[])?.i()?;
        match value_ordinal {
            0 => Ok(Self::Signal),
            1 => Ok(Self::Object),
            _ => Err(anyhow!(
                "Unexpected CanonicalLangTypeCategory ordinal: {}",
                value_ordinal
            )),
        }
    }
}

impl IntoJni for CanonicalLangTypeCategory {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class(
            "com/dallonf/ktcause/types/CanonicalLangTypeId$CanonicalLangTypeIdCategory",
        )?;
        match self {
            CanonicalLangTypeCategory::Object => {
                let jni_result = env.get_static_field(
                    class,
                    "OBJECT",
                    "Lcom/dallonf/ktcause/types/CanonicalLangTypeId$CanonicalLangTypeIdCategory;",
                )?;
                Ok(jni_result)
            }
            CanonicalLangTypeCategory::Signal => {
                let jni_result = env.get_static_field(
                    class,
                    "SIGNAL",
                    "Lcom/dallonf/ktcause/types/CanonicalLangTypeId$CanonicalLangTypeIdCategory;",
                )?;
                Ok(jni_result)
            }
        }
    }
}

impl FromJni for CanonicalLangType {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        noisy_log(env, "CanonicalLangType::from_jni");
        let class_name = get_class_name(env, value)?;
        match class_name.as_ref() {
            "SignalCanonicalLangType" => Ok(Self::Signal(value.jni_into(env)?)),
            _ => Err(anyhow!(
                "Unsupported CanonicalLangType class: {}",
                class_name
            )),
        }
    }
}

impl FromJni for SignalCanonicalLangType {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        noisy_log(env, "SignalCanonicalLangType::from_jni");
        let type_id = env
            .call_method(
                value,
                "getId",
                "()Lcom/dallonf/ktcause/types/CanonicalLangTypeId;",
                &[],
            )?
            .l()?
            .jni_into(env)?;
        noisy_log(
            env,
            &format!(
                "SignalCanonicalLangType::from_jni - got type_id: {:?}",
                &type_id
            ),
        );
        let fields = env
            .call_method(value, "getFields", "()Ljava/util/List;", &[])?
            .l()?
            .jni_into(env)?;
        noisy_log(env, "SignalCanonicalLangType::from_jni - got fields");

        Ok(Self::new(
            type_id,
            fields, // TODO: hardcoded
            LangType::Action.into(),
        ))
    }
}

/// Java name: ObjectField
impl FromJni for CanonicalTypeField {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        noisy_log(env, "CanonicalTypeField::from_jni");
        let name = env
            .call_method(value, "getName", "()Ljava/lang/String;", &[])?
            .l()?
            .jni_into(env)?;
        let value_type = env
            .call_method(
                value,
                "getValueConstraint",
                "()Lcom/dallonf/ktcause/types/ConstraintReference;",
                &[],
            )?
            .l()?
            .pipe(|jni_constraint_reference| {
                jni_constraint_reference_to_inferred_lang_type(env, &jni_constraint_reference)
            })?;

        Ok(Self { name, value_type })
    }
}

pub fn jni_constraint_reference_to_inferred_lang_type(
    env: &mut JNIEnv,
    jni_constraint_reference: &JObject,
) -> Result<AnyInferredLangType> {
    noisy_log(env, "jni_constraint_reference_to_inferred_lang_type");
    let class_name = get_class_name(env, jni_constraint_reference)?;
    match class_name.as_ref() {
        "ResolvedConstraint" => {
            let value_type = env
                .call_method(
                    jni_constraint_reference,
                    "getValueType",
                    "()Lcom/dallonf/ktcause/types/ResolvedValueLangType;",
                    &[],
                )?
                .l()?
                .jni_into(env)?;
            Ok(AnyInferredLangType::Known(value_type))
        }
        "Pending" => Ok(AnyInferredLangType::Error(
            LangError::NotSupportedInRust.into(),
        )),
        "Error" => Ok(AnyInferredLangType::Error(
            LangError::NotSupportedInRust.into(),
        )),
        _ => Err(anyhow!(
            "Unexpected ConstraintReference class: {}",
            class_name
        )),
    }
}

pub fn inferred_value_lang_type_to_jni_constraint_reference<'local, T: IntoJni>(
    env: &mut JNIEnv<'local>,
    lang_type: &InferredType<T>,
) -> Result<JValueOwned<'local>> {
    let jni_lang_type = lang_type.into_jni(env)?.l()?;
    let constraint_reference = env.call_method(
        &jni_lang_type,
        "valueToConstraintReference",
        "()Lcom/dallonf/ktcause/types/ConstraintReference;",
        &[],
    )?;
    Ok(constraint_reference)
}

pub fn lang_type_to_jni_constraint_value_lang_type<'local>(
    env: &mut JNIEnv<'local>,
    lang_type: &LangType,
) -> Result<JValueOwned<'local>> {
    let resolved_value_lang_type = lang_type.into_jni(env)?;
    let class = env.find_class("com/dallonf/ktcause/types/ConstraintValueLangType")?;
    let jni_constraint_value_lang_type = env.new_object(
        class,
        "(Lcom/dallonf/ktcause/types/ResolvedValueLangType;)V",
        &[resolved_value_lang_type.borrow()],
    )?;
    Ok(jni_constraint_value_lang_type.into())
}

/// Java name: ValueLangType
impl FromJni for AnyInferredLangType {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        noisy_log(env, "AnyInferredLangType::from_jni");
        let error_class = env.find_class("com/dallonf/ktcause/types/ErrorLangType")?;
        let resolved_class = env.find_class("com/dallonf/ktcause/types/ResolvedValueLangType")?;
        let class_name = get_class_name(env, value)?;

        if class_name == "Pending" {
            Ok(AnyInferredLangType::Error(
                LangError::NotSupportedInRust.into(),
            ))
        } else if env.is_instance_of(value, error_class)? {
            Ok(AnyInferredLangType::Error(
                LangError::NotSupportedInRust.into(),
            ))
        } else if env.is_instance_of(value, resolved_class)? {
            Ok(AnyInferredLangType::Known(value.jni_into(env)?))
        } else {
            Err(anyhow!("Unexpected langtype class: {}", class_name))
        }
    }
}

impl<T> IntoJni for InferredType<T>
where
    T: IntoJni,
{
    fn into_jni<'local>(
        &self,
        env: &mut jni::JNIEnv<'local>,
    ) -> Result<jni::objects::JValueOwned<'local>> {
        match self {
            InferredType::Known(known) => known.into_jni(env),
            InferredType::Error(err) => err.into_jni(env),
        }
    }
}

/// Java name: ResolvedValueLangType
impl FromJni for LangType {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        noisy_log(env, "LangType::from_jni");
        let class_name = get_class_name(env, value)?;
        match class_name.as_ref() {
            "ConstraintValueLangType" => {
                noisy_log(env, "- ConstraintValueLangType");
                let value_type: LangType = env
                    .call_method(
                        value,
                        "getValueType",
                        "()Lcom/dallonf/ktcause/types/ResolvedValueLangType;",
                        &[],
                    )?
                    .l()?
                    .jni_into(env)?;
                Ok(LangType::TypeReference(value_type.into()))
            }
            "ActionValueLangType" => {
                noisy_log(env, "- ActionValueLangType");
                Ok(LangType::Action)
            }
            "InstanceValueLangType" => {
                noisy_log(env, "- InstanceValueLangType");
                let canonical_type_id: Arc<CanonicalLangTypeId> = env
                    .call_method(
                        value,
                        "getCanonicalTypeId",
                        "()Lcom/dallonf/ktcause/types/CanonicalLangTypeId;",
                        &[],
                    )?
                    .l()?
                    .jni_into(env)?;
                noisy_log(
                    env,
                    &format!(
                        "- ({})",
                        canonical_type_id
                            .name
                            .clone()
                            .unwrap_or("[unnamed]".to_owned().into())
                    ),
                );
                Ok(LangType::Instance(InstanceLangType {
                    type_id: canonical_type_id,
                }))
            }
            "FunctionValueLangType" => {
                noisy_log(env, "- FunctionValueLangType");
                let name: Arc<String> = env
                    .call_method(value, "getName", "()Ljava/lang/String;", &[])?
                    .l()?
                    .jni_into(env)?;
                let return_type = env
                    .call_method(
                        value,
                        "getReturnConstraint",
                        "()Lcom/dallonf/ktcause/types/ConstraintReference;",
                        &[],
                    )?
                    .l()?
                    .pipe(|it| jni_constraint_reference_to_inferred_lang_type(env, &it))?;
                noisy_log(env, &format!("- ({})", name));
                Ok(LangType::Function(FunctionLangType { name, return_type }))
            }
            "PrimitiveValueLangType" => {
                noisy_log(env, "- PrimitiveValueLangType");
                let jni_kind = env
                    .call_method(
                        value,
                        "getKind",
                        "()Lcom/dallonf/ktcause/types/LangPrimitiveKind;",
                        &[],
                    )?
                    .l()?;
                let jni_kind_ordinal = env.call_method(jni_kind, "ordinal", "()I", &[])?.i()?;
                match jni_kind_ordinal {
                    0 => {
                        noisy_log(env, " - Text");
                        Ok(LangType::Primitive(PrimitiveLangType::Text))
                    }
                    1 => {
                        noisy_log(env, " - Number");
                        Ok(LangType::Primitive(PrimitiveLangType::Number))
                    }
                    _ => Err(anyhow!(
                        "don't support other primitive types yet. This is type {}",
                        jni_kind_ordinal
                    )),
                }
            }
            "AnythingValueLangType" => {
                noisy_log(env, "- AnythingValueLangType");
                Ok(LangType::Anything)
            }
            "OptionValueLangType" => {
                noisy_log(env, "- OptionValueLangType");
                let inner = OneOfLangType::from_jni(env, value)?;
                Ok(LangType::OneOf(inner))
            }
            _ => Err(anyhow!(
                "Unsupported ResolvedValueLangType class: {}",
                class_name
            )),
        }
    }
}

impl IntoJni for LangType {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        match self {
            LangType::TypeReference(value_type) => {
                let class = env.find_class("com/dallonf/ktcause/types/ConstraintValueLangType")?;
                let jni_value_type = value_type.into_jni(env)?;
                let result = env.new_object(
                    class,
                    "(Lcom/dallonf/ktcause/types/ResolvedValueLangType;)V",
                    &[jni_value_type.borrow()],
                )?;
                Ok(result.into())
            }
            LangType::Action => {
                let class = env.find_class("com/dallonf/ktcause/types/ActionValueLangType")?;
                let result = env.new_object(class, "()V", &[])?;
                Ok(result.into())
            }
            LangType::Instance(instance_type) => instance_type.into_jni(env),
            LangType::Function(function_type) => function_type.into_jni(env),
            LangType::Primitive(primitive_type) => primitive_type.into_jni(env),
            LangType::Anything => {
                let class = env.find_class("com/dallonf/ktcause/types/AnythingValueLangType")?;
                let result = env.new_object(class, "()V", &[])?;
                Ok(result.into())
            }
            LangType::OneOf(one_of_type) => one_of_type.into_jni(env),
        }
    }
}

impl IntoJni for FunctionLangType {
    fn into_jni<'local>(
        &self,
        env: &mut jni::JNIEnv<'local>,
    ) -> Result<jni::objects::JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/types/FunctionValueLangType")?;
        let name = self.name.into_jni(env)?;
        let return_type =
            inferred_value_lang_type_to_jni_constraint_reference(env, &self.return_type)?;
        let params = vec![].conv::<Vec<()>>().into_jni(env)?;
        let result = env.new_object(
            class,
            "(Ljava/lang/String;Lcom/dallonf/ktcause/types/ConstraintReference;Ljava/util/List;)V",
            &[name.borrow(), return_type.borrow(), params.borrow()],
        )?;
        Ok(result.into())
    }
}

impl IntoJni for InstanceLangType {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/types/InstanceValueLangType")?;
        let jni_canonical_type = self.type_id.into_jni(env)?;
        let result = env.new_object(
            class,
            "(Lcom/dallonf/ktcause/types/CanonicalLangTypeId;)V",
            &[jni_canonical_type.borrow()],
        )?;
        Ok(result.into())
    }
}

impl IntoJni for PrimitiveLangType {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/types/PrimitiveValueLangType")?;
        let kind_class = env.find_class("com/dallonf/ktcause/types/LangPrimitiveKind")?;
        let jni_kind = match self {
            PrimitiveLangType::Text => {
                let jni_text = env.get_static_field(
                    kind_class,
                    "TEXT",
                    "Lcom/dallonf/ktcause/types/LangPrimitiveKind;",
                )?;
                jni_text
            }
            PrimitiveLangType::Number => {
                let jni_text = env.get_static_field(
                    kind_class,
                    "NUMBER",
                    "Lcom/dallonf/ktcause/types/LangPrimitiveKind;",
                )?;
                jni_text
            }
        };
        let result = env.new_object(
            class,
            "(Lcom/dallonf/ktcause/types/LangPrimitiveKind;)V",
            &[jni_kind.borrow()],
        )?;
        Ok(result.into())
    }
}

impl IntoJni for OneOfLangType {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/types/OptionValueLangType")?;
        let options = self
            .options
            .iter()
            .map(|option| inferred_value_lang_type_to_jni_constraint_reference(env, option))
            .collect::<Result<Vec<_>>>()?
            .into_jni(env)?;
        let result = env.new_object(class, "()Ljava/util/List;", &[options.borrow()])?;
        Ok(result.into())
    }
}

// Java name: OptionValueLangType
impl FromJni for OneOfLangType {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        let options = env
            .call_method(value, "getOptions", "()Ljava/util/List;", &[])?
            .l()?;
        let jni_list = env.get_list(&options)?;
        let mut options = vec![];
        let mut iter = jni_list.iter(env)?;
        while let Some(jni_item) = iter.next(env)? {
            let option = jni_constraint_reference_to_inferred_lang_type(env, &jni_item)?;
            options.push(option);
        }
        Ok(OneOfLangType { options })
    }
}
