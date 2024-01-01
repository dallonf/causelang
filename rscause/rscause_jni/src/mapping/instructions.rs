use crate::mapping::{into_jni_optional_int, IntoJni};
use anyhow::Result;
use jni::objects::JValueOwned;
use rscause_compiler::instructions::{self as instructions, Instruction, InstructionPhase};

include!("gen/instruction_mapping.rs");

impl IntoJni for InstructionPhase {
    fn into_jni<'local>(&self, env: &mut jni::JNIEnv<'local>) -> Result<JValueOwned<'local>> {
        let classname = "com/dallonf/ktcause/CompiledFile$Procedure$InstructionPhase";
        let field_sig = format!("L{};", classname);
        let class = env.find_class(classname)?;
        let result = match self {
            InstructionPhase::Setup => env.get_static_field(class, "SETUP", field_sig),
            InstructionPhase::Execute => env.get_static_field(class, "EXECUTE", field_sig),
            InstructionPhase::Plumbing => env.get_static_field(class, "PLUMBING", field_sig),
            InstructionPhase::Cleanup => env.get_static_field(class, "CLEANUP", field_sig),
        };
        result.map_err(|e| e.into())
    }
}
