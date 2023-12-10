use std::sync::Arc;

use crate::mapping::{FromJni, IntoJni, JniInto};
use crate::util::{get_class_name, noisy_log};
use anyhow::Result;
use jni::objects::{JObject, JValueOwned};
use jni::JNIEnv;
use rscause_compiler::ast::DocumentRange;
use rscause_compiler::breadcrumbs::Breadcrumbs;
use rscause_compiler::error_types::{self, SourcePosition};
use rscause_compiler::error_types::{ErrorPosition, LangError};
use rscause_compiler::lang_types;
use rscause_compiler::resolve_types::ResolverError;
use tap::TryConv;

include!("./gen/error_types.rs");

impl FromJni for SourcePosition {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        let path: Arc<String> = {
            let jni_node = env
                .call_method(value, "getPath", "()Ljava/lang/String;", &[])?
                .l()?;
            let jni_node = JObject::from(jni_node);
            jni_node.jni_into(env)?
        };
        let breadcrumbs: Breadcrumbs = {
            let jni_node = env
                .call_method(
                    value,
                    "getBreadcrumbs",
                    "()Lcom/dallonf/ktcause/ast/Breadcrumbs;",
                    &[],
                )?
                .l()?;
            let jni_node = JObject::from(jni_node);
            jni_node.jni_into(env)?
        };
        let position: DocumentRange = {
            let jni_node = env
                .call_method(
                    value,
                    "getPosition",
                    "()Lcom/dallonf/ktcause/ast/DocumentRange;",
                    &[],
                )?
                .l()?;
            let jni_node = JObject::from(jni_node);
            jni_node.jni_into(env)?
        };
        Ok(SourcePosition {
            path,
            breadcrumbs,
            position,
        })
    }
}

impl IntoJni for SourcePosition {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/ast/SourcePosition$Source")?;
        let path = self.path.into_jni(env)?;
        let breadcrumbs = self.breadcrumbs.into_jni(env)?;
        let position = self.position.into_jni(env)?;
        let result = env.new_object(
          class,
          "(Ljava/lang/String;Lcom/dallonf/ktcause/ast/Breadcrumbs;Lcom/dallonf/ktcause/ast/DocumentRange;)V",
          &[path.borrow(), breadcrumbs.borrow(), position.borrow()],
        )?;
        Ok(result.into())
    }
}

impl FromJni for ErrorPosition {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        let class_name = get_class_name(env, value)?;
        match class_name.as_ref() {
            "Source" => {
                let source_position: SourcePosition = {
                    let jni_node = env
                        .call_method(
                            value,
                            "getSourcePosition",
                            "()Lcom/dallonf/ktcause/error_types/SourcePosition;",
                            &[],
                        )?
                        .l()?;
                    let jni_node = JObject::from(jni_node);
                    jni_node.jni_into(env)?
                };
                Ok(ErrorPosition::Source(source_position))
            }
            _ => Err(anyhow::anyhow!(
                "Unknown ErrorPosition class: {}",
                class_name
            )),
        }
    }
}

impl IntoJni for ErrorPosition {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        match self {
            ErrorPosition::Source(source_position) => source_position.into_jni(env),
        }
    }
}

impl IntoJni for ResolverError {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        noisy_log(env, "ResolverError.into_jni");
        let class = env.find_class("com/dallonf/ktcause/Resolver$ResolverError")?;
        let position = self.position.into_jni(env)?;
        let error = self.error.into_jni(env)?;
        let result = env.new_object(
          class,
          "(Lcom/dallonf/ktcause/ast/SourcePosition$Source;Lcom/dallonf/ktcause/types/ErrorLangType;)V",
          &[position.borrow(), error.borrow()],
        )?;
        Ok(result.into())
    }
}
