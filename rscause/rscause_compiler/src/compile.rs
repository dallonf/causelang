use std::cell::RefCell;
use std::collections::{HashMap, VecDeque};
use std::rc::Rc;
use std::sync::Arc;

use crate::compiled_file::CompiledConstant;
use crate::instructions::{
    Instruction, InstructionPhase, NameValueInstruction, PopEffectsInstruction, PopInstruction,
    PopScopeInstruction, ReturnInstruction,
};
use crate::{
    ast,
    breadcrumbs::Breadcrumbs,
    compiled_file::{
        CompiledExport, CompiledFile, FunctionProcedureIdentity, Procedure, ProcedureIdentity,
    },
    lang_types::{CanonicalLangType, CanonicalLangTypeId, InferredType, LangType},
    resolve_types::ResolveTypesResult,
    tags::NodeTag,
};
use anyhow::{anyhow, Result};
use thiserror::Error;

#[derive(Error, Debug)]
#[error("support for {0} is not implemented")]
pub struct TodoCompilerError(String);

struct CompilerContext {
    procedures: Vec<Procedure>,
    types: Arc<ResolveTypesResult>,
    scope_stack: VecDeque<Rc<RefCell<CompilerScope>>>,
    node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
}
impl CompilerContext {
    fn next_scope_index(&mut self) -> u32 {
        let mut index = 0;
        for scope in self.scope_stack.iter().rev() {
            index += scope.borrow().named_value_indices.len() as u32;
        }
        index
    }

    fn add_to_scope(&mut self, breadcrumbs: &Breadcrumbs) -> Result<u32> {
        let index = self.next_scope_index();
        let current_scope = &mut self
            .scope_stack
            .back()
            .ok_or(anyhow!("No scope"))?
            .as_ref()
            .borrow_mut()
            .named_value_indices;
        if current_scope.contains_key(breadcrumbs) {
            return Err(anyhow!("{} already exists on scope!", breadcrumbs));
        }
        current_scope.insert(breadcrumbs.clone(), index);
        Ok(index)
    }
}

struct CompilerScope {
    scope_root: Breadcrumbs,
    scope_type: ScopeType,
    open_loop: Option<OpenLoop>,
    effect_count: u32,
    named_value_indices: HashMap<Breadcrumbs, u32>,
}
impl CompilerScope {
    fn size(&self) -> u32 {
        self.named_value_indices.len() as u32
    }
}

struct OpenLoop(Breadcrumbs);

enum ScopeType {
    Body,
    Function,
    Effect,
}

impl Procedure {
    fn write_instruction(&mut self, instruction: Instruction, breadcrumbs: &Breadcrumbs) {
        self.write_instruction_with_phase(instruction, breadcrumbs, InstructionPhase::Execute)
    }
    fn write_instruction_with_phase(
        &mut self,
        instruction: Instruction,
        breadcrumbs: &Breadcrumbs,
        phase: InstructionPhase,
    ) {
        // don't write no-op instructions
        match instruction {
            Instruction::NoOp(_) => return,
            Instruction::Pop(PopInstruction { number: 0 }) => return,
            Instruction::PopEffects(PopEffectsInstruction { number: 0 }) => return,
            Instruction::PopScope(PopScopeInstruction { values: 0 }) => return,
            _ => {}
        }
        self.instructions.push(instruction);
        // TODO: sourcemap
    }

    fn add_constant(&mut self, constant: CompiledConstant) -> u32 {
        let existing_index = self.constant_table.iter().position(|it| *it == constant);
        if let Some(existing_index) = existing_index {
            return existing_index as u32;
        } else {
            let index = self.constant_table.len();
            self.constant_table.push(constant);
            index as u32
        }
    }
}

pub fn compile(
    ast: &ast::FileNode,
    node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
    canonical_types: HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>,
    types: Arc<ResolveTypesResult>,
) -> Result<CompiledFile> {
    let mut ctx = CompilerContext {
        procedures: Vec::new(),
        types: types,
        scope_stack: VecDeque::new(),
        node_tags,
    };
    let mut exports: HashMap<Arc<String>, CompiledExport> = HashMap::new();

    for declaration in &ast.declarations {
        match declaration {
            ast::DeclarationNode::Import(_) => {}
            ast::DeclarationNode::Function(function) => {
                let procedure = compile_function_declaration(function.clone(), &mut ctx)?;
                let function_type = ctx
                    .types
                    .value_types
                    .get(&function.name.breadcrumbs)
                    .ok_or_else(|| anyhow!("No type for function at {}", &function.breadcrumbs))?
                    .clone()
                    .and_then(|function_type| match function_type.as_ref() {
                        LangType::Function(function_type) => {
                            InferredType::Known(function_type.clone().into())
                        }
                        _ => InferredType::Error,
                    });

                ctx.procedures.push(procedure);
                exports.insert(
                    function.name.text.clone(),
                    CompiledExport::Function {
                        procedure_index: ctx.procedures.len() as u32 - 1,
                        function_type,
                    },
                );
            }
        }
    }
    todo!()
}

fn compile_function_declaration(
    function: Arc<ast::FunctionNode>,
    ctx: &mut CompilerContext,
) -> Result<Procedure> {
    compile_function(
        function.name.text.clone(),
        &function.params,
        &function.breadcrumbs,
        ctx,
        |&mut procedure| {
            compile_body(function.body.clone(), procedure, ctx);
            // TODO: report errors
        },
    )
}

fn compile_function(
    name: Arc<String>,
    params: &[Arc<ast::FunctionSignatureParameterNode>],
    breadcrumbs: &Breadcrumbs,
    ctx: &mut CompilerContext,
    compile_body: impl FnOnce(&mut Procedure),
) -> std::result::Result<Procedure, anyhow::Error> {
    let mut procedure = Procedure {
        identity: ProcedureIdentity::Function(FunctionProcedureIdentity { name: name.clone() }),
        constant_table: Vec::new(),
        instructions: Vec::new(),
    };
    let mut function_scope = Rc::new(RefCell::new(CompilerScope {
        scope_root: breadcrumbs.clone(),
        scope_type: ScopeType::Function,
        open_loop: None,
        effect_count: 0,
        named_value_indices: HashMap::new(),
    }));
    let mut old_scope_stack = ctx.scope_stack.clone();
    ctx.scope_stack.clear(); // brand-new scope for every function
    ctx.scope_stack.push_back(function_scope.clone());

    ctx.add_to_scope(breadcrumbs); // the function itself is on the stack
    for param in params {
        ctx.add_to_scope(&param.breadcrumbs);
        procedure.write_instruction(
            Instruction::NameValue(NameValueInstruction {
                name_constant: procedure
                    .add_constant(CompiledConstant::String(param.name.text.clone())),
                variable: false,
                local_index: Some(ctx.scope_stack.back().unwrap().borrow().size() - 1),
            }),
            breadcrumbs,
        );
    }
    // TODO: handle closure captures

    compile_body(&mut procedure);
    assert_eq!(
        ctx.scope_stack.back().unwrap().as_ptr(),
        function_scope.as_ptr()
    );
    procedure.write_instruction_with_phase(
        Instruction::Return(ReturnInstruction {}),
        breadcrumbs,
        InstructionPhase::Cleanup,
    );

    ctx.scope_stack.clear();
    ctx.scope_stack.append(&mut old_scope_stack);
    Ok(procedure)
}
