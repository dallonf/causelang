use crate::ast::{
    self, AnyAstNode, AstNode, BreadcrumbTreeNode, BreakExpressionNode,
    FunctionSignatureParameterNode, LoopExpressionNode, NamedValueNode, SingleStatementBodyNode,
};
use crate::breadcrumbs::{Breadcrumbs, HasBreadcrumbs};
use crate::error_types::{
    compiler_bug_error, ActionIncompatibleWithValueTypesError,
    ActionIncompatibleWithValueTypesValueType, CompilerBugError, ErrorPosition,
    ExcessParametersError, ImplementationTodoError, LangError, MismatchedTypeError,
    MissingElseBranchError, MissingParametersError, SourcePosition, UnreachableBranchError,
    ValueUsedAsConstraintError,
};
use crate::infer_types::infer_types;
use crate::lang_types::{
    AnyInferredLangType, CanonicalLangType, CanonicalLangTypeCategory, CanonicalLangTypeId,
    CanonicalTypeField, FunctionLangType, InferredType, InstanceLangType, LangParameter, LangType,
    ObjectCanonicalLangType, OneOfLangType, PrimitiveLangType, SignalCanonicalLangType,
};
use crate::prelude::*;
use crate::tags::NodeTag;
use crate::{find_tag, find_tags};
use serde::{Deserialize, Serialize};
use std::borrow::Cow;
use std::collections::HashMap;
use std::fmt::Debug;
use std::sync::Arc;
use strum::EnumTryAs;

#[derive(Debug, Serialize, Deserialize)]
pub struct ExternalFileDescriptor {
    pub exports: HashMap<Arc<String>, Arc<LangType>>,
}

#[derive(Debug, Clone)]
pub struct ResolveTypesResult {
    pub value_types: HashMap<Breadcrumbs, AnyInferredLangType>,
    pub errors: Vec<ResolverError>,
    pub new_canonical_types: HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>,
}

#[derive(Debug, Clone, Serialize)]
pub struct ResolverError {
    pub position: SourcePosition,
    pub error: LangError,
}
impl ResolverError {
    fn new(source_position: SourcePosition, format: LangError) -> Self {
        Self {
            position: source_position,
            error: format,
        }
    }
}

