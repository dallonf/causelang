use std::{collections::HashMap, rc::Rc};

use anyhow::anyhow;
use itertools::Itertools;

use crate::lang_types::LangTypeResult;

use super::{
    hints::{Hint, TrackedHint},
    resolving_lang_types::*,
};

const MAX_ITERATIONS: u16 = 10_000;

pub fn infer_types(ctx: ResolvingLangTypesContext) -> anyhow::Result<()> {
    let mut ctx = InferTypesContext {
        resolving_types_ctx: ctx,
    };

    let mut unsolved_variables: HashMap<ResolvingLangTypeSource, Rc<LinkedResolvingLangType>> = ctx
        .resolving_types_ctx
        .all_variables()
        .filter(|it| {
            it.1.try_as_variable_ref()
                .map(|it| it.value.borrow().try_as_hints_ref().is_some())
                .unwrap_or(false)
        })
        .collect();
    let mut solved_variable_diagnostics: HashMap<ResolvingLangTypeSource, Vec<TrackedHint>> =
        Default::default();

    let mut iterations: u16 = 0;
    loop {
        // clone so we can mutate the map while iterating
        for (source, unsolved_variable) in unsolved_variables.clone() {
            let mut unsolved_variable_mut = unsolved_variable.try_as_variable_ref().ok_or_else(|| anyhow!(format!(
                "Somehow, {source:?} is a constant, but we're tracking it as an unsolved variable: {unsolved_variable:?}"
            )))?.value.borrow_mut();
            let hints = unsolved_variable_mut.try_as_hints_ref().ok_or_else(|| anyhow!(format!("Somehow, {source:?} is already solved, but we're tracking it as an unsolved variable: {unsolved_variable_mut:?}")))?;
            let variable_step_result = infer_variable_step(hints, &mut ctx).unwrap_or_else(|err| {
                InferVariableStepResult::Solved(
                    ctx.resolving_types_ctx.link_lang_type(Err(err)).into(),
                )
            });

            match variable_step_result {
                InferVariableStepResult::Solved(known_type) => {
                    solved_variable_diagnostics.insert(source.clone(), hints.clone());
                    *unsolved_variable_mut = ResolvingLangTypeValue::Known(known_type);
                    unsolved_variables.remove(&source);
                }
                InferVariableStepResult::Next {
                    mut add_hints,
                    remove_hint_indices,
                } => {
                    let mut new_hints = hints
                        .iter()
                        .enumerate()
                        .filter(|(i, _)| !remove_hint_indices.contains(i))
                        .map(|(_, hint)| hint.clone())
                        .collect_vec();
                    new_hints.append(&mut add_hints);
                    *unsolved_variable_mut = ResolvingLangTypeValue::Hints(new_hints);
                }
            }
        }

        iterations += 1;
        if iterations >= MAX_ITERATIONS {
            // TODO: really need to stop earlier if we get stuck
            // TODO: "solve" all remaining unsolved variables with a "solver iterations exceeded" error
            break;
        }
    }

    println!(
        "variables: {:#?}",
        ctx.resolving_types_ctx
            .all_variables()
            .map(|it| it.1)
            .collect_vec()
    );
    std::mem::drop(ctx.resolving_types_ctx);

    Ok(())
}

struct InferTypesContext {
    resolving_types_ctx: ResolvingLangTypesContext,
}

fn infer_variable_step(
    hints: &[TrackedHint],
    ctx: &mut InferTypesContext,
) -> LangTypeResult<InferVariableStepResult> {
    let mut add_hints = Vec::<TrackedHint>::new();
    let mut remove_hint_indices = Vec::<usize>::new();

    // A single EqualTo hint means it's been solved!
    if hints.len() == 1 && matches!(hints[0].hint, Hint::EqualTo(_)) {
        return Ok(InferVariableStepResult::Solved(
            hints[0].hint.try_as_equal_to_ref().unwrap().clone(),
        ));
    }

    Ok(InferVariableStepResult::Next {
        add_hints,
        remove_hint_indices,
    })
}

#[derive(Debug, Clone)]
enum InferVariableStepResult {
    Solved(ResolvingLangTypeLink),
    Next {
        add_hints: Vec<TrackedHint>,
        remove_hint_indices: Vec<usize>,
    },
}

#[derive(Debug, Clone)]
enum InferHintStepResult {
    Unchanged,
    ReplaceWith(Vec<TrackedHint>),
}
