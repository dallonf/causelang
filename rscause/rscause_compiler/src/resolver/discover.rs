use super::{
    hints::TrackedHint,
    resolving_lang_types::{
        ResolvingCanonicalLangType, ResolvingLangTypeSource, ResolvingLangTypeValue,
        ResolvingLangTypesContext,
    },
};
use crate::{
    ast::{self, *},
    breadcrumbs::{Breadcrumbs, HasBreadcrumbs},
    compiled_file::ExternalFileDescriptor,
    error_types::{anyhow_to_compiler_bug, ImplementationTodoError, LangError},
    find_tag,
    lang_types::{self, CanonicalLangTypeId, LangParameter, LangTypeResult, PrimitiveLangType},
    resolver::{
        hints::{Hint, ManyPossibleResultHint},
        resolving_lang_types::{
            FunctionResolvingLangType, ResolvingLangType, ResolvingLangTypeLink,
        },
    },
    tags::NodeTag,
};
use crate::{prelude::*, resolver::resolving_lang_types::ResolvingLangParameter};
use anyhow::anyhow;
use std::{borrow::Cow, cell::RefCell, collections::HashMap, rc::Rc, sync::Arc};
use tap::Pipe;

#[expect(dead_code)]
pub fn discover_types(
    path: Arc<String>,
    file: Arc<ast::FileNode>,
    node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
    canonical_types: &HashMap<Arc<CanonicalLangTypeId>, Arc<lang_types::CanonicalLangType>>,
    external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>>,
) -> anyhow::Result<()> {
    let resolving_types_ctx = ResolvingLangTypesContext::new()
        .pipe(RefCell::new)
        .pipe(Rc::new);

    let mut ctx = DiscoverTypesContext {
        path,
        root_node: file.clone(),
        node_tags,
        // TODO
        canonical_types: Default::default(),
        external_files,
        resolving_types_ctx,
    };

    let descendants = BreadcrumbTreeNode::from(&file.clone()).descendants();
    for descendant in &descendants {
        let discovered_result = discover_type_for_any_ast_node(descendant, &mut ctx);
        if let Some(discovered_result) = discovered_result {
            let discovered_value: ResolvingLangTypeValue =
                discovered_result.unwrap_or_else(|err| ResolvingLangTypeValue::from_error(err));
            let mut resolving_types_ctx = ctx.resolving_types_ctx.try_borrow_mut()?;

            let source = ResolvingLangTypeSource::Breadcrumb(descendant.breadcrumbs().to_owned());

            let existing_variable = resolving_types_ctx.get_variable(&source);
            if let Some(existing_variable) = existing_variable {
                let variable = existing_variable.try_as_variable_ref().ok_or(anyhow!(
                    "trying to resolve {}, linked type was not a variable, but was {:?}",
                    descendant.breadcrumbs(),
                    &existing_variable
                ))?;
                let mut value = variable.value.borrow_mut();
                let new_value = match &*value {
                    ResolvingLangTypeValue::Known(resolving_lang_type) => {
                        ResolvingLangTypeValue::from_error(LangError::compiler_bug(format!(
                      "trying to resolve {} with {:?}, but it already has a known value: {:?}",
                      descendant.breadcrumbs(),
                      &discovered_value,
                      resolving_lang_type,
                  )))
                    }
                    ResolvingLangTypeValue::Hints(tracked_hints) => {
                        if tracked_hints.len() == 0 {
                            discovered_value
                        } else {
                            ResolvingLangTypeValue::from_error(LangError::compiler_bug(format!(
                                "trying to resolve {} with {:?}, but it's already accumulated hints: {:?}",
                                descendant.breadcrumbs(),
                                &discovered_value,
                                tracked_hints,
                            )))
                        }
                    }
                };
                *value = new_value;
            } else {
                resolving_types_ctx.add_variable(source, discovered_value)?;
            }
        }
    }
    Ok(())
}

struct DiscoverTypesContext {
    path: Arc<String>,
    root_node: Arc<ast::FileNode>,
    node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
    canonical_types: HashMap<Arc<CanonicalLangTypeId>, Arc<ResolvingCanonicalLangType>>,
    external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>>,

