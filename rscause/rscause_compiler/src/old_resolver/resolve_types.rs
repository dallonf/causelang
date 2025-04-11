use super::infer_types::infer_types;
use super::old_resolving_lang_types::{
    AnyOldResolvingLangType, FunctionOldResolvingLangType, InstanceOldResolvingLangType,
    ObjectOldResolvingCanonicalLangType, OldResolvingCanonicalLangType,
    OldResolvingCanonicalTypeField, OldResolvingLangParameter, OldResolvingLangType,
    OldResolvingType, OneOfOldResolvingLangType, SignalOldResolvingCanonicalLangType,
};
use crate::ast::{
    self, AnyAstNode, AstNode, BreadcrumbTreeNode, ExpressionNode, FunctionSignatureParameterNode,
    LoopExpressionNode,
};
use crate::breadcrumbs::{Breadcrumbs, HasBreadcrumbs};
use crate::compiled_file::ExternalFileDescriptor;
use crate::error_types::{
    compiler_bug_error, ActionIncompatibleWithValueTypesError,
    ActionIncompatibleWithValueTypesValueType, CompilerBugError, ErrorPosition,
    ExcessParametersError, ImplementationTodoError, LangError, MismatchedTypeError,
    MissingElseBranchError, MissingParametersError, SourcePosition, UnreachableBranchError,
    ValueUsedAsConstraintError,
};
use crate::lang_types::{self, CanonicalLangTypeCategory, CanonicalLangTypeId, PrimitiveLangType};
use crate::prelude::*;
use crate::resolver::{ResolveTypesResult, ResolverError};
use crate::tags::NodeTag;
use crate::util::arc_into;
use crate::{find_tag, find_tags};
use serde::{Deserialize, Serialize};
use std::borrow::Cow;
use std::collections::HashMap;
use std::fmt::Debug;
use std::sync::Arc;
use strum::EnumTryAs;

