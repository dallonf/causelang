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

    if hints.len() == 1 && matches!(hints[0].hint, Hint::EqualTo(_)) {
        let solved_hint_equal_to = hints[0].hint.try_as_equal_to_ref().unwrap();
        // TODO: need to be able to "solve" a variable even if it just points to another variable
        // return Ok(InferVariableStepResult::Solved(()))
    }

    Ok(InferVariableStepResult::Next {
        add_hints,
        remove_hint_indices,
    })
}

#[derive(Debug, Clone)]
enum InferVariableStepResult {
    Solved(LangTypeResult<ResolvingLangType>),
    Next {
        add_hints: Vec<TrackedHint>,
        remove_hint_indices: Vec<usize>,
    },
}
impl From<LangTypeResult<InferVariableStepResult>> for InferVariableStepResult {
    fn from(value: LangTypeResult<InferVariableStepResult>) -> Self {
        match value {
            Ok(value) => value,
            Err(err) => InferVariableStepResult::Solved(Err(err)),
        }
    }
}

#[derive(Debug, Clone)]
enum InferHintStepResult {
    Unchanged,
    ReplaceWith(Vec<TrackedHint>),
}
