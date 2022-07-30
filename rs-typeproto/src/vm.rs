use std::any::Any;
use std::cell::RefCell;
use std::collections::{HashMap, VecDeque};
use std::fmt::Debug;
use std::ops::Deref;
use std::rc::Rc;
use std::sync::Arc;

use crate::compiled_file::{CompiledConstant, CompiledExport, CompiledFile, InstructionChunk};
use crate::core_globals::core_global_file;
use crate::core_runtime::get_core_export;
use crate::instructions::Instruction;
use crate::types::SignalCanonicalLangType;
use crate::types::{ResolvedValueLangType, ValueLangType};

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
    pending_signal: Option<Arc<RuntimeObject>>,
    parent: Option<WrappedCallFrame>,
}

#[derive(Debug, Clone)]
pub enum RuntimeValue {
    Action,
    String(Arc<String>),
    Integer(isize),
    Float(f64),
    Object(Arc<RuntimeObject>),
    TypeReference(Arc<RuntimeTypeReference>),
}

impl RuntimeValue {
    pub fn is_assignable_to(&self, lang_type: &ValueLangType) -> bool {
        // TODO: implement this!
        true
    }
}

#[derive(Clone)]
pub enum RuntimeTypeReference {
    Signal(SignalCanonicalLangType),
}

impl Debug for RuntimeTypeReference {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::Signal(signal) => f.debug_tuple("Signal").field(&signal.id).finish(),
        }
    }
}

#[derive(Debug)]
pub struct RuntimeObject {
    // TODO: probably want to be a little stricter on making these
    pub type_descriptor: Arc<RuntimeTypeReference>,
    pub values: Vec<RuntimeValue>,
}

#[derive(Debug)]
pub enum RunResult {
    Return(RuntimeValue),
    Cause(Arc<RuntimeObject>),
}

