use std::sync::Arc;

use crate::{
    core_descriptors::core_builtin_file,
    types::{CanonicalLangType, ResolvedValueLangType, ValueLangType},
    vm::{RuntimeNativeFunction, RuntimeTypeReference, RuntimeValue},
};

const BUILTINS_FILE: &'static str = "core/builtin.cau";
const STRING_FILE: &'static str = "core/string.cau";

pub fn get_core_export(file_name: &str, export_name: &str) -> Result<RuntimeValue, String> {
    match (file_name, export_name) {
        (BUILTINS_FILE, "Debug") => {
            let (_, core_builtin_file) = core_builtin_file();
            let debug = core_builtin_file.exports.get("Debug").unwrap();

            let signal_type = match debug {
                ValueLangType::Resolved(ResolvedValueLangType::Canonical(
                    CanonicalLangType::Signal(signal_type),
                )) => signal_type,
                _ => panic!("Debug wasn't a Signal"),
            }
            .to_owned();

            Ok(RuntimeValue::TypeReference(Arc::new(
                RuntimeTypeReference::Signal(signal_type),
            )))
        }
        (BUILTINS_FILE, "TypeError") => {
            let (_, core_builtin_file) = core_builtin_file();
            let debug = core_builtin_file.exports.get("TypeError").unwrap();

            let signal_type = match debug {
                ValueLangType::Resolved(ResolvedValueLangType::Canonical(
                    CanonicalLangType::Signal(signal_type),
                )) => signal_type,
                _ => panic!("TypeError wasn't a Signal"),
            }
            .to_owned();

            Ok(RuntimeValue::TypeReference(Arc::new(
                RuntimeTypeReference::Signal(signal_type),
            )))
        }
        (BUILTINS_FILE, "AssumptionBroken") => {
            let (_, core_builtin_file) = core_builtin_file();
            let debug = core_builtin_file.exports.get("AssumptionBroken").unwrap();

            let signal_type = match debug {
                ValueLangType::Resolved(ResolvedValueLangType::Canonical(
                    CanonicalLangType::Signal(signal_type),
                )) => signal_type,
                _ => panic!("AssumptionBroken wasn't a Signal"),
            }
            .to_owned();

            Ok(RuntimeValue::TypeReference(Arc::new(
                RuntimeTypeReference::Signal(signal_type),
            )))
        }
        (BUILTINS_FILE, export_name) => Err(format!("There is no builtin named {export_name}.")),

        (STRING_FILE, "append") => {
            fn append_string(params: &[RuntimeValue]) -> Result<RuntimeValue, String> {
                let val1 = match &params[0] {
                    RuntimeValue::String(string) => string,
                    _ => return Err("I was expecting the inputs to append to be strings.".into()),
                };
                let val2 = match &params[1] {
                    RuntimeValue::String(string) => string,
                    _ => return Err("I was expecting the inputs to append to be strings.".into()),
                };

                Ok(RuntimeValue::String(Arc::new(format!("{val1}{val2}"))))
            }
            Ok(RuntimeValue::NativeFunction(RuntimeNativeFunction {
                name: "appendString".to_owned().into(),
                function: append_string,
            }))
        }
        (STRING_FILE, export_name) => Err(format!(
            "There is no export named {export_name} in {STRING_FILE}."
        )),

        (file_name, _) => Err(format!("There is no core file called {file_name}.")),
    }
}
