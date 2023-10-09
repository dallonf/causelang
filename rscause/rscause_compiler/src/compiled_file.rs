use std::sync::Arc;

use crate::ast::NodeInfo;
use crate::instructions::Instruction;
use crate::lang_types::{FunctionLangType, InferredType};

#[derive(Debug, Clone)]
pub struct CompiledFile {
    pub path: Arc<String>,
    // TODO: pub types
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
    pub declaration: NodeInfo,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum CompiledConstant {
    String(Arc<String>),
}

#[derive(Debug, Clone)]
pub enum CompiledExport {
    Function {
        procedure_index: u32,
        function_type: InferredType<Arc<FunctionLangType>>,
    },
}
