#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub enum Instructions {
    NoOp(NoOpInstruction),
    Pop(PopInstruction),
    Swap(SwapInstruction),
    PopScope(PopScopeInstruction),
    RegisterEffect(RegisterEffectInstruction),
    PopEffects(PopEffectsInstruction),
    PushAction(PushActionInstruction),
    Literal(LiteralInstruction),
    Import(ImportInstruction),
    ImportSameFile(ImportSameFileInstruction),
    DefineFunction(DefineFunctionInstruction),
    ReadLocal(ReadLocalInstruction),
    WriteLocal(WriteLocalInstruction),
    ReadLocalThroughEffectScope(ReadLocalThroughEffectScopeInstruction),
    WriteLocalThroughEffectScope(WriteLocalThroughEffectScopeInstruction),
    Construct(ConstructInstruction),
    CallFunction(CallFunctionInstruction),
    GetMember(GetMemberInstruction),
    NameValue(NameValueInstruction),
    IsAssignableTo(IsAssignableToInstruction),
    Jump(JumpInstruction),
    JumpIfFalse(JumpIfFalseInstruction),
    StartLoop(StartLoopInstruction),
    ContinueLoop(ContinueLoopInstruction),
    BreakLoop(BreakLoopInstruction),
    Cause(CauseInstruction),
    RejectSignal(RejectSignalInstruction),
    FinishEffect(FinishEffectInstruction),
    Return(ReturnInstruction),
}

#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct NoOpInstruction {
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct PopInstruction {
    pub number: i32,
}
/// Reverses the top two items on the stack
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct SwapInstruction {
}
/// Pops a number of values while preserving the top of the stack
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct PopScopeInstruction {
    pub values: i32,
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct RegisterEffectInstruction {
    pub procedure_index: i32,
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct PopEffectsInstruction {
    pub number: i32,
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct PushActionInstruction {
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct LiteralInstruction {
    pub constant: i32,
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct ImportInstruction {
    pub file_path_constant: i32,
    pub export_name_constant: i32,
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct ImportSameFileInstruction {
    pub export_name_constant: i32,
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct DefineFunctionInstruction {
    pub procedure_index: i32,
    pub type_constant: i32,
    pub captured_values: i32,
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct ReadLocalInstruction {
    pub index: i32,
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct WriteLocalInstruction {
    pub index: i32,
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct ReadLocalThroughEffectScopeInstruction {
    pub effect_depth: i32,
    pub index: i32,
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct WriteLocalThroughEffectScopeInstruction {
    pub effect_depth: i32,
    pub index: i32,
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct ConstructInstruction {
    pub arity: i32,
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct CallFunctionInstruction {
    pub arity: i32,
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct GetMemberInstruction {
    pub index: i32,
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct NameValueInstruction {
    pub name_constant: i32,
    pub variable: Option<bool>,
    pub local_index: Option<i32>,
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct IsAssignableToInstruction {
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct JumpInstruction {
    pub instruction: i32,
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct JumpIfFalseInstruction {
    pub instruction: i32,
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct StartLoopInstruction {
    pub end_instruction: i32,
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct ContinueLoopInstruction {
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct BreakLoopInstruction {
    pub levels: i32,
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct CauseInstruction {
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct RejectSignalInstruction {
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct FinishEffectInstruction {
}
#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
pub struct ReturnInstruction {
}
