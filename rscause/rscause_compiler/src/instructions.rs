include!("gen/instruction_types.rs");

pub enum InstructionPhase {
    Setup,
    Execute,
    Plumbing,
    Cleanup,
}