pub fn resolve_types(
    path: Arc<String>,
    file: Arc<ast::FileNode>,
    node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
    canonical_types: &HashMap<Arc<CanonicalLangTypeId>, Arc<lang_types::CanonicalLangType>>,
    external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>>,
) -> ResolveTypesResult {
    let mut ctx = ResolveTypesContext::new(
        path.clone(),
        file.clone(),
        canonical_types
            .iter()
            .map(|(k, v)| (k.to_owned(), arc_into(v)))
            .collect(),
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
                Some(OldResolvingType::Known(it)) => it,
                Some(OldResolvingType::InferenceVariable(var)) => todo!(
                    "Todo: figure out how to resolve a edict on an inference variable ({var})"
                ),
                // we won't check edicts if this type is already an error
                Some(OldResolvingType::Error(_)) => return vec![],
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
                    if let OldResolvingType::Known(expected_type) = expected_type {
                        if !actual_type.is_assignable_to(&expected_type) {
                            vec![ResolverError::new(
                                source_position,
                                LangError::MismatchedType(MismatchedTypeError {
                                    expected: expected_type.as_ref().clone().into(),
                                    actual: Arc::new(actual_type.as_ref().to_owned().into()),
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
                    if let (OldResolvingType::Known(implicit_value), OldResolvingType::Known(assignable_to)) = (&rule.implicit_value, &rule.assignable_to) {
                        if !implicit_value.is_assignable_to(&assignable_to) {
                            vec![ResolverError::new(
                                source_position,
                                LangError::MismatchedType(MismatchedTypeError {
                                    expected: assignable_to.as_ref().clone().into(),
                                    actual: Arc::new(implicit_value.as_ref().to_owned().into()),
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
                    if let OldResolvingLangType::TypeReference(_) = actual_type.as_ref() {
                        vec![]
                    } else {
                        vec![ResolverError::new(
                            source_position,
                            LangError::ValueUsedAsConstraint(ValueUsedAsConstraintError {
                                r#type: Ok(Arc::new(actual_type.as_ref().to_owned().into())),
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
                            if with_value.as_ref() == &OldResolvingLangType::NeverContinues {
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
                                            OneOfOldResolvingLangType::new_with_one(with_value.clone().into())
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
                        if final_with_value.as_ref() != &OldResolvingLangType::NeverContinues {
                            errors.push(ResolverError::new(
                                source_position.clone(),
                                LangError::MissingElseBranch(MissingElseBranchError {
                                    options: Some(
                                        OneOfOldResolvingLangType::new_with_one(
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
                                .map(|it| OldResolvingLangType::Action.is_assignable_to(it.as_ref()))
                                .unwrap_or(false)
                        })
                        .collect_vec();
                    let non_action_returns = all_branches
                        .iter()
                        .filter(|branch| {
                            branch
                                .result
                                .try_as_known_ref()
                                .map(|it| !it.as_ref().is_assignable_to(&OldResolvingLangType::Action))
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
                                                r#type: Arc::new(it
                                                    .result
                                                    .try_as_known_ref()
                                                    .expect(&format!("should be a Known type (already filtered above), but found {:?}", &it.result))
                                                    .as_ref()
                                                    .to_owned()
                                                    .into()
                                                ),
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
                .map(|value_type| (breadcrumbs.clone(), value_type.to_owned().into()))
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
                    OldResolvingType::Known(_) => None,
                    OldResolvingType::InferenceVariable(_) => Some(LangError::NeverResolved),
                    OldResolvingType::Error(err) => {
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
        new_canonical_types: ctx
            .new_canonical_types
            .into_iter()
            .map(|it| (it.0, arc_into(&it.1)))
            .collect(),
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
    AssignableTo(AnyOldResolvingLangType),
    ImplicitValueAssignableTo(ImplicitValueAssignableToTypeEdict),
    MustBeTypeReference,
    ValidateBranchExpression(ValidateBranchExpressionTypeEdict),
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]

pub struct ImplicitValueAssignableToTypeEdict {
    pub implicit_value: AnyOldResolvingLangType,
    pub assignable_to: AnyOldResolvingLangType,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct ValidateBranchExpressionTypeEdict {
    pub branches: Vec<ValidateBranchExpressionTypeEdictBranch>,
    pub else_branch: Option<ValidateBranchExpressionTypeEdictBranch>,
    pub final_with_value: Option<AnyOldResolvingLangType>,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct ValidateBranchExpressionTypeEdictBranch {
    pub breadcrumbs: Breadcrumbs,
    pub remaining_with_value: Option<AnyOldResolvingLangType>,
    pub pattern: Option<AnyOldResolvingLangType>,
    pub result: AnyOldResolvingLangType,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize, EnumTryAs)]
pub enum TypeConstraint {
    EqualTo(AnyOldResolvingLangType),
    AssignableTo(AnyOldResolvingLangType),
    MemberOf(AnyOldResolvingLangType, Arc<String>),
    ReferencedType(AnyOldResolvingLangType),
    ResolveFrom(Breadcrumbs),
    Narrowed(NarrowedConstraint),
    UnreachableIfNeverContinues(AnyOldResolvingLangType),
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct NarrowedConstraint {
    pub base: AnyOldResolvingLangType,
    pub narrow: AnyOldResolvingLangType,
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
    pub canonical_types: HashMap<Arc<CanonicalLangTypeId>, Arc<OldResolvingCanonicalLangType>>,
    pub node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
    pub external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>>,

    pub value_types: HashMap<Breadcrumbs, Option<AnyOldResolvingLangType>>,

    pub constraints: Vec<(u64, TypeConstraint, ConstraintDiagnostic)>,
    pub edicts: Vec<TypeEdict>,
    pub next_inference_variable: u64,
    pub new_canonical_types: HashMap<Arc<CanonicalLangTypeId>, Arc<OldResolvingCanonicalLangType>>,
}

impl ResolveTypesContext {
    fn new(
        file_path: Arc<String>,
        root_node: Arc<ast::FileNode>,
        canonical_types: HashMap<Arc<CanonicalLangTypeId>, Arc<OldResolvingCanonicalLangType>>,
        node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
        external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>>,
    ) -> Self {
        Self {
            file_path,
            root_node,
            canonical_types,
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

    fn get_resolved_type<'a, T>(&mut self, node: &'a T) -> Option<AnyOldResolvingLangType>
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

    pub fn get_resolved_type_proxying_errors<'a, T>(
        &mut self,
        node: &'a T,
    ) -> AnyOldResolvingLangType
    where
        &'a T: Into<AnyAstNode>,
    {
        self.get_resolved_type(node)
            .map(|found_type| match found_type {
                OldResolvingType::Known(found_type) => OldResolvingType::Known(found_type),
                OldResolvingType::InferenceVariable(var) => {
                    OldResolvingType::InferenceVariable(var)
                }
                OldResolvingType::Error(err) => {
                    let source_position = ErrorPosition::Source(SourcePosition {
                        path: self.file_path.clone(),
                        breadcrumbs: node.into().breadcrumbs().clone(),
                        position: node.into().info().position,
                    });
                    OldResolvingType::Error(LangError::proxy_error(err, source_position).into())
                }
            })
            .unwrap_or_else(|| {
                OldResolvingType::Error(
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
    ) -> Option<Arc<OldResolvingCanonicalLangType>> {
        self.new_canonical_types
            .get(type_id)
            .cloned()
            .or_else(|| self.canonical_types.get(type_id).cloned())
    }
}

trait ResolveTypes: ast::AstNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType>;
    fn get_resolved_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
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
    ) -> Option<AnyOldResolvingLangType> {
        self.get_resolved_type(ctx)
            .map(|found_type| match found_type {
                OldResolvingType::Known(found_type) => OldResolvingType::Known(found_type),
                OldResolvingType::InferenceVariable(var) => {
                    OldResolvingType::InferenceVariable(var)
                }
                OldResolvingType::Error(err) => {
                    let source_position = ErrorPosition::Source(SourcePosition {
                        path: ctx.file_path.clone(),
                        breadcrumbs: self.breadcrumbs().clone(),
                        position: self.info().position,
                    });
                    OldResolvingType::Error(LangError::proxy_error(err, source_position).into())
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
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
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
            Self::ExpressionStatement(node) => node.compute_type(ctx),
            Self::EffectStatement(node) => node.compute_type(ctx),
            Self::SetExpression(node) => node.compute_type(ctx),
            Self::CauseExpression(node) => node.compute_type(ctx),
            Self::CallExpression(node) => node.compute_type(ctx),
            Self::PipeCallExpression(node) => node.compute_type(ctx),
            Self::MemberExpression(node) => node.compute_type(ctx),
            Self::IdentifierExpression(node) => node.compute_type(ctx),
            Self::StringLiteralExpression(node) => node.compute_type(ctx),
            Self::NumberLiteralExpression(node) => node.compute_type(ctx),
            Self::IdentifierTypeReference(node) => resolve_identifier_type_reference(node, ctx),
            Self::FunctionTypeReference(node) => node.compute_type(ctx),
            Self::Pattern(node) => {
                let referenced_type = ctx
                    .get_resolved_type_proxying_errors(&node.type_reference)
                    .and_then(|referenced_type| match referenced_type.as_ref() {
                        OldResolvingLangType::TypeReference(it) => it.clone(),
                        _ => LangError::ValueUsedAsConstraint(ValueUsedAsConstraintError {
                            r#type: Ok(Arc::new(referenced_type.as_ref().to_owned().into())),
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
            Self::SingleExpressionBody(node) => node.compute_type(ctx),
            Self::GroupExpression(node) => {
                Some(ctx.get_resolved_type_proxying_errors(&node.expression))
            }
            Self::BlockExpression(node) => node.block.compute_type(ctx),
            Self::FunctionExpression(node) => node.compute_type(ctx),
            Self::BranchExpression(node) => node.compute_type(ctx),
            Self::IfBranchOption(_) => None,
            Self::IsBranchOption(_) => None,
            Self::ElseBranchOption(_) => None,
            Self::LoopExpression(node) => node.compute_type(ctx),
            Self::ReturnExpression(_) => Some(OldResolvingLangType::NeverContinues.into()),
            Self::BreakExpression(node) => node.compute_type(ctx),
        }
    }
}

fn resolve_identifier_type_reference(
    node: &ast::IdentifierTypeReferenceNode,
    ctx: &mut ResolveTypesContext,
) -> Option<OldResolvingType<Arc<OldResolvingLangType>>> {
    let tags = ctx.get_tags(node);
    let reference_tag = find_tag!(&tags, NodeTag::ValueComesFrom);
    let reference_tag = match reference_tag.ok_or(LangError::NotInScope) {
        Ok(it) => it,
        Err(err) => return Some(OldResolvingType::Error(err.into())),
    };
    let source_node = match ctx.node_at_path(&reference_tag.source) {
        Ok(it) => it,
        Err(err) => return Some(OldResolvingType::Error(err.into())),
    };
    let source_node_type = ctx.get_resolved_type_proxying_errors(&source_node);
    ctx.edicts.push(TypeEdict {
        breadcrumbs: node.breadcrumbs().clone(),
        rule: TypeEdictRule::MustBeTypeReference,
        diagnostic: "An IdentifierTypeReference must refer to a type".into(),
    });
    Some(source_node_type)
}

impl ResolveTypes for ast::FunctionTypeReferenceNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
        let return_type_var = ctx.add_inference_variable();
        let return_type = ctx.get_resolved_type_proxying_errors(&self.return_type);
        ctx.constraints.push((
            return_type_var,
            TypeConstraint::ReferencedType(return_type),
            ConstraintDiagnostic::Resolver(
                self.breadcrumbs().clone(),
                "Function type reference return type".into(),
            ),
        ));
        let params = self
            .params
            .iter()
            .map(|param| {
                let inference_var = ctx.add_inference_variable();
                let param_type = param
                    .type_reference
                    .as_ref()
                    .map(|it| ctx.get_resolved_type_proxying_errors(it));
                if let Some(param_type) = param_type {
                    ctx.constraints.push((
                        inference_var,
                        TypeConstraint::ReferencedType(param_type),
                        ConstraintDiagnostic::Resolver(
                            param.breadcrumbs().clone(),
                            "Function type reference parameter type".into(),
                        ),
                    ));
                }
                OldResolvingLangParameter {
                    name: param.name.text.clone(),
                    value_type: OldResolvingType::InferenceVariable(inference_var).into(),
                }
            })
            .collect();

        Some(
            OldResolvingLangType::TypeReference(
                FunctionOldResolvingLangType {
                    name: None,
                    params,
                    return_type: OldResolvingType::InferenceVariable(return_type_var).into(),
                }
                .into(),
            )
            .into(),
        )
    }
}

impl ResolveTypes for ast::ImportMappingNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
        let tags = self.get_tags(ctx);

        let comes_from_tag = find_tag!(&tags, NodeTag::ValueComesFrom);
        let bad_file_tag = comes_from_tag
            .and_then(|comes_from_tag| ctx.node_tags.get(&comes_from_tag.source))
            .and_then(|source_tags| find_tag!(&source_tags, NodeTag::BadFileReference));
        if bad_file_tag.is_some() {
            return Some(LangError::ImportPathInvalid.into());
        }

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
                .map(|export| OldResolvingType::Known(arc_into(export)))
                .unwrap_or_else(|err| OldResolvingType::Error(err.into())),
        )
    }
}

fn compute_function_type(
    name_node: Option<&ast::IdentifierNode>,
    param_nodes: &[Arc<FunctionSignatureParameterNode>],
    return_type_node: Option<&ast::TypeReferenceNode>,
    tags: &Vec<NodeTag>,
    ctx: &mut ResolveTypesContext,
) -> Option<AnyOldResolvingLangType> {
    let name = name_node.map(|it| it.text.clone());
    let explicit_return_type = return_type_node.map(|it| {
        let type_reference = ctx.get_resolved_type_proxying_errors(it);
        type_reference.and_then(|type_reference| type_reference.get_referenced_value_type())
    });

    let can_return = tags
        .iter()
        .filter_map(|tag| match tag {
            NodeTag::FunctionCanReturnTypeOf(tag) => {
                let return_type_var = ctx.add_inference_variable();
                ctx.constraints.push((
                    return_type_var,
                    TypeConstraint::ResolveFrom(tag.return_expression_value.to_owned()),
                    ConstraintDiagnostic::Resolver(
                        tag.return_expression_value.to_owned(),
                        "Function return value".to_owned(),
                    ),
                ));
                let return_type = AnyOldResolvingLangType::InferenceVariable(return_type_var);
                Some((tag.return_expression_value.clone(), return_type))
            }
            NodeTag::FunctionCanReturnAction(tag) => Some((
                tag.return_expression.clone(),
                AnyOldResolvingLangType::from(OldResolvingLangType::Action),
            )),
            _ => None,
        })
        .collect_vec();

    let params: Vec<_> = param_nodes
        .iter()
        .map(|param_node| {
            let value_type = param_node
                .type_reference
                .as_ref()
                .map(|it| ctx.get_resolved_type_proxying_errors(it))
                .unwrap_or_else(|| LangError::NeverResolved.into())
                .and_then(|it| it.get_referenced_value_type());
            OldResolvingLangParameter {
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
                diagnostic: "return value must be assignable to function's return type".into(),
            });
        }
    }
    let get_inferred_return_type = || {
        let explicit_returns = can_return.iter().map(|it| it.1.clone()).collect();
        OneOfOldResolvingLangType::new(explicit_returns).simplify_to_value()
    };
    let return_type = explicit_return_type.unwrap_or_else(get_inferred_return_type);

    let function_type = FunctionOldResolvingLangType {
        name,
        params,
        return_type,
    };
    Some(function_type.into())
}

impl ResolveTypes for ast::FunctionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
        let tags = ctx.get_tags(self).as_ref().to_owned();
        compute_function_type(
            Some(&self.name),
            &self.params,
            self.return_type.as_ref(),
            &tags,
            ctx,
        )
    }
}

impl ResolveTypes for ast::BlockBodyNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
        let result_var = ctx.add_inference_variable();

        if let Some(result) = &self.result {
            ctx.constraints.push((
                result_var,
                TypeConstraint::ResolveFrom(result.breadcrumbs().to_owned()),
                ConstraintDiagnostic::Resolver(
                    result.breadcrumbs().to_owned(),
                    "Explicit result of block".into(),
                ),
            ));
        } else {
            ctx.constraints.push((
                result_var,
                TypeConstraint::EqualTo(OldResolvingLangType::Action.into()),
                ConstraintDiagnostic::Resolver(
                    self.breadcrumbs().to_owned(),
                    "Block results in Action by default".into(),
                ),
            ));
        }

        for statement in &self.statements {
            let statement_result_var = ctx.add_inference_variable();
            ctx.constraints.push((
                statement_result_var,
                TypeConstraint::ResolveFrom(statement.breadcrumbs().to_owned()),
                ConstraintDiagnostic::Resolver(
                    statement.breadcrumbs().to_owned(),
                    "Block statement (for flow analysis)".to_owned(),
                ),
            ));
            ctx.constraints.push((
                result_var,
                TypeConstraint::UnreachableIfNeverContinues(OldResolvingType::InferenceVariable(
                    statement_result_var,
                )),
                ConstraintDiagnostic::Resolver(
                    statement.breadcrumbs().to_owned(),
                    "Entire block result is unreachable if one statement is NeverContinues"
                        .to_owned(),
                ),
            ));
        }

        Some(OldResolvingType::InferenceVariable(result_var))
    }
}

impl ResolveTypes for ast::ExpressionStatementNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
        ctx.get_resolved_type_proxying_errors(&self.expression)
            .pipe(Some)
    }
}

impl ResolveTypes for ast::EffectStatementNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
        let result_type: AnyOldResolvingLangType = ctx
            .get_resolved_type_proxying_errors(&self.pattern)
            .and_then(|pattern_type| match pattern_type.as_ref() {
                // TODO: if the pattern is pending when we hit this resolution,
                // this validation probably gets skipped...
                OldResolvingLangType::Instance(instance) => {
                    if instance.type_id.category == CanonicalLangTypeCategory::Signal {
                        let result_type = ctx
                            .get_canonical_type(&instance.type_id)
                            .ok_or(LangError::compiler_bug(format!(
                                "Missing type for {}",
                                instance.type_id.to_string()
                            )))
                            .and_then(|canonical_type| match canonical_type.as_ref() {
                                OldResolvingCanonicalLangType::Signal(signal) => Ok(signal.clone()),
                                _ => Err(LangError::NotCausable),
                            })
                            .map(|signal| signal.result().clone());
                        result_type.unwrap_or_else(|err| OldResolvingType::Error(err.into()))
                    } else {
                        LangError::NotCausable.into()
                    }
                }
                // can't guarantee a result when capturing AnySignal
                // TODO: this is actually probably a bug in the language that you
                // can define effects for signals that require a result via AnySignal
                OldResolvingLangType::AnySignal => {
                    OldResolvingType::Known(OldResolvingLangType::Action.into())
                }
                _ => OldResolvingType::Error(LangError::NotCausable.into()),
            });

        ctx.edicts.push(TypeEdict {
            breadcrumbs: self.body.info().breadcrumbs.clone().into(),
            rule: TypeEdictRule::AssignableTo(result_type),
            diagnostic: "Result of effect handler must be assignable to effect's result".into(),
        });

        return Some(OldResolvingLangType::Action.into());
    }
}

impl ResolveTypes for ast::SetExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
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
            rule: TypeEdictRule::AssignableTo(OldResolvingType::InferenceVariable(
                expected_type_var,
            )),
            diagnostic: "expression assignable to variable".into(),
        });

        return Some(OldResolvingType::InferenceVariable(expected_type_var));
    }
}

impl ResolveTypes for ast::CauseExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
        let canonical_type_id = match ctx
            .get_resolved_type_proxying_errors(&self.signal)
            .to_result_assuming_inferred()
            .and_then(|it| match it.as_ref() {
                OldResolvingLangType::Instance(instance) => Ok(instance.type_id.clone()),
                OldResolvingLangType::TypeReference(instance) => instance
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
            Err(err) => return Some(OldResolvingType::Error(err.into())),
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
                OldResolvingCanonicalLangType::Signal(signal_type) => {
                    Ok(signal_type.result().clone())
                }
                _ => Err(LangError::NotCausable.into()),
            })
            .unwrap_or_else(|err: Arc<LangError>| OldResolvingType::Error(err.into()));
        Some(signal_result_type)
    }
}

impl ResolveTypes for ast::CallExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
        resolve_call_expression(
            &self.callee,
            &self
                .parameters
                .iter()
                .map(|it| it.breadcrumbs().clone())
                .collect_vec(),
            ctx,
        )
    }
}

impl ResolveTypes for ast::PipeCallExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
        let parameters = vec![
            vec![self.subject.breadcrumbs().clone()],
            self.parameters
                .iter()
                .map(|it| it.value.breadcrumbs().clone())
                .collect(),
        ]
        .concat();
        resolve_call_expression(&self.callee, &parameters, ctx)
    }
}

fn resolve_call_expression(
    callee_expression: &ExpressionNode,
    parameter_breadcrumbs: &[Breadcrumbs],
    ctx: &mut ResolveTypesContext,
) -> Option<AnyOldResolvingLangType> {
    let callee_type = ctx.get_resolved_type_proxying_errors(callee_expression);
    let callee_type = match callee_type.to_result_assuming_inferred() {
        Ok(it) => it,
        Err(err) => return Some(OldResolvingType::Error(err.into())),
    };

    let result_type = match callee_type.as_ref() {
        OldResolvingLangType::Function(function_type) => Ok(function_type.return_type.clone()),
        OldResolvingLangType::TypeReference(referenced_type) => referenced_type
            .clone()
            .to_result_assuming_inferred()
            .and_then(|referenced_type| match referenced_type.as_ref() {
                OldResolvingLangType::Instance(instance) => Ok(instance.clone().into()),
                OldResolvingLangType::StopgapDictionary | OldResolvingLangType::StopgapList => {
                    Ok(referenced_type.into())
                }
                _ => Err(LangError::NotCallable.into()),
            }),
        _ => Err(LangError::NotCallable.into()),
    };

    #[derive(Debug, Clone, Eq, PartialEq)]
    struct ExpectedCallParameter {
        name: Arc<String>,
        value_type: AnyOldResolvingLangType,
    }

    let expected_parameters = match callee_type.as_ref() {
        OldResolvingLangType::Function(function_type) => Ok(function_type
            .params
            .iter()
            .map(|it| ExpectedCallParameter {
                name: it.name.clone(),
                value_type: it.value_type.clone(),
            })
            .collect_vec()),
        OldResolvingLangType::TypeReference(referenced_type) => referenced_type
            .clone()
            .to_result_assuming_inferred()
            .and_then(|referenced_type| match referenced_type.as_ref() {
                OldResolvingLangType::Instance(instance) => {
                    let instance_type = instance;
                    let canonical_type = ctx.get_canonical_type(&instance_type.type_id).ok_or(
                        LangError::compiler_bug(format!(
                            "Missing canonical type: {}",
                            instance_type.type_id.to_string()
                        )),
                    )?;
                    Ok(canonical_type
                        .fields()
                        .iter()
                        .map(|it| ExpectedCallParameter {
                            name: it.name.clone(),
                            value_type: it.value_type.clone(),
                        })
                        .collect_vec())
                }
                OldResolvingLangType::StopgapDictionary | OldResolvingLangType::StopgapList => {
                    Ok(vec![])
                }
                _ => Err(LangError::NotCallable.into()),
            }),
        _ => Err(LangError::NotCallable.into()),
    };

    match expected_parameters {
        Ok(parameters) => {
            match parameter_breadcrumbs.len().cmp(&parameters.len()) {
                std::cmp::Ordering::Less => {
                    return Some(
                        LangError::MissingParameters(MissingParametersError {
                            names: parameters[parameter_breadcrumbs.len()..]
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

            for (lang_param, param_breadcrumbs) in
                parameters.iter().zip(parameter_breadcrumbs.iter())
            {
                ctx.edicts.push(TypeEdict {
                    breadcrumbs: param_breadcrumbs.clone(),
                    rule: TypeEdictRule::AssignableTo(lang_param.value_type.clone()),
                    diagnostic:
                        "Function call param must be assignable to function definition param type"
                            .into(),
                });
            }
        }
        Err(err) => return Some(OldResolvingType::Error(err.into())),
    }

    Some(result_type.unwrap_or_else(|err| OldResolvingType::Error(err.into())))
}

impl ResolveTypes for ast::MemberExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
        let object = ctx.get_resolved_type_proxying_errors(&self.object_expression);
        let var = ctx.add_inference_variable();
        ctx.constraints.push((
            var,
            TypeConstraint::MemberOf(object.clone(), self.member_identifier.text.clone()),
            ConstraintDiagnostic::Resolver(self.breadcrumbs().clone(), "Member expression".into()),
        ));
        return Some(OldResolvingType::InferenceVariable(var));
    }
}

impl ResolveTypes for ast::IdentifierExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
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
            .map(|it| matches!(it.as_ref(), OldResolvingLangType::Action))
            .unwrap_or(false);
        if is_action_reference {
            return Some(OldResolvingLangType::Action.into());
        }

        return Some(resolved_type);
    }
}

impl ResolveTypes for ast::StringLiteralExpressionNode {
    fn compute_type(&self, _ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
        Some(OldResolvingLangType::Primitive(PrimitiveLangType::Text).into())
    }
}

impl ResolveTypes for ast::NumberLiteralExpressionNode {
    fn compute_type(&self, _ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
        Some(OldResolvingLangType::Primitive(PrimitiveLangType::Number).into())
    }
}

impl ResolveTypes for ast::DeclarationStatementNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
        ctx.get_resolved_type_proxying_errors(&self.declaration)
            .pipe(Some)
    }
}

impl ResolveTypes for ast::NamedValueNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
        let annotated_type = self
            .type_annotation
            .as_ref()
            .map(|it| ctx.get_resolved_type_proxying_errors(it))
            .map(|annotated_type| {
                let annotated_type = annotated_type.to_result_assuming_inferred()?;
                match annotated_type.as_ref() {
                    OldResolvingLangType::TypeReference(OldResolvingType::Known(value_type)) => {
                        Ok(value_type.clone())
                    }
                    OldResolvingLangType::TypeReference(OldResolvingType::Error(err)) => {
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
                            r#type: Ok(Arc::new(annotated_type.as_ref().to_owned().into())),
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
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
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
                OldResolvingCanonicalTypeField {
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
            OldResolvingCanonicalLangType::Object(ObjectOldResolvingCanonicalLangType {
                type_id: id.as_ref().to_owned(),
                fields,
            })
            .into(),
        );

        Some(
            OldResolvingLangType::TypeReference(
                OldResolvingLangType::Instance(InstanceOldResolvingLangType {
                    type_id: id.clone(),
                })
                .into(),
            )
            .into(),
        )
    }
}

impl ResolveTypes for ast::SignalTypeNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
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
                OldResolvingCanonicalTypeField {
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
                    .conv::<AnyOldResolvingLangType>()
            })
            .unwrap_or(OldResolvingLangType::Action.into());
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
            OldResolvingCanonicalLangType::Signal(SignalOldResolvingCanonicalLangType {
                type_id: id.as_ref().to_owned(),
                fields,
                result: result_type,
            })
            .into(),
        );

        Some(
            OldResolvingLangType::TypeReference(
                OldResolvingLangType::Instance(InstanceOldResolvingLangType {
                    type_id: id.clone(),
                })
                .into(),
            )
            .into(),
        )
    }
}

impl ResolveTypes for ast::ObjectFieldNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
        let type_reference = ctx.get_resolved_type_proxying_errors(&self.type_annotation);
        Some(type_reference.try_get_referenced_type().into())
    }
}

impl ResolveTypes for ast::OneOfTypeNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
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
                    TypeConstraint::ReferencedType(OldResolvingType::InferenceVariable(
                        type_reference_var,
                    )),
                    ConstraintDiagnostic::Resolver(it.breadcrumbs().clone(), "oneof option".into()),
                ));
                OldResolvingType::InferenceVariable(referenced_type_var)
            })
            .collect_vec();
        Some(
            OldResolvingLangType::TypeReference(
                OldResolvingLangType::OneOf(OneOfOldResolvingLangType { options }).into(),
            )
            .into(),
        )
    }
}

impl ResolveTypes for ast::FunctionExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
        let tags = ctx.get_tags(self).as_ref().to_owned();
        compute_function_type(None, &self.params, self.return_type.as_ref(), &tags, ctx)
    }
}

