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
    pub actual_error: Arc<LangError>,
    pub proxy_chain: Vec<ErrorPosition>,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct ImplementationTodoError {
    pub description: String,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct MismatchedTypeError {
    pub expected: lang_types::AnyInferredLangType,
    pub actual: lang_types::LangType,
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
    pub options: Option<lang_types::OneOfLangType>,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct ActionIncompatibleWithValueTypesError {
    pub actions: Vec<SourcePosition>,
    pub types: Vec<lang_types::LangType>,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct ConstraintUsedAsValueError {
    pub r#type: lang_types::LangType,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct ValueUsedAsConstraintError {
    pub r#type: lang_types::AnyInferredLangType,
}
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct CompilerBugError {
    pub description: String,
}
