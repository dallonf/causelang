use std::{collections::HashMap, hash::Hash, sync::Arc};

use anyhow::Result;
use jni::{objects::JObject, JNIEnv};

use crate::util::noisy_log;

pub mod ast;
pub mod breadcrumbs;
pub mod lang_types;
pub mod compiler_misc;

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

impl<T> FromJni for Vec<T>
where
    T: FromJni,
{
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        noisy_log(env, "Vec<T>::from_jni");
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

impl<Key, Value> FromJni for HashMap<Key, Value>
where
    Key: FromJni + Eq + Hash,
    Value: FromJni,
{
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        noisy_log(env, "HashMap<Key, Value>::from_jni");
        let jni_map = env.get_map(value)?;
        let mut map = HashMap::new();
        let mut iter = jni_map.iter(env)?;
        while let Some((jni_key, jni_value)) = iter.next(env)? {
            let key: Key = jni_key.jni_into(env)?;
            let value: Value = jni_value.jni_into(env)?;
            map.insert(key, value);
        }
        Ok(map)
    }
}

impl<T> FromJni for Option<T>
where
    T: FromJni,
{
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        noisy_log(env, "Option<T>::from_jni");
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
        noisy_log(env, "Arc<T>::from_jni");
        Ok(Arc::new(value.jni_into(env)?))
    }
}

impl<T> FromJni for Box<T>
where
    T: FromJni,
{
    fn from_jni<'local>(env: &mut JNIEnv, value: &JObject<'local>) -> Result<Self> {
        noisy_log(env, "Box<T>::from_jni");
        Ok(Box::new(value.jni_into(env)?))
    }
}
