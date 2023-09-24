use std::sync::Arc;

#[derive(Debug, Clone)]
pub struct CompiledFile {
    pub path: Arc<String>,
    // pub types
    pub procedures: Vec<Procedure>,
    // pub exports
}

#[derive(Debug, Clone)]
pub struct Procedure {
    pub identity: ProcedureIdentity,
    // pub constantTable
    // pub instructions
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
