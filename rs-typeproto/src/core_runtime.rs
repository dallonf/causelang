use std::sync::Arc;

use crate::{
    core_descriptors::core_builtin_file,
    types::{CanonicalLangType, ResolvedValueLangType, ValueLangType},
    vm::{RuntimeTypeReference, RuntimeValue},
};

const BUILTINS_FILE: &'static str = "core/builtin.cau";

pub fn get_core_export(file_name: &str, export_name: &str) -> Result<RuntimeValue, String> {
    match (file_name, export_name) {
        (BUILTINS_FILE, "Debug") => {
            let (_, core_global_file) = core_builtin_file();
            let debug = core_global_file.exports.get("Debug").unwrap();

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
            let (_, core_global_file) = core_builtin_file();
            let debug = core_global_file.exports.get("TypeError").unwrap();

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
        (BUILTINS_FILE, export_name) => Err(format!("There is no global named {export_name}.")),
        (file_name, _) => Err(format!("There is no core file called {file_name}.")),
    }
}