pub fn resolve_types(
    path: Arc<String>,
    file: Arc<ast::FileNode>,
    node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
    canonical_types: Arc<HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>>,
    external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>>,
) -> ResolveTypesResult {
    let mut ctx = ResolveTypesContext::new(
        path.clone(),
        file.clone(),
        canonical_types,
        node_tags,
        external_files,
    );

    // scan types of all nodes
    let descendants = BreadcrumbTreeNode::from(&file.clone()).descendants();
    for descendant in &descendants {
        descendant.get_resolved_type(&mut ctx);
    }

    infer_types(&mut ctx);

    // Check all edicts of known types
    let edict_errors = ctx
        .edicts
        .iter()
        .flat_map(|edict| {
            let actual_type = ctx
                .value_types
                .get(&edict.breadcrumbs)
                .map(|it| it.as_ref())
                .flatten();
            let source_position = ctx.get_source_position_for_breadcrumbs(&edict.breadcrumbs);
            let actual_type = match actual_type {
                Some(InferredType::Known(it)) => it,
                Some(InferredType::InferenceVariable(var)) => todo!(
                    "Todo: figure out how to resolve a edict on an inference variable ({var})"
                ),
                // we won't check edicts if this type is already an error
                Some(InferredType::Error(_)) => return vec![],
                None => {
                    return vec![ResolverError::new(
                        source_position,
                        LangError::compiler_bug(format!(
                            "No type found when computing edict for {}",
                            &edict.breadcrumbs
                        )),
                    )]
                }
            }
            .clone();
            match &edict.rule {
                TypeEdictRule::AssignableTo(expected_type) => {
                    if let InferredType::Known(expected_type) = expected_type {
                        if !actual_type.is_assignable_to(&expected_type) {
                            vec![ResolverError::new(
                                source_position,
                                LangError::MismatchedType(MismatchedTypeError {
                                    expected: expected_type.as_ref().clone(),
                                    actual: actual_type,
                                }),
                            )]
                        } else {
                            vec![]
                        }
                    } else {
                        vec![]
                    }
                }
                TypeEdictRule::ImplicitValueAssignableTo(rule) => {
                    if let (InferredType::Known(implicit_value), InferredType::Known(assignable_to)) = (&rule.implicit_value, &rule.assignable_to) {
                        if !implicit_value.is_assignable_to(&assignable_to) {
                            vec![ResolverError::new(
                                source_position,
                                LangError::MismatchedType(MismatchedTypeError {
                                    expected: assignable_to.as_ref().clone(),
                                    actual: implicit_value.clone(),
                                }),
                            )]
                        } else {
                            vec![]
                        }
                    } else {
                        vec![]
                    }
                }
                TypeEdictRule::MustBeTypeReference => {
                    if let LangType::TypeReference(_) = actual_type.as_ref() {
                        vec![]
                    } else {
                        vec![ResolverError::new(
                            source_position,
                            LangError::ValueUsedAsConstraint(ValueUsedAsConstraintError {
                                r#type: AnyInferredLangType::Known(actual_type.clone()),
                            }),
                        )]
                    }
                }

                TypeEdictRule::ValidateBranchExpression(edict) => {
                    let mut errors = vec![];
                    let all_branches = {
                        let mut branches = edict.branches.clone();
                        if let Some(else_branch) = &edict.else_branch {
                            branches.push(else_branch.clone());
                        }
                        branches
                    };
                    'branch: for branch in &all_branches {
                        let branch_source_position =
                            ctx.get_source_position_for_breadcrumbs(&branch.breadcrumbs);
                        // Branches unreachable because with-value is NeverContinues
                        if let Some(with_value) = branch
                            .remaining_with_value
                            .as_ref()
                            .and_then(|it| it.try_as_known_ref())
                            .cloned()
                        {
                            if with_value.as_ref() == &LangType::NeverContinues {
                                errors.push(ResolverError::new(
                                    branch_source_position,
                                    LangError::UnreachableBranch(UnreachableBranchError {
                                        options: None,
                                    }),
                                ));
                                continue 'branch;
                            }
                        }

                        // Branches unreachable because with-value is not assignable to
                        // is-pattern.
                        // TODO: Can this be merged with the above?
                        if let (Some(with_value), Some(pattern)) = (
                            branch
                                .remaining_with_value
                                .as_ref()
                                .and_then(|it| it.try_as_known_ref()),
                            branch.pattern.as_ref().and_then(|it| it.try_as_known_ref()),
                        ) {
                            if !pattern.is_assignable_to(&with_value) {
                                errors.push(ResolverError::new(
                                    branch_source_position,
                                    LangError::UnreachableBranch(UnreachableBranchError {
                                        options: Some(
                                            OneOfLangType::new_with_one(with_value.clone().into())
                                                .simplify()
                                                .into(),
                                        ),
                                    }),
                                ));
                            }
                            continue 'branch;
                        }
                    }

                    // A with-value must be NeverContinues at the end of the
                    // branch expression, otherwise an else-branch is needed to make it
                    // exhaustive
                    if let Some(final_with_value) = edict
                        .final_with_value
                        .as_ref()
                        .and_then(|it| it.try_as_known_ref())
                    {
                        if final_with_value.as_ref() != &LangType::NeverContinues {
                            errors.push(ResolverError::new(
                                source_position.clone(),
                                LangError::MissingElseBranch(MissingElseBranchError {
                                    options: Some(
                                        OneOfLangType::new_with_one(
                                            final_with_value.clone().into(),
                                        )
                                        .simplify()
                                        .into(),
                                    ),
                                }),
                            ));
                        }
                    }

                    // If any branch returns Action, all branches must
                    let action_returns = all_branches
                        .iter()
                        .filter(|branch| {
                            branch
                                .result
                                .to_result_assuming_inferred_ref()
                                // sneaky little inversion here -
                                // NeverContinues returns will be excluded with this logic
                                .map(|it| LangType::Action.is_assignable_to(it.as_ref()))
                                .unwrap_or(false)
                        })
                        .collect_vec();
                    let non_action_returns = all_branches
                        .iter()
                        .filter(|branch| {
                            branch
                                .result
                                .try_as_known_ref()
                                .map(|it| !it.as_ref().is_assignable_to(&LangType::Action))
                                .unwrap_or(false)
                        })
                        .collect_vec();
                    if !action_returns.is_empty() && !non_action_returns.is_empty() {
                        errors.push(ResolverError::new(
                            source_position,
                            LangError::ActionIncompatibleWithValueTypes(
                                ActionIncompatibleWithValueTypesError {
                                    actions: action_returns
                                        .into_iter()
                                        .map(|it| {
                                            ctx.get_source_position_for_breadcrumbs(&it.breadcrumbs)
                                        })
                                        .collect(),
                                    types: Some(
                                        non_action_returns
                                            .into_iter()
                                            .map(|it| ActionIncompatibleWithValueTypesValueType {
                                                r#type: it
                                                    .result
                                                    .try_as_known_ref()
                                                    .expect(&format!("should be a Known type (already filtered above), but found {:?}", &it.result))
                                                    .to_owned(),
                                                position: ctx.get_source_position_for_breadcrumbs(
                                                    &it.breadcrumbs,
                                                ),
                                            })
                                            .collect(),
                                    ),
                                },
                            ),
                        ));
                    }

                    errors
                }
            }
        })
        .collect_vec();

    // collect known types for breadcrumbs
    let result = ctx
        .value_types
        .iter()
        .filter_map(|(breadcrumbs, value_type)| {
            value_type
                .as_ref()
                .map(|value_type| (breadcrumbs.clone(), value_type.clone()))
        })
        .collect();

    // collect and report known errors
    let errors = ctx
        .value_types
        .iter()
        .filter_map(|(breadcrumbs, resolved_type)| {
            resolved_type.as_ref().and_then(|resolved_type| {
                let position = BreadcrumbTreeNode::from(&file)
                    .at_path(breadcrumbs)
                    .map_err(|err| {
                        compiler_bug_error(format!(
                            "Couldn't report an error at path {:?}: {}",
                            breadcrumbs, err
                        ))
                    })
                    .and_then(|node| match node {
                        BreadcrumbTreeNode::Node(Some(node)) => Ok(node.info().position.clone()),
                        BreadcrumbTreeNode::Node(None) => Err(compiler_bug_error(format!(
                            "Empty node at path {:?}; couldn't report an error",
                            breadcrumbs
                        ))),
                        BreadcrumbTreeNode::List(_) => Err(compiler_bug_error(format!(
                            "List node at path {:?}; couldn't report an error",
                            breadcrumbs
                        ))),
                    });
                let found_error = match resolved_type {
                    // TODO: Need to handle errors in nested types
                    InferredType::Known(_) => None,
                    InferredType::InferenceVariable(_) => Some(LangError::NeverResolved),
                    InferredType::Error(err) => {
                        if let LangError::ProxyError(_) = err.as_ref() {
                            return None;
                        }

                        Some(err.as_ref().to_owned())
                    }
                };
                found_error.map(|err| match position {
                    Ok(position) => ResolverError {
                        position: SourcePosition {
                            path: path.clone(),
                            breadcrumbs: breadcrumbs.clone(),
                            position,
                        },
                        error: err,
                    },
                    Err(err) => ResolverError {
                        position: SourcePosition {
                            path: path.clone(),
                            breadcrumbs: file.breadcrumbs().clone(),
                            position: file.info().position.clone(),
                        },
                        error: err,
                    },
                })
            })
        })
        .chain(edict_errors.into_iter())
        .collect();

    ResolveTypesResult {
        value_types: result,
        errors,
        new_canonical_types: ctx.new_canonical_types,
    }
}

/// A type "edict" is an explicit declaration that a given node's value
/// must satisfy some rules. They are also used as extra constraints
/// in type inference.
/// ex. a named value must be assignable to its type annotation,
/// a function parameter must fit the function definition.
#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct TypeEdict {
    pub breadcrumbs: Breadcrumbs,
    pub rule: TypeEdictRule,
    pub diagnostic: String,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub enum TypeEdictRule {
    AssignableTo(AnyInferredLangType),
    ImplicitValueAssignableTo(ImplicitValueAssignableToTypeEdict),
    MustBeTypeReference,
    ValidateBranchExpression(ValidateBranchExpressionTypeEdict),
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]

