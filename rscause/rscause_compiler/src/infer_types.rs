use std::{
    cell::RefCell,
    collections::{HashMap, HashSet},
    hash::{DefaultHasher, Hash, Hasher},
    str::FromStr,
    sync::Arc,
};

use itertools::Itertools;
use tap::Conv;

use crate::{
    ast::{AnyAstNode, AstNode},
    breadcrumbs::{self, Breadcrumbs},
    error_types::{
        CompilerBugError, ErrorPosition, LangError, SourcePosition, ValueUsedAsConstraintError,
    },
    lang_types::{
        AnyInferredLangType, AnyLangTypeResult, HasInference, InferredType, LangType, OneOfLangType,
    },
    resolve_types::{
        NarrowedConstraint, ResolveTypesContext, TypeConstraint, TypeEdictRule,
        ValidateBranchExpressionTypeEdict, ValidateBranchExpressionTypeEdictBranch,
    },
};

pub fn infer_types(ctx: &mut ResolveTypesContext) {
    let mut variables = HashMap::<u64, RefCell<Vec<TypeConstraint>>>::new();
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
        // TODO: preserve diagnostics
        let constraints_for_var = variables.entry(constraint.0).or_default();
        constraints_for_var.borrow_mut().push(constraint.1.clone());
    }

    let mut solved_variables = HashMap::<u64, AnyLangTypeResult>::new();

    let mut last_hash = hash_variables(&variables);

    while !variables.is_empty() {
        for (_id, constraints) in variables.iter() {
            let mut new_constraints: Vec<TypeConstraint> = vec![];
            let mut pending_constraints: Vec<TypeConstraint> = vec![];
            for constraint in constraints.borrow().clone() {
                match constraint {
                    TypeConstraint::EqualTo(_) | TypeConstraint::AssignableTo(_) => {
                        // these can't be simplified individually
                        pending_constraints.push(constraint)
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
                        new_constraints.push(TypeConstraint::EqualTo(result));
                    }

                    TypeConstraint::ReferencedType(type_reference) => {
                        let result = 'result: {
                            let known_reference = match type_reference {
                                InferredType::Known(known) => known,
                                InferredType::Error(lang_error) => {
                                    // TODO: maybe proxy this
                                    break 'result TypeConstraint::EqualTo(InferredType::Error(
                                        lang_error,
                                    ));
                                }
                                InferredType::InferenceVariable(var) => {
                                    break 'result TypeConstraint::ReferencedType(type_reference)
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
                        new_constraints.push(result);
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
                                    .find(|it| it.name == name)
                                    .map(|field| InferredType::Known(field.clone()))
                                    .unwrap_or(LangError::DoesNotHaveMember.into())
                            })
                            .and_then(|field| field.value_type);

                        match field_type {
                            InferredType::Known(field_type) => {
                                new_constraints.push(TypeConstraint::EqualTo(field_type.into()));
                            }
                            InferredType::Error(error) => {
                                new_constraints
                                    .push(TypeConstraint::EqualTo(InferredType::Error(error)));
                            }
                            InferredType::InferenceVariable(_) => {
                                // keep the constraint as-is
                                new_constraints.push(TypeConstraint::MemberOf(inferred_type, name))
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
                                    TypeConstraint::Narrowed(narrowed)
                                }
                            },
                            InferredType::Error(lang_error) => {
                                TypeConstraint::EqualTo(InferredType::Error(lang_error.clone()))
                            }
                            InferredType::InferenceVariable(_) => {
                                TypeConstraint::Narrowed(narrowed)
                            }
                        };
                        new_constraints.push(result);
                    }
                }
            }

            new_constraints.append(&mut pending_constraints);
            constraints.replace(new_constraints);
        }

        let mut solved_this_iteration = vec![];
        for (id, constraints) in variables.iter() {
            if is_solved(&constraints.borrow()) {
                solved_variables.insert(
                    *id,
                    get_solution(&constraints.borrow())
                        .unwrap()
                        .to_result_assuming_inferred(),
                );
                solved_this_iteration.push(*id);
            }
        }
        println!(
            "Solved variables this iteration: {:#?}",
            solved_this_iteration
        );
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
                            dbg!(&inferred_type);
                            dbg!(solved_id);
                            dbg!(inferred_type.fill_variable(
                                *solved_id,
                                solved_variables[solved_id].clone().into(),
                            ))
                        })
                };

                let new_constraints = constraints
                    .take()
                    .into_iter()
                    .map(|constraint| match constraint {
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
                    })
                    .collect();

                return Some((id, RefCell::new(new_constraints)));
            })
            .collect();

        let new_hash = hash_variables(&variables);
        if new_hash == last_hash {
            break;
        }
        last_hash = new_hash;
    }

    // Fill all solved variables
    for (id, solution) in solved_variables {
        for ptr in ctx.value_types.values_mut() {
            if let Some(ptr) = ptr {
                *ptr = ptr.fill_variable(id, solution.clone().into()).into();
            }
        }
        for ptr in ctx.new_canonical_types.values_mut() {
            *ptr = ptr.fill_variable(id, solution.clone().into()).into();
        }
        for ptr in ctx.edicts.iter_mut() {
            ptr.rule = match &ptr.rule {
                TypeEdictRule::AssignableTo(inferred_type) => TypeEdictRule::AssignableTo(
                    inferred_type.fill_variable(id, solution.clone().into()),
                ),
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
                                    .map(|it| it.fill_variable(id, solution.clone().into())),
                                pattern: branch
                                    .pattern
                                    .as_ref()
                                    .map(|it| it.fill_variable(id, solution.clone().into())),
                                result: branch.result.fill_variable(id, solution.clone().into()),
                            })
                            .collect(),
                        else_branch: edict.else_branch.as_ref().map(|branch| {
                            ValidateBranchExpressionTypeEdictBranch {
                                breadcrumbs: branch.breadcrumbs.clone(),
                                remaining_with_value: branch
                                    .remaining_with_value
                                    .as_ref()
                                    .map(|it| it.fill_variable(id, solution.clone().into())),
                                pattern: branch
                                    .pattern
                                    .as_ref()
                                    .map(|it| it.fill_variable(id, solution.clone().into())),
                                result: branch.result.fill_variable(id, solution.clone().into()),
                            }
                        }),
                        final_with_value: edict
                            .final_with_value
                            .as_ref()
                            .map(|it| it.fill_variable(id, solution.clone().into())),
                    })
                }
            };
        }
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

