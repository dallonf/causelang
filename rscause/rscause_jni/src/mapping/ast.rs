use anyhow::Result;
use jni::JNIEnv;

pub use super::gen::ast_mapping as mapping;

pub trait JniToAstNode<T> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<T>;
}
