use std::borrow::Cow;
use std::cell::RefCell;
use std::collections::{HashMap, VecDeque};
use std::fmt::Debug;
use std::rc::Rc;
use std::sync::Arc;

use serde::{Deserialize, Serialize};

use crate::ast::Breadcrumbs;
use crate::compiled_file::{CompiledConstant, CompiledExport, CompiledFile};
use crate::compiler::{compile, CompilerInput};
use crate::core_runtime::get_core_export;
use crate::instructions::Instruction;
use crate::parse;
use crate::resolver::{
    resolve_for_file, FileResolverInput, ResolutionType, ResolvedFile, ResolverError,
};
use crate::types::{
    CanonicalLangTypeId, ErrorSourcePosition, LangTypeError, SignalCanonicalLangType,
};
use crate::types::{ResolvedValueLangType, ValueLangType};
use crate::{analyzer, core_builtin};

type WrappedCallFrame = Rc<RefCell<CallFrame>>;

pub struct LangVm {
    files: HashMap<String, Arc<CompiledFile>>,
    compile_errors: Vec<ResolverError>,
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

#[derive(Debug, Clone, PartialEq, Serialize)]
pub enum RuntimeValue {
    BadValue(RuntimeBadValue),
    Action,
    String(Arc<String>),
    Integer(i64),
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

#[derive(Clone, PartialEq, Eq, Serialize)]
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

impl RuntimeTypeReference {
    pub fn type_id(&self) -> &CanonicalLangTypeId {
        match self {
            RuntimeTypeReference::Signal(signal) => &signal.id,
        }
    }
}

#[derive(Debug, PartialEq, Serialize)]
pub struct RuntimeObject {
    // TODO: probably want to be a little stricter on making these
    pub type_descriptor: Arc<RuntimeTypeReference>,
    pub values: Vec<RuntimeValue>,
}

#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct RuntimeBadValue {
    pub file_path: String,
    pub breadcrumbs: Breadcrumbs,
}

#[derive(Debug, Clone, Serialize)]
pub struct ErrorTrace {
    pub file_path: String,
    pub breadcrumbs: Breadcrumbs,
    pub error: LangTypeError,
    pub proxy_chain: Vec<Breadcrumbs>,
}

#[derive(Debug, Serialize)]
pub enum RunResult {
    Returned(RuntimeValue),
    Caused(Arc<RuntimeObject>),
}

impl RunResult {
    pub fn expect_caused(self) -> Arc<RuntimeObject> {
        if let RunResult::Caused(signal) = self {
            signal
        } else {
            panic!("I expected {:?} to be a caused result.", self);
        }
    }

    pub fn expect_returned(self) -> RuntimeValue {
        if let RunResult::Returned(result) = self {
            result
        } else {
            panic!("I expected {:?} to be a returned result.", self);
        }
    }
}

const COMPILE_ERROR_ASSURANCE: &'static str =
    "This probably isn't your fault. This shouldn't happen if the compiler is working properly.";

impl LangVm {
    pub fn new() -> LangVm {
        LangVm {
            files: HashMap::new(),
            compile_errors: Vec::new(),
            call_frame: None,
            stack: VecDeque::new(),
        }
    }

    pub fn add_compiled_file(&mut self, file: CompiledFile) {
        self.files.insert(file.path.to_owned(), file.into());
    }

    pub fn add_file(&mut self, file_path: &str, source: &str) {
        let ast_node = parse::parse(source).unwrap();
        let analyzed_file = analyzer::analyze_file(&ast_node);
        // TODO: need a step between here and compilation to allow for loading other files

        let resolved_file = resolve_for_file(FileResolverInput {
            path: file_path.into(),
            file_node: &ast_node,
            analyzed: &analyzed_file,
            // TODO: need to include the other files in the VM
            other_files: HashMap::new(),
        });
        self.compile_errors.append(&mut resolved_file.get_errors());
        let compiled_file = compile(CompilerInput {
            file_node: &ast_node,
            analyzed: &analyzed_file,
            resolved: &resolved_file,
        });

        self.add_compiled_file(compiled_file);
    }

    pub fn get_compile_errors(&self) -> &[ResolverError] {
        &self.compile_errors
    }

