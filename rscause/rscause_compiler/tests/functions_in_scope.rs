use std::{collections::HashMap, sync::Arc};

use rscause_compiler::{
    ast,
    breadcrumbs::Breadcrumbs,
    compile::compile,
    lang_types::{CanonicalLangType, CanonicalLangTypeId},
    resolve_types::{resolve_types, ExternalFileDescriptor},
    tags::NodeTag,
};

#[test]
fn test_functions_in_scope() {
    let ast: Arc<ast::FileNode> =
        serde_lexpr::from_str(include_str!("fixtures/functions_in_scope/ast.txt")).unwrap();
    let tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>> =
        serde_lexpr::from_str(include_str!("fixtures/functions_in_scope/tags.txt")).unwrap();
    let canonical_types: Arc<HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>> =
        serde_lexpr::from_str(include_str!(
            "fixtures/functions_in_scope/canonical_types.txt"
        ))
        .unwrap();
    let external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>> = serde_lexpr::from_str(
        include_str!("fixtures/functions_in_scope/external_files.txt"),
    )
    .unwrap();
    let types = resolve_types(
        ast.clone(),
        tags.clone(),
        canonical_types.clone(),
        external_files.clone(),
    );
    let compiled = compile(
        "project/test.cau".to_string().into(),
        ast.as_ref(),
        tags,
        canonical_types,
        types.into(),
    )
    .unwrap();
    println!("{:#?}", compiled);
}