pub struct ImplicitValueAssignableToTypeEdict {
    pub implicit_value: AnyInferredLangType,
    pub assignable_to: AnyInferredLangType,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct ValidateBranchExpressionTypeEdict {
    pub branches: Vec<ValidateBranchExpressionTypeEdictBranch>,
    pub else_branch: Option<ValidateBranchExpressionTypeEdictBranch>,
    pub final_with_value: Option<AnyInferredLangType>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct ValidateBranchExpressionTypeEdictBranch {
    pub breadcrumbs: Breadcrumbs,
    pub remaining_with_value: Option<AnyInferredLangType>,
    pub pattern: Option<AnyInferredLangType>,
    pub result: AnyInferredLangType,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize, EnumTryAs)]
pub enum TypeConstraint {
    EqualTo(AnyInferredLangType),
    AssignableTo(AnyInferredLangType),
    MemberOf(AnyInferredLangType, Arc<String>),
    ReferencedType(AnyInferredLangType),
    ResolveFrom(Breadcrumbs),
    Narrowed(NarrowedConstraint),
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct NarrowedConstraint {
    pub base: AnyInferredLangType,
    pub narrow: AnyInferredLangType,
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub enum ConstraintDiagnostic {
    Unknown,
    PendingInference,
    Resolver(Breadcrumbs, String),
    Inferred(String, Vec<(usize, TypeConstraint, ConstraintDiagnostic)>),
}

pub struct ResolveTypesContext {
    pub file_path: Arc<String>,
    pub root_node: Arc<ast::FileNode>,
    pub canonical_types: Arc<HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>>,
    pub node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
    pub external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>>,

    pub value_types: HashMap<Breadcrumbs, Option<AnyInferredLangType>>,

    pub constraints: Vec<(u64, TypeConstraint, ConstraintDiagnostic)>,
    pub edicts: Vec<TypeEdict>,
    pub next_inference_variable: u64,
    pub new_canonical_types: HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>,
}

impl ResolveTypesContext {
    fn new(
        file_path: Arc<String>,
        root_node: Arc<ast::FileNode>,
        canonical_types: Arc<HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>>,
        node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
        external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>>,
    ) -> Self {
        Self {
            file_path,
            root_node,
            canonical_types: canonical_types.clone(),
            node_tags,
            external_files,

            value_types: HashMap::new(),

            constraints: vec![],
            edicts: vec![],
            next_inference_variable: 0,
            new_canonical_types: HashMap::new(),
        }
    }

    fn node_at_path(&self, breadcrumbs: &Breadcrumbs) -> Result<ast::AnyAstNode, LangError> {
        BreadcrumbTreeNode::from(&self.root_node)
            .at_path(breadcrumbs)
            .map_err(|err| {
                compiler_bug_error(format!(
                    "Couldn't find a node at path {:?}: {}",
                    breadcrumbs, err
                ))
            })
            .and_then(|node| match node {
                BreadcrumbTreeNode::Node(Some(node)) => Ok(node),
                BreadcrumbTreeNode::Node(None) => Err(compiler_bug_error(format!(
                    "Empty node at path {:?}",
                    breadcrumbs
                ))),
                BreadcrumbTreeNode::List(_) => Err(compiler_bug_error(format!(
                    "List node at path {:?}",
                    breadcrumbs
                ))),
            })
    }

    fn get_resolved_type<'a, T>(&mut self, node: &'a T) -> Option<AnyInferredLangType>
    where
        &'a T: Into<AnyAstNode>,
    {
        let node: AnyAstNode = node.into();
        if let Some(already_resolved) = self.value_types.get(node.breadcrumbs()) {
            return already_resolved.clone();
        }
        let resolved = node.compute_type(self);
        self.value_types
            .insert(node.breadcrumbs().clone(), resolved.clone());
        resolved
    }

    pub fn get_resolved_type_proxying_errors<'a, T>(&mut self, node: &'a T) -> AnyInferredLangType
    where
        &'a T: Into<AnyAstNode>,
    {
        self.get_resolved_type(node)
            .map(|found_type| match found_type {
                InferredType::Known(found_type) => InferredType::Known(found_type),
                InferredType::InferenceVariable(var) => InferredType::InferenceVariable(var),
                InferredType::Error(err) => {
                    let source_position = ErrorPosition::Source(SourcePosition {
                        path: self.file_path.clone(),
                        breadcrumbs: node.into().breadcrumbs().clone(),
                        position: node.into().info().position,
                    });
                    InferredType::Error(LangError::proxy_error(err, source_position).into())
                }
            })
            .unwrap_or_else(|| {
                InferredType::Error(
                    LangError::compiler_bug(format!(
                        "no type found for node at {:?}",
                        node.into().breadcrumbs()
                    ))
                    .into(),
                )
            })
    }

    fn get_tags<'a, 'b>(&'a self, node: &impl AstNode) -> Cow<'a, Vec<NodeTag>> {
        self.node_tags
            .get(node.breadcrumbs())
            .map(|it| Cow::Borrowed(it))
            .unwrap_or(Cow::Owned(vec![]))
    }

    fn get_source_position(&self, node: &impl AstNode) -> SourcePosition {
        SourcePosition {
            path: self.file_path.clone(),
            breadcrumbs: node.breadcrumbs().clone(),
            position: node.info().position.clone(),
        }
    }

    fn get_source_position_for_breadcrumbs(&self, breadcrumbs: &Breadcrumbs) -> SourcePosition {
        self.get_source_position(
            &self
                .node_at_path(breadcrumbs)
                .expect("breadcrumbs must be in the tree"),
        )
    }

    fn add_inference_variable(&mut self) -> u64 {
        let new_id = self.next_inference_variable;
        self.next_inference_variable += 1;
        new_id
    }

    pub fn get_canonical_type(
        &self,
        type_id: &CanonicalLangTypeId,
    ) -> Option<Arc<CanonicalLangType>> {
        self.new_canonical_types
            .get(type_id)
            .cloned()
            .or_else(|| self.canonical_types.get(type_id).cloned())
    }
}

trait ResolveTypes: ast::AstNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType>;
    fn get_resolved_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        if let Some(already_resolved) = ctx.value_types.get(self.breadcrumbs()) {
            return already_resolved.clone();
        }
        let resolved = self.compute_type(ctx);
        ctx.value_types
            .insert(self.breadcrumbs().clone(), resolved.clone());
        resolved
    }
    fn get_resolved_type_proxying_errors(
        &self,
        ctx: &mut ResolveTypesContext,
    ) -> Option<AnyInferredLangType> {
        self.get_resolved_type(ctx)
            .map(|found_type| match found_type {
                InferredType::Known(found_type) => InferredType::Known(found_type),
                InferredType::InferenceVariable(var) => InferredType::InferenceVariable(var),
                InferredType::Error(err) => {
                    let source_position = ErrorPosition::Source(SourcePosition {
                        path: ctx.file_path.clone(),
                        breadcrumbs: self.breadcrumbs().clone(),
                        position: self.info().position,
                    });
                    InferredType::Error(LangError::proxy_error(err, source_position).into())
                }
            })
    }
    fn get_tags<'a, 'b>(&'a self, ctx: &'b ResolveTypesContext) -> Cow<'b, Vec<NodeTag>> {
        ctx.node_tags
            .get(&self.breadcrumbs())
            .map(|it| Cow::Borrowed(it))
            .unwrap_or(Cow::Owned(vec![]))
    }
}

