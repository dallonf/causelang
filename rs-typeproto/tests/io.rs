use cause_typeproto::vm::{LangVm, RuntimeValue};

mod common;

#[test]
fn receive_value_from_input_effect() {
    let script = r#"
        import core/string { append }
        import test/io { Print, Prompt }

        function main() {
            cause Print("What is your name?")
            cause Print(append("Hello, ", cause Prompt()))
        }
    "#;
    let mut vm = LangVm::new();
    vm.add_file("project/test.cau", script);
    common::expect_no_compile_errors(&vm);

    let result = vm
        .execute_function("project/test.cau", "main", &vec![])
        .unwrap();
    let result =
        common::expect_valid_caused(&result, &vm.get_type_id("test/io.cau", "Print").unwrap());
    insta::assert_yaml_snapshot!(result, @"");

    let result = vm.resume_execution(RuntimeValue::Action).unwrap();
    common::expect_valid_caused(&result, &vm.get_type_id("test/io.cau", "Prompt").unwrap());
    insta::assert_yaml_snapshot!(result, @"");

    let result = vm
        .resume_execution(RuntimeValue::String("Bob".to_owned().into()))
        .unwrap();
    let result =
        common::expect_valid_caused(&result, &vm.get_type_id("test/io.cau", "Print").unwrap());
    assert_eq!(
        result.values[0],
        RuntimeValue::String("Hello, Bob".to_owned().into())
    );
    insta::assert_yaml_snapshot!(result, @"");
}
