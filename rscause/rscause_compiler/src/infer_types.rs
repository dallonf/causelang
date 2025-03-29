use std::{
    cell::RefCell,
    collections::{HashMap, HashSet},
    hash::{DefaultHasher, Hash, Hasher},
    sync::Arc,
};

use itertools::Itertools;
use tap::{Conv, Pipe};

use crate::{
    ast::{AnyAstNode, AstNode},
    error_types::{
        CompilerBugError, ErrorPosition, LangError, SourcePosition, ValueUsedAsConstraintError,
    },
    lang_types::{
        AnyInferredLangType, AnyLangTypeResult, HasInference, InferredType, LangType, OneOfLangType,
    },
    resolve_types::{
        ConstraintDiagnostic, ImplicitValueAssignableToTypeEdict, NarrowedConstraint,
        ResolveTypesContext, TypeConstraint, TypeEdictRule, ValidateBranchExpressionTypeEdict,
        ValidateBranchExpressionTypeEdictBranch, ValidateCallParameterTypeEdict,
        ValidateCallTypeEdict,
    },
};

#[derive(Debug)]
pub struct InferTypesResult {
    pub solved_variables: HashMap<u64, (AnyInferredLangType, ConstraintDiagnostic)>,
    pub unsolved_variables: HashMap<u64, Vec<(TypeConstraint, ConstraintDiagnostic)>>,
}

