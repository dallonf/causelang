use std::{collections::HashMap, sync::Arc};

use discover::{discover_types, DiscoverTypesResult};
use infer::infer_types;

use crate::{
    ast,
    breadcrumbs::Breadcrumbs,
    compiled_file::ExternalFileDescriptor,
    lang_types::{CanonicalLangType, CanonicalLangTypeId},
    tags::NodeTag,
};

mod discover;
mod hints;
mod infer;
mod resolving_lang_types;

pub fn resolve_types(
    path: Arc<String>,
    file: Arc<ast::FileNode>,
    node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
    external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>>,
    canonical_types: Arc<HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>>,
) -> anyhow::Result<()> {
    let DiscoverTypesResult {
        resolving_types_ctx,
        new_canonical_types,
    } = discover_types(
        path.clone(),
        file.clone(),
        node_tags.clone(),
        external_files.clone(),
    )?;

    infer_types(path.clone(), file.clone(), resolving_types_ctx)?;

    Ok(())
}
