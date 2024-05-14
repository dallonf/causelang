use std::collections::HashMap;
use std::fs::File;
use std::io::{BufWriter, Write};
use std::sync::Arc;

use jni::objects::{JClass, JObject, JString, JValue, JValueOwned};
use jni::sys::jvalue;
use jni::JNIEnv;
use mapping::{IntoJni, JniInto};
use rscause_compiler::ast::FileNode;
use rscause_compiler::breadcrumbs::Breadcrumbs;
use rscause_compiler::compile::compile;
use rscause_compiler::compiled_file::CompiledFile;
use rscause_compiler::lang_types::{CanonicalLangType, CanonicalLangTypeId};
use rscause_compiler::resolve_types::{resolve_types, ExternalFileDescriptor, ResolverError};
use rscause_compiler::tags::NodeTag;
use serde::{Deserialize, Serialize};
use tap::Pipe;
use util::{jtry, noisy_log};

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
        let ast: Arc<FileNode> = env
            .get_string(&jni_ast_json)?
            .to_str()?
            .pipe(|it| serde_json::from_str(&it))?;
        let canonical_types: Arc<HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>> = env
            .get_string(&jni_canonical_types_json)?
            .to_str()?
            .pipe(|it| serde_json::from_str(&it))?;
        let external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>> = env
            .get_string(&jni_external_files_json)?
            .to_str()?
            .pipe(|it| serde_json::from_str(&it))?;
        let tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>> = env
            .get_string(&jni_tags_json)?
            .to_str()?
            .pipe(|it| serde_json::from_str(&it))?;

        let resolved_types: Arc<_> = resolve_types(
            path.clone(),
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

impl IntoJni for RustCompilerResult {
    fn into_jni<'local>(
        &self,
        env: &mut jni::JNIEnv<'local>,
    ) -> anyhow::Result<JValueOwned<'local>> {
        noisy_log(env, "RustCompilerResult.into_jni");
        let compiled_file = self.compiled_file.into_jni(env)?;
        let errors = self.errors.into_jni(env)?;
        let result = env
            .new_object(
                "com/dallonf/ktcause/RustCompiler$RustCompilerResult",
                "(Lcom/dallonf/ktcause/CompiledFile;Ljava/util/List;)V",
                &[compiled_file.borrow(), errors.borrow()],
            )?
            .into();
        Ok(result)
    }
}