    resolving_types_ctx: Rc<RefCell<ResolvingLangTypesContext>>,
}
impl DiscoverTypesContext {
    fn get_tags_for_node<'a>(&'a self, node: &impl AstNode) -> Cow<'a, Vec<NodeTag>> {
        self.get_tags(node.breadcrumbs())
    }

    fn get_tags<'a>(&'a self, breadcrumbs: &Breadcrumbs) -> Cow<'a, Vec<NodeTag>> {
        self.node_tags
            .get(breadcrumbs)
            .map(|it| Cow::Borrowed(it))
            .unwrap_or(Cow::Owned(vec![]))
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

    fn get_link_for_node(&mut self, breadcrumbs: &Breadcrumbs) -> ResolvingLangTypeLink {
        let mut types_ctx = self.resolving_types_ctx.borrow_mut();
        let breadcrumbs_source = ResolvingLangTypeSource::Breadcrumb(breadcrumbs.to_owned());
        let existing = types_ctx.get_variable(&breadcrumbs_source);
        if let Some(existing) = existing {
            return existing.into();
        } else {
            types_ctx
                .add_variable(breadcrumbs_source, ResolvingLangTypeValue::Hints(vec![]))
                .expect("we just checked for the source above, shouldn't be possible for it to come back")
                .into()
        }
    }

    fn link_lang_type(
        &mut self,
        lang_type: LangTypeResult<ResolvingLangType>,
    ) -> ResolvingLangTypeLink {
        let mut types_ctx = self.resolving_types_ctx.borrow_mut();
        types_ctx.link_lang_type(lang_type).into()
    }

    fn create_id_variable(
        &mut self,
        hints: Vec<TrackedHint>,
    ) -> LangTypeResult<(u64, ResolvingLangTypeLink)> {
        let mut types_ctx = self.resolving_types_ctx.borrow_mut();
        let (id, variable) = types_ctx
            .create_id_variable(hints)
            .map_err(anyhow_to_compiler_bug)?;
        Ok((id, variable.into()))
    }
}

type DiscoverResult = LangTypeResult<ResolvingLangTypeValue>;

fn discover_type_for_any_ast_node(
    node: &AnyAstNode,
    ctx: &mut DiscoverTypesContext,
) -> Option<DiscoverResult> {
    match node {
        AnyAstNode::Identifier(_) => None,
        AnyAstNode::IdentifierTypeReference(node) => {
            Some(discover_type_for_identifier_type_reference(node, ctx))
        }
        AnyAstNode::FunctionTypeReference(node) => {
            Some(discover_type_for_function_type_reference(node, ctx))
        }
        AnyAstNode::Pattern(node) => Some(discover_type_for_pattern(node, ctx)),
        AnyAstNode::FunctionSignatureParameter(node) => {
            Some(discover_type_for_function_signature_parameter(node, ctx))
        }
        AnyAstNode::FunctionCallParameter(node) => {
            Some(discover_type_for_function_call_parameter(node, ctx))
        }
        AnyAstNode::File(_) => None,
        AnyAstNode::Import(_) => None,
        AnyAstNode::ImportPath(_) => None,
        AnyAstNode::ImportMapping(node) => Some(discover_type_for_import_mapping(node, ctx)),
        AnyAstNode::Function(node) => Some(discover_type_for_function(
            Some(&node.name),
            &node.params,
            node.return_type.as_ref(),
            &node.breadcrumbs(),
            ctx,
        )),
        AnyAstNode::NamedValue(node) => Some(discover_type_for_named_value(node, ctx)),
        AnyAstNode::ObjectType(object_type_node) => todo!(),
        AnyAstNode::SignalType(signal_type_node) => todo!(),
        AnyAstNode::ObjectField(object_field_node) => todo!(),
        AnyAstNode::OneOfType(one_of_type_node) => todo!(),
        AnyAstNode::BlockBody(block_body_node) => todo!(),
        AnyAstNode::SingleExpressionBody(single_expression_body_node) => todo!(),
        AnyAstNode::ExpressionStatement(expression_statement_node) => todo!(),
        AnyAstNode::DeclarationStatement(declaration_statement_node) => todo!(),
        AnyAstNode::EffectStatement(effect_statement_node) => todo!(),
        AnyAstNode::GroupExpression(group_expression_node) => todo!(),
        AnyAstNode::BlockExpression(block_expression_node) => todo!(),
        AnyAstNode::FunctionExpression(node) => Some(discover_type_for_function(
            None,
            &node.params,
            node.return_type.as_ref(),
            &node.breadcrumbs(),
            ctx,
        )),
        AnyAstNode::BranchExpression(branch_expression_node) => todo!(),
        AnyAstNode::IfBranchOption(if_branch_option_node) => todo!(),
        AnyAstNode::IsBranchOption(is_branch_option_node) => todo!(),
        AnyAstNode::ElseBranchOption(else_branch_option_node) => todo!(),
        AnyAstNode::LoopExpression(loop_expression_node) => todo!(),
        AnyAstNode::SetExpression(set_expression_node) => todo!(),
        AnyAstNode::CauseExpression(cause_expression_node) => todo!(),
        AnyAstNode::CallExpression(call_expression_node) => todo!(),
        AnyAstNode::PipeCallExpression(pipe_call_expression_node) => todo!(),
        AnyAstNode::MemberExpression(member_expression_node) => todo!(),
        AnyAstNode::IdentifierExpression(identifier_expression_node) => todo!(),
        AnyAstNode::StringLiteralExpression(_) => Some(Ok(ResolvingLangType::Primitive(
            PrimitiveLangType::Text,
        )
        .into())),
        AnyAstNode::NumberLiteralExpression(_) => Some(Ok(ResolvingLangType::Primitive(
            PrimitiveLangType::Number,
        )
        .into())),
        AnyAstNode::ReturnExpression(_) => Some(Ok(ResolvingLangTypeValue::from_type(
            ResolvingLangType::NeverContinues,
        ))),
        AnyAstNode::BreakExpression(_) => Some(Ok(ResolvingLangTypeValue::from_type(
            ResolvingLangType::NeverContinues,
        ))),
    }
}

