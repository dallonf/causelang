use std::sync::Arc;
use std::{collections::HashMap, rc::Rc};

use anyhow::anyhow;

use crate::ast::{AstNode, BreadcrumbTreeNode};
use crate::breadcrumbs::Breadcrumbs;
use crate::error_types::{
    ActionIncompatibleWithValueTypesError, ActionIncompatibleWithValueTypesValueType,
    ConstraintUsedAsValueError, ErrorPosition, LangError, ProxyErrorError, SourcePosition,
    ValueUsedAsConstraintError,
};
use crate::lang_types::CanonicalLangTypeId;
use crate::{ast, lang_types, prelude::*};
use crate::{error_types::anyhow_to_compiler_bug, lang_types::LangTypeResult};

use super::hints::OneOfOptionHint;
use super::{
    hints::{Hint, TrackedHint},
    resolving_lang_types::*,
};

const MAX_ITERATIONS: u16 = 10_000;

pub fn infer_types(
    file_path: Arc<String>,
    root_node: Arc<ast::FileNode>,
    ctx: ResolvingLangTypesContext,
    canonical_types: HashMap<CanonicalLangTypeId, Arc<ResolvingCanonicalLangType>>,
) -> anyhow::Result<()> {
    let mut ctx = InferTypesContext {
        file_path,
        root_node,
        resolving_types_ctx: ctx,
        canonical_types,
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
                .unwrap_or_else(|err| InferVariableStepResult::Solved {
                    result: ctx.resolving_types_ctx.link_lang_type(Err(err)).into(),
                    inferred_from: hints.clone(),
                });

            match variable_step_result {
                InferVariableStepResult::Solved {
                    result,
                    inferred_from,
                } => {
                    solved_variable_diagnostics.insert(source.clone(), inferred_from);
                    *unsolved_variable_mut = ResolvingLangTypeValue::Known(result);
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
    canonical_types: HashMap<CanonicalLangTypeId, Arc<ResolvingCanonicalLangType>>,
}
impl InferTypesContext {
    fn build_equal_to_hint(
        &mut self,
        lang_type: ResolvingLangType,
        reason: impl Into<String>,
        source_hint: &TrackedHint,
    ) -> TrackedHint {
        TrackedHint::new(
            Hint::EqualTo(
                self.resolving_types_ctx
                    .link_lang_type(Ok(lang_type))
                    .into(),
            ),
            reason.into(),
            Some(vec![source_hint.to_owned()]),
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

    /// Wraps a LangError in a ProxyError for the current source.
    /// Currently a no-op for Id sources.
    fn proxy_error(
        &self,
        error: Arc<LangError>,
        source: &ResolvingLangTypeSource,
    ) -> Arc<LangError> {
        if let ResolvingLangTypeSource::Breadcrumb(breadcrumbs_source) = source {
            let node = match self.node_at_path(breadcrumbs_source) {
                Ok(it) => it,
                Err(err) => return err,
            };
            let mut proxy_err = match error.as_ref() {
                LangError::ProxyError(e) => e.clone(),
                _ => ProxyErrorError {
                    actual_error: error.clone(),
                    proxy_chain: vec![],
                },
            };
            let position = SourcePosition {
                path: self.file_path.clone(),
                breadcrumbs: breadcrumbs_source.clone(),
                position: node.info().position.clone(),
            };
            proxy_err
                .proxy_chain
                .insert(0, ErrorPosition::Source(position));
            Arc::new(LangError::ProxyError(proxy_err))
        } else {
            error
        }
    }

    /// Designed so you can `?` away errors and not have to think about them.
    /// Returns Ok(None) if the link hasn't been solved yet.
    fn read_snapshot_proxying_errors(
        &self,
        link: &ResolvingLangTypeLink,
        source: &ResolvingLangTypeSource,
    ) -> LangTypeResult<Option<ResolvingLangType>> {
        let maybe_linked_type_snapshot = link
            .linked_type()?
            .get_snapshot_value()
            .map_err(anyhow_to_compiler_bug)?;

        let linked_type_snapshot = match maybe_linked_type_snapshot {
            Some(it) => it,
            None => return Ok(None),
        };

        match linked_type_snapshot {
            Ok(value) => Ok(Some(value)),
            Err(err) => Err(self.proxy_error(err, source)),
        }
    }

    fn get_source_position(&self, breadcrumbs: &Breadcrumbs) -> LangTypeResult<SourcePosition> {
        let position = self.node_at_path(&breadcrumbs)?.info().position;
        let source_position = SourcePosition {
            path: self.file_path.clone(),
            breadcrumbs: breadcrumbs.clone(),
            position,
        };
        Ok(source_position)
    }
}

#[derive(Debug, Clone)]
enum InferVariableStepResult {
    Solved {
        result: ResolvingLangTypeLink,
        inferred_from: Vec<TrackedHint>,
    },
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
        let hint_step_result: InferHintStepResult = infer_hint_step(hint, source, ctx)?;
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

    // An UnreachableIfNeverContinues hint will either be removed,
    // or will solve with NeverContinues
    {
        let unreachable_if_never_continues = hints
            .iter()
            .enumerate()
            .filter_map(|(i, it)| {
                if let Hint::UnreachableIfNeverContinues(link) = &it.hint {
                    Some((i, it, link))
                } else {
                    None
                }
            })
            .collect_vec();
        let solved_hints = unreachable_if_never_continues
            .iter()
            .filter_map(|(i, tracked_hint, link)| {
                let snapshot = link
                    .linked_type()
                    .and_then(|it| it.get_snapshot_value().map_err(anyhow_to_compiler_bug))
                    .transpose()?;
                Some(snapshot.map(|snapshot| (*i, *tracked_hint, snapshot)))
            })
            .collect::<LangTypeResult<Vec<_>>>()?;
        let (never_continues, has_value): (Vec<_>, Vec<_>) = solved_hints
            .iter()
            .cloned()
            .partition(|(_, _, link)| matches!(link, Ok(ResolvingLangType::NeverContinues)));
        if never_continues.len() > 0 {
            return Ok(InferVariableStepResult::Solved {
                result: ctx
                    .resolving_types_ctx
                    .link_lang_type(Ok(ResolvingLangType::NeverContinues))
                    .into(),
                inferred_from: never_continues
                    .into_iter()
                    .map(|(_, hint, _)| hint.clone())
                    .collect(),
            });
        }
        // Remove any hints of this type that point to a non-NeverContinues value
        for (i, _, _) in has_value {
            remove_hint_indices.push(i);
        }
        // no need to early return here; removing a few UnreachableIfNeverContinues
        // hints won't change the outcome of other rules
    }

    // A single EqualTo hint means it's been solved!
    if hints.len() == 1 && matches!(hints[0].hint, Hint::EqualTo(_)) {
        return Ok(InferVariableStepResult::Solved {
            result: hints[0].hint.try_as_equal_to_ref().unwrap().clone(),
            inferred_from: vec![hints[0].to_owned()],
        });
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
    ctx: &mut InferTypesContext,
) -> LangTypeResult<InferHintStepResult> {
    match &hint.hint {
        Hint::ReferencedType(link) => infer_referenced_type_hint(link, source, hint, ctx)?,
        Hint::TypeReference(link) => infer_type_reference_hint(link, source, hint, ctx)?,
        Hint::OneOf(one_of_hints) => infer_one_of_hint(one_of_hints.clone(), source, hint, ctx)?,
        Hint::CallResult(link) => infer_call_result_hint(link, source, hint, ctx)?,
        Hint::CauseResult(resolving_lang_type_link) => todo!(),
        Hint::MemberOf(resolving_lang_type_link, field_name) => todo!(),
        // handled with more complex rules
        Hint::EqualTo(_) => InferHintStepResult::Unchanged,
        Hint::UnreachableIfNeverContinues(_) => InferHintStepResult::Unchanged,
    }
    .pipe(Ok)
}

fn infer_referenced_type_hint(
    link: &ResolvingLangTypeLink,
    source: &ResolvingLangTypeSource,
    hint: &TrackedHint,
    ctx: &mut InferTypesContext,
) -> LangTypeResult<InferHintStepResult> {
    let linked_type_snapshot = match ctx.read_snapshot_proxying_errors(link, source)? {
        Some(it) => it,
        None => return Ok(InferHintStepResult::Unchanged),
    };

    match linked_type_snapshot {
        ResolvingLangType::TypeReference(referenced_type) => {
            let hint = TrackedHint::new(
                Hint::EqualTo(referenced_type),
                "referenced type",
                Some(vec![hint.clone()]),
            );
            Ok(InferHintStepResult::ReplaceWith(vec![hint]))
        }
        value_type => {
            let resolved_value_type = value_type
                .try_conv::<lang_types::LangType>()
                .map_err(anyhow_to_compiler_bug)
                .map(Arc::new);
            Err(
                LangError::ValueUsedAsConstraint(ValueUsedAsConstraintError {
                    r#type: resolved_value_type,
                })
                .pipe(Arc::new),
            )
        }
    }
}

fn infer_type_reference_hint(
    link: &ResolvingLangTypeLink,
    source: &ResolvingLangTypeSource,
    hint: &TrackedHint,
    ctx: &mut InferTypesContext,
) -> LangTypeResult<InferHintStepResult> {
    let linked_type_snapshot = match ctx.read_snapshot_proxying_errors(link, source)? {
        Some(it) => it,
        None => return Ok(InferHintStepResult::Unchanged),
    };

    match linked_type_snapshot {
        type_reference @ ResolvingLangType::TypeReference(_) => {
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
        value_type => {
            Ok(InferHintStepResult::ReplaceWith(vec![ctx
                .build_equal_to_hint(
                    value_type,
                    "type reference of value type",
                    hint,
                )]))
        }
    }
}

fn infer_one_of_hint(
    option_hints: Rc<Vec<OneOfOptionHint>>,
    _source: &ResolvingLangTypeSource,
    hint: &TrackedHint,
    ctx: &mut InferTypesContext,
) -> LangTypeResult<InferHintStepResult> {
    let solved_hints = option_hints
        .iter()
        .map(
            |hint| -> LangTypeResult<Option<(ResolvingLangType, Breadcrumbs)>> {
                let maybe_linked_type_snapshot = hint
                    .value
                    .linked_type()?
                    .get_snapshot_value()
                    .map_err(anyhow_to_compiler_bug)?;

                maybe_linked_type_snapshot
                    .transpose()
                    .map_err(|err| {
                        ctx.proxy_error(
                            err,
                            &ResolvingLangTypeSource::Breadcrumb(hint.source_breadcrumbs.clone()),
                        )
                    })?
                    .map(|solved_type| (solved_type, hint.source_breadcrumbs.clone()))
                    .pipe(Ok)
            },
        )
        .filter_map(|result| result.transpose())
        .collect_vec();

    if solved_hints.len() < option_hints.len() {
        // not all of the options are solved
        return Ok(InferHintStepResult::Unchanged);
    }

    let (actions, values): (Vec<_>, Vec<_>) = solved_hints
        .iter()
        .filter_map(|result| result.as_ref().ok())
        .partition(|it| match it.0 {
            ResolvingLangType::Action => true,
            _ => false,
        });

    if actions.len() > 0 && values.len() > 0 {
        return Err(LangError::ActionIncompatibleWithValueTypes(
            ActionIncompatibleWithValueTypesError {
                actions: actions
                    .into_iter()
                    .map(|(_, breadcrumbs)| ctx.get_source_position(breadcrumbs))
                    .collect::<LangTypeResult<Vec<_>>>()?,
                types: values
                    .into_iter()
                    .map(|(value, breadcrumbs)| -> LangTypeResult<_> {
                        let source_position = ctx.get_source_position(breadcrumbs)?;
                        let value_type = value
                            .clone()
                            .try_conv::<lang_types::LangType>()
                            .map_err(anyhow_to_compiler_bug)?
                            .pipe(Arc::new);
                        ActionIncompatibleWithValueTypesValueType {
                            r#type: value_type,
                            position: source_position,
                        }
                        .pipe(Ok)
                    })
                    .collect::<LangTypeResult<Vec<_>>>()?
                    .pipe(Some),
            },
        )
        .pipe(Arc::new));
    }

    let new_type =
        OneOfResolvingLangType::new(option_hints.iter().map(|hint| hint.value.clone()).collect());

    Ok(InferHintStepResult::ReplaceWith(vec![
        ctx.build_equal_to_hint(new_type.into(), "one of", hint)
    ]))
}

fn infer_call_result_hint(
    link: &ResolvingLangTypeLink,
    source: &ResolvingLangTypeSource,
    hint: &TrackedHint,
    ctx: &mut InferTypesContext,
) -> LangTypeResult<InferHintStepResult> {
    let callee_snapshot = ctx.read_snapshot_proxying_errors(link, source)?;
    let (reason, call_result) = match callee_snapshot {
        Some(ResolvingLangType::Function(it)) => ("function return value", it.return_type),
        Some(ResolvingLangType::TypeReference(type_reference)) => {
            let instance_snapshot = ctx.read_snapshot_proxying_errors(&type_reference, source)?;
            let instance = match instance_snapshot {
                Some(ResolvingLangType::Instance(it)) => it,
                Some(_) => return Err(LangError::NotCallable.into()),
                None => return Ok(InferHintStepResult::Unchanged),
            };
            (
                "construct an object",
                ctx.resolving_types_ctx
                    .link_lang_type(Ok(ResolvingLangType::Instance(instance)))
                    .into(),
            )
        }
        Some(ResolvingLangType::Instance(instance)) => {
            if instance.type_id.is_unique {
                (
                    "'construct' a unique instance",
                    ctx.resolving_types_ctx
                        .link_lang_type(Ok(ResolvingLangType::Instance(instance)))
                        .into(),
                )
            } else {
                return Err(LangError::NotCallable.into());
            }
        }
        Some(_) => return Err(LangError::NotCallable.into()),
        None => return Ok(InferHintStepResult::Unchanged),
    };

    let new_hint = TrackedHint::new(Hint::EqualTo(call_result), reason, Some(vec![hint.clone()]));
    Ok(InferHintStepResult::ReplaceWith(vec![new_hint]))
}