pub fn infer_types(ctx: &mut ResolveTypesContext) -> InferTypesResult {
    let mut variables = HashMap::<u64, RefCell<Vec<(TypeConstraint, ConstraintDiagnostic)>>>::new();
    let variable_ids = ctx
        .value_types
        .values()
        .flat_map(|value| {
            value
                .as_ref()
                .map(|value| value.recursive_inferred_types())
                .unwrap_or_default()
        })
        .chain(
            ctx.new_canonical_types
                .values()
                .flat_map(|canonical_type| canonical_type.recursive_inferred_types()),
        )
        .filter_map(|inferred_type| {
            if let InferredType::InferenceVariable(id) = inferred_type {
                Some(id)
            } else {
                None
            }
        })
        .collect::<HashSet<_>>();
    for id in variable_ids {
        variables.insert(id, vec![].into());
    }
    for constraint in ctx.constraints.iter() {
        let constraints_for_var = variables.entry(constraint.0).or_default();
        constraints_for_var
            .borrow_mut()
            .push((constraint.1.clone(), constraint.2.clone()));
    }

    let mut solved_variables = HashMap::<u64, (AnyLangTypeResult, ConstraintDiagnostic)>::new();

    let mut last_hash = hash_variables(&variables);

    while !variables.is_empty() {
        for (id, constraints) in variables.iter() {
            let mut new_constraints: Vec<(TypeConstraint, ConstraintDiagnostic)> = vec![];
            let mut pending_constraints: Vec<(TypeConstraint, ConstraintDiagnostic)> = vec![];
            for (constraint, diagnostic) in constraints.borrow().clone() {
                match &constraint {
                    TypeConstraint::EqualTo(_) | TypeConstraint::AssignableTo(_) => {
                        // these can't be simplified individually
                        pending_constraints.push((constraint, diagnostic))
                    }
                    TypeConstraint::ResolveFrom(breadcrumbs) => {
                        let value_type = ctx
                            .value_types
                            .get(&breadcrumbs)
                            .and_then(|it| it.as_ref())
                            .cloned();
                        let result = 'result: {
                            let Some(value_type) = value_type else {
                                break 'result InferredType::Error(Arc::new(
                                    LangError::NeverResolved,
                                ));
                            };
                            if let InferredType::Error(error) = &value_type {
                                let error = error.clone();
                                let node = ctx
                                    .root_node
                                    .clone()
                                    .conv::<AnyAstNode>()
                                    .node_at_path(&breadcrumbs);
                                let node = match node {
                                    Ok(node) => node,
                                    Err(err) => {
                                        break 'result InferredType::Error(Arc::new(
                                            LangError::compiler_bug(err.to_string()),
                                        ));
                                    }
                                };
                                let source_position = ErrorPosition::Source(SourcePosition {
                                    path: ctx.file_path.clone(),
                                    breadcrumbs: breadcrumbs.clone(),
                                    position: node.info().position,
                                });
                                break 'result InferredType::Error(
                                    LangError::proxy_error(error, source_position).into(),
                                );
                            }

                            value_type
                        };
                        new_constraints.push((
                            TypeConstraint::EqualTo(result),
                            ConstraintDiagnostic::Inferred(
                                "resolved from AST".into(),
                                vec![(*id, constraint, diagnostic)],
                            ),
                        ));
                    }
                    TypeConstraint::ReferencedType(type_reference) => {
                        let result = 'result: {
                            let known_reference = match type_reference {
                                InferredType::Known(known) => known,
                                InferredType::Error(lang_error) => {
                                    // TODO: maybe proxy this
                                    break 'result TypeConstraint::EqualTo(InferredType::Error(
                                        lang_error.to_owned(),
                                    ));
                                }
                                InferredType::InferenceVariable(_) => {
                                    break 'result TypeConstraint::ReferencedType(
                                        type_reference.to_owned(),
                                    )
                                }
                            };
                            let Some(referenced_type) = known_reference.try_as_type_reference_ref()
                            else {
                                break 'result TypeConstraint::EqualTo(InferredType::Error(
                                    LangError::ValueUsedAsConstraint(ValueUsedAsConstraintError {
                                        r#type: known_reference.clone().into(),
                                    })
                                    .into(),
                                ));
                            };
                            TypeConstraint::EqualTo(referenced_type.clone())
                        };
                        new_constraints.push((
                            result,
                            ConstraintDiagnostic::Inferred(
                                "unwrapped type reference".into(),
                                vec![(*id, constraint, diagnostic)],
                            ),
                        ));
                    }
                    TypeConstraint::MemberOf(inferred_type, name) => {
                        let field_type = inferred_type
                            .clone()
                            .and_then(|known| {
                                if let LangType::Instance(instance) = known.as_ref() {
                                    InferredType::Known(instance.clone())
                                } else {
                                    InferredType::Error(LangError::DoesNotHaveAnyMembers.into())
                                }
                            })
                            .and_then(|instance| {
                                ctx.get_canonical_type(&instance.type_id)
                                    .map(|canonical_type| InferredType::Known(canonical_type))
                                    .unwrap_or(InferredType::Error(
                                        LangError::CompilerBug(CompilerBugError {
                                            description: format!(
                                                "Missing canonical type: {}",
                                                instance.type_id.to_string()
                                            ),
                                        })
                                        .into(),
                                    ))
                            })
                            .map(|canonical_type| canonical_type.fields())
                            .and_then(|fields| {
                                fields
                                    .iter()
                                    .find(|it| it.name == *name)
                                    .map(|field| InferredType::Known(field.clone()))
                                    .unwrap_or(LangError::DoesNotHaveMember.into())
                            })
                            .and_then(|field| field.value_type);

                        match field_type {
                            InferredType::Known(field_type) => {
                                new_constraints.push((
                                    TypeConstraint::EqualTo(field_type.into()),
                                    ConstraintDiagnostic::Inferred(
                                        "member of instance".into(),
                                        vec![(*id, constraint, diagnostic)],
                                    ),
                                ));
                            }
                            InferredType::Error(error) => {
                                new_constraints.push((
                                    TypeConstraint::EqualTo(InferredType::Error(error)),
                                    ConstraintDiagnostic::Inferred(
                                        "member of instance (error)".into(),
                                        vec![(*id, constraint, diagnostic)],
                                    ),
                                ));
                            }
                            InferredType::InferenceVariable(_) => {
                                // keep the constraint as-is
                                new_constraints.push((
                                    TypeConstraint::MemberOf(
                                        inferred_type.to_owned(),
                                        name.to_owned(),
                                    ),
                                    diagnostic,
                                ))
                            }
                        }
                    }
                    TypeConstraint::Narrowed(narrowed) => {
                        let result = match &narrowed.base {
                            InferredType::Known(base) => match &narrowed.narrow {
                                InferredType::Known(narrow) => {
                                    let oneof = OneOfLangType::new_with_one(base.clone().into());
                                    let oneof = oneof.narrow(&narrow.clone());
                                    TypeConstraint::EqualTo(oneof.simplify_to_value().into())
                                }
                                InferredType::Error(_) => {
                                    // This ignores any error in the narrowing type.
                                    // Maybe that's fine? Hopefully it would be reported elsewhere.
                                    TypeConstraint::EqualTo(base.clone().into())
                                }
                                InferredType::InferenceVariable(_) => {
                                    TypeConstraint::Narrowed(narrowed.to_owned())
                                }
                            },
                            InferredType::Error(lang_error) => {
                                TypeConstraint::EqualTo(InferredType::Error(lang_error.clone()))
                            }
                            InferredType::InferenceVariable(_) => {
                                TypeConstraint::Narrowed(narrowed.to_owned())
                            }
                        };
                        new_constraints.push((
                            result,
                            ConstraintDiagnostic::Inferred(
                                "narrowed".into(),
                                vec![(*id, constraint, diagnostic)],
                            ),
                        ));
                    }
                    TypeConstraint::UnreachableIfNeverContinues(unreachable_trigger) => {
                        match unreachable_trigger {
                            InferredType::Known(unreachable_trigger) => {
                                let is_never_continues = matches!(
                                    unreachable_trigger.as_ref(),
                                    LangType::NeverContinues
                                );

                                if is_never_continues {
                                    // override all other rules
                                    // NOTE: we'll have to add a priority system of some sort
                                    // if there are other rules that want to override all rules,
                                    // including this one that wants to override all rules
                                    new_constraints.clear();
                                    pending_constraints.clear();
                                    new_constraints.push((
                                        TypeConstraint::EqualTo(InferredType::Known(
                                            LangType::NeverContinues.into(),
                                        )),
                                        ConstraintDiagnostic::Inferred(
                                            "unreachable result".into(),
                                            constraints
                                                .borrow()
                                                .iter()
                                                .map(|(prev_constraint, prev_diagnostic)| {
                                                    (
                                                        *id,
                                                        prev_constraint.to_owned(),
                                                        prev_diagnostic.to_owned(),
                                                    )
                                                })
                                                .collect(),
                                        ),
                                    ));
                                    break;
                                }
                            }
                            InferredType::Error(_) => {} // remove constraint
                            InferredType::InferenceVariable(_) => pending_constraints.push((
                                TypeConstraint::UnreachableIfNeverContinues(
                                    unreachable_trigger.to_owned(),
                                ),
                                diagnostic,
                            )),
                        }
                    }
                    TypeConstraint::CallResult(callee_type) => match callee_type {
                        InferredType::Known(callee_type) => {
                            let result_type: AnyInferredLangType = match callee_type.as_ref() {
                                LangType::Function(function_type) => {
                                    function_type.return_type.clone().into()
                                }
                                LangType::TypeReference(referenced_type) => match referenced_type {
                                    InferredType::Known(referenced_type) => {
                                        match referenced_type.as_ref() {
                                            LangType::Instance(instance) => instance.clone().into(),
                                            LangType::StopgapDictionary | LangType::StopgapList => {
                                                referenced_type.clone().into()
                                            }
                                            _ => LangError::NotCallable.into(),
                                        }
                                    }
                                    InferredType::Error(lang_error) => {
                                        AnyInferredLangType::Error(lang_error.clone())
                                    }
                                    InferredType::InferenceVariable(_) => {
                                        pending_constraints.push((
                                            TypeConstraint::CallResult(
                                                callee_type.to_owned().into(),
                                            ),
                                            diagnostic,
                                        ));
                                        break;
                                    }
                                },
                                _ => LangError::NotCallable.into(),
                            };

                            new_constraints.push((
                                TypeConstraint::EqualTo(result_type),
                                ConstraintDiagnostic::Inferred(
                                    "call result".into(),
                                    vec![(
                                        *id,
                                        TypeConstraint::CallResult(callee_type.clone().into()),
                                        diagnostic,
                                    )],
                                ),
                            ))
                        }
                        InferredType::Error(lang_error) => new_constraints.push((
                            TypeConstraint::EqualTo(lang_error.as_ref().to_owned().into()),
                            ConstraintDiagnostic::Inferred(
                                "call result of function".into(),
                                constraints
                                    .borrow()
                                    .iter()
                                    .map(|(prev_constraint, prev_diagnostic)| {
                                        (
                                            *id,
                                            prev_constraint.to_owned(),
                                            prev_diagnostic.to_owned(),
                                        )
                                    })
                                    .collect(),
                            ),
                        )),
                        // keep it around until the callee type is resolved
                        InferredType::InferenceVariable(_) => new_constraints.push((
                            TypeConstraint::CallResult(callee_type.to_owned()),
                            diagnostic,
                        )),
                    },
                }
            }

            new_constraints.append(&mut pending_constraints);
            constraints.replace(new_constraints);
        }

        let mut solved_this_iteration = vec![];
        for (id, constraints) in variables.iter() {
            let just_constraints = constraints
                .borrow()
                .iter()
                .map(|(constraints, _)| constraints.to_owned())
                .collect_vec();
            if is_solved(&just_constraints) {
                solved_variables.insert(
                    *id,
                    (
                        get_solution(&just_constraints)
                            .unwrap()
                            .to_result_assuming_inferred(),
                        ConstraintDiagnostic::Inferred(
                            format!("solved var {id}"),
                            constraints
                                .borrow()
                                .iter()
                                .map(|(constraint, diagnostic)| {
                                    (*id, constraint.to_owned(), diagnostic.to_owned())
                                })
                                .collect(),
                        ),
                    ),
                );
                solved_this_iteration.push(*id);
            }
        }
        variables = variables
            .into_iter()
            .filter_map(|(id, constraints)| {
                if solved_variables.contains_key(&id) {
                    return None;
                }

                let fill_solved_variables = |inferred_type: AnyInferredLangType| {
                    solved_this_iteration
                        .iter()
                        .fold(inferred_type, |inferred_type, solved_id| {
                            inferred_type.fill_variable(
                                *solved_id,
                                solved_variables[solved_id].0.clone().into(),
                            )
                        })
                };

                let new_constraints = constraints
                    .take()
                    .into_iter()
                    .map(|(constraint, diagnostic)| {
                        let new_constraint = match constraint {
                            TypeConstraint::EqualTo(inferred_type) => {
                                TypeConstraint::EqualTo(fill_solved_variables(inferred_type))
                            }
                            TypeConstraint::AssignableTo(inferred_type) => {
                                TypeConstraint::AssignableTo(fill_solved_variables(inferred_type))
                            }
                            TypeConstraint::MemberOf(inferred_type, name) => {
                                TypeConstraint::MemberOf(fill_solved_variables(inferred_type), name)
                            }
                            TypeConstraint::ReferencedType(inferred_type) => {
                                TypeConstraint::ReferencedType(fill_solved_variables(inferred_type))
                            }
                            TypeConstraint::ResolveFrom(breadcrumbs) => {
                                TypeConstraint::ResolveFrom(breadcrumbs)
                            }
                            TypeConstraint::Narrowed(narrowed) => {
                                TypeConstraint::Narrowed(NarrowedConstraint {
                                    base: fill_solved_variables(narrowed.base),
                                    narrow: fill_solved_variables(narrowed.narrow),
                                })
                            }
                            TypeConstraint::UnreachableIfNeverContinues(inferred_type) => {
                                TypeConstraint::UnreachableIfNeverContinues(fill_solved_variables(
                                    inferred_type,
                                ))
                            }
                            TypeConstraint::CallResult(callee_type) => {
                                TypeConstraint::CallResult(fill_solved_variables(callee_type))
                            }
                        };
                        (new_constraint, diagnostic)
                    })
                    .collect();

                return Some((id, RefCell::new(new_constraints)));
            })
            .collect();

        for &solved_id in &solved_this_iteration {
            let solution = &solved_variables[&solved_id];
            for ptr in ctx.new_canonical_types.values_mut() {
                *ptr = ptr
                    .fill_variable(solved_id, solution.0.clone().into())
                    .into();
            }
        }

        let new_hash = hash_variables(&variables);
        if new_hash == last_hash {
            break;
        }
        last_hash = new_hash;
    }

    // Fill all solved variables
    for (&id, solution) in &solved_variables {
        for ptr in ctx.value_types.values_mut() {
            if let Some(ptr) = ptr {
                *ptr = ptr.fill_variable(id, solution.0.clone().into()).into();
            }
        }
        for ptr in ctx.edicts.iter_mut() {
            ptr.rule = match &ptr.rule {
                TypeEdictRule::AssignableTo(inferred_type) => TypeEdictRule::AssignableTo(
                    inferred_type.fill_variable(id, solution.0.clone().into()),
                ),
                TypeEdictRule::ImplicitValueAssignableTo(rule) => {
                    TypeEdictRule::ImplicitValueAssignableTo(ImplicitValueAssignableToTypeEdict {
                        assignable_to: rule
                            .assignable_to
                            .fill_variable(id, solution.0.clone().into()),
                        implicit_value: rule
                            .implicit_value
                            .fill_variable(id, solution.0.clone().into()),
                    })
                }
                TypeEdictRule::MustBeTypeReference => TypeEdictRule::MustBeTypeReference,
                TypeEdictRule::ValidateBranchExpression(edict) => {
                    TypeEdictRule::ValidateBranchExpression(ValidateBranchExpressionTypeEdict {
                        branches: edict
                            .branches
                            .iter()
                            .map(|branch| ValidateBranchExpressionTypeEdictBranch {
                                breadcrumbs: branch.breadcrumbs.clone(),
                                remaining_with_value: branch
                                    .remaining_with_value
                                    .as_ref()
                                    .map(|it| it.fill_variable(id, solution.0.clone().into())),
                                pattern: branch
                                    .pattern
                                    .as_ref()
                                    .map(|it| it.fill_variable(id, solution.0.clone().into())),
                                result: branch.result.fill_variable(id, solution.0.clone().into()),
                            })
                            .collect(),
                        else_branch: edict.else_branch.as_ref().map(|branch| {
                            ValidateBranchExpressionTypeEdictBranch {
                                breadcrumbs: branch.breadcrumbs.clone(),
                                remaining_with_value: branch
                                    .remaining_with_value
                                    .as_ref()
                                    .map(|it| it.fill_variable(id, solution.0.clone().into())),
                                pattern: branch
                                    .pattern
                                    .as_ref()
                                    .map(|it| it.fill_variable(id, solution.0.clone().into())),
                                result: branch.result.fill_variable(id, solution.0.clone().into()),
                            }
                        }),
                        final_with_value: edict
                            .final_with_value
                            .as_ref()
                            .map(|it| it.fill_variable(id, solution.0.clone().into())),
                    })
                }
                TypeEdictRule::ValidateCall(validate_call_type_edict) => {
                    TypeEdictRule::ValidateCall(ValidateCallTypeEdict {
                        callee_type: validate_call_type_edict
                            .callee_type
                            .fill_variable(id, solution.0.clone().into()),
                        parameters: validate_call_type_edict
                            .parameters
                            .iter()
                            .map(|it| it.fill_variable(id, solution.0.clone().into()))
                            .collect(),
                    })
                }
                TypeEdictRule::ValidateCallParameter(validate_call_parameter_type_edict) => {
                    TypeEdictRule::ValidateCallParameter(ValidateCallParameterTypeEdict {
                        callee_type: validate_call_parameter_type_edict
                            .callee_type
                            .fill_variable(id, solution.0.clone().into()),
                        parameter_index: validate_call_parameter_type_edict.parameter_index,
                    })
                }
            };
        }
    }

    InferTypesResult {
        solved_variables: solved_variables
            .iter()
            .map(|(&id, (result, diagnostic))| {
                (
                    id,
                    (
                        AnyInferredLangType::from(result.to_owned()),
                        diagnostic.to_owned(),
                    ),
                )
            })
            .collect(),
        unsolved_variables: variables
            .iter()
            .map(|(&id, constraints_cell)| (id, constraints_cell.borrow().to_owned()))
            .collect(),
    }
}

