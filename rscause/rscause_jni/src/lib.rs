use std::collections::HashMap;
use std::sync::Arc;

use jni::objects::{JClass, JObject, JString, JValueOwned};
use jni::sys::jvalue;
use jni::JNIEnv;
use mapping::{strict_transfer_jstring, JniInto};
use rscause_compiler::ast::FileNode;
use rscause_compiler::breadcrumbs::Breadcrumbs;
use rscause_compiler::compile::compile;
use rscause_compiler::compiled_file::{CompiledFile, ExternalFileDescriptor};
use rscause_compiler::lang_types::{CanonicalLangType, CanonicalLangTypeId};
use rscause_compiler::old_resolver::resolve_types::resolve_types;
use rscause_compiler::resolver::ResolverError;
use rscause_compiler::tags::NodeTag;
use serde::Serialize;
use tap::Pipe;
use util::jtry;

mod mapping;
mod util;

#[no_mangle]
pub extern "system" fn Java_com_dallonf_ktcause_RustCompiler_compileInner<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    jni_path: JObject<'local>,
    jni_ast_json: JString<'local>,
    jni_tags_json: JString<'local>,
    jni_canonical_types_json: JString<'local>,
    jni_external_files_json: JString<'local>,
) -> jvalue {
    jtry(&mut env, move |mut env| {
        let path: Arc<String> = jni_path.jni_into(&mut env)?;

        let ast: Arc<FileNode> = strict_transfer_jstring(&mut env, &jni_ast_json)?
            .pipe(|it| serde_json::from_str(&it))?;
        let canonical_types: Arc<HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>> =
            strict_transfer_jstring(&mut env, &jni_canonical_types_json)?
                .pipe(|it| serde_json::from_str(&it))?;
        let external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>> =
            strict_transfer_jstring(&mut env, &jni_external_files_json)?
                .pipe(|it| serde_json::from_str(&it))?;
        let tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>> =
            strict_transfer_jstring(&mut env, &jni_tags_json)?
                .pipe(|it| serde_json::from_str(&it))?;

        let resolved_types: Arc<_> = resolve_types(
            path.clone(),
            ast.clone(),
            tags.clone(),
            canonical_types.as_ref(),
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

        let result = RustCompilerResult {
            compiled_file,
            errors: resolved_types.errors.clone(),
        };

        let result_json = serde_json::to_string_pretty(&result)?;
        let result_json_jni = env.new_string(result_json)?;
        Ok(JValueOwned::Object(result_json_jni.into()).as_jni())
    })
}

#[derive(Debug, Clone, Serialize)]
struct RustCompilerResult {
    pub compiled_file: CompiledFile,
    pub errors: Vec<ResolverError>,
}
