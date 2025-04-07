use std::sync::Arc;
use std::{collections::HashMap, rc::Rc};

use anyhow::anyhow;

use crate::ast::{AstNode, BreadcrumbTreeNode};
use crate::breadcrumbs::Breadcrumbs;
use crate::error_types::{
    ConstraintUsedAsValueError, ErrorPosition, LangError, ProxyErrorError, SourcePosition,
};
use crate::{ast, lang_types, prelude::*};
use crate::{error_types::anyhow_to_compiler_bug, lang_types::LangTypeResult};

use super::{
    hints::{Hint, TrackedHint},
    resolving_lang_types::*,
};

const MAX_ITERATIONS: u16 = 10_000;

pub fn infer_types(
    file_path: Arc<String>,
    root_node: Arc<ast::FileNode>,
    ctx: ResolvingLangTypesContext,
) -> anyhow::Result<()> {
    let mut ctx = InferTypesContext {
        file_path,
        root_node,
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
            let variable_step_result = infer_variable_step(hints, &source, &mut ctx)
                .unwrap_or_else(|err| {
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
    file_path: Arc<String>,
    root_node: Arc<ast::FileNode>,
}
impl InferTypesContext {
    fn build_equal_to_hint(
        &mut self,
        lang_type: LangTypeResult<ResolvingLangType>,
        reason: impl Into<String>,
        inferred_from: &Vec<TrackedHint>,
    ) -> TrackedHint {
        TrackedHint::new(
            Hint::EqualTo(self.resolving_types_ctx.link_lang_type(lang_type).into()),
            reason.into(),
            Some(inferred_from.clone()),
        )
    }

    fn node_at_path(&self, breadcrumbs: &Breadcrumbs) -> LangTypeResult<ast::AnyAstNode> {
        BreadcrumbTreeNode::from(&self.root_node)
            .at_path(breadcrumbs)
            .map_err(|err| {
                LangError::compiler_bug(format!(
                    "Couldn't find a node at path {:?}: {}",
                    breadcrumbs, err
                ))
                .into()
            })
            .and_then(|node| match node {
                BreadcrumbTreeNode::Node(Some(node)) => Ok(node),
                BreadcrumbTreeNode::Node(None) => Err(LangError::compiler_bug(format!(
                    "Empty node at path {:?}",
                    breadcrumbs
                ))
                .into()),
                BreadcrumbTreeNode::List(_) => Err(LangError::compiler_bug(format!(
                    "List node at path {:?}",
                    breadcrumbs
                ))
                .into()),
            })
    }
}

#[derive(Debug, Clone)]
enum InferVariableStepResult {
    Solved(ResolvingLangTypeLink),
    Next {
        add_hints: Vec<TrackedHint>,
        remove_hint_indices: Vec<usize>,
    },
}

fn infer_variable_step(
    hints: &[TrackedHint],
    source: &ResolvingLangTypeSource,
    ctx: &mut InferTypesContext,
) -> LangTypeResult<InferVariableStepResult> {
    let mut add_hints = Vec::<TrackedHint>::new();
    let mut remove_hint_indices = Vec::<usize>::new();

    // first process all the hints that can be processed independently
    for (i, hint) in hints.into_iter().enumerate() {
        let inferred_from = vec![hint.clone()];
        let hint_step_result: InferHintStepResult =
            infer_hint_step(hint, source, &inferred_from, ctx)?;
        match hint_step_result {
            InferHintStepResult::Unchanged => {}
            InferHintStepResult::ReplaceWith(mut new_hints) => {
                remove_hint_indices.push(i);
                add_hints.append(&mut new_hints);
            }
        }
    }
    // If any of those came up with results, end the step here
    // so the remaining rules don't have to deal with updates in progress
    if add_hints.len() > 0 || remove_hint_indices.len() > 0 {
        return Ok(InferVariableStepResult::Next {
            add_hints,
            remove_hint_indices,
        });
    }

    // Now, more complex rules, processed one at a time

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
enum InferHintStepResult {
    Unchanged,
    ReplaceWith(Vec<TrackedHint>),
}

fn infer_hint_step(
    hint: &TrackedHint,
    source: &ResolvingLangTypeSource,
    inferred_from: &Vec<TrackedHint>,
    ctx: &mut InferTypesContext,
) -> LangTypeResult<InferHintStepResult> {
    match &hint.hint {
        // EqualTo is handled with complex rules
        Hint::EqualTo(_) => InferHintStepResult::Unchanged,
        Hint::ReferencedType(resolving_lang_type_link) => todo!(),
        Hint::TypeReference(link) => infer_type_reference_hint(link, source, inferred_from, ctx)?,
        Hint::OneOf(one_of_option_hints) => todo!(),
        Hint::UnreachableIfNeverContinues(resolving_lang_type_link) => todo!(),
        Hint::CallResult(resolving_lang_type_link) => todo!(),
        Hint::CauseResult(resolving_lang_type_link) => todo!(),
        Hint::MemberOf(resolving_lang_type_link, field_name) => todo!(),
    }
    .pipe(Ok)
}

fn infer_type_reference_hint(
    link: &ResolvingLangTypeLink,
    source: &ResolvingLangTypeSource,
    inferred_from: &Vec<TrackedHint>,
    ctx: &mut InferTypesContext,
) -> LangTypeResult<InferHintStepResult> {
    let maybe_linked_type_snapshot = link
        .linked_type()?
        .get_snapshot_value()
        .map_err(anyhow_to_compiler_bug)?;

    if let Some(linked_type_snapshot) = maybe_linked_type_snapshot {
        return match linked_type_snapshot {
            Ok(type_reference @ ResolvingLangType::TypeReference(_)) => {
                let resolved_type_reference = type_reference
                    .try_conv::<lang_types::LangType>()
                    .map_err(anyhow_to_compiler_bug)?;
                Err(
                    LangError::ConstraintUsedAsValue(ConstraintUsedAsValueError {
                        r#type: resolved_type_reference,
                    })
                    .pipe(Arc::new),
                )
            }
            Ok(value_type) => Ok(InferHintStepResult::ReplaceWith(vec![ctx
                .build_equal_to_hint(
                    Ok(value_type),
                    "type reference of value type",
                    inferred_from,
                )])),
            Err(err) => {
                // TODO: proxying errors needs to be _much_ easier
                if let ResolvingLangTypeSource::Breadcrumb(breadcrumbs_source) = source {
                    let node = ctx.node_at_path(breadcrumbs_source)?;
                    let mut proxy_err = match err.as_ref() {
                        LangError::ProxyError(e) => e.clone(),
                        _ => ProxyErrorError {
                            actual_error: err.clone(),
                            proxy_chain: vec![],
                        },
                    };
                    let position = SourcePosition {
                        path: ctx.file_path.clone(),
                        breadcrumbs: breadcrumbs_source.clone(),
                        position: node.info().position.clone(),
                    };
                    proxy_err
                        .proxy_chain
                        .insert(0, ErrorPosition::Source(position));
                    Err(Arc::new(LangError::ProxyError(proxy_err)))
                } else {
                    Err(err)
                }
            }
        };
    } else {
        return Ok(InferHintStepResult::Unchanged);
    }
}
