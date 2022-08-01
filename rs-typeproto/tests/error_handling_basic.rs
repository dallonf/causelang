use cause_typeproto::{types::LangTypeError, vm::LangVm};

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

    println!("{result:?}");
}