impl ResolveTypes for AnyAstNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        match self {
            Self::File(_) => None,
            Self::Identifier(_) => None,
            Self::Import(_) => None,
            Self::ImportPath(_) => None,
            Self::ImportMapping(node) => node.compute_type(ctx),
            Self::Function(node) => node.compute_type(ctx),
            Self::NamedValue(node) => node.compute_type(ctx),
            Self::ObjectType(node) => node.compute_type(ctx),
            Self::SignalType(node) => node.compute_type(ctx),
            Self::ObjectField(node) => node.compute_type(ctx),
            Self::OneOfType(node) => node.compute_type(ctx),
            Self::BlockBody(node) => node.compute_type(ctx),
            Self::DeclarationStatement(node) => node.compute_type(ctx),
            Self::BlockExpression(node) => node.block.compute_type(ctx),
            Self::ExpressionStatement(node) => node.compute_type(ctx),
            Self::EffectStatement(node) => node.compute_type(ctx),
            Self::SetStatement(node) => node.compute_type(ctx),
            Self::CauseExpression(node) => node.compute_type(ctx),
            Self::CallExpression(node) => node.compute_type(ctx),
            Self::MemberExpression(node) => node.compute_type(ctx),
            Self::IdentifierExpression(node) => node.compute_type(ctx),
            Self::StringLiteralExpression(node) => node.compute_type(ctx),
            Self::NumberLiteralExpression(node) => node.compute_type(ctx),
            Self::IdentifierTypeReference(node) => resolve_identifier_type_reference(node, ctx),
            Self::Pattern(node) => {
                let referenced_type = ctx
                    .get_resolved_type_proxying_errors(&node.type_reference)
                    .and_then(|referenced_type| match referenced_type.as_ref() {
                        LangType::TypeReference(it) => it.clone(),
                        _ => LangError::ValueUsedAsConstraint(ValueUsedAsConstraintError {
                            r#type: InferredType::Known(referenced_type),
                        })
                        .into(),
                    });
                Some(referenced_type)
            }
            Self::FunctionSignatureParameter(node) => node.compute_type(ctx),
            Self::FunctionCallParameter(node) => {
                let value_type = ctx.get_resolved_type_proxying_errors(&node.value);
                Some(value_type)
            }
            Self::SingleStatementBody(node) => node.compute_type(ctx),
            Self::BranchExpression(node) => node.compute_type(ctx),
            Self::IfBranchOption(_) => None,
            Self::IsBranchOption(_) => None,
            Self::ElseBranchOption(_) => None,
            Self::LoopExpression(node) => node.compute_type(ctx),
            Self::ReturnExpression(_) => Some(LangType::NeverContinues.into()),
            Self::BreakExpression(node) => node.compute_type(ctx),
        }
    }
}

fn resolve_identifier_type_reference(
    node: &ast::IdentifierTypeReferenceNode,
    ctx: &mut ResolveTypesContext,
) -> Option<InferredType<Arc<LangType>>> {
    let tags = ctx.get_tags(node);
    let reference_tag = find_tag!(&tags, NodeTag::ValueComesFrom);
    let reference_tag = match reference_tag.ok_or(LangError::NotInScope) {
        Ok(it) => it,
        Err(err) => return Some(InferredType::Error(err.into())),
    };
    let source_node = match ctx.node_at_path(&reference_tag.source) {
        Ok(it) => it,
        Err(err) => return Some(InferredType::Error(err.into())),
    };
    let source_node_type = ctx.get_resolved_type_proxying_errors(&source_node);
    ctx.edicts.push(TypeEdict {
        breadcrumbs: node.breadcrumbs().clone(),
        rule: TypeEdictRule::MustBeTypeReference,
        diagnostic: "An IdentifierTypeReference must refer to a type".into(),
    });
    Some(source_node_type)
}

impl ResolveTypes for ast::ImportMappingNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let tags = self.get_tags(ctx);
        let reference_file_tag = find_tag!(&tags, NodeTag::ReferencesFile);
        let export = reference_file_tag
            .ok_or_else(|| {
                LangError::CompilerBug(CompilerBugError {
                    description: format!(
                        "No reference file tag found for import mapping: {:?}",
                        self.breadcrumbs()
                    ),
                })
            })
            .and_then(|tag| {
                ctx.external_files
                    .get(&tag.path)
                    .ok_or_else(|| LangError::FileNotFound)
                    .map(|file| (tag, file))
            })
            .and_then(|(_tag, file)| {
                file.exports
                    .get(&self.source_name.text)
                    .ok_or_else(|| LangError::ExportNotFound)
            });
        Some(
            export
                .map(|export| InferredType::Known(export.clone()))
                .unwrap_or_else(|err| InferredType::Error(err.into())),
        )
    }
}

impl ResolveTypes for ast::FunctionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let name = self.name.text.clone();
        let explicit_return_type = self.return_type.as_ref().map(|it| {
            let type_reference = ctx.get_resolved_type_proxying_errors(it);
            type_reference.and_then(|type_reference| type_reference.get_referenced_value_type())
        });

        let tags = ctx.get_tags(self).as_ref().to_owned();
        let can_return = tags
            .iter()
            .filter_map(|tag| match tag {
                NodeTag::FunctionCanReturnTypeOf(tag) => {
                    let expression_node = ctx
                        .node_at_path(&tag.return_expression_value)
                        .expect("FunctionCanReturnTypeOf tag did not point to a node");
                    let resolved_type = ctx.get_resolved_type_proxying_errors(&expression_node);
                    Some((tag.return_expression_value.clone(), resolved_type))
                }
                NodeTag::FunctionCanReturnAction(tag) => Some((
                    tag.return_expression.clone(),
                    AnyInferredLangType::from(LangType::Action),
                )),
                _ => None,
            })
            .collect_vec();

        let params: Vec<_> = self
            .params
            .iter()
            .map(|param_node| {
                let value_type = param_node
                    .type_reference
                    .as_ref()
                    .map(|it| ctx.get_resolved_type_proxying_errors(it))
                    .unwrap_or_else(|| LangError::NeverResolved.into())
                    .and_then(|it| it.get_referenced_value_type());
                LangParameter {
                    name: param_node.name.text.clone(),
                    value_type,
                }
            })
            .collect();
        if let Some(explicit_return_type) = &explicit_return_type {
            for possible_return in &can_return {
                ctx.edicts.push(TypeEdict {
                    breadcrumbs: possible_return.0.to_owned(),
                    rule: TypeEdictRule::ImplicitValueAssignableTo(
                        ImplicitValueAssignableToTypeEdict {
                            implicit_value: possible_return.1.to_owned(),
                            assignable_to: explicit_return_type.to_owned(),
                        },
                    ),
                    diagnostic: "return expression must be assignable to function's return type"
                        .into(),
                });
            }
            ctx.edicts.push(TypeEdict {
                breadcrumbs: self.body.breadcrumbs().clone(),
                rule: TypeEdictRule::AssignableTo(explicit_return_type.clone()),
                diagnostic: "Result of function body must be assignable to function's return type"
                    .into(),
            });
        }
        let get_inferred_return_type = || {
            let body_type = ctx.get_resolved_type_proxying_errors(&self.body);
            let explicit_returns = can_return.iter().map(|it| it.1.clone()).collect();
            OneOfLangType::new(vec![vec![body_type], explicit_returns].concat()).simplify_to_value()
        };
        let return_type = explicit_return_type.unwrap_or_else(get_inferred_return_type);

        let function_type = FunctionLangType {
            name,
            params,
            return_type,
        };
        Some(function_type.into())
    }
}

