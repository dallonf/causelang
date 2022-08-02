use std::sync::Arc;

use cause_typeproto::{
    types::CanonicalLangTypeId,
    vm::{LangVm, RunResult, RuntimeBadValue, RuntimeObject, RuntimeValue},
};

pub fn expect_no_compile_errors(vm: &LangVm) {
    let errors = vm.get_compile_errors();
    if errors.len() != 0 {
        panic!("compile errors: {errors:#?}");
    }
}

pub fn expect_type_error(result: &RunResult, vm: &LangVm) -> RuntimeBadValue {
    let result = match result {
        RunResult::Returned(_) => panic!("Expected a signal"),
        RunResult::Caused(signal) => signal,
    };

    assert_eq!(
        result.type_descriptor.type_id(),
        &vm.get_type_id("core/builtin.cau", "TypeError").unwrap()
    );
    assert!(result.values.len() == 1);
    match &result.values[0] {
        RuntimeValue::BadValue(bad_value) => bad_value.to_owned(),
        it => panic!("{:?} should be a BadValue", it),
    }
}

pub fn expect_invalid_caused(result: &RunResult) -> RuntimeBadValue {
    let result = match result {
        RunResult::Returned(_) => panic!("Expected a signal"),
        RunResult::Caused(signal) => signal,
    };

    let validated = result.as_ref().to_owned().validate();

    validated.as_bad_value().expect("Expected a bad value")
}

pub fn expect_valid_caused(
    result: &RunResult,
    expected_type: &CanonicalLangTypeId,
) -> Arc<RuntimeObject> {
    let signal = result
        .to_owned()
        .expect_caused()
        .as_ref()
        .to_owned()
        .validate()
        .as_object()
        .expect("invalid signal");

    assert_eq!(signal.type_descriptor.type_id(), expected_type);
    signal
}
