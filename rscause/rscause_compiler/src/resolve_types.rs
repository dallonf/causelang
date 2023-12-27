use crate::ast::{self, AnyAstNode, AstNode, BreadcrumbTreeNode};
use crate::breadcrumbs::{Breadcrumbs, HasBreadcrumbs};
use crate::error_types::{
    compiler_bug_error, CompilerBugError, ErrorPosition, LangError, MismatchedTypeError,
    SourcePosition, ValueUsedAsConstraintError,
};
use crate::find_tag;
use crate::lang_types::{
    AnyInferredLangType, CanonicalLangType, CanonicalLangTypeId, FunctionLangType, InferredType,
    LangType, PrimitiveLangType,
};
use crate::tags::NodeTag;
use serde::{Deserialize, Serialize};
use std::borrow::Cow;
use std::collections::HashMap;
use std::sync::Arc;
use tap::Pipe;

#[derive(Debug, Serialize, Deserialize)]
pub struct ExternalFileDescriptor {
    pub exports: HashMap<Arc<String>, Arc<LangType>>,
}

#[derive(Debug, Clone)]
pub struct ResolveTypesResult {
    pub value_types: HashMap<Breadcrumbs, AnyInferredLangType>,
    pub errors: Vec<ResolverError>,
}

#[derive(Debug, Clone)]
pub struct ResolverError {
    pub position: SourcePosition,
    pub error: LangError,
}
impl ResolverError {
    fn new(source_position: SourcePosition, format: LangError) -> Self {
        Self {
            position: source_position,
            error: format,
        }
    }
}