impl ResolveTypes for ast::BlockBodyNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let last_statement = self.statements.last();
        let last_statement_type = last_statement
            .map(|it| ctx.get_resolved_type_proxying_errors(it))
            .unwrap_or(LangType::Action.into());
        Some(last_statement_type)
    }
}

impl ResolveTypes for ast::ExpressionStatementNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        ctx.get_resolved_type_proxying_errors(&self.expression)
            .pipe(Some)
    }
}

impl ResolveTypes for ast::EffectStatementNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let result_type: AnyInferredLangType = ctx
            .get_resolved_type_proxying_errors(&self.pattern)
            .and_then(|pattern_type| match pattern_type.as_ref() {
                // TODO: if the pattern is pending when we hit this resolution,
                // this validation probably gets skipped...
                LangType::Instance(instance) => {
                    if instance.type_id.category == CanonicalLangTypeCategory::Signal {
                        let result_type = ctx
                            .get_canonical_type(&instance.type_id)
                            .ok_or(LangError::compiler_bug(format!(
                                "Missing type for {}",
                                instance.type_id.to_string()
                            )))
                            .and_then(|canonical_type| match canonical_type.as_ref() {
                                CanonicalLangType::Signal(signal) => Ok(signal.clone()),
                                _ => Err(LangError::NotCausable),
                            })
                            .map(|signal| signal.result().clone());
                        result_type.unwrap_or_else(|err| InferredType::Error(err.into()))
                    } else {
                        LangError::NotCausable.into()
                    }
                }
                // can't guarantee a result when capturing AnySignal
                // TODO: this is actually probably a bug in the language that you
                // can define effects for signals that require a result via AnySignal
                LangType::AnySignal => InferredType::Known(LangType::Action.into()),
                _ => InferredType::Error(LangError::NotCausable.into()),
            });

        ctx.edicts.push(TypeEdict {
            breadcrumbs: self.body.info().breadcrumbs.clone().into(),
            rule: TypeEdictRule::AssignableTo(result_type),
            diagnostic: "Result of effect handler must be assignable to effect's result".into(),
        });

        return Some(LangType::Action.into());
    }
}

impl ResolveTypes for ast::SetStatementNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let tags = ctx.get_tags(self);
        let tag = match find_tag!(&tags, NodeTag::SetsVariable) {
            Some(tag) => tag,
            None => return Some(LangError::NotInScope.into()),
        };
        let variable_breadcrumbs = tag.variable;
        let variable = ctx
            .root_node
            .clone()
            .conv::<AnyAstNode>()
            .node_at_path(&variable_breadcrumbs)
            .expect("Couldn't find SetsVariable.variable breadcrumbs");
        match variable.try_as_named_value() {
            Some(variable) if variable.is_variable => {}
            _ => return Some(LangError::NotVariable.into()),
        }

        if find_tag!(&tags, NodeTag::UsesCapturedValue).is_some() {
            return Some(LangError::OuterVariable.into());
        }

        let expected_type_var = ctx.add_inference_variable();
        ctx.constraints.push((
            expected_type_var,
            TypeConstraint::ResolveFrom(variable_breadcrumbs.clone()),
            ConstraintDiagnostic::Resolver(
                variable_breadcrumbs.clone(),
                "set statement variable".into(),
            ),
        ));
        ctx.edicts.push(TypeEdict {
            breadcrumbs: self.expression.breadcrumbs().clone(),
            rule: TypeEdictRule::AssignableTo(InferredType::InferenceVariable(expected_type_var)),
            diagnostic: "expression assignable to variable".into(),
        });

        return Some(LangType::Action.into());
    }
}

impl ResolveTypes for ast::CauseExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let canonical_type_id = match ctx
            .get_resolved_type_proxying_errors(&self.signal)
            .to_result_assuming_inferred()
            .and_then(|it| match it.as_ref() {
                LangType::Instance(instance) => Ok(instance.type_id.clone()),
                LangType::TypeReference(instance) => instance
                    .clone()
                    .to_result_assuming_inferred()
                    .and_then(|it| {
                        it.try_as_instance_ref()
                            .cloned()
                            .ok_or(LangError::NotCausable.into())
                    })
                    .and_then(|instance| {
                        if instance.type_id.is_unique {
                            Ok(instance.type_id.clone())
                        } else {
                            Err(LangError::NotCausable.into())
                        }
                    }),
                _ => Err(LangError::NotCausable.into()),
            }) {
            Ok(it) => it,
            Err(err) => return Some(InferredType::Error(err.into())),
        };
        if canonical_type_id.category != CanonicalLangTypeCategory::Signal {
            return Some(LangError::NotCallable.into());
        }
        let signal_result_type = ctx
            .get_canonical_type(&canonical_type_id)
            .ok_or(
                LangError::CompilerBug(CompilerBugError {
                    description: format!(
                        "Couldn't find a canonical symbol: {:?}",
                        &canonical_type_id
                    ),
                })
                .into(),
            )
            .and_then(|canonical_type| match canonical_type.as_ref() {
                CanonicalLangType::Signal(signal_type) => Ok(signal_type.result().clone()),
                _ => Err(LangError::NotCausable.into()),
            })
            .unwrap_or_else(|err: Arc<LangError>| InferredType::Error(err.into()));
        Some(signal_result_type)
    }
}

