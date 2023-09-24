use std::sync::Arc;

use crate::instructions::Instruction;

#[derive(Debug, Clone)]
pub struct CompiledFile {
    pub path: Arc<String>,
    // pub types
    pub procedures: Vec<Procedure>,
    pub exports: Vec<CompiledExport>,
}

#[derive(Debug, Clone)]
pub struct Procedure {
    pub identity: ProcedureIdentity,
    pub constant_table: Vec<CompiledConstant>,
    pub instructions: Vec<Instruction>,
}

#[derive(Debug, Clone)]
pub enum ProcedureIdentity {
    Function(FunctionProcedureIdentity),
}

#[derive(Debug, Clone)]
pub struct FunctionProcedureIdentity {
    pub name: Arc<String>,
    // declaration
}

#[derive(Debug, Clone)]
pub enum CompiledConstant {
    String(Arc<String>),
}

#[derive(Debug, Clone)]
pub enum CompiledExport {
    Function {
        procedure_index: u32,
        //type
    },
}