pub fn resolve_types(
    path: Arc<String>,
    file: Arc<ast::FileNode>,
    node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
    canonical_types: Arc<HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>>,
    external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>>,
) -> ResolveTypesResult {
    let mut ctx = ResolveTypesContext {
        file_path: path.clone(),
        root_node: file.clone(),
        value_types: HashMap::new(),
        contraints: vec![],
        node_tags,
        canonical_types,
        external_files,
    };
    let descendants = BreadcrumbTreeNode::from(&file.clone()).descendants();
    for descendant in &descendants {
        descendant.get_resolved_type(&mut ctx);
    }
    let result = ctx
        .value_types
        .iter()
        .filter_map(|(breadcrumbs, value_type)| {
            value_type
                .as_ref()
                .map(|value_type| (breadcrumbs.clone(), value_type.clone()))
        })
        .collect();

    let constraint_errors = ctx
        .contraints
        .iter()
        .filter_map(|(breadcrumbs, constraint)| {
            let actual_type = ctx
                .value_types
                .get(breadcrumbs)
                .map(|it| it.as_ref())
                .flatten();
            let source_position = SourcePosition {
                path: path.clone(),
                breadcrumbs: breadcrumbs.clone(),
                position: ctx
                    .node_at_path(breadcrumbs)
                    .expect("constraints must be placed on a node actually in the tree")
                    .info()
                    .position,
            };
            let actual_type = match actual_type {
                Some(InferredType::Known(it)) => it,
                // we won't check constraints if this type is already an error
                Some(InferredType::Error(_)) => return None,
                None => {
                    return Some(ResolverError::new(
                        source_position,
                        LangError::compiler_bug(format!(
                            "No type found when computing constraint for {}",
                            breadcrumbs
                        )),
                    ))
                }
            }
            .clone();
            match constraint {
                TypeConstaint::AssignableTo(_) => todo!(),
                TypeConstaint::EqualTo(expected_type) => {
                    if let InferredType::Known(expected_type) = expected_type {
                        if &actual_type != expected_type {
                            Some(ResolverError::new(
                                source_position,
                                LangError::MismatchedType(MismatchedTypeError {
                                    expected: expected_type.as_ref().clone(),
                                    actual: actual_type,
                                }),
                            ))
                        } else {
                            None
                        }
                    } else {
                        None
                    }
                }
                TypeConstaint::MustBeTypeReference => {
                    if let LangType::TypeReference(_) = actual_type.as_ref() {
                        None
                    } else {
                        Some(ResolverError::new(
                            source_position,
                            LangError::ValueUsedAsConstraint(ValueUsedAsConstraintError {
                                r#type: AnyInferredLangType::Known(actual_type.clone()),
                            }),
                        ))
                    }
                }
            }
        })
        .collect::<Vec<_>>();

    let errors = ctx
        .value_types
        .iter()
        .filter_map(|(breadcrumbs, resolved_type)| {
            resolved_type
                .as_ref()
                .and_then(|resolved_type| match resolved_type {
                    // TODO: Need to handle errors in nested types
                    InferredType::Known(_) => None,
                    InferredType::Error(err) => {
                        if let LangError::ProxyError(_) = err.as_ref() {
                            return None;
                        }
                        let position = BreadcrumbTreeNode::from(&file)
                            .at_path(breadcrumbs)
                            .map_err(|err| {
                                compiler_bug_error(format!(
                                    "Couldn't report an error at path {:?}: {}",
                                    breadcrumbs, err
                                ))
                            })
                            .and_then(|node| match node {
                                BreadcrumbTreeNode::Node(Some(node)) => {
                                    Ok(node.info().position.clone())
                                }
                                BreadcrumbTreeNode::Node(None) => Err(compiler_bug_error(format!(
                                    "Empty node at path {:?}; couldn't report an error",
                                    breadcrumbs
                                ))),
                                BreadcrumbTreeNode::List(_) => Err(compiler_bug_error(format!(
                                    "List node at path {:?}; couldn't report an error",
                                    breadcrumbs
                                ))),
                            });
                        match position {
                            Ok(position) => Some(ResolverError {
                                position: SourcePosition {
                                    path: path.clone(),
                                    breadcrumbs: breadcrumbs.clone(),
                                    position,
                                },
                                error: err.as_ref().to_owned(),
                            }),
                            Err(err) => Some(ResolverError {
                                position: SourcePosition {
                                    path: path.clone(),
                                    breadcrumbs: file.breadcrumbs().clone(),
                                    position: file.info().position.clone(),
                                },
                                error: err,
                            }),
                        }
                    }
                })
        })
        .chain(constraint_errors.into_iter())
        .collect();

    ResolveTypesResult {
        value_types: result,
        errors,
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
enum TypeConstaint {
    AssignableTo(AnyInferredLangType),
    EqualTo(AnyInferredLangType),
    MustBeTypeReference,
}

struct ResolveTypesContext {
    file_path: Arc<String>,
    root_node: Arc<ast::FileNode>,
    value_types: HashMap<Breadcrumbs, Option<AnyInferredLangType>>,
    contraints: Vec<(Breadcrumbs, TypeConstaint)>,
    canonical_types: Arc<HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>>,
    node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
    external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>>,
}

impl ResolveTypesContext {
    fn node_at_path(&self, breadcrumbs: &Breadcrumbs) -> Result<ast::AnyAstNode, LangError> {
        BreadcrumbTreeNode::from(&self.root_node)
            .at_path(breadcrumbs)
            .map_err(|err| {
                compiler_bug_error(format!(
                    "Couldn't find a node at path {:?}: {}",
                    breadcrumbs, err
                ))
            })
            .and_then(|node| match node {
                BreadcrumbTreeNode::Node(Some(node)) => Ok(node),
                BreadcrumbTreeNode::Node(None) => Err(compiler_bug_error(format!(
                    "Empty node at path {:?}",
                    breadcrumbs
                ))),
                BreadcrumbTreeNode::List(_) => Err(compiler_bug_error(format!(
                    "List node at path {:?}",
                    breadcrumbs
                ))),
            })
    }

    fn get_resolved_type<'a, T>(&mut self, node: &'a T) -> Option<AnyInferredLangType>
    where
        &'a T: Into<AnyAstNode>,
        T: AstNode,
    {
        let node: AnyAstNode = node.into();
        if let Some(already_resolved) = self.value_types.get(node.breadcrumbs()) {
            return already_resolved.clone();
        }
        let resolved = node.compute_type(self);
        self.value_types
            .insert(node.breadcrumbs().clone(), resolved.clone());
        resolved
    }

    fn get_resolved_type_proxying_errors<'a, T>(
        &mut self,
        node: &'a T,
    ) -> Option<AnyInferredLangType>
    where
        &'a T: Into<AnyAstNode>,
        T: AstNode,
    {
        self.get_resolved_type(node)
            .map(|found_type| match found_type {
                InferredType::Known(found_type) => InferredType::Known(found_type),
                InferredType::Error(err) => {
                    let source_position = ErrorPosition::Source(SourcePosition {
                        path: self.file_path.clone(),
                        breadcrumbs: node.breadcrumbs().clone(),
                        position: node.info().position,
                    });
                    InferredType::Error(LangError::proxy_error(err, source_position).into())
                }
            })
    }

    fn get_tags<'a, 'b>(&'a self, node: &impl AstNode) -> Cow<'a, Vec<NodeTag>> {
        self.node_tags
            .get(node.breadcrumbs())
            .map(|it| Cow::Borrowed(it))
            .unwrap_or(Cow::Owned(vec![]))
    }
}

trait ResolveTypes: ast::AstNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType>;
    fn get_resolved_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        if let Some(already_resolved) = ctx.value_types.get(self.breadcrumbs()) {
            return already_resolved.clone();
        }
        let resolved = self.compute_type(ctx);
        ctx.value_types
            .insert(self.breadcrumbs().clone(), resolved.clone());
        resolved
    }
    fn get_resolved_type_proxying_errors(
        &self,
        ctx: &mut ResolveTypesContext,
    ) -> Option<AnyInferredLangType> {
        self.get_resolved_type(ctx)
            .map(|found_type| match found_type {
                InferredType::Known(found_type) => InferredType::Known(found_type),
                InferredType::Error(err) => {
                    let source_position = ErrorPosition::Source(SourcePosition {
                        path: ctx.file_path.clone(),
                        breadcrumbs: self.breadcrumbs().clone(),
                        position: self.info().position,
                    });
                    InferredType::Error(LangError::proxy_error(err, source_position).into())
                }
            })
    }
    fn get_tags<'a, 'b>(&'a self, ctx: &'b ResolveTypesContext) -> Cow<'b, Vec<NodeTag>> {
        ctx.node_tags
            .get(&self.breadcrumbs())
            .map(|it| Cow::Borrowed(it))
            .unwrap_or(Cow::Owned(vec![]))
    }
}

