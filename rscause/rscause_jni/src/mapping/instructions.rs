use crate::mapping::IntoJni;
use anyhow::Result;
use jni::objects::{JValueOwned};
use rscause_compiler::instructions::{self as instructions, Instruction};

include!("gen/instruction_mapping.rs");
