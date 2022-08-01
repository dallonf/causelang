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

    insta::assert_yaml_snapshot!(vm.get_compile_errors(), @r###"
    ---
    - file_path: project/hello.cau
      location:
        - Name: declarations
        - Index: 1
        - Name: body
        - Name: statements
        - Index: 0
        - Name: expression
        - Name: argument
      error:
        MissingArguments:
          - message
    "###);

    let result = vm
        .execute_function("project/hello.cau", "main", &vec![])
        .unwrap();

    expect_type_error(&result, &vm);
    insta::assert_yaml_snapshot!(&result.expect_caused().values, @r###"
    ---
    - BadValue:
        file_path: project/hello.cau
        breadcrumbs:
          - Name: declarations
          - Index: 1
          - Name: body
          - Name: statements
          - Index: 0
          - Name: expression
    "###);
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

    insta::assert_yaml_snapshot!(vm.get_compile_errors(), @r###"
    ---
    - file_path: project/hello.cau
      location:
        - Name: declarations
        - Index: 1
        - Name: body
        - Name: statements
        - Index: 0
        - Name: expression
        - Name: argument
        - Name: arguments
        - Index: 0
      error:
        MismatchedType:
          expected:
            Primitive: String
          actual:
            Primitive: Integer
    "###);

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

    insta::assert_yaml_snapshot!(vm.get_compile_errors(), @r###"
    ---
    - file_path: project/hello.cau
      location:
        - Name: declarations
        - Index: 1
        - Name: body
        - Name: statements
        - Index: 0
        - Name: expression
      error: NotCausable
    "###);

    let result = vm
        .execute_function("project/hello.cau", "main", &vec![])
        .unwrap();
    expect_type_error(&result, &vm);
}

#[test]
fn non_existent_signal() {
    let script = r#"
      function main() {
          cause DoesntExist("oops")
      }
    "#;

    let mut vm = LangVm::new();
    vm.add_file("project/hello.cau", script);

    insta::assert_yaml_snapshot!(vm.get_compile_errors(), @"");

    let result = vm
        .execute_function("project/hello.cau", "main", &vec![])
        .unwrap();
    expect_type_error(&result, &vm);
}
