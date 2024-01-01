use serde::{Deserialize, Serialize};

include!("gen/instruction_types.rs");

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub enum InstructionPhase {
    Setup,
    Execute,
    Plumbing,
    Cleanup,
}
