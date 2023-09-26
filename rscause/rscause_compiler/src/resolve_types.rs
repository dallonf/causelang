use std::collections::HashMap;

use crate::lang_types::{InferredType, LangType};

pub struct ResolveTypesContext {
    valueTypes: HashMap<String, InferredType<LangType>>,
}
