use std::{collections::HashMap, hash::Hash, sync::Arc};

use anyhow::anyhow;
use discover::{discover_types, DiscoverTypesResult};
use infer::{infer_types, InferTypesResult};
use resolving_lang_types::{ResolvingCanonicalLangType, ResolvingLangTypeSource};
use serde::Serialize;
use tap::TryConv;

use crate::{
    ast::{self, AstNode, BreadcrumbTreeNode, FileNode},
    breadcrumbs::Breadcrumbs,
    compiled_file::ExternalFileDescriptor,
    error_types::{anyhow_to_compiler_bug, LangError, SourcePosition},
    lang_types::{self, CanonicalLangType, CanonicalLangTypeId, LangType, LangTypeResult},
    tags::NodeTag,
};

mod discover;
mod hints;
mod infer;
mod resolving_lang_types;

#[derive(Debug, Clone)]
pub struct ResolveTypesResult {
    pub value_types: HashMap<Breadcrumbs, lang_types::FallibleLangType>,
    pub errors: Vec<ResolverError>,
    pub new_canonical_types: HashMap<Arc<CanonicalLangTypeId>, Arc<lang_types::CanonicalLangType>>,
}

#[derive(Debug, Clone, Serialize)]
pub struct ResolverError {
    pub position: SourcePosition,
    pub error: LangError,
}
impl ResolverError {
    pub fn new(source_position: SourcePosition, format: LangError) -> Self {
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
    external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>>,
    canonical_types: Arc<HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>>,
) -> anyhow::Result<ResolveTypesResult> {
    let DiscoverTypesResult {
        mut resolving_types_ctx,
        new_canonical_types,
    } = discover_types(
        path.clone(),
        file.clone(),
        node_tags.clone(),
        external_files.clone(),
    )?;

    let combined_canonical_types = {
        let mut result: HashMap<_, _> = canonical_types
            .iter()
            .map(|it| {
                let imported = ResolvingCanonicalLangType::import(
                    &mut resolving_types_ctx,
                    it.1.as_ref().to_owned(),
                )?;
                Ok((it.0.as_ref().to_owned(), Arc::new(imported)))
            })
            .collect::<anyhow::Result<_>>()?;
        for (id, value) in &new_canonical_types {
            result.insert(id.clone(), value.clone());
        }
        result
    };

    let InferTypesResult {
        resolving_types_ctx,
    } = infer_types(
        path.clone(),
        file.clone(),
        resolving_types_ctx,
        combined_canonical_types,
    )?;

    let mut value_types = HashMap::new();
    let mut errors = vec![];

    for (source, link) in resolving_types_ctx.all_variables() {
        if let ResolvingLangTypeSource::Breadcrumb(breadcrumbs) = source {
            let value = link
                .get_snapshot_value()
                .map_err(anyhow_to_compiler_bug)
                .and_then(|option| option.unwrap_or(Err(Arc::new(LangError::NeverResolved))));
            let lang_type = value
                .and_then(|resolving_type| {
                    LangType::try_from(resolving_type).map_err(anyhow_to_compiler_bug)
                })
                .map(Arc::new);

            value_types.insert(breadcrumbs.clone(), lang_type.clone());

            if let Err(error) = lang_type {
                let position = node_at_path(&file, &breadcrumbs)?.info().position;
                let source_position = SourcePosition {
                    path: path.clone(),
                    breadcrumbs: breadcrumbs.clone(),
                    position,
                };
                errors.push(ResolverError {
                    position: source_position,
                    error: error.as_ref().to_owned(),
                });
            }
        }
    }

    Ok(ResolveTypesResult {
        value_types,
        errors,
        new_canonical_types: HashMap::new(),
        // TODO: export new canonical types
        // new_canonical_types: new_canonical_types
        //     .into_iter()
        //     .map(|(id, resolving_canonical_type)| {
        //         (Arc::new(id), todo!())
        //     })
        //     .collect(),
    })
}

fn node_at_path(file: &FileNode, breadcrumbs: &Breadcrumbs) -> anyhow::Result<ast::AnyAstNode> {
    BreadcrumbTreeNode::from(file)
        .at_path(breadcrumbs)
        .map_err(|err| anyhow!("Couldn't find a node at path {:?}: {}", breadcrumbs, err).into())
        .and_then(|node| match node {
            BreadcrumbTreeNode::Node(Some(node)) => Ok(node),
            BreadcrumbTreeNode::Node(None) => {
                Err(anyhow!("Empty node at path {:?}", breadcrumbs).into())
            }
            BreadcrumbTreeNode::List(_) => {
                Err(anyhow!("List node at path {:?}", breadcrumbs).into())
            }
        })
}
