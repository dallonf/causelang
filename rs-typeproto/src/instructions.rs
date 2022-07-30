use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum Instruction {
    Pop,
    Constant(usize),
    Import,
    ReadLocal(usize),
    Construct,
    CallFunction,
    Cause,
    Return,
}
