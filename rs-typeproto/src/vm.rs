use std::{
    cell::RefCell,
    collections::{HashMap, VecDeque},
    rc::Rc,
    sync::Arc,
};

use crate::compiled_file::{CompiledExport, CompiledFile, InstructionChunk};

type WrappedCallFrame = Rc<RefCell<CallFrame>>;

pub struct LangVm {
    files: HashMap<String, Arc<CompiledFile>>,
    call_frame: Option<WrappedCallFrame>,
    stack: VecDeque<RuntimeValue>,
}

struct CallFrame {
    file: Arc<CompiledFile>,
    chunk_id: usize,
    instruction: usize,
    stack_start: usize,
    parent: Option<WrappedCallFrame>,
}

#[derive(Debug, Clone)]
pub enum RuntimeValue {
    Action,
    String(Arc<String>),
    Integer(isize),
    Float(f64),
    Object(Arc<RuntimeObject>),
}

#[derive(Debug)]
pub struct RuntimeObject {
    type_id: String,
    values: Vec<RuntimeValue>,
}

#[derive(Debug)]
pub enum RunResult {
    Return(RuntimeValue),
    Cause(Arc<RuntimeObject>),
}

impl LangVm {
    pub fn new() -> LangVm {
        LangVm {
            files: HashMap::new(),
            call_frame: None,
            stack: VecDeque::new(),
        }
    }

    pub fn add_compiled_file(&mut self, file: CompiledFile) {
        self.files.insert(file.path.to_owned(), file.into());
    }

    pub fn execute_function(
        &mut self,
        file_path: String,
        function_name: String,
    ) -> Result<RunResult, String> {
        let file = self
            .files
            .get(&file_path)
            .ok_or_else(|| format!("I don't know about any file at {file_path}."))?;

        let export = file.exports.get(&function_name).ok_or_else(|| format!("The file at {file_path} doesn't contain anything (at least that's not private) called {function_name}."))?;

        let chunk_id = if let CompiledExport::Chunk(chunk_id) = export {
            Ok(*chunk_id)
        } else {
            Err(format!(
                "{function_name} isn't a function, so I can't execute it."
            ))
        }?;

        self.stack.clear();
        let call_frame = CallFrame {
            file: file.clone(),
            chunk_id,
            instruction: 0,
            stack_start: 0,
            parent: None,
        };
        self.call_frame = Some(RefCell::new(call_frame).into());

        self.execute()
    }

    fn execute(&mut self) -> Result<RunResult, String> {
        loop {
            let mut call_frame = self
                .call_frame
                .as_ref()
                .ok_or("I'm not ready to execute anything!")?
                .borrow_mut();
            let chunk = call_frame.file.chunks.get(call_frame.chunk_id).ok_or("I couldn't find any code to execute... this probably isn't your fault. This shouldn't happen if the compiler is working properly.")?;

            let instruction = chunk.instructions
                .get(call_frame.instruction)
                .ok_or_else(|| format!("I've gotten to instruction #{} in chunk #{} in {}, but there are no more instructions to read! This probably isn't your fault. This shouldn't happen if the compiler is working properly.", call_frame.instruction, call_frame.chunk_id, call_frame.file.path))?;

            println!("instruction: {instruction:?}");

            call_frame.instruction += 1;
        }
    }
}
