use std::collections::HashMap;

use serde::{Deserialize, Serialize};

use crate::instructions::Instruction;
use crate::types::CanonicalLangType;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CompiledFile {
    pub path: String,
    pub types: HashMap<String, CanonicalLangType>,
    pub chunks: Vec<InstructionChunk>,
    pub exports: HashMap<String, CompiledExport>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct InstructionChunk {
    pub constant_table: Vec<CompiledConstant>,
    pub instructions: Vec<Instruction>,
}

#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub enum CompiledConstant {
    String(String),
    Integer(isize),
    Float(f64),
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum CompiledExport {
    Type(String),
    Chunk(usize),
    Constant(CompiledConstant),
}
