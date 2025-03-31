use std::{collections::HashMap, sync::Arc};

use rscause_compiler::{
    ast::FileNode,
    breadcrumbs::Breadcrumbs,
    compile::compile,
    lang_types::{CanonicalLangType, CanonicalLangTypeId},
    old_resolving_lang_types::OldResolvingCanonicalLangType,
    resolve_types::{resolve_types, ExternalFileDescriptor},
    tags::NodeTag,
};
use tap::Pipe;

#[test]
fn test_tmp() {
    let path = Arc::new("project/test.cau".to_owned());
    let ast: Arc<FileNode> = serde_json::from_str(include_str!("fixtures/tmp/ast.json")).unwrap();
    let node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>> =
        serde_json::from_str(include_str!("fixtures/tmp/tags.json")).unwrap();
    let canonical_types: Arc<HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>> =
        serde_json::from_str(include_str!("fixtures/tmp/canonical_types.json")).unwrap();
    let external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>> =
        serde_json::from_str(include_str!("fixtures/tmp/external_files.json")).unwrap();
    let resolve_types_result = resolve_types(
        path,
        ast.clone(),
        node_tags.clone(),
        &canonical_types,
        external_files.clone(),
    )
    .pipe(Arc::new);
    let compile_result = compile(
        Arc::new("project/test.cau".to_owned()),
        &ast,
        node_tags,
        canonical_types,
        resolve_types_result.clone(),
    )
    .pipe(Arc::new);
    println!("resolve_types_result: {:#?}", resolve_types_result);
    println!("compile_result: {:#?}", compile_result);
}
