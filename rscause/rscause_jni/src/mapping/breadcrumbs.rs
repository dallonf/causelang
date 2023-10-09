use crate::util::noisy_log;

use super::{ast::J_BREADCRUMB_NAMES, FromJni, IntoJni, JniInto};
use anyhow::anyhow;
use jni::objects::JValueOwned;
use rscause_compiler::{
    ast,
    breadcrumbs::{BreadcrumbEntry, BreadcrumbName, Breadcrumbs},
};

impl FromJni for Breadcrumbs {
    fn from_jni<'local>(
        env: &mut jni::JNIEnv,
        value: &jni::objects::JObject<'local>,
    ) -> anyhow::Result<Self> {
        noisy_log(env, "Breadcrumbs.getEntries()");
        let entries = &env
            .call_method(value, "getEntries", "()Ljava/util/List;", &[])?
            .l()?;
        let entries: Vec<BreadcrumbEntry> = entries.jni_into(env)?;
        Ok(Breadcrumbs { entries })
    }
}

impl IntoJni for Breadcrumbs {
    fn into_jni<'local>(
        &self,
        env: &mut jni::JNIEnv<'local>,
    ) -> anyhow::Result<jni::objects::JValueOwned<'local>> {
        let class = env.find_class("com/dallonf/ktcause/ast/Breadcrumbs")?;
        let jni_entries = self.entries.into_jni(env)?;
        let jni_breadcrumbs =
            env.new_object(class, "(Ljava/util/List;)V", &[jni_entries.borrow()])?;
        Ok(jni_breadcrumbs.into())
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
                let breadcrumb_name_index = J_BREADCRUMB_NAMES
                    .iter()
                    .position(|it| it == &name)
                    .ok_or_else(|| anyhow!("Couldn't find breadcrumb name {}", &name))?;
                let breadcrumb_name =
                    BreadcrumbName::new(ast::BREADCRUMB_NAMES[breadcrumb_name_index]);
                Ok(Self::Name(breadcrumb_name))
            }
            _ => Err(anyhow!("Unknown breadcrumb entry type: {}", class_name)),
        }
    }
}

impl IntoJni for BreadcrumbEntry {
    fn into_jni<'local>(
        &self,
        env: &mut jni::JNIEnv<'local>,
    ) -> anyhow::Result<jni::objects::JValueOwned<'local>> {
        match self {
            BreadcrumbEntry::Index(index) => {
                let class =
                    env.find_class("com/dallonf/ktcause/ast/Breadcrumbs$BreadcrumbEntry$Index")?;
                let jni_index = JValueOwned::Int((*index).try_into()?);
                let jni_entry = env.new_object(class, "(I)V", &[jni_index.borrow()])?;
                Ok(jni_entry.into())
            }
            BreadcrumbEntry::Name(name) => {
                let class =
                    env.find_class("com/dallonf/ktcause/ast/Breadcrumbs$BreadcrumbEntry$Name")?;
                let jni_name = name.name.into_jni(env)?;
                let jni_entry =
                    env.new_object(class, "(Ljava/lang/String;)V", &[jni_name.borrow()])?;
                Ok(jni_entry.into())
            }
        }
    }
}
