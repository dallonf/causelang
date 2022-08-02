use std::collections::HashMap;

use cause_typeproto::analyzer;
use cause_typeproto::compiled_file::{
    CompiledConstant, CompiledExport, CompiledFile, InstructionChunk,
};
use cause_typeproto::compiler::{compile, CompilerInput};
use cause_typeproto::instructions::Instruction;
use cause_typeproto::parse;
use cause_typeproto::resolver::{resolve_for_file, ExternalFileDescriptor, FileResolverInput};
use cause_typeproto::types::CanonicalLangTypeId;
use cause_typeproto::types::*;
use cause_typeproto::vm::{LangVm, RuntimeValue};

#[test]
fn analyze_hello_world() {
    let script = r#"
      import langtest/signals { Print }

      function main() {
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
        file_node: &ast_node,
        analyzed: &analyzed_file,
        other_files,
    });
}

#[test]
fn hello_vm() {
    let chunk = InstructionChunk {
        constant_table: vec![
            CompiledConstant::String("Hello world!".into()),
            CompiledConstant::String("core/builtin.cau".into()),
            CompiledConstant::String("Debug".into()),
        ],
        instructions: vec![
            Instruction::Literal(0),
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
        resolved: None,
    };

    let mut vm = LangVm::new();
    vm.add_compiled_file(compiled_file);

    let result = vm.execute_function("project/test.cau", "main", &vec![]);

    println!("caused signal: {result:?}");

    let result = vm.resume_execution(RuntimeValue::Action);

    println!("result: {result:?}");
}

#[test]
fn hello_e2e() {
    let script = r#"
      function main() {
          cause Debug("Hello world!")
      }
    "#;

    let ast_node = parse::parse(script).unwrap();
    let analyzed_file = analyzer::analyze_file(&ast_node);

    let resolved_file = resolve_for_file(FileResolverInput {
        path: "project/test.cau".into(),
        file_node: &ast_node,
        analyzed: &analyzed_file,
        other_files: HashMap::new(),
    });

    let compiled_file = compile(CompilerInput {
        file_node: &ast_node,
        analyzed: &analyzed_file,
        resolved: &resolved_file,
    });

    println!("{:#?}", compiled_file);

    let mut vm = LangVm::new();
    vm.add_compiled_file(compiled_file);
    let result = vm.execute_function("project/test.cau", "main", &vec![]);
    println!("caused signal: {result:?}");
    let result = vm.resume_execution(RuntimeValue::Action);
    println!("result: {result:?}");
}
