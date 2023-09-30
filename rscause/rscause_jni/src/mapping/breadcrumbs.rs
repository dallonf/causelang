use super::{FromJni, JniInto};
use anyhow::anyhow;
use rscause_compiler::breadcrumbs::{BreadcrumbEntry, BreadcrumbName, Breadcrumbs};

impl FromJni for Breadcrumbs {
    fn from_jni<'local>(
        env: &mut jni::JNIEnv,
        value: &jni::objects::JObject<'local>,
    ) -> anyhow::Result<Self> {
        let entries = &env
            .call_method(value, "getEntries", "Ljava/util/List;", &[])?
            .l()?;
        let entries: Vec<BreadcrumbEntry> = entries.jni_into(env)?;
        Ok(Breadcrumbs { entries })
    }
}

impl FromJni for BreadcrumbEntry {
    fn from_jni<'local>(
        env: &mut jni::JNIEnv,
        value: &jni::objects::JObject<'local>,
    ) -> anyhow::Result<Self> {
        let class = env.get_object_class(value)?;
        let class_name: String = env
            .call_method(&class, "getSimpleName", "()Ljava/lang/String;", &[])?
            .l()?
            .jni_into(env)?;
        match class_name.as_str() {
            "Index" => {
                let index: usize = env
                    .call_method(value, "getIndex", "()I", &[])?
                    .i()?
                    .try_into()?;
                Ok(Self::Index(index))
            }
            "Name" => {
                let name: String = env
                    .call_method(value, "getName", "()Ljava/lang/String;", &[])?
                    .l()?
                    .jni_into(env)?;
                Ok(Self::Name(BreadcrumbName::new(&name)))
            }
            _ => Err(anyhow!("Unknown breadcrumb entry type: {}", class_name)),
        }
    }
}
