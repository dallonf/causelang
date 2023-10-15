use std::{
    collections::HashMap,
    hash::{self, Hash},
    sync::Arc,
};

use anyhow::Result;
use jni::{
    objects::{JObject, JValue, JValueOwned},
    JNIEnv,
};

use crate::util::noisy_log;

pub mod ast;
pub mod breadcrumbs;
pub mod compiled_file;
pub mod compiler_misc;
pub mod instructions;
pub mod lang_types;
pub mod tags;

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

pub trait IntoJni {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>>;
}

impl IntoJni for () {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        Ok(JValueOwned::Void)
    }
}

impl IntoJni for bool {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        Ok(JValueOwned::Bool(*self as u8))
    }
}

impl IntoJni for i32 {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        Ok(JValueOwned::Int(*self))
    }
}

impl IntoJni for u32 {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        (*self as i32).into_jni(env)
    }
}

impl IntoJni for str {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        env.new_string(self).map(Into::into).map_err(Into::into)
    }
}

impl IntoJni for String {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        self.as_str().into_jni(env)
    }
}

impl<T> IntoJni for Arc<T>
where
    T: IntoJni,
{
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        self.as_ref().into_jni(env)
    }
}

impl<T> IntoJni for Option<T>
where
    T: IntoJni,
{
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        match self {
            Some(value) => value.into_jni(env),
            None => Ok(JValueOwned::Object(JObject::null())),
        }
    }
}

impl<Key, Value> IntoJni for HashMap<Key, Value>
where
    Key: IntoJni,
    Value: IntoJni,
{
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let hash_map_class = env.find_class("java/util/HashMap")?;
        let hash_map = env.new_object(hash_map_class, "(I)V", &[JValue::Int(self.len() as i32)])?;
        for (key, value) in self {
            let jni_key = key.into_jni(env)?;
            let jni_value = value.into_jni(env)?;
            env.call_method(
                &hash_map,
                "put",
                "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                &[jni_key.borrow(), jni_value.borrow()],
            )?;
        }
        Ok(hash_map.into())
    }
}

impl<T> IntoJni for Vec<T>
where
    T: IntoJni,
{
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let list_class = env.find_class("java/util/ArrayList")?;
        let list_obj = env.new_object(list_class, "(I)V", &[JValue::Int(self.len() as i32)])?;
        let list = env.get_list(&list_obj)?;
        for item in self {
            let jni_item = item.into_jni(env)?.l()?;
            list.add(env, &jni_item)?;
        }
        Ok(list_obj.into())
    }
}
