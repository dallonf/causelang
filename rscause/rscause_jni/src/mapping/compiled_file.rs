use std::collections::{hash_map, HashMap};

use super::IntoJni;
use anyhow::Result;
use jni::{
    objects::{JObject, JValue, JValueOwned},
    JNIEnv,
};
use rscause_compiler::compiled_file::CompiledFile;

impl IntoJni for CompiledFile {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let compiled_file_class = env.find_class("com/dallonf/ktcause/CompiledFile")?;
        let jni_path = self.path.into_jni(env)?;
        let jni_compiled_file = env.new_object(compiled_file_class, "(Ljava/lang/String;Ljava/util/Map;Ljava/util/List;Ljava/util/Map;Lcom/dallonf/ktcause/Debug$DebugContext;)V", &[
            jni_path.borrow(),
            // HashMap::new().into_jni(env)?,
            // self.procedures.into_jni(env)?,
            // self.exports.into_jni(env)?,
            // JValue::Object(&JObject::null()),
        ])?; //.map(|it| it.into()).map_err(|it| it.into());
        Ok(jni_compiled_file.into())
    }
}