fn get_solution(constraints: &[TypeConstraint]) -> Option<AnyInferredLangType> {
    if constraints.len() > 1 || constraints.len() == 0 {
        return None;
    }

    let lone_equals_constraint = if let TypeConstraint::EqualTo(equal) = &constraints[0] {
        if let InferredType::InferenceVariable(_) = equal {
            return None;
        }
        equal
    } else {
        return None;
    };

    if !lone_equals_constraint.has_pending() {
        return Some(lone_equals_constraint.clone());
    } else {
        return None;
    }
}

fn is_solved(constraints: &[TypeConstraint]) -> bool {
    return get_solution(constraints).is_some();
}

/// Hashes the variables for the purposes of determining if anything has changed
/// from the previous inference iteration
fn hash_variables(
    variables: &HashMap<u64, RefCell<Vec<(TypeConstraint, ConstraintDiagnostic)>>>,
) -> u64 {
    let mut hasher = DefaultHasher::new();
    let mut values = variables
        .iter()
        .map(|(i, constraints)| {
            (
                *i,
                constraints
                    .borrow()
                    .iter()
                    .map(|(constraint, _)| constraint.to_owned())
                    .collect_vec(),
            )
        })
        .collect_vec();
    values.sort_by_key(|a| a.0);
    values.hash(&mut hasher);
    return hasher.finish();
}