impl ResolveTypes for AnyAstNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        match self {
            Self::File(_) => None,
            Self::Identifier(_) => None,
            Self::Import(_) => None,
            Self::ImportPath(_) => None,
            Self::ImportMapping(node) => node.compute_type(ctx),
            Self::Function(node) => node.compute_type(ctx),
            Self::NamedValue(node) => node.compute_type(ctx),
            Self::BlockBody(node) => node.compute_type(ctx),
            Self::DeclarationStatement(node) => node.compute_type(ctx),
            Self::ExpressionStatement(node) => node.compute_type(ctx),
            Self::CauseExpression(node) => node.compute_type(ctx),
            Self::CallExpression(node) => node.compute_type(ctx),
            Self::IdentifierExpression(node) => node.compute_type(ctx),
            Self::StringLiteralExpression(node) => node.compute_type(ctx),
            Self::NumberLiteralExpression(node) => node.compute_type(ctx),
            Self::IdentifierTypeReference(node) => resolve_identifier_type_reference(node, ctx),
            Self::Pattern(_) => todo!("Pattern"),
            Self::FunctionSignatureParameter(_) => todo!("FunctionSignatureParameter"),
            Self::FunctionCallParameter(_) => None, /* TODO? typechecking */
            Self::SingleStatementBody(_) => todo!("SingleStatementBody"),
            Self::BranchExpression(_) => todo!("BranchExpression"),
            Self::IfBranchOption(_) => todo!("IfBranchOption"),
            Self::IsBranchOption(_) => todo!("IsBranchOption"),
            Self::ElseBranchOption(_) => todo!("ElseBranchOption"),
        }
    }
}

fn resolve_identifier_type_reference(
    node: &ast::IdentifierTypeReferenceNode,
    ctx: &mut ResolveTypesContext,
) -> Option<InferredType<Arc<LangType>>> {
    let tags = ctx.get_tags(node);
    let reference_tag = find_tag!(&tags, NodeTag::ValueComesFrom);
    let reference_tag = match reference_tag.ok_or(LangError::NotInScope) {
        Ok(it) => it,
        Err(err) => return Some(InferredType::Error(err.into())),
    };
    let source_node = match ctx.node_at_path(&reference_tag.source) {
        Ok(it) => it,
        Err(err) => return Some(InferredType::Error(err.into())),
    };
    let source_node_type = match ctx.get_resolved_type_proxying_errors(&source_node) {
        Some(it) => it,
        None => {
            return Some(InferredType::Error(
                LangError::compiler_bug(format!(
                    "no type found for source node at {}",
                    reference_tag.source
                ))
                .into(),
            ))
        }
    };
    ctx.contraints.push((
        node.breadcrumbs().clone(),
        TypeConstaint::MustBeTypeReference,
    ));
    Some(source_node_type)
}

