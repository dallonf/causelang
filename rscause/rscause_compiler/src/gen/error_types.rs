#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize, EnumTryAs)]
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
    MissingElseBranch(MissingElseBranchError),
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

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct ProxyErrorError {
    pub actual_error: Arc<LangError>,
    pub proxy_chain: Vec<ErrorPosition>,
}
#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct ImplementationTodoError {
    pub description: String,
}
#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct MismatchedTypeError {
    pub expected: old_resolving_lang_types::OldResolvingLangType,
    pub actual: Arc<old_resolving_lang_types::OldResolvingLangType>,
}
#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct MissingParametersError {
    pub names: Vec<String>,
}
#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct ExcessParametersError {
    pub expected: u32,
}
#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct MissingElseBranchError {
    pub options: Option<old_resolving_lang_types::OneOfOldResolvingLangType>,
}
#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct UnreachableBranchError {
    pub options: Option<old_resolving_lang_types::OneOfOldResolvingLangType>,
}
#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct ActionIncompatibleWithValueTypesError {
    pub actions: Vec<SourcePosition>,
    pub types: Option<Vec<ActionIncompatibleWithValueTypesValueType>>,
}
#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct ConstraintUsedAsValueError {
    pub r#type: old_resolving_lang_types::OldResolvingLangType,
}
#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct ValueUsedAsConstraintError {
    pub r#type: old_resolving_lang_types::AnyOldResolvingLangType,
}
#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct CompilerBugError {
    pub description: String,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct ActionIncompatibleWithValueTypesValueType {
    pub r#type: Arc<old_resolving_lang_types::OldResolvingLangType>,
    pub position: SourcePosition,
}

