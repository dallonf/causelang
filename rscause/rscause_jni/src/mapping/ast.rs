use rscause_compiler::ast::{DocumentPosition, DocumentRange, NodeInfo};
use std::sync::Arc;

use super::{FromJni, IntoJni, JniInto};
use anyhow::Result;
use jni::{
    objects::{JObject, JValueOwned},
    JNIEnv,
};
use rscause_compiler::ast;

use crate::util::noisy_log;

include!("gen/ast_mapping.rs");

impl FromJni for NodeInfo {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        let position = env
            .call_method(
                value,
                "getPosition",
                "()Lcom/dallonf/ktcause/ast/DocumentRange;",
                &[],
            )?
            .l()?
            .jni_into(env)?;
        let breadcrumbs = env
            .call_method(
                value,
                "getBreadcrumbs",
                "()Lcom/dallonf/ktcause/ast/Breadcrumbs;",
                &[],
            )?
            .l()?
            .jni_into(env)?;
        Ok(NodeInfo {
            position,
            breadcrumbs,
        })
    }
}

impl IntoJni for NodeInfo {
    fn into_jni<'local>(
        &self,
        env: &mut jni::JNIEnv<'local>,
    ) -> Result<jni::objects::JValueOwned<'local>> {
        let info_class = env.find_class("com/dallonf/ktcause/ast/NodeInfo")?;
        let jni_breadcrumbs = self.breadcrumbs.into_jni(env)?;
        let jni_position = self.position.into_jni(env)?;
        let jni_info = env.new_object(
            info_class,
            "(Lcom/dallonf/ktcause/ast/DocumentRange;Lcom/dallonf/ktcause/ast/Breadcrumbs;)V",
            &[jni_breadcrumbs.borrow(), jni_position.borrow()],
        )?;
        Ok(jni_info.into())
    }
}

impl FromJni for DocumentRange {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        let start = env
            .call_method(
                value,
                "getStart",
                "()Lcom/dallonf/ktcause/ast/DocumentPosition;",
                &[],
            )?
            .l()?
            .jni_into(env)?;
        let end = env
            .call_method(
                value,
                "getEnd",
                "()Lcom/dallonf/ktcause/ast/DocumentPosition;",
                &[],
            )?
            .l()?
            .jni_into(env)?;
        Ok(DocumentRange { start, end })
    }
}

impl IntoJni for DocumentRange {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/ast/DocumentRange")?;
        let jni_start = self.start.into_jni(env)?;
        let jni_end = self.end.into_jni(env)?;
        let jni_range = env.new_object(
            class,
            "(Lcom/dallonf/ktcause/ast/DocumentPosition;Lcom/dallonf/ktcause/ast/DocumentPosition;)V",
            &[jni_start.borrow(), jni_end.borrow()],
        )?;
        Ok(jni_range.into())
    }
}

impl FromJni for DocumentPosition {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        let line = env.call_method(value, "getLine", "()I", &[])?.i()? as u32;
        let column = env.call_method(value, "getColumn", "()I", &[])?.i()? as u32;
        Ok(DocumentPosition { line, column })
    }
}

impl IntoJni for DocumentPosition {
    fn into_jni<'local>(
        &self,
        env: &mut jni::JNIEnv<'local>,
    ) -> Result<jni::objects::JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/ast/DocumentPosition")?;
        let jni_line = JValueOwned::Int(self.line as i32);
        let jni_column = JValueOwned::Int(self.column as i32);
        let jni_position =
            env.new_object(class, "(II)V", &[jni_line.borrow(), jni_column.borrow()])?;
        Ok(jni_position.into())
    }
}
