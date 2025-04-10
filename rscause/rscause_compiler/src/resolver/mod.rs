use std::{collections::HashMap, sync::Arc};

use discover::{discover_types, DiscoverTypesResult};
use infer::infer_types;
use resolving_lang_types::ResolvingCanonicalLangType;

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
        for (id, value) in new_canonical_types {
            result.insert(id, value.clone());
        }
        result
    };

    infer_types(
        path.clone(),
        file.clone(),
        resolving_types_ctx,
        combined_canonical_types,
    )?;

    Ok(())
}
