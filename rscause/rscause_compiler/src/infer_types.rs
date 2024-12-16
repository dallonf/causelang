use std::{
    cell::RefCell,
    collections::{HashMap, HashSet},
    hash::{DefaultHasher, Hash, Hasher},
    str::FromStr,
};

use itertools::Itertools;

use crate::{
    breadcrumbs::Breadcrumbs,
    error_types::{CompilerBugError, LangError},
    lang_types::{AnyInferredLangType, HasInference, InferredType, LangType},
    resolve_types::{ResolveTypesContext, TypeConstraint},
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
            ctx.canonical_types
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

    let mut last_hash = hash_variables(&variables);

    while !variables.values().any(|it| is_solved(&it.borrow())) {
        for (_id, constraints) in variables.iter() {
            let mut new_constraints: Vec<TypeConstraint> = vec![];
            for constraint in constraints.borrow().clone() {
                match constraint {
                    TypeConstraint::EqualTo(_) | TypeConstraint::AssignableTo(_) => {
                        // these can't be simplified individually
                        new_constraints.push(constraint)
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
                                ctx.canonical_types
                                    .get(&instance.type_id)
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
                }
            }
            constraints.replace(new_constraints);
        }

        let new_hash = hash_variables(&variables);
        if new_hash == last_hash {
            break;
        }
        last_hash = new_hash;
    }

    // Fill all solved variables
    for (id, constraints) in variables {
        let solution = match get_solution(constraints.borrow().as_slice()) {
            Some(it) => it,
            None => continue,
        };

        for ptr in ctx.value_types.values_mut() {
            if let Some(ptr) = ptr {
                *ptr = ptr.fill_variable(id, solution.clone());
            }
        }
        for ptr in ctx.canonical_types.values_mut() {
            *ptr = ptr.fill_variable(id, solution.clone()).into();
        }
    }
}

fn get_solution(constraints: &[TypeConstraint]) -> Option<AnyInferredLangType> {
    if constraints.len() > 1 || constraints.len() == 0 {
        return None;
    }

    let lone_equals_constraint = if let TypeConstraint::EqualTo(equal) = &constraints[0] {
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
            .canonical_types
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

    let mut all_canonical_type_ids = ctx.canonical_types.keys().collect_vec();
    all_canonical_type_ids.sort();
    hasher.write_usize(all_canonical_type_ids.len());
    for type_id in all_canonical_type_ids {
        ctx.canonical_types.get(type_id).unwrap().hash(&mut hasher);
    }

    return hasher.finish();
}
