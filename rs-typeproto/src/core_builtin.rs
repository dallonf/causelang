use std::collections::HashMap;

use crate::resolver::ExternalFileDescriptor;
use crate::types::*;

pub fn core_builtin_file() -> (String, ExternalFileDescriptor) {
    let filename = "core/builtin";
    let descriptor = ExternalFileDescriptor {
        exports: HashMap::from_iter(vec![
            (
                "String".to_owned(),
                ValueLangType::Resolved(ResolvedValueLangType::PrimitiveType(
                    PrimitiveLangType::String,
                )),
            ),
            (
                "Integer".to_owned(),
                ValueLangType::Resolved(ResolvedValueLangType::PrimitiveType(
                    PrimitiveLangType::Integer,
                )),
            ),
            (
                "Float".to_owned(),
                ValueLangType::Resolved(ResolvedValueLangType::PrimitiveType(
                    PrimitiveLangType::Float,
                )),
            ),
            (
                "Action".to_owned(),
                ValueLangType::Resolved(ResolvedValueLangType::PrimitiveType(
                    PrimitiveLangType::Action,
                )),
            ),
            (
                "Debug".to_owned(),
                ValueLangType::Resolved(ResolvedValueLangType::Canonical(
                    CanonicalLangType::Signal(SignalCanonicalLangType {
                        id: CanonicalLangTypeId {
                            path: "core/builtin".to_owned(),
                            parent_name: None,
                            name: Some("Debug".to_owned()),
                            number: 0,
                        },
                        name: "Debug".to_owned(),
                        params: vec![LangParameter {
                            name: "message".to_owned(),
                            value_type: ValueLangType::Resolved(
                                ResolvedValueLangType::Primitive(PrimitiveLangType::String),
                            ),
                        }],
                        result: Box::new(ValueLangType::Resolved(
                            ResolvedValueLangType::Primitive(PrimitiveLangType::Action),
                        )),
                    }),
                )),
            ),
            (
                "TypeError".to_owned(),
                ValueLangType::Resolved(ResolvedValueLangType::Canonical(
                    CanonicalLangType::Signal(SignalCanonicalLangType {
                        id: CanonicalLangTypeId {
                            path: "core/builtin".to_owned(),
                            parent_name: None,
                            name: Some("TypeError".to_owned()),
                            number: 0,
                        },
                        name: "TypeError".to_owned(),
                        params: vec![LangParameter {
                            name: "error".to_owned(),
                            value_type: ValueLangType::Resolved(ResolvedValueLangType::BadValue),
                        }],
                        result: Box::new(ValueLangType::Resolved(
                            ResolvedValueLangType::NeverContinues,
                        )),
                    }),
                )),
            ),
        ]),
    };
    (filename.to_owned(), descriptor)
}