impl ResolveTypes for ast::CallExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let callee_type = ctx.get_resolved_type_proxying_errors(&self.callee);
        let callee_type = match callee_type.to_result_assuming_inferred() {
            Ok(it) => it,
            Err(err) => return Some(InferredType::Error(err.into())),
        };

        let result_type = match callee_type.as_ref() {
            LangType::Function(function_type) => Ok(function_type.return_type.clone()),
            LangType::TypeReference(referenced_type) => referenced_type
                .clone()
                .to_result_assuming_inferred()
                .and_then(|referenced_type| {
                    let instance_type = match referenced_type.as_ref() {
                        LangType::Instance(instance) => Ok(instance),
                        _ => Err(LangError::NotCallable),
                    }?;
                    Ok(instance_type.clone().into())
                }),
            _ => Err(LangError::NotCallable.into()),
        };

        #[derive(Debug, Clone, Eq, PartialEq)]
        struct CallParameter {
            name: Arc<String>,
            value_type: AnyInferredLangType,
        }

        let parameters = match callee_type.as_ref() {
            LangType::Function(function_type) => Ok(function_type
                .params
                .iter()
                .map(|it| CallParameter {
                    name: it.name.clone(),
                    value_type: it.value_type.clone(),
                })
                .collect_vec()),
            LangType::TypeReference(referenced_type) => referenced_type
                .clone()
                .to_result_assuming_inferred()
                .and_then(|referenced_type| {
                    let instance_type = match referenced_type.as_ref() {
                        LangType::Instance(instance) => Ok(instance),
                        _ => Err(LangError::NotCallable),
                    }?;
                    let canonical_type = ctx.get_canonical_type(&instance_type.type_id).ok_or(
                        LangError::compiler_bug(format!(
                            "Missing canonical type: {}",
                            instance_type.type_id.to_string()
                        )),
                    )?;
                    Ok(canonical_type
                        .fields()
                        .iter()
                        .map(|it| CallParameter {
                            name: it.name.clone(),
                            value_type: it.value_type.clone(),
                        })
                        .collect_vec())
                }),
            _ => Err(LangError::NotCallable.into()),
        };
        match parameters {
            Ok(parameters) => {
                match self.parameters.len().cmp(&parameters.len()) {
                    std::cmp::Ordering::Less => {
                        return Some(
                            LangError::MissingParameters(MissingParametersError {
                                names: parameters[self.parameters.len()..]
                                    .into_iter()
                                    .map(|it| it.name.as_ref().to_owned())
                                    .collect(),
                            })
                            .into(),
                        )
                    }
                    std::cmp::Ordering::Greater => {
                        return Some(
                            LangError::ExcessParameters(ExcessParametersError {
                                expected: parameters.len() as u32,
                            })
                            .into(),
                        )
                    }
                    std::cmp::Ordering::Equal => {}
                }
                for (lang_param, param_node) in parameters.iter().zip(self.parameters.iter()) {
                    ctx.edicts.push(TypeEdict {
                    breadcrumbs: param_node.breadcrumbs().clone(),
                    rule: TypeEdictRule::AssignableTo(lang_param.value_type.clone()),
                    diagnostic:
                        "Function call param must be assignable to function definition param type"
                            .into(),
                });
                }
            }
            Err(err) => return Some(InferredType::Error(err.into())),
        }

        Some(result_type.unwrap_or_else(|err| InferredType::Error(err.into())))
    }
}

impl ResolveTypes for ast::MemberExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let object = ctx.get_resolved_type_proxying_errors(&self.object_expression);
        let var = ctx.add_inference_variable();
        ctx.constraints.push((
            var,
            TypeConstraint::MemberOf(object.clone(), self.member_identifier.text.clone()),
            ConstraintDiagnostic::Resolver(self.breadcrumbs().clone(), "Member expression".into()),
        ));
        return Some(InferredType::InferenceVariable(var));
    }
}

impl ResolveTypes for ast::IdentifierExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let tags = self.get_tags(ctx).as_ref().to_owned();

        let comes_from_tag = match find_tag!(&tags, NodeTag::ValueComesFrom) {
            Some(it) => it,
            None => return Some(LangError::NotInScope.into()),
        };

        if find_tag!(&tags, NodeTag::UsesCapturedValue).is_some() {
            let source_node = ctx
                .root_node
                .clone()
                .conv::<AnyAstNode>()
                .node_at_path(&comes_from_tag.source);
            let source_node = match source_node {
                Ok(it) => it,
                Err(err) => return Some(LangError::compiler_bug(err.to_string()).into()),
            };
            match source_node {
                AnyAstNode::NamedValue(source_node) if source_node.is_variable => {
                    return Some(LangError::OuterVariable.into())
                }
                _ => {}
            }
        };

        let resolved_type = AnyAstNode::from(&ctx.root_node)
            .node_at_path(&comes_from_tag.source)
            .map_err(|err| LangError::compiler_bug(err.to_string()))
            .and_then(|node| {
                node.get_resolved_type_proxying_errors(ctx)
                    .ok_or(LangError::compiler_bug(format!(
                        "no type found for identifier reference: {}",
                        comes_from_tag.source
                    )))
            })
            .unwrap_or_else(|err| err.into());

        // special case: using `Action` as a keyword creates an Action value,
        // not a reference to the `Action` type
        let is_action_reference = resolved_type
            .try_as_known_ref()
            .and_then(|it| it.try_as_type_reference_ref())
            .and_then(|it| it.try_as_known_ref())
            .map(|it| matches!(it.as_ref(), LangType::Action))
            .unwrap_or(false);
        if is_action_reference {
            return Some(LangType::Action.into());
        }

        return Some(resolved_type);
    }
}

impl ResolveTypes for ast::StringLiteralExpressionNode {
    fn compute_type(&self, _ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        Some(LangType::Primitive(PrimitiveLangType::Text).into())
    }
}

impl ResolveTypes for ast::NumberLiteralExpressionNode {
    fn compute_type(&self, _ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        Some(LangType::Primitive(PrimitiveLangType::Number).into())
    }
}

impl ResolveTypes for ast::DeclarationStatementNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        ctx.get_resolved_type_proxying_errors(&self.declaration)
            .pipe(Some)
    }
}

