use std::sync::Arc;

use anyhow::Result;
use jni::{objects::JObject, JNIEnv};

use crate::util::noisy_log;

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
        noisy_log(env, "String::from_jni");
        let value = env.new_local_ref(value)?;
        let jni_string = value.into();
        let jni_string = env.get_string(&jni_string)?;
        Ok(jni_string.to_str()?.to_owned())
    }
}

impl<T> FromJni for Arc<T>
where
    T: FromJni,
{
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        noisy_log(env, "Arc<T>::from_jni");
        Ok(Arc::new(value.jni_into(env)?))
    }
}
