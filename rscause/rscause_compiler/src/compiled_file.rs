use std::collections::HashMap;
use std::sync::Arc;

use num::BigRational;

use crate::ast::NodeInfo;
use crate::error_types::{ErrorPosition, LangError};
use crate::instructions::Instruction;
use crate::lang_types::{FunctionLangType, InferredType, AnyInferredLangType, LangType};

#[derive(Debug, Clone)]
pub struct CompiledFile {
    pub path: Arc<String>,
    // TODO: pub types
    pub procedures: Vec<Procedure>,
    pub exports: HashMap<Arc<String>, CompiledExport>,
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
    Number(BigRational),
    Error(ErrorConst),
    Type(Arc<LangType>),
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ErrorConst {
    pub source_position: ErrorPosition,
    pub error: Arc<LangError>,
}

#[derive(Debug, Clone)]
pub enum CompiledExport {
    Function {
        procedure_index: u32,
        function_type: InferredType<Arc<FunctionLangType>>,
    },
}
