use std::collections::HashMap;

use cause_typeproto::compiled_file::{CompiledExport, CompiledFile};
use cause_typeproto::types::{
    CanonicalLangType, CanonicalLangTypeId, LangParameter, PrimitiveLangType,
    ResolvedValueLangType, SignalCanonicalLangType,
};
use cause_typeproto::vm::{LangVm, RuntimeValue};

mod common;

fn io_file() -> CompiledFile {
    let print_id = CanonicalLangTypeId {
        path: "test/io.cau".into(),
        parent_name: None,
        name: Some("Print".into()),
        number: 0,
    };

    CompiledFile {
        path: "test/io.cau".into(),
        types: HashMap::from_iter(
            vec![(
                print_id.to_owned(),
                CanonicalLangType::Signal(SignalCanonicalLangType {
                    id: print_id.to_owned(),
                    name: "Print".into(),
                    params: vec![LangParameter {
                        name: "message".into(),
                        value_type: ResolvedValueLangType::Primitive(PrimitiveLangType::String)
                            .into(),
                    }],
                    result: Box::new(
                        ResolvedValueLangType::Primitive(PrimitiveLangType::Action).into(),
                    ),
                }),
            )]
            .into_iter(),
        ),
        chunks: vec![],
        exports: HashMap::from_iter(
            vec![(
                "Print".to_owned(),
                CompiledExport::Type(print_id.to_owned()),
            )]
            .into_iter(),
        ),
        resolved: None,
    }
}

#[test]
fn receive_value_from_input_effect() {
    let mut vm = LangVm::new();
    vm.add_compiled_file(io_file());
    vm.add_file(
        "project/test.cau",
        r#"
        import core/string { append }
        import test/io { Print, Prompt }

        function main() {
            cause Print("What is your name?")
            cause Print(append("Hello, ", cause Prompt()))
        }
    "#,
    );
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
