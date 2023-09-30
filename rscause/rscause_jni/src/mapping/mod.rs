use std::sync::Arc;

use anyhow::Result;
use jni::{objects::JObject, JNIEnv};

pub mod ast;
pub mod breadcrumbs;

pub trait FromJni: Sized {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self>;
}

pub trait JniInto<T>: Sized {
    fn jni_into(&self, env: &mut JNIEnv) -> Result<T>;
}

impl<'local, T> JniInto<T> for JObject<'local>
where
    T: FromJni,
{
    fn jni_into(&self, env: &mut JNIEnv) -> Result<T> {
        T::from_jni(env, self)
    }
}

impl FromJni for String {
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        let value = env.new_local_ref(value)?;
        let jni_string = value.into();
        let jni_string = env.get_string(&jni_string)?;
        Ok(jni_string.to_str()?.to_owned())
    }
}

impl<T> FromJni for Vec<T>
where
    T: FromJni,
{
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        let jni_list = env.get_list(value)?;
        let mut list = vec![];
        let mut iter = jni_list.iter(env)?;
        while let Some(jni_item) = iter.next(env)? {
            let node: T = jni_item.jni_into(env)?;
            list.push(node);
        }
        Ok(list)
    }
}

impl<T> FromJni for Option<T>
where
    T: FromJni,
{
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        if value.is_null() {
            Ok(None)
        } else {
            Ok(Some(value.jni_into(env)?))
        }
    }
}

impl<T> FromJni for Arc<T>
where
    T: FromJni,
{
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        Ok(Arc::new(value.jni_into(env)?))
    }
}

impl<T> FromJni for Box<T>
where
    T: FromJni,
{
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        Ok(Box::new(value.jni_into(env)?))
    }
}
