use anyhow::Result;
use jni::{
    objects::{JObject, JString},
    JNIEnv,
};
use rscause_compiler::ast_nodes::{self as ast, DeclarationNode};

pub trait JniToAstNode<T> {
    fn to_ast_node(&self, env: &mut JNIEnv) -> Result<T>;
}
