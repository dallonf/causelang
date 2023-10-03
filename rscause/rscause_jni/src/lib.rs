use std::collections::HashMap;
use std::fs::File;
use std::sync::Arc;

use jni::objects::{JClass, JObject, JValue};
use jni::sys::{jstring, jvalue};
use jni::JNIEnv;
use mapping::JniInto;
use rscause_compiler::ast::FileNode;
use rscause_compiler::breadcrumbs::Breadcrumbs;
use rscause_compiler::lang_types::{CanonicalLangType, CanonicalLangTypeId};
use rscause_compiler::resolve_types::{resolve_types, ExternalFileDescriptor};
use rscause_compiler::tags::NodeTag;
use util::{jprintln, jtry};

mod mapping;
mod util;

#[no_mangle]
pub extern "system" fn Java_com_dallonf_ktcause_RustCompiler_hello<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jstring {
    jprintln(&mut env, "Logging from Rust v2!").unwrap();

    let output = env.new_string("Hello from Rust!").unwrap();
    output.into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_dallonf_ktcause_RustCompiler_logAst<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    jni_ast: JObject<'local>,
) -> jvalue {
    jtry(&mut env, move |mut env| {
        let ast: FileNode = jni_ast.jni_into(&mut env)?;

        jprintln(&mut env, format!("AST: {:#?}", ast).as_str())?;

        Ok(JValue::Object(&JObject::null()).as_jni())
    })
}

#[no_mangle]
pub extern "system" fn Java_com_dallonf_ktcause_RustCompiler_logResolvedTypes<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    jni_ast: JObject<'local>,
    jni_tags: JObject<'local>,
    jni_canonical_types: JObject<'local>,
    jni_external_files: JObject<'local>,
) -> jvalue {
    jtry(&mut env, move |mut env| {
        let ast: FileNode = jni_ast.jni_into(&mut env)?;
        let canonical_types: HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>> =
            jni_canonical_types.jni_into(&mut env)?;
        let external_files: HashMap<Arc<String>, ExternalFileDescriptor> =
            jni_external_files.jni_into(&mut env)?;
        let tags: HashMap<Breadcrumbs, Vec<NodeTag>> = jni_tags.jni_into(&mut env)?;

        serde_lexpr::to_writer(&File::create("ast.txt")?, &ast)?;
        serde_lexpr::to_writer(&File::create("tags.txt")?, &tags)?;
        serde_lexpr::to_writer(&File::create("canonical_types.txt")?, &canonical_types)?;
        serde_lexpr::to_writer(&File::create("external_files.txt")?, &external_files)?;

        let resolved_types = resolve_types(
            ast.into(),
            tags.into(),
            canonical_types,
            external_files.into(),
        );

        jprintln(
            &mut env,
            format!("resolved types: {:#?}", resolved_types).as_str(),
        )?;

        Ok(JValue::Object(&JObject::null()).as_jni())
    })
}