impl ResolveTypes for ast::BranchExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
        if self.branches.is_empty() {
            return Some(OldResolvingLangType::Action.into());
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
                        remaining_with_value: with_value_var
                            .map(OldResolvingType::InferenceVariable),
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
                        remaining_with_value: with_value_var
                            .map(OldResolvingType::InferenceVariable),
                        result: resolved_type.clone(),
                    });
                    if let Some(with_value_var) = &mut with_value_var {
                        let prev_with_value_var = *with_value_var;
                        *with_value_var = ctx.add_inference_variable();
                        ctx.constraints.push((
                            *with_value_var,
                            TypeConstraint::Narrowed(NarrowedConstraint {
                                base: OldResolvingType::InferenceVariable(prev_with_value_var),
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
                remaining_with_value: with_value_var.map(OldResolvingType::InferenceVariable),
                pattern: None,
                result: resolved_type.clone(),
            });
            with_value_var = with_value_var.map(|_| {
                let var = ctx.add_inference_variable();
                ctx.constraints.push((
                    var,
                    TypeConstraint::EqualTo(OldResolvingLangType::NeverContinues.into()),
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
                remaining_with_value: Some(OldResolvingLangType::NeverContinues.into()),
                result: unreachable_error.into(),
            });
        }

        ctx.edicts.push(TypeEdict {
            breadcrumbs: self.breadcrumbs().to_owned(),
            rule: TypeEdictRule::ValidateBranchExpression(ValidateBranchExpressionTypeEdict {
                branches: branches.clone(),
                else_branch: else_branch_info.clone(),
                final_with_value: with_value_var.map(OldResolvingType::InferenceVariable),
            }),
            diagnostic: "Branch expression".into(),
        });

        // A branch statement without a with-value always needs an else branch
        if self.with_value.is_none() && !else_branch_info.is_some() {
            return Some(
                LangError::MissingElseBranch(MissingElseBranchError { options: None }).into(),
            );
        }

        let mut result =
            OneOfOldResolvingLangType::new(branches.into_iter().map(|it| it.result).collect());
        if let Some(else_branch_info) = &else_branch_info {
            result = result.expand(&else_branch_info.result);
        }
        return Some(result.simplify_to_value());
    }
}

impl ResolveTypes for LoopExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
        let tags = ctx.get_tags(self);
        let breaks = find_tags!(&tags, NodeTag::LoopBreaksAt).collect_vec();

        if breaks.is_empty() {
            return Some(OldResolvingLangType::NeverContinues.into());
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
                            OldResolvingLangType::Action.into()
                        }
                    })
                    .unwrap_or_else(|it| it.into());

                return (it.break_expression.clone(), break_expression_type);
            })
            .collect_vec();

        // TODO: assert that all or no break types are Action

        let loop_result_type = OneOfOldResolvingLangType::new(
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
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
        let tags = ctx.get_tags(self);
        let break_tag = find_tag!(&tags, NodeTag::BreaksLoop);
        if break_tag.is_some() {
            return Some(OldResolvingLangType::NeverContinues.into());
        } else {
            return Some(LangError::CannotBreakHere.into());
        }
    }
}

impl ResolveTypes for ast::SingleExpressionBodyNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
        return ctx
            .get_resolved_type_proxying_errors(&self.expression)
            .pipe(Some);
    }
}

impl ResolveTypes for FunctionSignatureParameterNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyOldResolvingLangType> {
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
