use cause_typeproto::types::{LangTypeError, PrimitiveLangType, ResolvedValueLangType};
use cause_typeproto::vm::LangVm;

use crate::common::expect_type_error;

mod common;

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
        .unwrap();
    expect_type_error(&result, &vm);
}

#[test]
fn mistyped_argument() {
    let script = r#"
      function main() {
          cause Debug(1)
      }
    "#;

    let mut vm = LangVm::new();
    vm.add_file("project/hello.cau", script);

    if vm.get_compile_errors().len() != 1
        || !vm.get_compile_errors().iter().any(|err| {
            err.error
                == LangTypeError::MismatchedType {
                    expected: ResolvedValueLangType::Primitive(PrimitiveLangType::String),
                    actual: ResolvedValueLangType::Primitive(PrimitiveLangType::Integer),
                }
        })
    {
        panic!("Wrong errors: {:#?}", vm.get_compile_errors());
    }

    let result = vm
        .execute_function("project/hello.cau", "main", &vec![])
        .unwrap();
    expect_type_error(&result, &vm);
}

#[test]
fn cause_non_signal() {
    let script = r#"
      function main() {
          cause "oops"
      }
    "#;

    let mut vm = LangVm::new();
    vm.add_file("project/hello.cau", script);

    if vm.get_compile_errors().len() != 1
        || !vm
            .get_compile_errors()
            .iter()
            .any(|err| err.error == LangTypeError::NotCausable)
    {
        panic!("Wrong errors: {:#?}", vm.get_compile_errors());
    }

    let result = vm
        .execute_function("project/hello.cau", "main", &vec![])
        .unwrap();
    expect_type_error(&result, &vm);
}
