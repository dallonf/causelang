use crate::ast::{self, AnyAstNode, AstNode, BreadcrumbTreeNode};
use crate::breadcrumbs::Breadcrumbs;
use crate::find_tag;
use crate::lang_types::{FunctionLangType, InferredType, LangType};
use crate::tags::NodeTag;
use std::borrow::Cow;
use std::collections::HashMap;
use std::sync::Arc;

pub struct ExternalFileDescriptor {
    pub exports: HashMap<Arc<String>, Arc<LangType>>,
}

pub fn resolve_types(
    file: &ast::FileNode,
    node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
    external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>>,
) -> ResolveTypesContext {
    let mut ctx = ResolveTypesContext {
        value_types: HashMap::new(),
        node_tags,
        external_files,
    };
    let descendants = BreadcrumbTreeNode::from(&Arc::new(file.clone())).descendants();
    for descendant in &descendants {
        descendant.resolve_type_cached(&mut ctx);
    }
    ctx
}

pub struct ResolveTypesContext {
    value_types: HashMap<Breadcrumbs, Option<InferredType<Arc<LangType>>>>,
    node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
    external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>>,
}

trait ResolveTypes: ast::AstNode {
    fn resolve_type(&self, ctx: &mut ResolveTypesContext) -> Option<InferredType<Arc<LangType>>>;
    fn resolve_type_cached(
        &self,
        ctx: &mut ResolveTypesContext,
    ) -> Option<InferredType<Arc<LangType>>> {
        if let Some(already_resolved) = ctx.value_types.get(self.breadcrumbs()) {
            return already_resolved.clone();
        }
        let resolved = self.resolve_type(ctx);
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
    fn resolve_type(&self, ctx: &mut ResolveTypesContext) -> Option<InferredType<Arc<LangType>>> {
        match self {
            Self::ImportMapping(node) => node.resolve_type(ctx),
            _ => None,
        }
    }
}

impl<T> ResolveTypes for T
where
    T: AstNode,
    AnyAstNode: for<'a> From<&'a T>,
{
    fn resolve_type(&self, ctx: &mut ResolveTypesContext) -> Option<InferredType<Arc<LangType>>> {
        AnyAstNode::from(self).resolve_type(ctx)
    }
}

impl ResolveTypes for ast::ImportMappingNode {
    fn resolve_type(&self, ctx: &mut ResolveTypesContext) -> Option<InferredType<Arc<LangType>>> {
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
    fn resolve_type(&self, ctx: &mut ResolveTypesContext) -> Option<InferredType<Arc<LangType>>> {
        let name = self.name.text.clone();
        let explicit_return_type = self.return_type.as_ref().and_then(|it| {
            let type_reference = it.resolve_type_cached(ctx);
            match type_reference {
                Some(InferredType::Known(type_reference)) => match type_reference.as_ref() {
                    LangType::TypeReference(referenced_type) => Some(referenced_type.clone()),
                    _ => Some(InferredType::Error),
                },
                Some(InferredType::Error) => Some(InferredType::Error),
                None => None,
            }
        });
        let get_inferred_return_type = || self.body.resolve_type_cached(ctx);
        let return_type = explicit_return_type.or_else(get_inferred_return_type);
        let function_type = FunctionLangType {
            name,
            return_type: return_type.unwrap_or(InferredType::Error),
        };
        Some(InferredType::Known(Arc::new(LangType::Function(
            function_type,
        ))))
    }
}
