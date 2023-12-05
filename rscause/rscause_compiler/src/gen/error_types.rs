pub enum LangError {
    NeverResolved(NeverResolvedError),
    NotInScope(NotInScopeError),
    FileNotFound(FileNotFoundError),
    ImportPathInvalid(ImportPathInvalidError),
    ExportNotFound(ExportNotFoundError),
    ProxyError(ProxyErrorError),
    NotCallable(NotCallableError),
    NotCausable(NotCausableError),
    ImplementationTodo(ImplementationTodoError),
    MismatchedType(MismatchedTypeError),
    MissingParameters(MissingParametersError),
    ExcessParameters(ExcessParametersError),
    UnknownParameter(UnknownParameterError),
    MissingElseBranch(MissingElseBranchError),
    UnreachableBranch(UnreachableBranchError),
    ActionIncompatibleWithValueTypes(ActionIncompatibleWithValueTypesError),
    ConstraintUsedAsValue(ConstraintUsedAsValueError),
    ValueUsedAsConstraint(ValueUsedAsConstraintError),
    DoesNotHaveAnyMembers(DoesNotHaveAnyMembersError),
    DoesNotHaveMember(DoesNotHaveMemberError),
    NotVariable(NotVariableError),
    OuterVariable(OuterVariableError),
    CannotBreakHere(CannotBreakHereError),
    NotSupportedInRust(NotSupportedInRustError),
}

pub struct NeverResolvedError {}
pub struct NotInScopeError {}
pub struct FileNotFoundError {}
pub struct ImportPathInvalidError {}
pub struct ExportNotFoundError {}
pub struct ProxyErrorError {}
pub struct NotCallableError {}
pub struct NotCausableError {}
pub struct ImplementationTodoError {}
pub struct MismatchedTypeError {}
pub struct MissingParametersError {}
pub struct ExcessParametersError {}
pub struct UnknownParameterError {}
pub struct MissingElseBranchError {}
pub struct UnreachableBranchError {}
pub struct ActionIncompatibleWithValueTypesError {}
pub struct ConstraintUsedAsValueError {}
pub struct ValueUsedAsConstraintError {}
pub struct DoesNotHaveAnyMembersError {}
pub struct DoesNotHaveMemberError {}
pub struct NotVariableError {}
pub struct OuterVariableError {}
pub struct CannotBreakHereError {}
pub struct NotSupportedInRustError {}
