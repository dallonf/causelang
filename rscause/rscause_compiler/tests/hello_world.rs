use std::sync::Arc;

use rscause_compiler::resolve_types::resolve_types;

#[test]
fn test_hello_world() {
    let result = resolve_types(
        Arc::new("project/test.cau".to_owned()),
        serde_json::from_str(include_str!("fixtures/hello_world/ast.json")).unwrap(),
        serde_json::from_str(include_str!("fixtures/hello_world/tags.json")).unwrap(),
        serde_json::from_str(include_str!("fixtures/hello_world/canonical_types.json")).unwrap(),
        serde_json::from_str(include_str!("fixtures/hello_world/external_files.json")).unwrap(),
    );
    println!("{:#?}", result);
}