impl ResolveTypes for ast::ImportMappingNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let tags = self.get_tags(ctx);
        let reference_file_tag = find_tag!(&tags, NodeTag::ReferencesFile);
        let export = reference_file_tag
            .ok_or_else(|| {
                LangError::CompilerBug(CompilerBugError {
                    description: format!(
                        "No reference file tag found for import mapping: {:?}",
                        self.breadcrumbs()
                    ),
                })
            })
            .and_then(|tag| {
                ctx.external_files
                    .get(&tag.path)
                    .ok_or_else(|| LangError::FileNotFound)
                    .map(|file| (tag, file))
            })
            .and_then(|(_tag, file)| {
                file.exports
                    .get(&self.source_name.text)
                    .ok_or_else(|| LangError::ExportNotFound)
            });
        Some(
            export
                .map(|export| InferredType::Known(export.clone()))
                .unwrap_or_else(|err| InferredType::Error(err.into())),
        )
    }
}

impl ResolveTypes for ast::FunctionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let name = self.name.text.clone();
        let explicit_return_type = self.return_type.as_ref().and_then(|it| {
            let type_reference = ctx.get_resolved_type_proxying_errors(it);
            type_reference.map(|type_reference| {
                type_reference.and_then(|type_reference| match type_reference.as_ref() {
                    LangType::TypeReference(referenced_type) => referenced_type.clone(),
                    _ => InferredType::Error(
                        LangError::ValueUsedAsConstraint(ValueUsedAsConstraintError {
                            r#type: AnyInferredLangType::Known(type_reference.clone()),
                        })
                        .into(),
                    ),
                })
            })
        });
        let get_inferred_return_type = || ctx.get_resolved_type_proxying_errors(&self.body);
        let return_type = explicit_return_type.or_else(get_inferred_return_type);
        let function_type = FunctionLangType {
            name,
            return_type: return_type.unwrap_or(InferredType::Error(
                LangError::CompilerBug(CompilerBugError {
                    description: format!(
                        "No return type found for function: {:?}",
                        &self.info.breadcrumbs
                    ),
                })
                .into(),
            )),
        };
        Some(function_type.into())
    }
}

impl ResolveTypes for ast::BlockBodyNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let last_statement = self.statements.last();
        let last_statement_type = last_statement
            .and_then(|it| ctx.get_resolved_type_proxying_errors(it))
            .unwrap_or(LangType::Action.into());
        Some(last_statement_type)
    }
}

impl ResolveTypes for ast::ExpressionStatementNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        ctx.get_resolved_type_proxying_errors(&self.expression)
    }
}

impl ResolveTypes for ast::CauseExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let maybe_signal = ctx.get_resolved_type_proxying_errors(&self.signal);
        let signal_result_type = maybe_signal
            .ok_or(
                LangError::CompilerBug(CompilerBugError {
                    description: "No signal found".into(),
                })
                .into(),
            )
            .and_then(|maybe_signal| maybe_signal.to_result())
            .and_then(|maybe_signal| match maybe_signal.as_ref() {
                LangType::Instance(instance) => Ok(instance.type_id.clone()),
                _ => Err(LangError::NotCausable.into()),
            })
            .and_then(|signal_id| {
                ctx.canonical_types.get(signal_id.as_ref()).cloned().ok_or(
                    LangError::CompilerBug(CompilerBugError {
                        description: format!("Couldn't find a canonical symbol: {:?}", signal_id),
                    })
                    .into(),
                )
            })
            .and_then(|canonical_type| match canonical_type.as_ref() {
                CanonicalLangType::Signal(signal_type) => Ok(signal_type.result().clone()),
                _ => Err(LangError::NotCausable.into()),
            })
            .unwrap_or_else(|err| InferredType::Error(err.into()));
        Some(signal_result_type)
    }
}

