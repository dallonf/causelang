use crate::lang_types::LangTypeResult;

use super::{
    hints::{Hint, TrackedHint},
    resolving_lang_types::*,
};

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
