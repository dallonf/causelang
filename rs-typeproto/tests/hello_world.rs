use cause_typeproto::vm::{LangVm, RuntimeValue};

#[test]
fn hello_world() {
    let script = r#"
      function main() {
          cause Debug("Hello world!")
      }
    "#;

    let mut vm = LangVm::new();
    vm.add_file("project/hello.cau", script);

    let result = vm
        .execute_function("project/hello.cau", "main", &vec![])
        .unwrap()
        .expect_caused();
    assert_eq!(
        result.type_descriptor.type_id(),
        &vm.get_type_id("core/builtin", "Debug").unwrap()
    );
    assert_eq!(
        result.values,
        vec![RuntimeValue::String("Hello world!".to_owned().into())]
    );

    let result = vm
        .resume_execution(RuntimeValue::Action)
        .unwrap()
        .expect_returned();
    assert_eq!(result, RuntimeValue::Action);
}
