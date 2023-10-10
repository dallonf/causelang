use crate::util::{get_class_name, noisy_log};

use super::{FromJni, IntoJni, JniInto};
use anyhow::{anyhow, Error, Result};
use jni::{
    objects::{JObject, JValueOwned},
    JNIEnv,
};
use rscause_compiler::lang_types::{
    AnyInferredLangType, CanonicalLangType, CanonicalLangTypeId, CanonicalTypeField,
    FunctionLangType, InferredType, InstanceLangType, LangType, PrimitiveLangType,
    SignalCanonicalLangType,
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

        Ok(Self {
            path,
            parent_name,
            name,
            number,
        })
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
        noisy_log(env, "SignalCanonicalLangType::from_jni - got type_id");
        let fields = env
            .call_method(value, "getFields", "()Ljava/util/List;", &[])?
            .l()?
            .jni_into(env)?;
        noisy_log(env, "SignalCanonicalLangType::from_jni - got fields");

        Ok(Self {
            type_id,
            fields,
            // TODO: hardcoded
            result: LangType::Action.into(),
        })
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
        "Pending" => Ok(AnyInferredLangType::Error),
        "Error" => Ok(AnyInferredLangType::Error),
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

/// Java name: ValueLangType
impl FromJni for AnyInferredLangType {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        noisy_log(env, "AnyInferredLangType::from_jni");
        let error_class = env.find_class("com/dallonf/ktcause/types/ErrorLangType")?;
        let resolved_class = env.find_class("com/dallonf/ktcause/types/ResolvedValueLangType")?;
        let class_name = get_class_name(env, value)?;

        if class_name == "Pending" {
            Ok(AnyInferredLangType::Error)
        } else if env.is_instance_of(value, error_class)? {
            Ok(AnyInferredLangType::Error)
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
            InferredType::Error => {
                let error_class =
                    env.find_class("com/dallonf/ktcause/types/ErrorLangType$NotSupportedInRust")?;
                let error_instance = env.new_object(error_class, "()V", &[])?;
                Ok(error_instance.into())
            }
        }
    }
}

/// Java name: ResolvedValueLangType
impl FromJni for LangType {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        noisy_log(env, "AnyInferredLangType::from_jni");
        let class_name = get_class_name(env, value)?;
        match class_name.as_ref() {
            "ConstraintValueLangType" => {
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
            "ActionValueLangType" => Ok(LangType::Action),
            "InstanceValueLangType" => {
                let canonical_type = env
                    .call_method(
                        value,
                        "getCanonicalType",
                        "()Lcom/dallonf/ktcause/types/CanonicalLangType;",
                        &[],
                    )?
                    .l()?;
                let canonical_type_id = env
                    .call_method(
                        canonical_type,
                        "getId",
                        "()Lcom/dallonf/ktcause/types/CanonicalLangTypeId;",
                        &[],
                    )?
                    .l()?
                    .jni_into(env)?;
                Ok(LangType::Instance(InstanceLangType {
                    type_id: canonical_type_id,
                }))
            }
            "FunctionValueLangType" => {
                let name = env
                    .call_method(value, "getName", "()Ljava/lang/String;", &[])?
                    .l()?
                    .jni_into(env)?;
                let return_type: LangType = env
                    .call_method(
                        value,
                        "getReturnConstraint",
                        "()Lcom/dallonf/ktcause/types/ConstraintReference;",
                        &[],
                    )?
                    .l()?
                    .jni_into(env)?;
                Ok(LangType::Function(FunctionLangType {
                    name,
                    return_type: return_type.into(),
                }))
            }
            "PrimitiveValueLangType" => {
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
                    0 => Ok(LangType::Primitive(PrimitiveLangType::Text)),
                    _ => Err(anyhow!("don't support other primitive types yet")),
                }
            }
            "AnythingValueLangType" => Ok(LangType::Anything),
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
            &[name.borrow(), params.borrow(), return_type.borrow()],
        )?;
        Ok(result.into())
    }
}

impl IntoJni for InstanceLangType {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/types/InstanceValueLangType")?;
        let jni_canonical_type = self.type_id.into_jni(env)?;
    }
}