impl ResolveTypes for ast::NamedValueNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let annotated_type = self
            .type_annotation
            .as_ref()
            .map(|it| ctx.get_resolved_type_proxying_errors(it))
            .map(|annotated_type| {
                let annotated_type = annotated_type.to_result_assuming_inferred()?;
                match annotated_type.as_ref() {
                    LangType::TypeReference(InferredType::Known(value_type)) => {
                        Ok(value_type.clone())
                    }
                    LangType::TypeReference(InferredType::Error(err)) => {
                        Err(LangError::proxy_error(
                            err.clone(),
                            ErrorPosition::Source(SourcePosition {
                                breadcrumbs: self.breadcrumbs().clone(),
                                path: ctx.file_path.clone(),
                                position: self.info().position.clone(),
                            }),
                        )
                        .into())
                    }
                    _ => Err(
                        LangError::ValueUsedAsConstraint(ValueUsedAsConstraintError {
                            r#type: AnyInferredLangType::Known(annotated_type.clone()),
                        })
                        .pipe(Arc::new),
                    ),
                }
            });

        let inferred_type = ctx.get_resolved_type_proxying_errors(&self.value);

        if let Some(Ok(annotated_type)) = &annotated_type {
            ctx.edicts.push(TypeEdict {
                breadcrumbs: self.value.breadcrumbs().clone(),
                rule: TypeEdictRule::AssignableTo(annotated_type.clone().into()),
                diagnostic: "A named value must be assignable to its declared type".into(),
            });
        }

        let result = annotated_type
            .map(|annotated_type| annotated_type.into())
            .unwrap_or(inferred_type);
        Some(result)
    }
}

impl ResolveTypes for ast::ObjectTypeNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let canonical_id_tag = ctx
            .get_tags(self)
            .iter()
            .find_map(|it| it.try_as_canonical_id_info_ref().cloned())
            .ok_or(LangError::compiler_bug(format!(
                "Couldn't find CanonicalIdInfo tag at {}",
                self.breadcrumbs()
            )));
        let canonical_id_tag = match canonical_id_tag {
            Ok(it) => it,
            Err(err) => return Some(err.into()),
        };
        let fields = self
            .fields
            .iter()
            .map(|field| {
                let field_type = ctx.get_resolved_type_proxying_errors(field);
                CanonicalTypeField {
                    name: field.name.text.clone(),
                    value_type: field_type,
                }
            })
            .collect_vec();
        let id = Arc::new(CanonicalLangTypeId {
            path: ctx.file_path.clone(),
            parent_name: canonical_id_tag.parent_name.clone(),
            name: self.name.text.clone().into(),
            number: canonical_id_tag.index,
            category: CanonicalLangTypeCategory::Object,
            is_unique: fields.is_empty(),
        });

        ctx.new_canonical_types.insert(
            id.clone(),
            CanonicalLangType::Object(ObjectCanonicalLangType {
                type_id: id.as_ref().to_owned(),
                fields,
            })
            .into(),
        );

        Some(
            LangType::TypeReference(
                LangType::Instance(InstanceLangType {
                    type_id: id.clone(),
                })
                .into(),
            )
            .into(),
        )
    }
}

impl ResolveTypes for ast::SignalTypeNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let canonical_id_tag = ctx
            .get_tags(self)
            .iter()
            .find_map(|it| it.try_as_canonical_id_info_ref().cloned())
            .ok_or(LangError::compiler_bug(format!(
                "Couldn't find CanonicalIdInfo tag at {}",
                self.breadcrumbs()
            )));
        let canonical_id_tag = match canonical_id_tag {
            Ok(it) => it,
            Err(err) => return Some(err.into()),
        };
        let fields = self
            .fields
            .iter()
            .map(|field| {
                let field_type = ctx.get_resolved_type_proxying_errors(field);
                CanonicalTypeField {
                    name: field.name.text.clone(),
                    value_type: field_type,
                }
            })
            .collect_vec();
        let result_type = self
            .result
            .as_ref()
            .map(|it| {
                ctx.get_resolved_type_proxying_errors(it)
                    .try_get_referenced_type()
                    .conv::<AnyInferredLangType>()
            })
            .unwrap_or(LangType::Action.into());
        let id = Arc::new(CanonicalLangTypeId {
            path: ctx.file_path.clone(),
            parent_name: canonical_id_tag.parent_name.clone(),
            name: self.name.text.clone().into(),
            number: canonical_id_tag.index,
            category: CanonicalLangTypeCategory::Signal,
            is_unique: fields.is_empty(),
        });

        ctx.new_canonical_types.insert(
            id.clone(),
            CanonicalLangType::Signal(SignalCanonicalLangType {
                type_id: id.as_ref().to_owned(),
                fields,
                result: result_type,
            })
            .into(),
        );

        Some(
            LangType::TypeReference(
                LangType::Instance(InstanceLangType {
                    type_id: id.clone(),
                })
                .into(),
            )
            .into(),
        )
    }
}

impl ResolveTypes for ast::ObjectFieldNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let type_reference = ctx.get_resolved_type_proxying_errors(&self.type_annotation);
        Some(type_reference.try_get_referenced_type().into())
    }
}

impl ResolveTypes for ast::OneOfTypeNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let options = self
            .options
            .iter()
            .map(|it| {
                let type_reference_var = ctx.add_inference_variable();
                let referenced_type_var = ctx.add_inference_variable();
                ctx.constraints.push((
                    type_reference_var,
                    TypeConstraint::ResolveFrom(it.breadcrumbs().clone()),
                    ConstraintDiagnostic::Resolver(it.breadcrumbs().clone(), "oneof option".into()),
                ));
                ctx.constraints.push((
                    referenced_type_var,
                    TypeConstraint::ReferencedType(InferredType::InferenceVariable(
                        type_reference_var,
                    )),
                    ConstraintDiagnostic::Resolver(it.breadcrumbs().clone(), "oneof option".into()),
                ));
                InferredType::InferenceVariable(referenced_type_var)
            })
            .collect_vec();
        Some(LangType::TypeReference(LangType::OneOf(OneOfLangType { options }).into()).into())
    }
}

