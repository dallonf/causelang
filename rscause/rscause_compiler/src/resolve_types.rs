use crate::ast::{self, BreadcrumbTreeNode};
use crate::breadcrumbs::Breadcrumbs;
use crate::lang_types::{InferredType, LangType};
use crate::tags::NodeTag;
use std::collections::HashMap;
use std::sync::Arc;

pub struct ExternalFileDescriptor {
    pub exports: HashMap<Arc<String>, LangType>,
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
        descendant.resolve_types(&mut ctx);
    }
    ctx
}

pub struct ResolveTypesContext {
    value_types: HashMap<Breadcrumbs, InferredType<LangType>>,
    node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
    external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>>,
}

trait ResolveTypes {
    fn resolve_types(&self, ctx: &mut ResolveTypesContext);
}

impl ResolveTypes for ast::AnyAstNode {
    fn resolve_types(&self, ctx: &mut ResolveTypesContext) {
        match self {
            Self::ImportMapping(node) => node.resolve_types(ctx),
            _ => {}
        }
    }
}

impl ResolveTypes for ast::ImportMappingNode {
    fn resolve_types(&self, ctx: &mut ResolveTypesContext) {
        let tags = ctx.node_tags.get(&self.breadcrumbs);
        let reference_file_tag = tags.and_then(|tags| {
            tags.iter().find_map(|tag| match tag {
                NodeTag::ReferencesFile(tag) => Some(tag.clone()),
                _ => None,
            })
        });
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
        ctx.value_types.insert(
            self.breadcrumbs.clone(),
            export
                .map(|export| InferredType::Known(export.clone()))
                .unwrap_or_else(|_| InferredType::Error),
        );
    }
}
