use std::{collections::HashMap, sync::Arc};

use rscause_compiler::{
    ast::FileNode,
    breadcrumbs::Breadcrumbs,
    compile::compile,
    lang_types::{CanonicalLangType, CanonicalLangTypeId},
    resolve_types::{resolve_types, ExternalFileDescriptor},
    tags::NodeTag,
};
use tap::Pipe;

#[test]
fn test_tmp() {
    let ast: Arc<FileNode> =
        serde_lexpr::from_str(include_str!("fixtures/tmp/ast.txt")).unwrap();
    let node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>> =
        serde_lexpr::from_str(include_str!("fixtures/tmp/tags.txt")).unwrap();
    let canonical_types: Arc<HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>> =
        serde_lexpr::from_str(include_str!("fixtures/tmp/canonical_types.txt")).unwrap();
    let external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>> =
        serde_lexpr::from_str(include_str!("fixtures/tmp/external_files.txt")).unwrap();
    let resolve_types_result = resolve_types(
        ast.clone(),
        node_tags.clone(),
        canonical_types.clone(),
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
    println!("{:#?}", resolve_types_result);
    println!("{:#?}", compile_result);
}