fn has_pending(ctx: &mut ResolveTypesContext) -> bool {
    return ctx
        .value_types
        .values()
        .any(|it| it.as_ref().map(|it| it.has_pending()).unwrap_or(false))
        || ctx
            .new_canonical_types
            .values()
            .any(|it| it.as_ref().has_pending());
}

/// Hashes the variables for the purposes of determining if anything has changed
/// from the previous inference iteration
fn hash_variables(variables: &HashMap<u64, RefCell<Vec<TypeConstraint>>>) -> u64 {
    let mut hasher = DefaultHasher::new();
    let mut values = variables
        .iter()
        .map(|(i, constraints)| (*i, constraints.borrow().to_owned()))
        .collect_vec();
    values.sort_by_key(|a| a.0);
    values.hash(&mut hasher);
    return hasher.finish();
}

fn hash_ctx(ctx: &ResolveTypesContext) -> u64 {
    let mut hasher = DefaultHasher::new();

    let mut all_breadcrumbs = ctx.value_types.keys().collect_vec();
    all_breadcrumbs.sort();
    hasher.write_usize(all_breadcrumbs.len());
    for breadcrumb in all_breadcrumbs {
        ctx.value_types.get(breadcrumb).unwrap().hash(&mut hasher);
    }

    let mut all_canonical_type_ids = ctx.new_canonical_types.keys().collect_vec();
    all_canonical_type_ids.sort();
    hasher.write_usize(all_canonical_type_ids.len());
    for type_id in all_canonical_type_ids {
        ctx.new_canonical_types
            .get(type_id)
            .unwrap()
            .hash(&mut hasher);
    }

    return hasher.finish();
}
