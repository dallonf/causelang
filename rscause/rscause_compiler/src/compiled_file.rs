use std::collections::HashMap;
use std::sync::Arc;

use num::BigRational;
use serde::{Serialize, Serializer};

use crate::ast::NodeInfo;
use crate::error_types::{ErrorPosition, LangError};
use crate::instructions::{Instruction, InstructionPhase};
use crate::lang_types::CanonicalLangTypeId;
use crate::old_resolving_lang_types::{
    AnyOldResolvingLangType, CanonicalLangType, FunctionOldResolvingLangType, OldResolvingLangType,
    OldResolvingType,
};

#[derive(Debug, Clone, Serialize)]
pub struct CompiledFile {
    pub path: Arc<String>,
    pub types: Arc<HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>>,
    pub procedures: Vec<Procedure>,
    pub exports: HashMap<Arc<String>, CompiledExport>,
}

#[derive(Debug, Clone, Serialize)]
pub struct Procedure {
    pub identity: ProcedureIdentity,
    pub constant_table: Vec<CompiledConstant>,
    pub instructions: Vec<Instruction>,
    pub source_map: Option<Vec<Option<ProcedureInstructionMapping>>>,
}

#[derive(Debug, Clone, Serialize)]
pub enum ProcedureIdentity {
    Function(FunctionProcedureIdentity),
    Effect(EffectProcedureIdentity),
}

#[derive(Debug, Clone, Serialize)]
pub struct FunctionProcedureIdentity {
    pub name: Option<Arc<String>>,
    pub declaration: NodeInfo,
}

#[derive(Debug, Clone, Serialize)]
pub struct EffectProcedureIdentity {
    pub matches_type: Arc<OldResolvingLangType>,
    pub declaration: NodeInfo,
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize)]
pub struct ProcedureInstructionMapping {
    pub node_info: NodeInfo,
    pub phase: InstructionPhase,
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize)]
pub enum CompiledConstant {
    String(Arc<String>),
    #[serde(serialize_with = "serialize_big_rational")]
    Number(BigRational),
    Error(ErrorConst),
    Type(Arc<OldResolvingLangType>),
}

fn serialize_big_rational<S>(value: &BigRational, serializer: S) -> Result<S::Ok, S::Error>
where
    S: Serializer,
{
    serializer.serialize_str(&value.to_string())
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize)]
pub struct ErrorConst {
    pub source_position: ErrorPosition,
    pub error: Arc<LangError>,
}

#[derive(Debug, Clone, Serialize)]
pub enum CompiledExport {
    Function {
        procedure_index: u32,
        function_type: OldResolvingType<Arc<FunctionOldResolvingLangType>>,
    },
    Type(AnyOldResolvingLangType),
    Error(Arc<LangError>),
}
