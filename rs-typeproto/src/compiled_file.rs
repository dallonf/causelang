use std::collections::HashMap;

use serde::{Deserialize, Serialize};

use crate::ast::Breadcrumbs;
use crate::instructions::Instruction;
use crate::types::CanonicalLangType;
use crate::vm::RuntimeBadValue;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CompiledFile {
    pub path: String,
    pub types: HashMap<String, CanonicalLangType>,
    pub chunks: Vec<InstructionChunk>,
    pub exports: HashMap<String, CompiledExport>,
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
pub struct InstructionChunk {
    pub constant_table: Vec<CompiledConstant>,
    pub instructions: Vec<Instruction>,
}

impl InstructionChunk {
    pub fn add_constant(&mut self, constant: CompiledConstant) -> usize {
        let existing_index =
            self.constant_table
                .iter()
                .enumerate()
                .find_map(|(i, existing_constant)| {
                    if existing_constant == &constant {
                        Some(i)
                    } else {
                        None
                    }
                });

        if let Some(existing_index) = existing_index {
            existing_index
        } else {
            self.constant_table.push(constant);
            self.constant_table.len() - 1
        }
    }

    pub fn write_instruction(&mut self, instruction: Instruction) {
        self.instructions.push(instruction);
    }

    pub fn write_literal(&mut self, constant: CompiledConstant) {
        let index = self.add_constant(constant);
        self.write_instruction(Instruction::Literal(index));
    }
}

#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub enum CompiledConstant {
    String(String),
    Integer(i64),
    Float(f64),
    Error(RuntimeBadValue),
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum CompiledExport {
    Type(String),
    Chunk(usize),
    Constant(CompiledConstant),
}
