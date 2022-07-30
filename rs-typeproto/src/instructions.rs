use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum Instruction {
    Pop,
    PushAction,
    Constant(usize),
    Import {
      file_path_constant: usize,
      export_name_constant: usize,
    },
    ReadLocal(usize),
    Construct,
    CallFunction,
    Cause,
    Return,
}
