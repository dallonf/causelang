use std::collections::HashMap;
use std::fs::File;
use std::sync::Arc;

use jni::objects::{JClass, JObject, JValueOwned};
use jni::sys::jvalue;
use jni::JNIEnv;
use mapping::{IntoJni, JniInto};
use rscause_compiler::ast::FileNode;
use rscause_compiler::breadcrumbs::Breadcrumbs;
use rscause_compiler::compile::compile;
use rscause_compiler::lang_types::{CanonicalLangType, CanonicalLangTypeId};
use rscause_compiler::resolve_types::{resolve_types, ExternalFileDescriptor};
use rscause_compiler::tags::NodeTag;
use tap::Pipe;
use util::jtry;

mod mapping;
mod util;

#[no_mangle]
pub extern "system" fn Java_com_dallonf_ktcause_RustCompiler_generateTestOutput<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    test_name: JObject<'local>,
    jni_ast: JObject<'local>,
    jni_tags: JObject<'local>,
    jni_canonical_types: JObject<'local>,
    jni_external_files: JObject<'local>,
) -> jvalue {
    jtry(&mut env, move |mut env| {
        let test_name: String = test_name.jni_into(&mut env)?;
        let ast: Arc<FileNode> = jni_ast.jni_into(&mut env)?;
        let canonical_types: Arc<HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>> =
            jni_canonical_types.jni_into(&mut env)?;
        let external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>> =
            jni_external_files.jni_into(&mut env)?;
        let tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>> = jni_tags.jni_into(&mut env)?;
        let path = format!("../rscause/rscause_compiler/tests/fixtures/{}", test_name);
        serde_lexpr::to_writer(File::create(format!("{path}/ast.txt"))?, &ast)?;
        serde_lexpr::to_writer(File::create(format!("{path}/tags.txt"))?, &tags)?;
        serde_lexpr::to_writer(
            File::create(format!("{path}/canonical_types.txt"))?,
            &canonical_types,
        )?;
        serde_lexpr::to_writer(
            File::create(format!("{path}/external_files.txt"))?,
            &external_files,
        )?;
        Ok(JValueOwned::Void.as_jni())
    })
}

#[no_mangle]
pub extern "system" fn Java_com_dallonf_ktcause_RustCompiler_compileInner<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    jni_path: JObject<'local>,
    jni_ast: JObject<'local>,
    jni_tags: JObject<'local>,
    jni_canonical_types: JObject<'local>,
    jni_external_files: JObject<'local>,
) -> jvalue {
    jtry(&mut env, move |mut env| {
        let path: Arc<String> = jni_path.jni_into(&mut env)?;
        let ast: Arc<FileNode> = jni_ast.jni_into(&mut env)?;
        let canonical_types: Arc<HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>> =
            jni_canonical_types.jni_into(&mut env)?;
        let external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>> =
            jni_external_files.jni_into(&mut env)?;
        let tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>> = jni_tags.jni_into(&mut env)?;

        // serde_lexpr::to_writer(File::create("ast.txt")?, &ast)?;
        // serde_lexpr::to_writer(File::create("tags.txt")?, &tags)?;
        // serde_lexpr::to_writer(File::create("canonical_types.txt")?, &canonical_types)?;
        // serde_lexpr::to_writer(File::create("external_files.txt")?, &external_files)?;

        let resolved_types: Arc<_> = resolve_types(
            ast.clone(),
            tags.clone(),
            canonical_types.clone(),
            external_files.clone(),
        )
        .into();

        let compiled_file = compile(
            path.clone(),
            &ast,
            tags.into(),
            canonical_types,
            resolved_types.clone(),
        )?;

        compiled_file.into_jni(&mut env)?.as_jni().pipe(Ok)
    })
}
