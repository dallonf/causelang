use std::sync::Arc;

use super::{FromJni, JniInto};
use anyhow::Result;
use jni::{objects::JObject, JNIEnv};
use rscause_compiler::tags;
use rscause_compiler::breadcrumbs::Breadcrumbs;
use crate::util::{noisy_log, get_class_name};

include!("gen/tag_mappings.rs");
