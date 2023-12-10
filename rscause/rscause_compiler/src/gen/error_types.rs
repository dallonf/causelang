#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub enum LangError {
    NeverResolved,
    NotInScope,
    FileNotFound,
    ImportPathInvalid,
    ExportNotFound,
    ProxyError(ProxyErrorError),
    NotCallable,
    NotCausable,
    ImplementationTodo(ImplementationTodoError),
    MismatchedType(MismatchedTypeError),
    MissingParameters(MissingParametersError),
    ExcessParameters(ExcessParametersError),
    UnknownParameter,
    MissingElseBranch,
    UnreachableBranch(UnreachableBranchError),
    ActionIncompatibleWithValueTypes(ActionIncompatibleWithValueTypesError),
    ConstraintUsedAsValue(ConstraintUsedAsValueError),
    ValueUsedAsConstraint(ValueUsedAsConstraintError),
    DoesNotHaveAnyMembers,
    DoesNotHaveMember,
    NotVariable,
    OuterVariable,
    CannotBreakHere,
    NotSupportedInRust,
    CompilerBug(CompilerBugError),
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct ProxyErrorError {
    pub actual_error: LangType,
    pub proxy_chain: Vec<ErrorPosition>,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct ImplementationTodoError {
    pub description: String,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct MismatchedTypeError {
    pub expected: AnyInferredLangType,
    pub actual: LangType,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct MissingParametersError {
    pub names: Vec<String>,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct ExcessParametersError {
    pub expected: u32,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct UnreachableBranchError {
    pub options: Option<OneOfLangType>,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct ActionIncompatibleWithValueTypesError {
    pub actions: Vec<SourcePosition>,
    pub types: Vec<LangType>,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct ConstraintUsedAsValueError {
    pub r#type: LangType,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct ValueUsedAsConstraintError {
    pub r#type: AnyInferredLangType,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct CompilerBugError {
    pub description: String,
}