fn discover_type_for_identifier_type_reference(
    node: &IdentifierTypeReferenceNode,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let tags = ctx.get_tags_for_node(node);
    let reference_tag =
        find_tag!(&tags, NodeTag::ValueComesFrom).ok_or(Arc::new(LangError::NotInScope))?;
    let source_node = ctx.node_at_path(&reference_tag.source)?;
    let source_node_type = ctx.get_link_for_node(source_node.breadcrumbs());
    Ok(ResolvingLangTypeValue::from_link(
        source_node_type,
        "IdentifierTypeReference",
    ))
}

fn discover_type_for_function_type_reference(
    node: &FunctionTypeReferenceNode,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let params = node
        .params
        .iter()
        .map(|it| {
            let value_type = it
                .type_reference
                .as_ref()
                .map(|it| ctx.get_link_for_node(it.breadcrumbs()))
                .unwrap_or_else(|| {
                    ctx.link_lang_type(Err(LangError::ImplementationTodo(
                        ImplementationTodoError {
                            description: "Function type parameters must have type annotations"
                                .into(),
                        },
                    )
                    .into()))
                });
            ResolvingLangParameter {
                name: it.name.text.clone(),
                value_type: ResolvingLangTypeLink::from(value_type),
            }
        })
        .collect_vec();

    let return_type = ctx.get_link_for_node(node.return_type.breadcrumbs());

    let function_type = ResolvingLangType::Function(FunctionResolvingLangType {
        name: None,
        params,
        return_type: return_type.into(),
    });

    Ok(ResolvingLangType::TypeReference(ctx.link_lang_type(function_type.into())).into())
}

fn discover_type_for_pattern(node: &PatternNode, ctx: &mut DiscoverTypesContext) -> DiscoverResult {
    let type_reference = ctx.get_link_for_node(node.type_reference.breadcrumbs());
    let value_type = ctx.create_id_variable(vec![TrackedHint::new(
        Hint::ReferencedType(type_reference.clone()),
        "patterns are represented by the value type they match",
        None,
    )])?;
    Ok(ResolvingLangTypeValue::from_link(
        value_type.1,
        "values matched by pattern",
    ))
}

fn discover_type_for_function_signature_parameter(
    node: &FunctionSignatureParameterNode,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let type_reference_node = node.type_reference.as_ref().ok_or(
        LangError::ImplementationTodo(ImplementationTodoError {
            description: "Function type parameters must have type annotations".into(),
        })
        .pipe(Arc::new),
    )?;
    let type_reference = ctx.get_link_for_node(type_reference_node.breadcrumbs());
    let value_type = ctx.create_id_variable(vec![TrackedHint::new(
        Hint::ReferencedType(type_reference.clone()),
        "patterns are represented by the value type they match",
        None,
    )])?;
    Ok(ResolvingLangTypeValue::from_link(
        value_type.1,
        "value of function parameter",
    ))
}

fn discover_type_for_function_call_parameter(
    node: &FunctionCallParameterNode,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let expression_value = ctx.get_link_for_node(node.value.breadcrumbs());
    Ok(ResolvingLangTypeValue::from_link(
        expression_value,
        "value of function call parameter",
    ))
}

