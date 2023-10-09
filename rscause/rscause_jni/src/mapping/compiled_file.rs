use std::{
    borrow::BorrowMut,
    collections::{hash_map, HashMap},
};

use super::IntoJni;
use anyhow::Result;
use jni::{
    objects::{JObject, JValue, JValueOwned},
    JNIEnv,
};
use rscause_compiler::compiled_file::{
    CompiledFile, FunctionProcedureIdentity, Procedure, ProcedureIdentity,
};

impl IntoJni for CompiledFile {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let compiled_file_class = env.find_class("com/dallonf/ktcause/CompiledFile")?;
        let jni_path = self.path.into_jni(env)?;
        let jni_types = HashMap::<(), ()>::new().into_jni(env)?;
        let jni_procedures = self.procedures.into_jni(env)?;
        let jni_exports = self.exports.into_jni(env)?;
        let jni_debug_context = JValueOwned::Object(JObject::null());

        let jni_compiled_file = env.new_object(compiled_file_class, "(Ljava/lang/String;Ljava/util/Map;Ljava/util/List;Ljava/util/Map;Lcom/dallonf/ktcause/Debug$DebugContext;)V", &[
            jni_path.borrow(),
            jni_types.borrow(),
            jni_procedures.borrow(),
            jni_exports.borrow(),
        ])?;
        Ok(jni_compiled_file.into())
    }
}

impl IntoJni for Procedure {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let procedure_class = env.find_class("com/dallonf/ktcause/CompiledFile$Procedure")?;
        let jni_identity = self.identity.into_jni(env)?;
        let jni_constant_table = self.constant_table.into_jni(env)?;
        let jni_instructions = self.instructions.into_jni(env)?;
        let jni_source_map = JValueOwned::Object(JObject::null());

        let jni_procedure = env.new_object(procedure_class, "(Lcom/dallonf/ktcause/CompiledFile$Procedure$ProcedureIdentity;Ljava/util/List;Ljava/util/List;Ljava/util/List;)V", &[
            jni_identity.borrow(),
            jni_constant_table.borrow(),
            jni_instructions.borrow(),
            jni_source_map.borrow(),
        ])?;
        Ok(jni_procedure.into())
    }
}

impl IntoJni for ProcedureIdentity {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        match self {
            ProcedureIdentity::Function(function) => function.into_jni(env),
        }
    }
}

impl IntoJni for FunctionProcedureIdentity {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let class = env
            .find_class("com/dallonf/ktcause/CompiledFile$Procedure$ProcedureIdentity$Function")?;
        let name_jni = self.name.into_jni(env)?;
        let node_info_jni = self.declaration.into_jni(env)?;
        let result = env.new_object(
            class,
            "(Ljava/lang/String;Lcom/dallonf/ktcause/ast/NodeInfo;)V",
            &[name_jni.borrow(), node_info_jni.borrow()],
        )?;
        Ok(result.into())
    }
}
