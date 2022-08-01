use cause_typeproto::{
    types::CanonicalLangTypeId,
    vm::{LangVm, RunResult, RuntimeTypeReference, RuntimeValue},
};

#[test]
fn hello_world() {
    let script = r#"
      function main() {
          cause Debug("Hello world!")
      }
    "#;

    let mut vm = LangVm::new();
    vm.add_file("project/hello.cau", script);

    let result = vm.execute_function("project/hello.cau".into(), "main".into());
    if let Ok(RunResult::Cause(signal)) = &result {
        if let RuntimeTypeReference::Signal(signal) = signal.type_descriptor.as_ref() {
            assert_eq!(
                signal.id,
                CanonicalLangTypeId {
                    path: "core/$globals".to_owned(),
                    parent_name: None,
                    name: Some("Debug".to_owned()),
                    number: 0,
                }
            )
        } else {
            panic!("{result:?} is not a signal");
        }
    } else {
        panic!("first result: {result:?}");
    }
    let result = vm.resume_execution(RuntimeValue::Action);
    if let Ok(RunResult::Return(result)) = result {
        assert_eq!(result, RuntimeValue::Action);
    } else {
        panic!("final result: {result:?}");
    }
}
