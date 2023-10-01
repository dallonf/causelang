use rscause_compiler::resolve_types::ExternalFileDescriptor;

use super::{FromJni, JniInto};

impl FromJni for ExternalFileDescriptor {
    fn from_jni<'local>(
        env: &mut jni::JNIEnv,
        value: &jni::objects::JObject<'local>,
    ) -> anyhow::Result<Self> {
        let exports = env
            .call_method(value, "getExports", "()Ljava/util/Map;", &[])?
            .l()?
            .jni_into(env)?;
        Ok(ExternalFileDescriptor { exports })
    }
}
