use std::collections::HashMap;

use cause_typeproto::analyzer;
use cause_typeproto::compiled_file::{
    CompiledConstant, CompiledExport, CompiledFile, InstructionChunk,
};
use cause_typeproto::instructions::Instruction;
use cause_typeproto::parse;
use cause_typeproto::resolver::{resolve_for_file, ExternalFileDescriptor, FileResolverInput};
use cause_typeproto::types::CanonicalLangTypeId;
use cause_typeproto::types::*;
use cause_typeproto::vm::LangVm;

#[test]
fn hello_world() {
    let script = r#"
      import langtest/signals { Print }

      fn main() {
          let val: Integer = "howdy"
          let excess = "oh no"
          cause Print(val, excess)
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

#[test]
fn hello_vm() {
    let chunk = InstructionChunk {
        constant_table: vec![
            CompiledConstant::String("Hello world!".into()),
            CompiledConstant::String("core/$globals".into()),
            CompiledConstant::String("Debug".into()),
        ],
        instructions: vec![
            Instruction::Constant(0),
            Instruction::Import {
                file_path_constant: 1,
                export_name_constant: 2,
            },
            Instruction::Construct,
            Instruction::Cause,
            Instruction::Return,
        ],
    };
    let compiled_file = CompiledFile {
        path: "project/test.cau".into(),
        types: HashMap::new(),
        chunks: vec![chunk],
        exports: HashMap::from_iter(vec![("main".into(), CompiledExport::Chunk(0))]),
    };

    let mut vm = LangVm::new();
    vm.add_compiled_file(compiled_file);

    let result = vm.execute_function("project/test.cau".into(), "main".into());

    println!("{result:?}");
}
