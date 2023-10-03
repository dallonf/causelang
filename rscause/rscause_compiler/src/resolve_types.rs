use crate::ast::{self, AnyAstNode, AstNode, BreadcrumbTreeNode};
use crate::breadcrumbs::Breadcrumbs;
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

#[derive(Debug, Serialize, Deserialize)]
pub struct ExternalFileDescriptor {
    pub exports: HashMap<Arc<String>, Arc<LangType>>,
}

#[derive(Debug, Clone)]
pub struct ResolveTypesResult {
    pub value_types: HashMap<Breadcrumbs, AnyInferredLangType>,
}

pub fn resolve_types(
    file: Arc<ast::FileNode>,
    node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
    canonical_types: HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>,
    external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>>,
) -> ResolveTypesResult {
    let mut ctx = ResolveTypesContext {
        root_node: file.clone(),
        value_types: HashMap::new(),
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
        .into_iter()
        .filter_map(|(breadcrumbs, value_type)| {
            value_type.map(|value_type| (breadcrumbs, value_type))
        })
        .collect();
    ResolveTypesResult {
        value_types: result,
    }
}

struct ResolveTypesContext {
    root_node: Arc<ast::FileNode>,
    value_types: HashMap<Breadcrumbs, Option<AnyInferredLangType>>,
    canonical_types: HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>,
    node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
    external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>>,
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
            Self::ImportMapping(node) => node.compute_type(ctx),
            Self::Function(node) => node.compute_type(ctx),
            Self::BlockBody(node) => node.compute_type(ctx),
            Self::ExpressionStatement(node) => node.compute_type(ctx),
            Self::CauseExpression(node) => node.compute_type(ctx),
            Self::CallExpression(node) => node.compute_type(ctx),
            Self::IdentifierExpression(node) => node.compute_type(ctx),
            Self::StringLiteralExpression(node) => node.compute_type(ctx),
            _ => None,
        }
    }
}

impl<T> ResolveTypes for T
where
    T: AstNode,
    AnyAstNode: for<'a> From<&'a T>,
{
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        AnyAstNode::from(self).compute_type(ctx)
    }
}

impl ResolveTypes for ast::ImportMappingNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let tags = self.get_tags(ctx);
        let reference_file_tag = find_tag!(&tags, NodeTag::ReferencesFile);
        let export = reference_file_tag
            .ok_or_else(|| {
                println!(
                    "No reference file tag found for import mapping: {:?}",
                    self.breadcrumbs
                );
                ()
            })
            .and_then(|tag| {
                ctx.external_files
                    .get(&tag.path)
                    .ok_or_else(|| {
                        println!("File not found: {}", &tag.path);
                        ()
                    })
                    .map(|file| (tag, file))
            })
            .and_then(|(tag, file)| {
                file.exports.get(&self.source_name.text).ok_or_else(|| {
                    println!("Export not found: {}::{}", self.source_name.text, tag.path);
                    ()
                })
            });
        Some(
            export
                .map(|export| InferredType::Known(export.clone()))
                .unwrap_or_else(|_| InferredType::Error),
        )
    }
}

impl ResolveTypes for ast::FunctionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let name = self.name.text.clone();
        let explicit_return_type = self.return_type.as_ref().and_then(|it| {
            let type_reference = it.get_resolved_type(ctx);
            type_reference.map(|type_reference| {
                type_reference.and_then(|type_reference| match type_reference.as_ref() {
                    LangType::TypeReference(referenced_type) => referenced_type.clone(),
                    _ => InferredType::Error,
                })
            })
        });
        let get_inferred_return_type = || self.body.get_resolved_type(ctx);
        let return_type = explicit_return_type.or_else(get_inferred_return_type);
        let function_type = FunctionLangType {
            name,
            return_type: return_type.unwrap_or(InferredType::Error),
        };
        Some(function_type.into())
    }
}

impl ResolveTypes for ast::BlockBodyNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let last_statement = self.statements.last();
        let last_statement_type = last_statement
            .and_then(|it| it.get_resolved_type(ctx))
            .unwrap_or(LangType::Action.into());
        Some(last_statement_type)
    }
}

impl ResolveTypes for ast::ExpressionStatementNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        self.expression.get_resolved_type(ctx)
    }
}

impl ResolveTypes for ast::CauseExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let maybe_signal = self.signal.get_resolved_type(ctx);
        let signal_result_type = maybe_signal
            .ok_or(())
            .and_then(|maybe_signal| maybe_signal.to_result())
            .and_then(|maybe_signal| match maybe_signal.as_ref() {
                LangType::Instance(instance) => Ok(instance.type_id.clone()),
                _ => Err(()),
            })
            .and_then(|signal_id| {
                ctx.canonical_types
                    .get(signal_id.as_ref())
                    .cloned()
                    .ok_or(())
            })
            .and_then(|canonical_type| match canonical_type.as_ref() {
                CanonicalLangType::Signal(signal_type) => Ok(signal_type.result.clone()),
                _ => Err(()),
            })
            .unwrap_or(InferredType::Error);
        Some(signal_result_type)
    }
}

impl ResolveTypes for ast::CallExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let callee_type = self.callee.get_resolved_type(ctx);
        let result_type = callee_type
            .unwrap_or(InferredType::Error)
            .to_result()
            .and_then(|callee_type| match callee_type.as_ref() {
                LangType::Function(function_type) => Ok(function_type.return_type.clone()),
                LangType::TypeReference(referenced_type) => referenced_type
                    .clone()
                    .to_result()
                    .and_then(|referenced_type| {
                        let instance_type = match referenced_type.as_ref() {
                            LangType::Instance(instance) => Ok(instance),
                            _ => Err(()),
                        }?;
                        Ok(instance_type.clone().into())
                    }),
                _ => Err(()),
            });
        Some(result_type.unwrap_or(InferredType::Error))
    }
}

impl ResolveTypes for ast::IdentifierExpressionNode {
    fn compute_type(&self, ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        let tags = self.get_tags(ctx);
        let reference_tag = find_tag!(&tags, NodeTag::ValueComesFrom);
        let referenced_type = reference_tag.ok_or(()).and_then(|reference_tag| {
            AnyAstNode::from(&ctx.root_node)
                .node_at_path(&reference_tag.source)
                .map_err(|_| ())
                .and_then(|node| node.get_resolved_type(ctx).ok_or(()))
        });
        Some(referenced_type.unwrap_or(InferredType::Error))
    }
}

impl ResolveTypes for ast::StringLiteralExpressionNode {
    fn compute_type(&self, _ctx: &mut ResolveTypesContext) -> Option<AnyInferredLangType> {
        Some(LangType::Primitive(PrimitiveLangType::Text).into())
    }
}
