use super::{
    hints::TrackedHint,
    resolving_lang_types::{
        ObjectResolvingCanonicalLangType, ResolvingCanonicalLangType, ResolvingCanonicalTypeField,
        ResolvingLangTypeSource, ResolvingLangTypeValue, ResolvingLangTypesContext,
        SignalResolvingCanonicalLangType,
    },
};
use crate::{
    ast::{self, *},
    breadcrumbs::{Breadcrumbs, HasBreadcrumbs},
    compiled_file::ExternalFileDescriptor,
    error_types::{anyhow_to_compiler_bug, ImplementationTodoError, LangError},
    find_tag, find_tags,
    lang_types::{
        CanonicalLangTypeCategory, CanonicalLangTypeId, LangTypeResult, PrimitiveLangType,
    },
    resolver::{
        hints::{Hint, OneOfOptionHint},
        resolving_lang_types::*,
    },
    tags::NodeTag,
};
use crate::{prelude::*, resolver::resolving_lang_types::ResolvingLangParameter};
use anyhow::anyhow;
use std::{borrow::Cow, collections::HashMap, rc::Rc, sync::Arc};
use tap::Pipe;

#[expect(dead_code)]
pub fn discover_types(
    path: Arc<String>,
    file: Arc<ast::FileNode>,
    node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
    external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>>,
) -> anyhow::Result<()> {
    let resolving_types_ctx = ResolvingLangTypesContext::new();

    let mut ctx = DiscoverTypesContext {
        path,
        root_node: file.clone(),
        node_tags,
        external_files,

        resolving_types_ctx,
        new_canonical_types: Default::default(),
    };

    let descendants = BreadcrumbTreeNode::from(&file.clone()).descendants();
    for descendant in &descendants {
        let discovered_result = discover_type_for_any_ast_node(descendant, &mut ctx);
        if let Some(discovered_result) = discovered_result {
            let discovered_value: ResolvingLangTypeValue =
                discovered_result.unwrap_or_else(|err| ResolvingLangTypeValue::from_error(err));

            let source = ResolvingLangTypeSource::Breadcrumb(descendant.breadcrumbs().to_owned());

            let existing_variable = ctx.resolving_types_ctx.get_variable(&source);
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
                ctx.resolving_types_ctx
                    .add_variable(source, discovered_value)?;
            }
        }
    }
    Ok(())
}

struct DiscoverTypesContext {
    path: Arc<String>,
    root_node: Arc<ast::FileNode>,
    node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
    external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>>,

