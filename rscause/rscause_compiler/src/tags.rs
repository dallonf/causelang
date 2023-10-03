use crate::breadcrumbs::Breadcrumbs;
use serde::{Deserialize, Serialize};
use std::sync::Arc;

include!("gen/tags.rs");

#[macro_export]
macro_rules! find_tag {
    ($tags:expr, $tag_type:path) => {
        $tags.iter().find_map(|tag| match tag {
            $tag_type(tag) => Some(tag.clone()),
            _ => None,
        })
    };
}