impl ResolveTypes for ast::CallExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let callee_type = ctx.get_resolved_type_proxying_errors(&self.callee);
        let result_type = callee_type
            .unwrap_or(InferredType::Error(
                LangError::CompilerBug(CompilerBugError {
                    description: "No callee type found".into(),
                })
                .into(),
            ))
            .to_result()
            .and_then(|callee_type| match callee_type.as_ref() {
                LangType::Function(function_type) => Ok(function_type.return_type.clone()),
                LangType::TypeReference(referenced_type) => referenced_type
                    .clone()
                    .to_result()
                    .and_then(|referenced_type| {
                        let instance_type = match referenced_type.as_ref() {
                            LangType::Instance(instance) => Ok(instance),
                            _ => Err(LangError::NotCallable),
                        }?;
                        Ok(instance_type.clone().into())
                    }),
                _ => Err(LangError::NotCallable.into()),
            });
        Some(result_type.unwrap_or_else(|err| InferredType::Error(err.into())))
    }
}

impl ResolveTypes for ast::IdentifierExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let tags = self.get_tags(ctx);
        let reference_tag = find_tag!(&tags, NodeTag::ValueComesFrom);
        let referenced_type = reference_tag
            .ok_or(compiler_bug_error("No reference tag found for identifier"))
            .and_then(|reference_tag| {
                AnyAstNode::from(&ctx.root_node)
                    .node_at_path(&reference_tag.source)
                    .map_err(|err| {
                        LangError::CompilerBug(CompilerBugError {
                            description: err.to_string(),
                        })
                    })
                    .and_then(|node| {
                        node.get_resolved_type_proxying_errors(ctx)
                            .ok_or(compiler_bug_error(format!(
                                "no type found for reference: {}",
                                reference_tag.source
                            )))
                    })
            });
        Some(referenced_type.unwrap_or_else(|err| InferredType::Error(err.into())))
    }
}

impl ResolveTypes for ast::StringLiteralExpressionNode {
    fn compute_type(&self, _ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        Some(LangType::Primitive(PrimitiveLangType::Text).into())
    }
}

impl ResolveTypes for ast::NumberLiteralExpressionNode {
    fn compute_type(&self, _ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        Some(LangType::Primitive(PrimitiveLangType::Number).into())
    }
}

impl ResolveTypes for ast::DeclarationStatementNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        ctx.get_resolved_type_proxying_errors(&self.declaration)
    }
}

impl ResolveTypes for ast::NamedValueNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let annotated_type = self
            .type_annotation
            .as_ref()
            .and_then(|it| ctx.get_resolved_type_proxying_errors(it))
            .map(|annotated_type| {
                let annotated_type = annotated_type.to_result()?;
                match annotated_type.as_ref() {
                    LangType::TypeReference(InferredType::Known(value_type)) => {
                        Ok(value_type.clone())
                    }
                    LangType::TypeReference(InferredType::Error(err)) => {
                        Err(LangError::proxy_error(
                            err.clone(),
                            ErrorPosition::Source(SourcePosition {
                                breadcrumbs: self.breadcrumbs().clone(),
                                path: ctx.file_path.clone(),
                                position: self.info().position.clone(),
                            }),
                        )
                        .into())
                    }
                    _ => Err(
                        LangError::ValueUsedAsConstraint(ValueUsedAsConstraintError {
                            r#type: AnyInferredLangType::Known(annotated_type.clone()),
                        })
                        .pipe(Arc::new),
                    ),
                }
            });

        let inferred_type = ctx
            .get_resolved_type_proxying_errors(&self.value)
            .unwrap_or(
                LangError::compiler_bug(format!("No type found for {}", self.value.breadcrumbs()))
                    .pipe(|it| InferredType::Error(it.into())),
            );

        if let Some(Ok(annotated_type)) = &annotated_type {
            ctx.contraints.push((
                self.value.breadcrumbs().clone(),
                // TODO: should be TypeConstaint::AssignableTo
                TypeConstaint::EqualTo(annotated_type.clone().into()),
            ));
        }

        let result = annotated_type
            .map(|annotated_type| annotated_type.into())
            .unwrap_or(inferred_type);
        Some(result)
    }
}