impl ResolveTypes for ast::BranchExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        if self.branches.is_empty() {
            return Some(LangType::Action.into());
        }

        let mut branches = Vec::<ValidateBranchExpressionTypeEdictBranch>::new();
        let mut with_value_var = if let Some(with_value_node) = &self.with_value {
            let var = ctx.add_inference_variable();
            ctx.constraints.push((
                var,
                TypeConstraint::ResolveFrom(with_value_node.breadcrumbs().to_owned()),
                ConstraintDiagnostic::Resolver(
                    with_value_node.breadcrumbs().to_owned(),
                    "branch expression with-value".into(),
                ),
            ));
            Some(var)
        } else {
            None
        };

        let branches_before_else = self
            .branches
            .iter()
            .take_while(|branch| {
                if let ast::BranchOptionNode::Else(_) = branch {
                    false
                } else {
                    true
                }
            })
            .collect::<Vec<_>>();
        for branch in branches_before_else.iter().copied() {
            let resolved_type = ctx.get_resolved_type_proxying_errors(branch.body());
            match branch {
                ast::BranchOptionNode::If(_) => {
                    branches.push(ValidateBranchExpressionTypeEdictBranch {
                        breadcrumbs: branch.breadcrumbs().to_owned(),
                        pattern: None,
                        remaining_with_value: with_value_var.map(InferredType::InferenceVariable),
                        result: resolved_type.clone(),
                    });
                }
                ast::BranchOptionNode::Is(branch) => {
                    // TODO: it's probably an error to have an is-branch
                    // without a with-value
                    let pattern_type = ctx.get_resolved_type_proxying_errors(&branch.pattern);
                    branches.push(ValidateBranchExpressionTypeEdictBranch {
                        breadcrumbs: branch.breadcrumbs().to_owned(),
                        pattern: Some(pattern_type.clone()),
                        remaining_with_value: with_value_var.map(InferredType::InferenceVariable),
                        result: resolved_type.clone(),
                    });
                    if let Some(with_value_var) = &mut with_value_var {
                        let prev_with_value_var = *with_value_var;
                        *with_value_var = ctx.add_inference_variable();
                        ctx.constraints.push((
                            *with_value_var,
                            TypeConstraint::Narrowed(NarrowedConstraint {
                                base: InferredType::InferenceVariable(prev_with_value_var),
                                narrow: pattern_type.clone(),
                            }),
                            ConstraintDiagnostic::Resolver(
                                branch.breadcrumbs().to_owned(),
                                "narrowing with-value after is-branch".into(),
                            ),
                        ));
                    }
                }
                ast::BranchOptionNode::Else(_) => unreachable!("there should be no else branches"),
            }
        }

        let else_branch = self.branches.iter().find_map(|it| match it {
            ast::BranchOptionNode::Else(branch) => Some(branch),
            _ => None,
        });
        let mut else_branch_info = None;
        if let Some(else_branch) = else_branch {
            let resolved_type = ctx.get_resolved_type_proxying_errors(&else_branch.body);
            else_branch_info = Some(ValidateBranchExpressionTypeEdictBranch {
                breadcrumbs: else_branch.breadcrumbs().to_owned(),
                remaining_with_value: with_value_var.map(InferredType::InferenceVariable),
                pattern: None,
                result: resolved_type.clone(),
            });
            with_value_var = with_value_var.map(|_| {
                let var = ctx.add_inference_variable();
                ctx.constraints.push((
                    var,
                    TypeConstraint::EqualTo(LangType::NeverContinues.into()),
                    ConstraintDiagnostic::Resolver(
                        else_branch.breadcrumbs().to_owned(),
                        "with-value becomes unaccessible after else-branch".into(),
                    ),
                ));
                var
            });
        }

        let branches_after_else = self
            .branches
            .iter()
            .skip(branches_before_else.len() + 1)
            .collect::<Vec<_>>();
        for branch in branches_after_else.iter().copied() {
            let unreachable_error =
                LangError::UnreachableBranch(UnreachableBranchError { options: None });
            branches.push(ValidateBranchExpressionTypeEdictBranch {
                breadcrumbs: branch.breadcrumbs().to_owned(),
                pattern: None,
                remaining_with_value: Some(LangType::NeverContinues.into()),
                result: unreachable_error.into(),
            });
        }

        ctx.edicts.push(TypeEdict {
            breadcrumbs: self.breadcrumbs().to_owned(),
            rule: TypeEdictRule::ValidateBranchExpression(ValidateBranchExpressionTypeEdict {
                branches: branches.clone(),
                else_branch: else_branch_info.clone(),
                final_with_value: with_value_var.map(InferredType::InferenceVariable),
            }),
            diagnostic: "Branch expression".into(),
        });

        // A branch statement without a with-value always needs an else branch
        if self.with_value.is_none() && !else_branch_info.is_some() {
            return Some(
                LangError::MissingElseBranch(MissingElseBranchError { options: None }).into(),
            );
        }

        let mut result = OneOfLangType::new(branches.into_iter().map(|it| it.result).collect());
        if let Some(else_branch_info) = &else_branch_info {
            result = result.expand(&else_branch_info.result);
        }
        return Some(result.simplify_to_value());
    }
}

impl ResolveTypes for LoopExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let tags = ctx.get_tags(self);
        let breaks = find_tags!(&tags, NodeTag::LoopBreaksAt).collect_vec();

        if breaks.is_empty() {
            return Some(LangType::NeverContinues.into());
        }

        let break_types = breaks
            .iter()
            .map(|it| {
                let break_expression = ctx.node_at_path(&it.break_expression);
                let break_expression_type = break_expression
                    .and_then(|break_expression| {
                        break_expression.try_as_break_expression().ok_or(
                            LangError::compiler_bug(
                                "LoopBreaksAt.break_expression didn't point at a BreakExpression",
                            )
                            .into(),
                        )
                    })
                    .map(|break_expression| {
                        if let Some(with_value) = &break_expression.with_value {
                            ctx.get_resolved_type_proxying_errors(with_value)
                        } else {
                            LangType::Action.into()
                        }
                    })
                    .unwrap_or_else(|it| it.into());

                return (it.break_expression.clone(), break_expression_type);
            })
            .collect_vec();

        // TODO: assert that all or no break types are Action

        let loop_result_type = OneOfLangType::new(
            break_types
                .into_iter()
                .map(|(_, return_type)| return_type)
                .collect(),
        )
        .simplify_to_value();

        Some(loop_result_type)
    }
}

impl ResolveTypes for ast::BreakExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let tags = ctx.get_tags(self);
        let break_tag = find_tag!(&tags, NodeTag::BreaksLoop);
        if break_tag.is_some() {
            return Some(LangType::NeverContinues.into());
        } else {
            return Some(LangError::CannotBreakHere.into());
        }
    }
}

impl ResolveTypes for SingleStatementBodyNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        return ctx
            .get_resolved_type_proxying_errors(&self.statement)
            .pipe(Some);
    }
}

impl ResolveTypes for FunctionSignatureParameterNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        if let Some(type_reference_node) = &self.type_reference {
            let referenced_type = ctx
                .get_resolved_type_proxying_errors(type_reference_node)
                .and_then(|it| it.get_referenced_value_type());
            return Some(referenced_type);
        } else {
            return Some(
                LangError::ImplementationTodo(ImplementationTodoError {
                    description: "Function parameters must have an explicit type".to_string(),
                })
                .into(),
            );
        }
    }
}
