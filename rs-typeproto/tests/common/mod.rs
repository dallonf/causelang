use cause_typeproto::vm::{LangVm, RunResult, RuntimeBadValue, RuntimeValue};

pub fn expect_type_error(result: &RunResult, vm: &LangVm) -> RuntimeBadValue {
    let result = match result {
        RunResult::Returned(_) => panic!("Expected a signal"),
        RunResult::Caused(signal) => signal,
    };

    assert_eq!(
        result.type_descriptor.type_id(),
        &vm.get_type_id("core/builtin", "TypeError").unwrap()
    );
    assert!(result.values.len() == 1);
    match &result.values[0] {
        RuntimeValue::BadValue(bad_value) => bad_value.to_owned(),
        it => panic!("{:?} should be a BadValue", it),
    }
}

pub fn expect_invalid_cause(result: &RunResult) -> RuntimeBadValue {
    let result = match result {
        RunResult::Returned(_) => panic!("Expected a signal"),
        RunResult::Caused(signal) => signal,
    };

    let validated = result.as_ref().to_owned().validate();

    validated.as_bad_value().expect("Expected a bad value")
}
