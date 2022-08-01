use cause_typeproto::{
    types::LangTypeError,
    vm::{LangVm, RuntimeValue},
};

#[test]
fn no_arguments_for_signal() {
    let script = r#"
      function main() {
          cause Debug()
      }
    "#;

    let mut vm = LangVm::new();
    vm.add_file("project/hello.cau", script);

    if vm.get_compile_errors().len() != 1
        || !vm
            .get_compile_errors()
            .iter()
            .any(|err| err.error == LangTypeError::MissingArguments(vec!["message".into()]))
    {
        panic!("Wrong errors: {:#?}", vm.get_compile_errors());
    }

    let result = vm
        .execute_function("project/hello.cau", "main", &vec![])
        .unwrap()
        .expect_caused();

    assert_eq!(
        result.type_descriptor.type_id(),
        &vm.get_type_id("core/builtin", "TypeError").unwrap()
    );
    assert!(result.values.len() == 1);
    assert!(match &result.values[0] {
        RuntimeValue::BadValue(_) => true,
        _ => false,
    });
}