fn discover_type_for_import_mapping(
    node: &ImportMappingNode,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let tags = ctx.get_tags_for_node(node);

    let comes_from_tag = find_tag!(&tags, NodeTag::ValueComesFrom);
    let bad_file_tag = comes_from_tag
        .and_then(|comes_from_tag| ctx.node_tags.get(&comes_from_tag.source))
        .and_then(|source_tags| find_tag!(&source_tags, NodeTag::BadFileReference));
    if bad_file_tag.is_some() {
        return Err(LangError::ImportPathInvalid.into());
    }

    let references_file_tag =
        find_tag!(tags, NodeTag::ReferencesFile).ok_or(LangError::compiler_bug(format!(
            "Missing ReferencesFile tag on {}",
            node.breadcrumbs()
        )))?;
    let external_file = ctx
        .external_files
        .get(&references_file_tag.path)
        .ok_or(LangError::FileNotFound.pipe(Arc::new))?;

    let export = external_file
        .exports
        .get(&node.source_name.text)
        .ok_or(LangError::ExportNotFound.pipe(Arc::new))?;

    let export_link = ResolvingLangTypeLink::import_type(
        &mut ctx.resolving_types_ctx.borrow_mut(),
        Ok(export.clone()),
    )
    .map_err(anyhow_to_compiler_bug)?;

    Ok(ResolvingLangTypeValue::from_link(
        export_link,
        format!(
            "import {} from {}",
            node.source_name.text, references_file_tag.path
        ),
    ))
}

/// Gets type for any function (both declaration and expression)
fn discover_type_for_function(
    name_node: Option<&ast::IdentifierNode>,
    param_nodes: &[Arc<FunctionSignatureParameterNode>],
    return_type_node: Option<&ast::TypeReferenceNode>,
    breadcrumbs: &Breadcrumbs,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let name = name_node.map(|it| it.text.clone());

    let explicit_return_type = return_type_node
        .map(|it| ctx.get_link_for_node(it.breadcrumbs()))
        .map(|it| -> LangTypeResult<ResolvingLangTypeLink> {
            ctx.create_id_variable(vec![TrackedHint::new(
                Hint::ReferencedType(it),
                "explicit function return",
                None,
            )])?
            .1
            .pipe(Ok)
        })
        .transpose()?;

    let return_type = explicit_return_type.map(Ok).unwrap_or_else(
        || -> LangTypeResult<ResolvingLangTypeLink> {
            let tags = ctx.get_tags(breadcrumbs).to_vec();
            let result_hint = tags
                .iter()
                .filter_map(|it| match it {
                    NodeTag::FunctionCanReturnTypeOf(tag) => {
                        let type_of_return = ctx.get_link_for_node(&tag.return_expression_value);
                        Some(ManyPossibleResultHint::Result(
                            type_of_return,
                            tag.return_expression_value.to_owned(),
                        ))
                    }
                    NodeTag::FunctionCanReturnAction(tag) => Some(ManyPossibleResultHint::Action(
                        tag.return_expression.to_owned(),
                    )),
                    _ => None,
                })
                .collect_vec()
                .pipe(|results| {
                    TrackedHint::new(
                        Hint::ManyPossibleResults(Rc::new(results)),
                        "function return types",
                        None,
                    )
                });

            ctx.create_id_variable(vec![result_hint])?.1.pipe(Ok)
        },
    )?;

    let params = param_nodes
        .into_iter()
        .map(|param_node| {
            let value_type = ctx.get_link_for_node(param_node.breadcrumbs());
            ResolvingLangParameter {
                name: param_node.name.text.clone(),
                value_type,
            }
        })
        .collect();

    Ok(ResolvingLangTypeValue::from_type(
        FunctionResolvingLangType {
            name,
            params,
            return_type,
        },
    ))
}

fn discover_type_for_named_value(
    node: &NamedValueNode,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let explicit_type = node
        .type_annotation
        .as_ref()
        .map(|type_node| ctx.get_link_for_node(type_node.breadcrumbs()))
        .map(|type_reference_link| -> LangTypeResult<_> {
            ctx.create_id_variable(vec![TrackedHint::new(
                Hint::ReferencedType(type_reference_link),
                "named value is the type of its annotation",
                None,
            )])?
            .1
            .pipe(Ok)
        })
        .transpose()?;

    let result_type =
        explicit_type.unwrap_or_else(|| ctx.get_link_for_node(node.value.breadcrumbs()));

    Ok(ResolvingLangTypeValue::from_link(
        result_type,
        "named value",
    ))
}
