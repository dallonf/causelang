use std::sync::Arc;

use rscause_compiler::resolve_types::resolve_types;

#[test]
fn test_hello_world() {
    let result = resolve_types(
        Arc::new("project/test.cau".to_owned()),
        serde_lexpr::from_str(include_str!("fixtures/hello_world/ast.txt")).unwrap(),
        serde_lexpr::from_str(include_str!("fixtures/hello_world/tags.txt")).unwrap(),
        serde_lexpr::from_str(include_str!("fixtures/hello_world/canonical_types.txt")).unwrap(),
        serde_lexpr::from_str(include_str!("fixtures/hello_world/external_files.txt")).unwrap(),
    );
    println!("{:#?}", result);
}