    resolving_types_ctx: ResolvingLangTypesContext,
    new_canonical_types: HashMap<CanonicalLangTypeId, Arc<ResolvingCanonicalLangType>>,
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
        let breadcrumbs_source = ResolvingLangTypeSource::Breadcrumb(breadcrumbs.to_owned());
        let existing = self.resolving_types_ctx.get_variable(&breadcrumbs_source);
        if let Some(existing) = existing {
            return existing.into();
        } else {
            self.resolving_types_ctx
                .add_variable(breadcrumbs_source, ResolvingLangTypeValue::Hints(vec![]))
                .expect("we just checked for the source above, shouldn't be possible for it to come back")
                .into()
        }
    }

    fn link_lang_type(
        &mut self,
        lang_type: LangTypeResult<ResolvingLangType>,
    ) -> ResolvingLangTypeLink {
        self.resolving_types_ctx.link_lang_type(lang_type).into()
    }

    fn create_id_variable(
        &mut self,
        hints: Vec<TrackedHint>,
    ) -> LangTypeResult<(u64, ResolvingLangTypeLink)> {
        let (id, variable) = self
            .resolving_types_ctx
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
        AnyAstNode::ObjectType(node) => Some(discover_type_for_object_type(node, ctx)),
        AnyAstNode::SignalType(node) => Some(discover_type_for_signal_type(node, ctx)),
        AnyAstNode::ObjectField(node) => Some(discover_type_for_object_field(node, ctx)),
        AnyAstNode::OneOfType(node) => Some(discover_type_for_one_of_type(node, ctx)),
        AnyAstNode::BlockBody(node) => Some(discover_type_for_block_body(node, ctx)),
        AnyAstNode::SingleExpressionBody(node) => {
            Some(discover_type_for_single_expression_body(node, ctx))
        }
        AnyAstNode::ExpressionStatement(node) => {
            Some(discover_type_for_expression_statement(node, ctx))
        }
        AnyAstNode::DeclarationStatement(node) => {
            Some(discover_type_for_declaration_statement(node, ctx))
        }
        AnyAstNode::EffectStatement(_) => Some(Ok(ResolvingLangType::Action.into())),
        AnyAstNode::GroupExpression(node) => Some(Ok(ResolvingLangTypeValue::from_link(
            ctx.get_link_for_node(node.expression.breadcrumbs()),
            "group expression",
        ))),
        AnyAstNode::BlockExpression(node) => Some(Ok(ResolvingLangTypeValue::from_link(
            ctx.get_link_for_node(node.block.breadcrumbs()),
            "block expression",
        ))),
        AnyAstNode::FunctionExpression(node) => Some(discover_type_for_function(
            None,
            &node.params,
            node.return_type.as_ref(),
            &node.breadcrumbs(),
            ctx,
        )),
        AnyAstNode::BranchExpression(node) => Some(discover_type_for_branch_expression(node, ctx)),
        AnyAstNode::IfBranchOption(node) => Some(Ok(ResolvingLangTypeValue::from_link(
            ctx.get_link_for_node(node.breadcrumbs()),
            "option body",
        ))),
        AnyAstNode::IsBranchOption(node) => Some(Ok(ResolvingLangTypeValue::from_link(
            ctx.get_link_for_node(node.breadcrumbs()),
            "option body",
        ))),
        AnyAstNode::ElseBranchOption(node) => Some(Ok(ResolvingLangTypeValue::from_link(
            ctx.get_link_for_node(node.breadcrumbs()),
            "option body",
        ))),
        AnyAstNode::LoopExpression(node) => Some(discover_type_for_loop_expression(node, ctx)),
        AnyAstNode::SetExpression(node) => Some(Ok(ResolvingLangTypeValue::from_link(
            ctx.get_link_for_node(node.expression.breadcrumbs()),
            "set expression value",
        ))),
        AnyAstNode::CauseExpression(node) => Some(discover_type_for_cause_expression(node, ctx)),
        AnyAstNode::CallExpression(node) => Some(discover_type_for_call_expression(node, ctx)),
        AnyAstNode::PipeCallExpression(node) => {
            Some(discover_type_for_pipe_call_expression(node, ctx))
        }
        AnyAstNode::MemberExpression(node) => Some(discover_type_for_member_expression(node, ctx)),
        AnyAstNode::IdentifierExpression(node) => {
            Some(discover_type_for_identifier_expression(node, ctx))
        }
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

    let export_link =
        ResolvingLangTypeLink::import_type(&mut ctx.resolving_types_ctx, Ok(export.clone()))
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
                        Some(OneOfOptionHint {
                            source_breadcrumbs: tag.return_expression_value.to_owned(),
                            value: type_of_return,
                        })
                    }
                    NodeTag::FunctionCanReturnAction(tag) => Some(OneOfOptionHint {
                        source_breadcrumbs: tag.return_expression.to_owned(),
                        value: ctx.link_lang_type(Ok(ResolvingLangType::Action)),
                    }),
                    _ => None,
                })
                .collect_vec()
                .pipe(|results| {
                    TrackedHint::new(Hint::OneOf(Rc::new(results)), "function return types", None)
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

fn discover_type_for_object_type(
    node: &ObjectTypeNode,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let tags = ctx.get_tags_for_node(node);
    let canonical_id_tag =
        find_tag!(&tags, NodeTag::CanonicalIdInfo).ok_or(LangError::compiler_bug(format!(
            "Missing CanonicalIdInfo tag on {}",
            node.breadcrumbs()
        )))?;

    let type_id = CanonicalLangTypeId {
        path: ctx.path.clone(),
        parent_name: canonical_id_tag.parent_name.clone(),
        name: node.name.text.clone().into(),
        number: canonical_id_tag.index,
        category: CanonicalLangTypeCategory::Object,
        is_unique: node.fields.is_empty(),
    };

    let fields = node
        .fields
        .iter()
        .map(|field| {
            let field_type = ctx.get_link_for_node(field.breadcrumbs());
            ResolvingCanonicalTypeField {
                name: field.name.text.clone(),
                value_type: field_type,
            }
        })
        .collect_vec();

    let canonical_type = ObjectResolvingCanonicalLangType {
        type_id: type_id.clone(),
        fields,
    };
    ctx.new_canonical_types.insert(
        type_id.clone(),
        Arc::new(ResolvingCanonicalLangType::Object(canonical_type)),
    );

    let instance_type = ResolvingLangType::Instance(InstanceResolvingLangType {
        type_id: Arc::new(type_id.clone()),
    })
    .pipe(|it| ctx.link_lang_type(Ok(it)));
    let type_reference = ctx.link_lang_type(Ok(ResolvingLangType::TypeReference(instance_type)));

    Ok(ResolvingLangTypeValue::from_link(
        type_reference,
        "object type",
    ))
}

fn discover_type_for_signal_type(
    node: &SignalTypeNode,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let tags = ctx.get_tags_for_node(node);
    let canonical_id_tag =
        find_tag!(&tags, NodeTag::CanonicalIdInfo).ok_or(LangError::compiler_bug(format!(
            "Missing CanonicalIdInfo tag on {}",
            node.breadcrumbs()
        )))?;

    let type_id = CanonicalLangTypeId {
        path: ctx.path.clone(),
        parent_name: canonical_id_tag.parent_name.clone(),
        name: node.name.text.clone().into(),
        number: canonical_id_tag.index,
        category: CanonicalLangTypeCategory::Signal,
        is_unique: node.fields.is_empty(),
    };

    let fields = node
        .fields
        .iter()
        .map(|field| {
            let field_type = ctx.get_link_for_node(field.breadcrumbs());
            ResolvingCanonicalTypeField {
                name: field.name.text.clone(),
                value_type: field_type,
            }
        })
        .collect_vec();

    let result = match &node.result {
        Some(result_node) => ctx.get_link_for_node(result_node.breadcrumbs()).pipe(
            |result_type_reference| -> LangTypeResult<_> {
                ctx.create_id_variable(vec![TrackedHint::new(
                    Hint::ReferencedType(result_type_reference),
                    "signal result",
                    None,
                )])?
                .1
                .pipe(Ok)
            },
        )?,
        None => ctx.link_lang_type(Ok(ResolvingLangType::Action)),
    };

    let canonical_type = SignalResolvingCanonicalLangType {
        type_id: type_id.clone(),
        fields,
        result,
    };

    ctx.new_canonical_types.insert(
        type_id.clone(),
        Arc::new(ResolvingCanonicalLangType::Signal(canonical_type)),
    );

    let instance_type = ResolvingLangType::Instance(InstanceResolvingLangType {
        type_id: Arc::new(type_id.clone()),
    })
    .pipe(|it| ctx.link_lang_type(Ok(it)));
    let type_reference = ctx.link_lang_type(Ok(ResolvingLangType::TypeReference(instance_type)));

    Ok(ResolvingLangTypeValue::from_link(
        type_reference,
        "signal type",
    ))
}

fn discover_type_for_object_field(
    node: &ObjectFieldNode,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let type_reference = ctx.get_link_for_node(node.type_annotation.breadcrumbs());
    let value_type = ctx.create_id_variable(vec![TrackedHint::new(
        Hint::ReferencedType(type_reference),
        "object field type reference",
        None,
    )])?;

    Ok(ResolvingLangTypeValue::from_link(
        value_type.1,
        "object field",
    ))
}

fn discover_type_for_one_of_type(
    node: &OneOfTypeNode,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let options = node
        .options
        .iter()
        .map(|option_node| -> LangTypeResult<_> {
            let type_reference = ctx.get_link_for_node(option_node.breadcrumbs());
            let value_type = ctx
                .create_id_variable(vec![TrackedHint::new(
                    Hint::ReferencedType(type_reference),
                    "oneof option type reference",
                    None,
                )])?
                .1;

            Ok(OneOfOptionHint {
                source_breadcrumbs: option_node.breadcrumbs().to_owned(),
                value: value_type,
            })
        })
        .collect::<LangTypeResult<Vec<_>>>()?;

    let value_type = ctx
        .create_id_variable(vec![TrackedHint::new(
            Hint::OneOf(Rc::new(options)),
            "oneof type",
            None,
        )])?
        .1;
    let type_reference = ctx
        .create_id_variable(vec![TrackedHint::new(
            Hint::ReferencedType(value_type),
            "oneof type reference",
            None,
        )])?
        .1;

    Ok(ResolvingLangTypeValue::from_link(
        type_reference,
        "oneof type",
    ))
}

fn discover_type_for_block_body(
    node: &BlockBodyNode,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let final_result = node
        .result
        .as_ref()
        .map(|it| ctx.get_link_for_node(it.breadcrumbs()))
        .unwrap_or_else(|| ctx.link_lang_type(Ok(ResolvingLangType::Action)));

    let statements_before_result = node
        .statements
        .iter()
        .map(|statement_node| ctx.get_link_for_node(statement_node.breadcrumbs()))
        .collect_vec();

    let mut hints = statements_before_result
        .iter()
        .map(|statement| {
            TrackedHint::new(
                Hint::UnreachableIfNeverContinues(statement.clone()),
                "statement potentially making the block result unreachable",
                None,
            )
        })
        .collect_vec();
    hints.push(TrackedHint::new(
        Hint::EqualTo(final_result.clone()),
        "block result expression",
        None,
    ));

    let result_type = ctx.create_id_variable(hints)?.1;

    Ok(ResolvingLangTypeValue::from_link(
        result_type,
        "block result",
    ))
}

fn discover_type_for_single_expression_body(
    node: &SingleExpressionBodyNode,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let result_type = ctx.get_link_for_node(node.expression.breadcrumbs());

    Ok(ResolvingLangTypeValue::from_link(
        result_type,
        "single expression body",
    ))
}

fn discover_type_for_expression_statement(
    node: &ExpressionStatementNode,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let expression_type = ctx.get_link_for_node(node.expression.breadcrumbs());
    let statement_type = ctx
        .create_id_variable(vec![TrackedHint::new(
            Hint::UnreachableIfNeverContinues(expression_type),
            "result might make expression statement unreachable",
            None,
        )])?
        .1;

    Ok(ResolvingLangTypeValue::from_link(
        statement_type,
        "expression statement",
    ))
}

fn discover_type_for_declaration_statement(
    node: &DeclarationStatementNode,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let declaration_type = ctx.get_link_for_node(node.declaration.breadcrumbs());
    let statement_type = ctx
        .create_id_variable(vec![TrackedHint::new(
            Hint::UnreachableIfNeverContinues(declaration_type),
            "result might make declaration statement unreachable",
            None,
        )])?
        .1;

    Ok(ResolvingLangTypeValue::from_link(
        statement_type,
        "declaration statement",
    ))
}

fn discover_type_for_branch_expression(
    node: &BranchExpressionNode,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let result_hints = node
        .branches
        .iter()
        .map(|branch_node| {
            let branch_result_value = ctx.get_link_for_node(branch_node.breadcrumbs());
            OneOfOptionHint {
                source_breadcrumbs: branch_node.breadcrumbs().to_owned(),
                value: branch_result_value,
            }
        })
        .collect_vec();

    Ok(ResolvingLangTypeValue::from_link(
        ctx.create_id_variable(vec![TrackedHint::new(
            Hint::OneOf(result_hints.into()),
            "branch can return any of its options",
            None,
        )])?
        .1,
        "branch expression",
    ))
}

fn discover_type_for_loop_expression(
    node: &LoopExpressionNode,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let tags = ctx.get_tags_for_node(node);
    let breaks = find_tags!(&tags, NodeTag::LoopBreaksAt).collect_vec();

    if breaks.is_empty() {
        return Ok(ResolvingLangTypeValue::from_type(
            ResolvingLangType::NeverContinues,
        ));
    }

    let break_type_hints = breaks
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
                        OneOfOptionHint {
                            source_breadcrumbs: with_value.breadcrumbs().to_owned(),
                            value: ctx.get_link_for_node(with_value.breadcrumbs()),
                        }
                    } else {
                        OneOfOptionHint {
                            source_breadcrumbs: break_expression.breadcrumbs().to_owned(),
                            value: ctx.link_lang_type(Ok(ResolvingLangType::Action)),
                        }
                    }
                })?;

            Ok(break_expression_type)
        })
        .collect::<LangTypeResult<Vec<_>>>()?;

    let loop_result_type = ctx
        .create_id_variable(vec![TrackedHint::new(
            Hint::OneOf(break_type_hints.into()),
            "loop result can come from any of its break expressions",
            None,
        )])?
        .1;

    Ok(ResolvingLangTypeValue::from_link(
        loop_result_type,
        "loop expression result",
    ))
}

