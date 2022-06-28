use std::collections::HashMap;

use cause_typeproto::analyzer;
use cause_typeproto::parse;
use cause_typeproto::resolver::{resolve_for_file, ExternalFileDescriptor};
use cause_typeproto::types::*;
use cause_typeproto::{resolver::FileResolverInput, types::CanonicalLangTypeId};

#[test]
fn hello_world() {
    let script = r#"
      import langtest/signals { Print }

      fn main() {
          let val: Integer = "howdy"
          cause Print(val)
      }
    "#;

    let ast_node = parse::parse(script).unwrap();
    let analyzed_file = analyzer::analyze_file(&ast_node);

    println!("node tags: {:#?}", analyzed_file.node_tags);

    let other_files = HashMap::from_iter(vec![(
        "langtest/signals".to_string(),
        ExternalFileDescriptor {
            exports: HashMap::from_iter(vec![(
                "Print".to_string(),
                ValueLangType::Resolved(ResolvedValueLangType::Canonical(
                    CanonicalLangType::Signal(SignalCanonicalLangType {
                        id: CanonicalLangTypeId {
                            path: "langtest/signals".into(),
                            parent_name: None,
                            name: Some("Print".into()),
                            number: 0,
                        },
                        name: "Print".into(),
                        params: vec![LangParameter {
                            name: "message".into(),
                            value_type: ValueLangType::Resolved(ResolvedValueLangType::Primitive(
                                PrimitiveLangType::String,
                            )),
                        }],
                        result: Box::new(ValueLangType::Resolved(
                            ResolvedValueLangType::Primitive(PrimitiveLangType::Action),
                        )),
                    }),
                )),
            )]),
        },
    )]);

    resolve_for_file(FileResolverInput {
        path: "test.cau".into(),
        file_node: ast_node,
        source: script.to_owned(),
        analyzed: analyzed_file,
        other_files,
    });
}