    pub fn get_error_from_bad_value(
        &self,
        bad_value: &RuntimeBadValue,
    ) -> Result<ErrorTrace, String> {
        let mut proxy_chain = vec![];

        let mut breadcrumbs: Cow<Breadcrumbs> = Cow::Borrowed(&bad_value.breadcrumbs);
        let file: &CompiledFile = self
            .files
            .get(&bad_value.file_path)
            .ok_or_else(|| {
                format!(
                    "This error comes from {}, but I don't know that file.",
                    bad_value.file_path
                )
            })?
            .as_ref();
        let resolved_file: &ResolvedFile = file.resolved.as_ref().ok_or_else(|| {
            format!(
                "I don't have detailed error reporting for {}",
                bad_value.file_path
            )
        })?;

        let found_error = loop {
            let found_error = resolved_file
                .resolved_types
                .get(&(
                    ResolutionType::Inferred,
                    breadcrumbs.to_owned().into_owned(),
                ))
                .ok_or("Invalid breadcrumb")?;

            match found_error {
                ValueLangType::Pending => break LangTypeError::NeverResolved,
                ValueLangType::Resolved(value) => {
                    return Err(format!(
                        "This points to a value that isn't an error: {:?}",
                        value
                    ))
                }
                ValueLangType::Error(LangTypeError::ProxyError {
                    caused_by:
                        ErrorSourcePosition::SameFile {
                            path: _path,
                            breadcrumbs: new_breadcrumbs,
                        },
                }) => {
                    proxy_chain.push(breadcrumbs.into_owned());
                    breadcrumbs = Cow::Owned(new_breadcrumbs.to_owned())
                }
                ValueLangType::Error(err) => break err.to_owned(),
            }
        };

        Ok(ErrorTrace {
            file_path: bad_value.file_path.to_owned(),
            breadcrumbs: breadcrumbs.into_owned(),
            error: found_error,
            proxy_chain,
        })
    }

    pub fn get_type_id(&self, file_path: &str, name: &str) -> Result<CanonicalLangTypeId, String> {
        if file_path == "core/builtin" {
            let (_, builtin) = core_builtin::core_builtin_file();
            let found = builtin
                .exports
                .get(name)
                .ok_or_else(|| format!("No export named {name}"))?;
            match found {
                ValueLangType::Resolved(ResolvedValueLangType::Canonical(canonical)) => {
                    Ok(canonical.id().to_owned())
                }
                ValueLangType::Resolved(ResolvedValueLangType::TypeReference(reference)) => {
                    Ok(reference.to_owned())
                }
                _ => Err(format!("{name} is not a type.")),
            }
        } else {
            Err("I don't support getting types from custom files yet.".into())
        }
    }

    pub fn execute_function(
        &mut self,
        file_path: &str,
        function_name: &str,
        arguments: &[RuntimeValue],
    ) -> Result<RunResult, String> {
        let file = self
            .files
            .get(file_path)
            .ok_or_else(|| format!("I don't know about any file at {file_path}."))?;

        let export = file.exports.get(function_name).ok_or_else(|| format!("The file at {file_path} doesn't contain anything (at least that's not private) called {function_name}."))?;

        let chunk_id = if let CompiledExport::Chunk(chunk_id) = export {
            Ok(*chunk_id)
        } else {
            Err(format!(
                "{function_name} isn't a function, so I can't execute it."
            ))
        }?;

        if arguments.len() != 0 {
            return Err("I don't support executing a function with arguments right now.".into());
        }

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

            if cfg!(feature = "vm_play_by_play") {
                println!("stack: {:?}", self.stack);
                println!("instruction: {instruction:?}");
            }

            match instruction {
                &Instruction::Pop => {
                    self.stack.pop_front();
                }
                &Instruction::PushAction => todo!(),
                &Instruction::Literal(constant_id) => {
                    let new_value = match get_constant!(constant_id) {
                        CompiledConstant::String(str_value) => {
                            RuntimeValue::String(str_value.to_owned().into())
                        }
                        CompiledConstant::Integer(int_value) => RuntimeValue::Integer(*int_value),
                        CompiledConstant::Float(float_value) => RuntimeValue::Float(*float_value),
                        CompiledConstant::Error(err) => RuntimeValue::BadValue(err.to_owned()),
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
                        let value = get_core_export(&file_path, &export_name);
                        self.stack.push_back(value?);
                    } else {
                        let file = self
                            .files
                            .get(file_path.as_str())
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
                        .ok_or(format!("Stack is empty. {COMPILE_ERROR_ASSURANCE}"))?;
                    let signal = match signal {
                        RuntimeValue::Object(it) => it,
                        unexpected => return Err(format!("I was told to cause a {unexpected:?}, but it's not a signal, or even an object."))
                    };

                    call_frame.pending_signal = Some(signal.clone());
                    call_frame.instruction += 1;
                    return Ok(RunResult::Caused(signal));
                }
                &Instruction::Return => {
                    let value = self
                        .stack
                        .pop_back()
                        .ok_or("Stack is empty. {COMPILE_ERROR_ASSURANCE}")?;

                    // TODO: handle popping a call frame and returning to a calling function
                    return Ok(RunResult::Returned(value));
                }
            }

            call_frame.instruction += 1;
        }
    }
}
