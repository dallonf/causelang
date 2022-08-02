use cause_typeproto::vm::LangVm;

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

    insta::assert_debug_snapshot!(vm.get_compile_errors(), @r###"
    [
        ResolverError {
            file_path: "project/hello.cau",
            location: Breadcrumbs(
                "declarations.1.body.statements.0.expression.argument",
            ),
            error: MissingArguments(
                [
                    "message",
                ],
            ),
        },
    ]
    "###);

    let result = vm
        .execute_function("project/hello.cau", "main", &vec![])
        .unwrap();

    let bad_value = common::expect_type_error(&result, &vm);
    insta::assert_debug_snapshot!(vm.get_error_from_bad_value(&bad_value).unwrap(), @r###"
    ErrorTrace {
        file_path: "project/hello.cau",
        breadcrumbs: Breadcrumbs(
            "declarations.1.body.statements.0.expression.argument",
        ),
        error: MissingArguments(
            [
                "message",
            ],
        ),
        proxy_chain: [
            Breadcrumbs(
                "declarations.1.body.statements.0.expression",
            ),
        ],
    }
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

    insta::assert_debug_snapshot!(vm.get_compile_errors(), @r###"
    [
        ResolverError {
            file_path: "project/hello.cau",
            location: Breadcrumbs(
                "declarations.1.body.statements.0.expression.argument.arguments.0",
            ),
            error: MismatchedType {
                expected: Primitive(
                    String,
                ),
                actual: Primitive(
                    Integer,
                ),
            },
        },
    ]
    "###);

    let result = vm
        .execute_function("project/hello.cau", "main", &vec![])
        .unwrap();
    let result = common::expect_invalid_caused(&result);
    insta::assert_debug_snapshot!(vm.get_error_from_bad_value(&result).unwrap(), @r###"
    ErrorTrace {
        file_path: "project/hello.cau",
        breadcrumbs: Breadcrumbs(
            "declarations.1.body.statements.0.expression.argument.arguments.0",
        ),
        error: MismatchedType {
            expected: Primitive(
                String,
            ),
            actual: Primitive(
                Integer,
            ),
        },
        proxy_chain: [],
    }
    "###);
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

    insta::assert_debug_snapshot!(vm.get_compile_errors(), @r###"
    [
        ResolverError {
            file_path: "project/hello.cau",
            location: Breadcrumbs(
                "declarations.1.body.statements.0.expression",
            ),
            error: NotCausable,
        },
    ]
    "###);

    let result = vm
        .execute_function("project/hello.cau", "main", &vec![])
        .unwrap();
    let bad_value = common::expect_type_error(&result, &vm);
    insta::assert_debug_snapshot!(vm.get_error_from_bad_value(&bad_value).unwrap(), @r###"
    ErrorTrace {
        file_path: "project/hello.cau",
        breadcrumbs: Breadcrumbs(
            "declarations.1.body.statements.0.expression",
        ),
        error: NotCausable,
        proxy_chain: [],
    }
    "###);
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

    insta::assert_debug_snapshot!(vm.get_compile_errors(), @r###"
    [
        ResolverError {
            file_path: "project/hello.cau",
            location: Breadcrumbs(
                "declarations.1.body.statements.0.expression.argument.callee",
            ),
            error: NotInScope,
        },
    ]
    "###);

    let result = vm
        .execute_function("project/hello.cau", "main", &vec![])
        .unwrap();
    let bad_value = common::expect_type_error(&result, &vm);
    insta::assert_debug_snapshot!(vm.get_error_from_bad_value(&bad_value).unwrap(), @r###"
    ErrorTrace {
        file_path: "project/hello.cau",
        breadcrumbs: Breadcrumbs(
            "declarations.1.body.statements.0.expression.argument.callee",
        ),
        error: NotInScope,
        proxy_chain: [
            Breadcrumbs(
                "declarations.1.body.statements.0.expression",
            ),
            Breadcrumbs(
                "declarations.1.body.statements.0.expression.argument",
            ),
        ],
    }
    "###);
}