fn discover_type_for_call_expression(
    node: &CallExpressionNode,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let function_type = ctx.get_link_for_node(node.callee.breadcrumbs());
    let function_result = ctx
        .create_id_variable(vec![TrackedHint::new(
            Hint::CallResult(function_type),
            "callee result",
            None,
        )])?
        .1;

    Ok(ResolvingLangTypeValue::from_link(
        function_result,
        "call expression result",
    ))
}

fn discover_type_for_pipe_call_expression(
    node: &PipeCallExpressionNode,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let function_type = ctx.get_link_for_node(node.callee.breadcrumbs());
    let function_result = ctx
        .create_id_variable(vec![TrackedHint::new(
            Hint::CallResult(function_type),
            "pipe call result",
            None,
        )])?
        .1;

    Ok(ResolvingLangTypeValue::from_link(
        function_result,
        "pipe call expression result",
    ))
}

fn discover_type_for_cause_expression(
    node: &CauseExpressionNode,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let signal_type = ctx.get_link_for_node(node.signal.breadcrumbs());
    let signal_result = ctx
        .create_id_variable(vec![TrackedHint::new(
            Hint::CauseResult(signal_type),
            "signal result",
            None,
        )])?
        .1;

    Ok(ResolvingLangTypeValue::from_link(
        signal_result,
        "cause expression result",
    ))
}

fn discover_type_for_member_expression(
    node: &MemberExpressionNode,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let object_type = ctx.get_link_for_node(node.object_expression.breadcrumbs());
    let member_name = node.member_identifier.text.clone();
    let member_type = ctx
        .create_id_variable(vec![TrackedHint::new(
            Hint::MemberOf(object_type, member_name),
            "member of object",
            None,
        )])?
        .1;
    Ok(ResolvingLangTypeValue::from_link(
        member_type,
        "member expression result",
    ))
}

fn discover_type_for_identifier_expression(
    node: &IdentifierExpressionNode,
    ctx: &mut DiscoverTypesContext,
) -> DiscoverResult {
    let tags = ctx.get_tags_for_node(node);
    let comes_from_tag = find_tag!(&tags, NodeTag::ValueComesFrom).ok_or(Arc::new(
        LangError::compiler_bug("IdentifierExpressionNode didn't have a ValueComesFrom tag"),
    ))?;
    let source_type = ctx.get_link_for_node(&comes_from_tag.source);
    Ok(ResolvingLangTypeValue::from_link(
        source_type,
        format!("resolved identifier: {}", &node.identifier.text),
    ))
}