const COMPILE_ERROR_ASSURANCE: &'static str =
    "This probably isn't your fault. This shouldn't happen if the compiler is working properly.";

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
            pending_signal: None,
            parent: None,
        };
        self.call_frame = Some(RefCell::new(call_frame).into());

        self.execute()
    }

    pub fn resume_execution(&mut self, value: RuntimeValue) -> Result<RunResult, String> {
        const STATE_ERROR: &'static str =
            "I'm not currently waiting for a signal, so I can't resume execution.";
        let mut call_frame = self
            .call_frame
            .as_ref()
            .map(|it| it.borrow_mut())
            .ok_or(STATE_ERROR)?;
        let pending_signal = call_frame.pending_signal.clone().ok_or(STATE_ERROR)?;
        let pending_signal = match pending_signal.type_descriptor.as_ref() {
            RuntimeTypeReference::Signal(signal) => signal,
        };

        if value.is_assignable_to(pending_signal.result.as_ref()) {
            call_frame.pending_signal = None;
            self.stack.push_back(value);
            drop(call_frame);

            self.execute()
        } else {
            Err(format!(
                "I need to resolve a {} signal with a {:?}, but {:?} isn't a {:?}.",
                pending_signal.name, pending_signal.result, value, pending_signal.name
            ))
        }
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

            macro_rules! get_constant {
                ( $x:expr ) => {
                    chunk.constant_table.get($x)
                        .ok_or_else(|| format!("I'm looking for a constant with the ID of {} in chunk #{} in {}, but I can't find it. This probably isn't your fault. This shouldn't happen if the compiler is working properly.", $x, call_frame.chunk_id, call_frame.file.path))?
                };
            }

            println!("stack: {:?}", self.stack);
            println!("instruction: {instruction:?}");

            match instruction {
                &Instruction::Pop => {
                    self.stack.pop_front();
                }
                &Instruction::PushAction => todo!(),
                &Instruction::Constant(constant_id) => {
                    let new_value = match get_constant!(constant_id) {
                        CompiledConstant::String(str_value) => {
                            RuntimeValue::String(str_value.to_owned().into())
                        }
                        CompiledConstant::Integer(int_value) => RuntimeValue::Integer(*int_value),
                        CompiledConstant::Float(float_value) => RuntimeValue::Float(*float_value),
                    };

                    self.stack.push_back(new_value);
                }
                &Instruction::Import {
                    file_path_constant,
                    export_name_constant,
                } => {
                    let file_path = match get_constant!(file_path_constant) {
                        CompiledConstant::String(str_value) => str_value,
                        unexpected => return Err(format!("I was expecting constant #{} to be a filepath string, but it was {:?}. {COMPILE_ERROR_ASSURANCE}", file_path_constant, unexpected))
                    };
                    let export_name = match get_constant!(export_name_constant) {
                        CompiledConstant::String(str_value) => str_value,
                        unexpected => return Err(format!("I was expecting constant #{} to be an identifier string, but it was {:?}. {COMPILE_ERROR_ASSURANCE}", file_path_constant, unexpected))
                    };

                    if file_path.starts_with("core/") {
                        let value = get_core_export(file_path, export_name);
                        self.stack.push_back(value?);
                    } else {
                        let file = self
                            .files
                            .get(file_path)
                            .ok_or_else(|| format!("I couldn't find the file: {}.", file_path))?;

                        let export = file.exports.get(export_name).ok_or_else(|| {
                            format!(
                                "The file {} doesn't export anything (at least non-private) called {}.",
                                file_path, export_name
                            )
                        })?;

                        let value: RuntimeValue = match export {
                            CompiledExport::Type(_) => todo!(),
                            CompiledExport::Chunk(_) => todo!(),
                            CompiledExport::Constant(_) => todo!(),
                        };

                        self.stack.push_back(value);
                    }
                }
                &Instruction::ReadLocal(_) => todo!(),
                &Instruction::Construct => {
                    let constructor_type = self.stack.pop_back();

                    let constructor_type = match constructor_type {
                        Some(RuntimeValue::TypeReference(type_reference)) => type_reference,
                        Some(unexpected) => {
                            return Err(format!(
                                "Tried to construct a {unexpected:?}. {COMPILE_ERROR_ASSURANCE}"
                            ))
                        }
                        None => return Err(format!("Stack is empty. {COMPILE_ERROR_ASSURANCE}")),
                    };

                    match constructor_type.as_ref() {
                        RuntimeTypeReference::Signal(signal) => {
                            let mut params =
                                Vec::<RuntimeValue>::with_capacity(signal.params.len());
                            for _ in 0..signal.params.len() {
                                params.push(self.stack.pop_back().ok_or_else(|| {
                                    format!("Stack is empty. {COMPILE_ERROR_ASSURANCE}")
                                })?);
                            }
                            params.reverse();

                            let object = RuntimeObject {
                                type_descriptor: constructor_type.clone(),
                                values: params,
                            };

                            self.stack.push_back(RuntimeValue::Object(object.into()));
                        }
                        unexpected => {
                            return Err(format!(
                                "Tried to construct a {unexpected:?}. {COMPILE_ERROR_ASSURANCE}"
                            ))
                        }
                    }
                }
                &Instruction::CallFunction => todo!(),
                &Instruction::Cause => {
                    let signal = self
                        .stack
                        .pop_back()
                        .ok_or("Stack is empty. {COMPILE_ERROR_ASSURANCE}")?;
                    let signal = match signal {
                        RuntimeValue::Object(it) => it,
                        unexpected => return Err(format!("I was told to cause a {unexpected:?}, but it's not a signal, or even an object."))
                    };

                    call_frame.pending_signal = Some(signal.clone());
                    call_frame.instruction += 1;
                    return Ok(RunResult::Cause(signal));
                }
                &Instruction::Return => {
                    let value = self
                        .stack
                        .pop_back()
                        .ok_or("Stack is empty. {COMPILE_ERROR_ASSURANCE}")?;

                    // TODO: handle popping a call frame and returning to a calling function
                    return Ok(RunResult::Return(value));
                }
            }

            call_frame.instruction += 1;
        }
    }
}
