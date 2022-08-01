use cause_typeproto::vm::{LangVm, RunResult, RuntimeValue};

pub fn expect_type_error(result: &RunResult, vm: &LangVm) {
    let result = match result {
        RunResult::Returned(_) => panic!("Expected a signal"),
        RunResult::Caused(signal) => signal,
    };

    assert_eq!(
        result.type_descriptor.type_id(),
        &vm.get_type_id("core/builtin", "TypeError").unwrap()
    );
    assert!(result.values.len() == 1);
    assert!(match &result.values[0] {
        RuntimeValue::BadValue(_) => true,
        _ => false,
    });
}
