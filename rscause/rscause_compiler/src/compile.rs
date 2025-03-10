use std::cell::RefCell;
use std::collections::{HashMap, VecDeque};
use std::rc::Rc;
use std::sync::Arc;

use crate::ast::{AnyAstNode, AstNode, NodeInfo};
use crate::breadcrumbs::HasBreadcrumbs;
use crate::compiled_file::{
    CompiledConstant, EffectProcedureIdentity, ErrorConst, ProcedureInstructionMapping,
};
use crate::error_types::{
    CompilerBugError, ErrorPosition, LangError, MissingElseBranchError, SourcePosition,
};
use crate::instructions::{
    BreakLoopInstruction, CallFunctionInstruction, CauseInstruction, ConstructInstruction,
    ContinueLoopInstruction, DefineFunctionInstruction, FinishEffectInstruction,
    GetMemberInstruction, ImportInstruction, ImportSameFileInstruction, Instruction,
    InstructionPhase, IsAssignableToInstruction, JumpIfFalseInstruction, JumpInstruction,
    LiteralInstruction, NameValueInstruction, NoOpInstruction, PopEffectsInstruction,
    PopInstruction, PopScopeInstruction, PushActionInstruction, ReadLocalInstruction,
    ReadLocalThroughEffectScopeInstruction, RegisterEffectInstruction, RejectSignalInstruction,
    ReturnInstruction, StartLoopInstruction, SwapInstruction, WriteLocalInstruction,
    WriteLocalThroughEffectScopeInstruction,
};
use crate::lang_types::OneOfLangType;
use crate::prelude::*;
use crate::resolve_types::ResolverError;
use crate::tags::{ReferencesFileNodeTag, TopLevelDeclarationNodeTag};
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
use crate::{find_tag, find_tags};
use anyhow::{anyhow, Result};
use num::{BigInt, BigRational};
use tap::Pipe;
use thiserror::Error;

#[derive(Error, Debug)]
#[error("support for {0} is not implemented")]
pub struct TodoCompilerError(String);

struct CompilerContext {
    path: Arc<String>,
    procedures: Vec<Procedure>,
    types: Arc<ResolveTypesResult>,
    constraint_errors: Arc<HashMap<Breadcrumbs, Vec<LangError>>>,
    canonical_types: HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>,
    scope_stack: VecDeque<Rc<RefCell<CompilerScope>>>,
    node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
}
impl CompilerContext {
    fn next_scope_index(&mut self) -> usize {
        let mut index = 0;
        for scope in self.scope_stack.iter().rev() {
            index += scope.borrow().named_value_indices.len();
            if !matches!(scope.borrow().scope_type, ScopeType::Body) {
                break;
            }
        }
        index
    }

    fn add_to_scope(&mut self, breadcrumbs: &Breadcrumbs) -> Result<usize> {
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

    fn get_tags(&self, breadcrumbs: &Breadcrumbs) -> Vec<NodeTag> {
        self.node_tags
            .get(breadcrumbs)
            .cloned()
            .unwrap_or_else(|| vec![])
    }

    fn check_for_badtype_error(&self, breadcrumbs: &Breadcrumbs) -> Result<Option<Arc<LangError>>> {
        let found_type = self
            .types
            .value_types
            .get(breadcrumbs)
            .ok_or(anyhow!("No type for {}", breadcrumbs))?;

        match found_type {
            InferredType::Error(err) => Some(err.clone()),
            InferredType::InferenceVariable(_) => Some(LangError::NeverResolved.into()),
            InferredType::Known(_) => None,
        }
        .or_else(|| {
            self.constraint_errors
                .get(breadcrumbs)
                .and_then(|errors_at_position| errors_at_position.iter().next())
                .map(|it| it.to_owned().into())
        })
        .pipe(Ok)
    }
}

#[derive(Debug)]
struct CompilerScope {
    scope_root: Breadcrumbs,
    scope_type: ScopeType,
    open_loop: Option<OpenLoop>,
    effect_count: u32,
    named_value_indices: HashMap<Breadcrumbs, usize>,
}
impl CompilerScope {
    fn new(scope_root: Breadcrumbs, scope_type: ScopeType) -> Self {
        Self {
            scope_root,
            scope_type,
            open_loop: None,
            effect_count: 0,
            named_value_indices: HashMap::new(),
        }
    }

    fn new_with_loop(scope_root: Breadcrumbs, scope_type: ScopeType, open_loop: OpenLoop) -> Self {
        let mut result = Self::new(scope_root, scope_type);
        result.open_loop = Some(open_loop);
        return result;
    }

    fn size(&self) -> usize {
        self.named_value_indices.len()
    }
}

#[derive(Debug)]
struct OpenLoop;

#[derive(Debug, Clone, PartialEq, Eq)]
enum ScopeType {
    Body,
    Function,
    Effect,
}

impl Procedure {
    fn write_instruction(&mut self, instruction: Instruction, node_info: Option<&NodeInfo>) {
        self.write_instruction_with_phase(instruction, node_info, InstructionPhase::Execute)
    }
    fn write_instruction_with_phase(
        &mut self,
        instruction: Instruction,
        node_info: Option<&NodeInfo>,
        phase: InstructionPhase,
    ) {
        // don't write no-op instructions
        match instruction {
            Instruction::NoOp(_) => return,
            Instruction::Pop(PopInstruction { number: 0 }) => return,
            Instruction::PopEffects(PopEffectsInstruction { number: 0 }) => return,
            Instruction::PopScope(PopScopeInstruction { values: 0 }) => return,
            _ => { /* continue */ }
        }
        self.instructions.push(instruction);
        if let Some(source_map) = &mut self.source_map {
            source_map.push(node_info.map(|node_info| ProcedureInstructionMapping {
                node_info: node_info.clone(),
                phase,
            }))
        }
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

    fn write_jump_placeholder(
        &mut self,
        node_info: &NodeInfo,
        phase: InstructionPhase,
    ) -> JumpPlaceholder {
        self.instructions
            .push(Instruction::NoOp(NoOpInstruction {}));
        if let Some(source_map) = &mut self.source_map {
            source_map.push(Some(ProcedureInstructionMapping {
                node_info: node_info.clone(),
                phase,
            }))
        }
        let index = self.instructions.len() - 1;
        JumpPlaceholder {
            index,
            make_instruction: Box::new(|instruction| {
                Instruction::Jump(JumpInstruction { instruction })
            }),
        }
    }

    fn write_jump_if_false_placeholder(
        &mut self,
        node_info: &NodeInfo,
        phase: InstructionPhase,
    ) -> JumpPlaceholder {
        self.instructions
            .push(Instruction::NoOp(NoOpInstruction {}));
        if let Some(source_map) = &mut self.source_map {
            source_map.push(Some(ProcedureInstructionMapping {
                node_info: node_info.clone(),
                phase,
            }))
        }
        let index = self.instructions.len() - 1;
        JumpPlaceholder {
            index,
            make_instruction: Box::new(|instruction| {
                Instruction::JumpIfFalse(JumpIfFalseInstruction { instruction })
            }),
        }
    }

    fn write_start_loop_placeholder(
        &mut self,
        node_info: &NodeInfo,
        phase: InstructionPhase,
    ) -> JumpPlaceholder {
        self.instructions
            .push(Instruction::NoOp(NoOpInstruction {}));
        if let Some(source_map) = &mut self.source_map {
            source_map.push(Some(ProcedureInstructionMapping {
                node_info: node_info.clone(),
                phase,
            }));
        }
        JumpPlaceholder {
            index: self.instructions.len() - 1,
            make_instruction: Box::new(|instruction| {
                Instruction::StartLoop(StartLoopInstruction {
                    end_instruction: instruction,
                })
            }),
        }
    }
}

struct JumpPlaceholder {
    index: usize,
    make_instruction: Box<dyn Fn(u32) -> Instruction>,
}

impl JumpPlaceholder {
    fn fill(self, procedure: &mut Procedure, jump_to: u32) {
        procedure.instructions[self.index] = (self.make_instruction)(jump_to);
    }

    fn fill_latest(self, procedure: &mut Procedure) {
        let jump_to = procedure.instructions.len() as u32;
        self.fill(procedure, jump_to);
    }
}

pub fn compile(
    path: Arc<String>,
    ast: &ast::FileNode,
    node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
    canonical_types: Arc<HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>>,
    types: Arc<ResolveTypesResult>,
) -> Result<CompiledFile> {
    let constraint_errors = {
        let mut errors = HashMap::new();
        for ResolverError { error, position } in types.errors.iter() {
            let errors_at_position = errors.entry(position.breadcrumbs.clone()).or_insert(vec![]);
            errors_at_position.push(error.clone());
        }
        Arc::new(errors)
    };
    let all_canonical_types = {
        let mut it = canonical_types.as_ref().to_owned();
        it.extend(
            types
                .new_canonical_types
                .iter()
                .map(|(k, v)| (k.clone(), v.clone())),
        );
        it
    };
    let mut ctx = CompilerContext {
        path: path.clone(),
        procedures: Vec::new(),
        canonical_types: all_canonical_types.into(),
        types,
        constraint_errors,
        scope_stack: VecDeque::new(),
        node_tags,
    };
    let mut exports: HashMap<Arc<String>, CompiledExport> = HashMap::new();

    for declaration in &ast.declarations {
        match declaration {
            ast::DeclarationNode::Import(_) => {}
            ast::DeclarationNode::Function(function) => {
                let procedure = compile_function_declaration(function, &mut ctx)?;
                let function_type = ctx
                    .types
                    .value_types
                    .get(function.breadcrumbs())
                    .ok_or_else(|| anyhow!("No type for function at {}", function.breadcrumbs()))?
                    .clone()
                    .and_then(|function_type| match function_type.as_ref() {
                        LangType::Function(function_type) => {
                            InferredType::Known(function_type.clone().into())
                        }
                        _ => InferredType::Error(
                            LangError::CompilerBug(CompilerBugError {
                                description: format!(
                                    "Function at {} has a non-function type",
                                    function.breadcrumbs()
                                ),
                            })
                            .into(),
                        ),
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
            ast::DeclarationNode::SignalType(_)
            | ast::DeclarationNode::ObjectType(_)
            | ast::DeclarationNode::OneOfType(_) => {
                let name = match declaration {
                    ast::DeclarationNode::SignalType(declaration) => declaration.name.text.clone(),
                    ast::DeclarationNode::ObjectType(declaration) => declaration.name.text.clone(),
                    ast::DeclarationNode::OneOfType(declaration) => declaration.name.text.clone(),
                    _ => unreachable!(),
                };
                let error = ctx.check_for_badtype_error(declaration.breadcrumbs())?;
                if let Some(error) = error {
                    exports.insert(name, CompiledExport::Error(error));
                    continue;
                }
                let resolved_type = ctx
                    .types
                    .value_types
                    .get(declaration.breadcrumbs())
                    .cloned()
                    .ok_or_else(|| {
                        anyhow!("No type for signal type at {}", declaration.breadcrumbs())
                    })?;
                let resolved_type = resolved_type.to_result_assuming_inferred().map_err(|err| {
                    anyhow!(
                        "Unexpected LangError at {}: {:?}",
                        declaration.breadcrumbs(),
                        err
                    )
                })?;
                let instance_type = resolved_type.get_referenced_value_type();
                exports.insert(name.clone(), CompiledExport::Type(instance_type));
            }
            ast::DeclarationNode::NamedValue(_) => { /* TODO */ }
        }
    }

    Ok(CompiledFile {
        path,
        procedures: ctx.procedures,
        exports,
        types: ctx.types.new_canonical_types.clone().into(),
    })
}

fn compile_function_declaration(
    function: &ast::FunctionNode,
    ctx: &mut CompilerContext,
) -> Result<Procedure> {
    compile_function(
        Some(function.name.text.clone()),
        &function.params,
        &function.info,
        ctx,
        |procedure, ctx| {
            compile_body(&function.body, procedure, ctx)?;
            if let Some(error) = ctx.check_for_badtype_error(function.body.breadcrumbs())? {
                compile_bad_value((&function.body).into(), error, procedure, ctx)?;
            }
            Ok(())
        },
    )
}

fn compile_function(
    name: Option<Arc<String>>,
    params: &[Arc<ast::FunctionSignatureParameterNode>],
    node_info: &NodeInfo,
    ctx: &mut CompilerContext,
    compile_body: impl FnOnce(&mut Procedure, &mut CompilerContext) -> Result<()>,
) -> Result<Procedure> {
    let mut procedure = Procedure {
        identity: ProcedureIdentity::Function(FunctionProcedureIdentity {
            name: name.clone(),
            declaration: node_info.clone(),
        }),
        constant_table: Vec::new(),
        instructions: Vec::new(),
        source_map: Some(Vec::new()),
    };
    let function_scope = Rc::new(RefCell::new(CompilerScope {
        scope_root: node_info.breadcrumbs.clone(),
        scope_type: ScopeType::Function,
        open_loop: None,
        effect_count: 0,
        named_value_indices: HashMap::new(),
    }));
    let mut old_scope_stack = ctx.scope_stack.clone();
    ctx.scope_stack.clear(); // brand-new scope for every function
    ctx.scope_stack.push_back(function_scope.clone());

    ctx.add_to_scope(&node_info.breadcrumbs)?; // the function itself is on the stack
    for param in params {
        ctx.add_to_scope(param.breadcrumbs())?;
        let name_constant =
            procedure.add_constant(CompiledConstant::String(param.name.text.clone()));
        procedure.write_instruction(
            Instruction::NameValue(NameValueInstruction {
                name_constant,
                variable: false,
                local_index: Some(
                    (ctx.scope_stack.back().unwrap().borrow().size() - 1).try_into()?,
                ),
            }),
            Some(node_info),
        );
    }
    let tags = ctx.get_tags(&node_info.breadcrumbs);
    let captured_value_tags = find_tags!(&tags, NodeTag::FunctionCapturesValue);
    for captured in captured_value_tags {
        ctx.add_to_scope(&captured.value)?;
    }

    compile_body(&mut procedure, ctx)?;
    assert_eq!(
        ctx.scope_stack.back().unwrap().as_ptr(),
        function_scope.as_ptr(),
        "function scope stack is not what we expected"
    );
    procedure.write_instruction_with_phase(
        Instruction::Return(ReturnInstruction {}),
        Some(node_info),
        InstructionPhase::Cleanup,
    );

    ctx.scope_stack.clear();
    ctx.scope_stack.append(&mut old_scope_stack);
    Ok(procedure)
}

fn compile_body(
    body: &ast::BodyNode,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    match body {
        ast::BodyNode::Block(block) => compile_block(block, procedure, ctx)?,
        ast::BodyNode::SingleExpression(body) => {
            compile_expression(&body.expression, procedure, ctx)?
        }
    };
    Ok(())
}

fn compile_block(
    block: &ast::BlockBodyNode,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    ctx.scope_stack
        .push_back(Rc::new(RefCell::new(CompilerScope {
            scope_root: block.breadcrumbs().clone(),
            scope_type: ScopeType::Body,
            open_loop: None,
            effect_count: 0,
            named_value_indices: HashMap::new(),
        })));

    for statement in block.statements.iter() {
        compile_statement(statement, procedure, ctx)?;
        // TODO: deal with NeverContinues
    }

    if let Some(result) = &block.result {
        compile_expression(result, procedure, ctx)?;
    } else {
        procedure.write_instruction_with_phase(
            Instruction::PushAction(PushActionInstruction {}),
            Some(&block.info),
            InstructionPhase::Cleanup,
        );
    }

    let scope = ctx
        .scope_stack
        .pop_back()
        .ok_or(anyhow!("No scope at end of block"))?;
    procedure.write_instruction_with_phase(
        Instruction::PopEffects(PopEffectsInstruction {
            number: scope.borrow().effect_count,
        }),
        Some(&block.info),
        InstructionPhase::Cleanup,
    );
    procedure.write_instruction_with_phase(
        Instruction::PopScope(PopScopeInstruction {
            values: scope.borrow().size().try_into()?,
        }),
        Some(&block.info),
        InstructionPhase::Cleanup,
    );
    Ok(())
}

fn compile_statement(
    statement: &ast::StatementNode,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    match statement {
        ast::StatementNode::Expression(statement) => {
            compile_expression(&statement.expression, procedure, ctx)?;
            procedure.write_instruction_with_phase(
                Instruction::Pop(PopInstruction { number: 1 }),
                Some(&statement.info),
                InstructionPhase::Cleanup,
            );
        }
        ast::StatementNode::Declaration(statement) => {
            compile_local_declaration(statement, procedure, ctx)?;
        }
        ast::StatementNode::Effect(statement) => {
            compile_effect_statement(statement, procedure, ctx)?;
        }
    }
    Ok(())
}

fn compile_local_declaration(
    statement: &ast::DeclarationStatementNode,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    match &statement.declaration {
        ast::DeclarationNode::Import(_) => {}

        ast::DeclarationNode::SignalType(_)
        | ast::DeclarationNode::ObjectType(_)
        | ast::DeclarationNode::OneOfType(_) => {
            let name = match &statement.declaration {
                ast::DeclarationNode::SignalType(declaration) => declaration.name.text.clone(),
                ast::DeclarationNode::ObjectType(declaration) => declaration.name.text.clone(),
                ast::DeclarationNode::OneOfType(declaration) => declaration.name.text.clone(),
                _ => unreachable!(),
            };
            let resolved_type = ctx
                .types
                .value_types
                .get(statement.declaration.breadcrumbs())
                .ok_or(anyhow!(
                    "couldn't resolve type for local signal declaration"
                ))?;
            let resolved_referenced_type = resolved_type
                .clone()
                .to_result_assuming_inferred()
                .and_then(|it| it.get_referenced_value_type().to_result_assuming_inferred());
            match resolved_referenced_type {
                Ok(resolved_type) => {
                    let constant =
                        procedure.add_constant(CompiledConstant::Type(resolved_type.clone()));
                    procedure.write_instruction(
                        Instruction::Literal(LiteralInstruction { constant }),
                        Some(&statement.declaration.info()),
                    );
                }
                Err(err) => {
                    compile_bad_value(statement.declaration.clone().into(), err, procedure, ctx)?
                }
            };
            ctx.add_to_scope(statement.declaration.breadcrumbs())?;
            let name_constant = procedure.add_constant(CompiledConstant::String(name.clone()));
            procedure.write_instruction(
                Instruction::NameValue(NameValueInstruction {
                    name_constant,
                    variable: false,
                    local_index: None,
                }),
                Some(&statement.declaration.info()),
            );
        }

        ast::DeclarationNode::Function(function) => {
            let tags = ctx.get_tags(function.breadcrumbs());
            let captured_values = find_tags!(&tags, NodeTag::FunctionCapturesValue).collect_vec();
            for captured in &captured_values {
                compile_value_reference(&function.info, &captured.value, procedure, ctx)?;
            }
            let new_procedure = compile_function_declaration(&function, ctx)?;

            if let Some(error) = ctx.check_for_badtype_error(function.breadcrumbs())? {
                compile_bad_value(function.into(), error, procedure, ctx)?;
            } else {
                ctx.procedures.push(new_procedure);
                let type_constant = procedure.add_constant(CompiledConstant::Type(
                    ctx.types
                        .value_types
                        .get(function.breadcrumbs())
                        .cloned()
                        .ok_or(anyhow!("missing type for {}", function.breadcrumbs()))?
                        .to_result_assuming_inferred()
                        .expect("cannot be an error due to check above"),
                ));
                procedure.write_instruction(
                    Instruction::DefineFunction(DefineFunctionInstruction {
                        type_constant,
                        procedure_index: ctx.procedures.len() as u32 - 1,
                        captured_values: captured_values.len() as u32,
                    }),
                    Some(function.info()),
                )
            }
            ctx.add_to_scope(function.breadcrumbs())?;
            let name_constant =
                procedure.add_constant(CompiledConstant::String(function.name.text.clone()));
            procedure.write_instruction(
                Instruction::NameValue(NameValueInstruction {
                    name_constant,
                    variable: false,
                    local_index: None,
                }),
                Some(function.info()),
            );
        }
        ast::DeclarationNode::NamedValue(named_value) => {
            compile_expression(&named_value.value, procedure, ctx)?;
            if let Some(error) = ctx.check_for_badtype_error(named_value.breadcrumbs())? {
                procedure.write_instruction(
                    Instruction::Pop(PopInstruction { number: 1 }),
                    Some(named_value.info()),
                );
                compile_bad_value(named_value.into(), error, procedure, ctx)?;
            }
            ctx.add_to_scope(named_value.breadcrumbs())?;
            let name_constant =
                procedure.add_constant(CompiledConstant::String(named_value.name.text.clone()));
            procedure.write_instruction(
                Instruction::NameValue(NameValueInstruction {
                    name_constant,
                    variable: named_value.is_variable,
                    local_index: None,
                }),
                Some(named_value.info()),
            );
        }
    }
    Ok(())
}

fn compile_effect_statement(
    statement: &ast::EffectStatementNode,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    let matching_type = ctx
        .types
        .value_types
        .get(&statement.pattern.type_reference.info().breadcrumbs)
        .ok_or(anyhow!("no type found for effect pattern"))?;
    let matching_type = if let InferredType::Known(matching_type) = matching_type {
        matching_type.clone()
    } else {
        // can't compile an effect without a valid type
        return Ok(());
    };

    let mut effect_procedure = Procedure {
        identity: ProcedureIdentity::Effect(EffectProcedureIdentity {
            matches_type: matching_type.clone(),
            declaration: statement.info().clone(),
        }),
        constant_table: Vec::new(),
        instructions: Vec::new(),
        source_map: Some(Vec::new()),
    };
    ctx.scope_stack
        .back()
        .ok_or(anyhow!("no scope"))?
        .borrow_mut()
        .effect_count += 1;
    ctx.scope_stack
        .push_back(Rc::new(RefCell::new(CompilerScope::new(
            statement.info().breadcrumbs.clone(),
            ScopeType::Effect,
        ))));
    ctx.add_to_scope(&statement.pattern.info.breadcrumbs)?;
    if let Some(name) = statement.pattern.name.as_ref().map(|it| it.text.clone()) {
        let name_constant = effect_procedure.add_constant(CompiledConstant::String(name.clone()));
        effect_procedure.write_instruction(
            Instruction::NameValue(NameValueInstruction {
                name_constant,
                variable: false,
                local_index: None,
            }),
            Some(&statement.info()),
        );
    }

    // Check the condition
    if ctx
        .check_for_badtype_error(&statement.pattern.type_reference.info().breadcrumbs)?
        .is_none()
    {
        effect_procedure.write_instruction(
            Instruction::ReadLocal(ReadLocalInstruction { index: 0 }),
            Some(statement.info()),
        );
        compile_value_flow_reference(
            (&statement.pattern.type_reference).into(),
            &mut effect_procedure,
            ctx,
        )?;
        effect_procedure.write_instruction(
            Instruction::IsAssignableTo(IsAssignableToInstruction {}),
            Some(statement.info()),
        );
        let reject_signal = effect_procedure
            .write_jump_if_false_placeholder(statement.info(), InstructionPhase::Execute);
        compile_body(&statement.body, &mut effect_procedure, ctx)?;
        effect_procedure.write_instruction_with_phase(
            Instruction::FinishEffect(FinishEffectInstruction {}),
            Some(statement.info()),
            InstructionPhase::Cleanup,
        );
        reject_signal.fill_latest(&mut effect_procedure);
    }

    effect_procedure.write_instruction_with_phase(
        Instruction::RejectSignal(RejectSignalInstruction {}),
        Some(statement.info()),
        InstructionPhase::Cleanup,
    );

    ctx.scope_stack.pop_back();
    ctx.procedures.push(effect_procedure);
    procedure.write_instruction(
        Instruction::RegisterEffect(RegisterEffectInstruction {
            procedure_index: ctx.procedures.len() as u32 - 1,
        }),
        Some(statement.info()),
    );

    Ok(())
}

fn compile_set_expression(
    expression: &ast::SetExpressionNode,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    compile_expression(&expression.expression, procedure, ctx)?;

    if let Some(error) = ctx.check_for_badtype_error(expression.breadcrumbs())? {
        procedure.write_instruction(
            Instruction::Pop(PopInstruction { number: 1 }),
            Some(&expression.info),
        );
        let error_const = add_error_constant(
            error.clone(),
            &AnyAstNode::SetExpression(expression.clone().into()),
            procedure,
            ctx,
        );
        match error.as_ref() {
            // MismatchedType errors are recoverable at runtime;
            // put a BadValue on the stack and keep going
            LangError::MismatchedType(_) => procedure.write_instruction(
                Instruction::Literal(LiteralInstruction {
                    constant: error_const,
                }),
                Some(&expression.info),
            ),

            _ => compile_type_error(error_const, procedure),
        }
        return Ok(());
    }

    let tags = ctx.get_tags(expression.breadcrumbs());
    let tag = find_tag!(&tags, NodeTag::SetsVariable).ok_or(anyhow!("Missing SetsVariable tag"))?;
    let value_reference = find_value_reference(&tag.variable, ctx)?;
    if value_reference.effect_depth > 0 {
        procedure.write_instruction(
            Instruction::WriteLocalThroughEffectScope(WriteLocalThroughEffectScopeInstruction {
                effect_depth: value_reference.effect_depth as u32,
                index: value_reference.found_index as u32,
            }),
            Some(&expression.info),
        );
    } else {
        procedure.write_instruction(
            Instruction::WriteLocal(WriteLocalInstruction {
                index: value_reference.found_index as u32,
            }),
            Some(&expression.info),
        );
    }

    Ok(())
}

fn compile_expression(
    expression: &ast::ExpressionNode,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    match expression {
        ast::ExpressionNode::Group(expression) => {
            compile_expression(&expression.expression, procedure, ctx)?
        }
        ast::ExpressionNode::Block(expression) => {
            compile_block(&expression.block, procedure, ctx)?;
        }
        ast::ExpressionNode::Function(expression) => {
            compile_function_expression(&expression, procedure, ctx)?;
        }
        ast::ExpressionNode::Branch(expression) => {
            compile_branch_expression(&expression, procedure, ctx)?;
        }
        ast::ExpressionNode::Loop(expression) => {
            compile_loop_expression(&expression, procedure, ctx)?;
        }
        ast::ExpressionNode::Return(expression) => {
            compile_return_expression(&expression, procedure, ctx)?;
        }
        ast::ExpressionNode::Break(expression) => {
            compile_break_expression(&expression, procedure, ctx)?;
        }
        ast::ExpressionNode::Set(expression) => {
            compile_set_expression(&expression, procedure, ctx)?
        }
        ast::ExpressionNode::Cause(expression) => {
            compile_cause_expression(expression.clone(), procedure, ctx)?;
        }
        ast::ExpressionNode::Call(expression) => {
            compile_call_expression(expression, procedure, ctx)?;
        }
        ast::ExpressionNode::PipeCall(expression) => {
            compile_pipe_call_expression(expression, procedure, ctx)?;
        }
        ast::ExpressionNode::Member(expression) => {
            compile_member_expression(expression.clone(), procedure, ctx)?
        }
        ast::ExpressionNode::Identifier(expression) => {
            compile_identifier_expression(expression.clone(), procedure, ctx)?;
        }
        ast::ExpressionNode::StringLiteral(expression) => {
            let constant =
                procedure.add_constant(CompiledConstant::String(expression.text.clone()));
            procedure.write_instruction(
                Instruction::Literal(LiteralInstruction { constant }),
                Some(&expression.info),
            );
        }
        ast::ExpressionNode::NumberLiteral(expression) => {
            let numerator = expression.value.mantissa().into();
            let denominator = BigInt::from(10).pow(expression.value.scale() as u32);
            let rational = BigRational::new(numerator, denominator);
            let constant = procedure.add_constant(CompiledConstant::Number(rational));
            procedure.write_instruction(
                Instruction::Literal(LiteralInstruction { constant }),
                Some(&expression.info),
            );
        }
    };

    let result_error = ctx.check_for_badtype_error(expression.breadcrumbs())?;
    if let Some(result_error) = result_error {
        procedure.write_instruction(
            Instruction::Pop(PopInstruction { number: 1 }),
            expression.info().into(),
        );
        compile_bad_value(expression.into(), result_error, procedure, ctx)?;
    }
    Ok(())
}

fn compile_identifier_expression(
    expression: Arc<ast::IdentifierExpressionNode>,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    compile_value_flow_reference((&expression.clone()).into(), procedure, ctx)
}

fn compile_cause_expression(
    expression: Arc<ast::CauseExpressionNode>,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    compile_expression(&expression.signal, procedure, ctx)?;

    if let Some(error) = ctx.check_for_badtype_error(&expression.breadcrumbs())? {
        let error_const = add_error_constant(error, &AnyAstNode::from(&expression), procedure, ctx);
        compile_type_error(error_const, procedure);
        return Ok(());
    }

    procedure.write_instruction(
        Instruction::Cause(CauseInstruction {}),
        Some(&expression.info),
    );
    Ok(())
}

fn compile_call_expression(
    expression: &ast::CallExpressionNode,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    compile_expression(&expression.callee, procedure, ctx)?;

    for param in &expression.parameters {
        compile_expression(&param.value, procedure, ctx)?;
        let badvalue = ctx.check_for_badtype_error(param.breadcrumbs())?;
        if let Some(badvalue) = badvalue {
            procedure.write_instruction(
                Instruction::Pop(PopInstruction { number: 1 }),
                Some(&param.info),
            );
            compile_bad_value(param.into(), badvalue, procedure, ctx)?;
        }
    }

    let callee_type = ctx
        .types
        .value_types
        .get(expression.callee.breadcrumbs())
        .cloned()
        .ok_or_else(|| anyhow!("No type for callee at {}", expression.callee.breadcrumbs()))?;

    let runtime_errors = ctx.check_for_badtype_error(expression.breadcrumbs())?;

    let error_preventing_call = runtime_errors
        .and_then(|runtime_errors| {
            // constructing is a sensitive operation; raise any error
            // if you're trying to construct an object
            let callee_is_construct = callee_type
                .clone()
                .to_result_assuming_inferred()
                .map(|it| matches!(it.as_ref(), LangType::TypeReference(_)))
                .unwrap_or(false);
            if callee_is_construct || runtime_errors.as_ref() == &LangError::NotCallable {
                Some(runtime_errors.clone())
            } else {
                None
            }
        })
        .or_else(|| callee_type.to_result_assuming_inferred_ref().err());

    if let Some(error_preventing_call) = error_preventing_call {
        // Don't call; pop all the arguments and the callee off the stack
        // and then raise an error
        procedure.write_instruction_with_phase(
            Instruction::Pop(PopInstruction {
                number: (expression.parameters.len() + 1) as u32,
            }),
            Some(&expression.info),
            InstructionPhase::Cleanup,
        );
        let error_const =
            add_error_constant(error_preventing_call, &expression.into(), procedure, ctx);
        compile_type_error(error_const, procedure);
        return Ok(());
    }

    let callee_type = callee_type
        .to_result_assuming_inferred()
        .map_err(|_| anyhow!("callee type was an error, but that should have been caught above"))?;

    match callee_type.as_ref() {
        LangType::TypeReference(type_reference) => {
            // TODO: handle unique types
            type_reference
                .clone()
                .to_result_assuming_inferred()
                .map_err(|_| anyhow!("Callee type is a reference to an error or unique type"))?
                .pipe(|instance_type| {
                    Ok(match instance_type.as_ref() {
                        LangType::Instance(instance) => {
                            let canonical_type = ctx.canonical_types.get(&instance.type_id).ok_or(
                                anyhow!("No canonical type found for {:?}", &instance.type_id),
                            )?;
                            let arity = canonical_type.fields().len() as u32;
                            procedure.write_instruction(
                                Instruction::Construct(ConstructInstruction { arity }),
                                Some(&expression.info),
                            );
                        }
                        LangType::StopgapDictionary | LangType::StopgapList => {
                            procedure.write_instruction(
                                Instruction::Construct(ConstructInstruction {
                                    arity: expression.parameters.len() as u32,
                                }),
                                Some(&expression.info),
                            );
                        }
                        _ => return Err(anyhow!("Can't construct a {instance_type:?}")),
                    })
                })?;
        }
        LangType::Function(_) => procedure.write_instruction(
            Instruction::CallFunction(CallFunctionInstruction {
                arity: expression.parameters.len() as u32,
            }),
            Some(&expression.info),
        ),
        _ => return Err(anyhow!("Callee {callee_type:?} is not callable")),
    }

    Ok(())
}

fn compile_pipe_call_expression(
    expression: &ast::PipeCallExpressionNode,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    // TODO: pretty much all of this is the same as compile_call_expression
    compile_expression(&expression.subject, procedure, ctx)?;
    compile_expression(&expression.callee, procedure, ctx)?;

    procedure.write_instruction_with_phase(
        Instruction::Swap(SwapInstruction {}),
        Some(&expression.info),
        InstructionPhase::Plumbing,
    );

    for param in &expression.parameters {
        compile_expression(&param.value, procedure, ctx)?;
        let badvalue = ctx.check_for_badtype_error(param.breadcrumbs())?;
        if let Some(badvalue) = badvalue {
            procedure.write_instruction(
                Instruction::Pop(PopInstruction { number: 1 }),
                Some(&param.info),
            );
            compile_bad_value(param.into(), badvalue, procedure, ctx)?;
        }
    }

    let callee_type = ctx
        .types
        .value_types
        .get(expression.callee.breadcrumbs())
        .cloned()
        .ok_or_else(|| anyhow!("No type for callee at {}", expression.callee.breadcrumbs()))?;

    let runtime_errors = ctx.check_for_badtype_error(expression.breadcrumbs())?;

    let error_preventing_call = runtime_errors
        .and_then(|runtime_errors| {
            // constructing is a sensitive operation; raise any error
            // if you're trying to construct an object
            let callee_is_construct = callee_type
                .clone()
                .to_result_assuming_inferred()
                .map(|it| matches!(it.as_ref(), LangType::TypeReference(_)))
                .unwrap_or(false);
            if callee_is_construct || runtime_errors.as_ref() == &LangError::NotCallable {
                Some(runtime_errors.clone())
            } else {
                None
            }
        })
        .or_else(|| callee_type.to_result_assuming_inferred_ref().err());

    if let Some(error_preventing_call) = error_preventing_call {
        // Don't call; pop all the arguments and the callee off the stack
        // and then raise an error
        procedure.write_instruction_with_phase(
            Instruction::Pop(PopInstruction {
                number: (expression.parameters.len() + 1) as u32,
            }),
            Some(&expression.info),
            InstructionPhase::Cleanup,
        );
        let error_const =
            add_error_constant(error_preventing_call, &expression.into(), procedure, ctx);
        compile_type_error(error_const, procedure);
        return Ok(());
    }

    let callee_type = callee_type
        .to_result_assuming_inferred()
        .map_err(|_| anyhow!("callee type was an error, but that should have been caught above"))?;

    match callee_type.as_ref() {
        LangType::TypeReference(type_reference) => {
            // TODO: handle unique types
            let canonical_type = type_reference
                .clone()
                .to_result_assuming_inferred()
                .map_err(|_| anyhow!("Callee type is a reference to an error or unique type"))
                .and_then(|instance_type| match instance_type.as_ref() {
                    LangType::Instance(instance) => {
                        ctx.canonical_types.get(&instance.type_id).ok_or(anyhow!(
                            "No canonical type found for {:?}",
                            &instance.type_id
                        ))
                    }
                    _ => Err(anyhow!("Can't construct a {instance_type:?}")),
                })?;
            let arity = canonical_type.fields().len() as u32;
            procedure.write_instruction(
                Instruction::Construct(ConstructInstruction { arity }),
                Some(&expression.info),
            )
        }
        LangType::Function(_) => procedure.write_instruction(
            Instruction::CallFunction(CallFunctionInstruction {
                arity: (expression.parameters.len() + 1) as u32,
            }),
            Some(&expression.info),
        ),
        _ => return Err(anyhow!("Callee {callee_type:?} is not callable")),
    }

    Ok(())
}

fn compile_member_expression(
    expression: Arc<ast::MemberExpressionNode>,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    compile_expression(&expression.object_expression, procedure, ctx)?;

    if let Some(error) = ctx.check_for_badtype_error(expression.breadcrumbs())? {
        procedure.write_instruction(
            Instruction::Pop(PopInstruction { number: 1 }),
            Some(&expression.info),
        );
        compile_bad_value(expression.into(), error, procedure, ctx)?;
        return Ok(());
    }

    // TODO: the resolver already did a lot of this work to figure out what type
    // is being referenced - can we lean on that?
    let object_type = ctx
        .types
        .value_types
        .get(expression.object_expression.breadcrumbs())
        .ok_or_else(|| anyhow!("No type found for object expression: {:?}", expression.member_identifier.breadcrumbs()))?
        .as_known()
        .ok_or_else(|| {
            anyhow!(
                "Object expression type is not known (should have been handled by BadValue check)"
            )
        })?
        .pipe(|object_type| {
            if let LangType::Instance(instance) = object_type.as_ref() {
                Ok(instance.to_owned())
            } else {
                Err(anyhow!("Object expression type is not an Instance (should have been handled by BadValue check)"))
            }
        })?;
    let fields = ctx
        .canonical_types
        .get(&object_type.type_id)
        .ok_or_else(|| {
            anyhow!(
                "Can't find canonical type for member expression. Member expression: {} Type ID: {}",
                expression.breadcrumbs(),
                object_type.type_id.to_string()
            )
        })?
        .fields();

    let field_name = &expression.member_identifier.text;
    let field_index = fields
        .iter()
        .enumerate()
        .find(|(_i, field)| &field.name == field_name)
        .ok_or_else(|| {
            anyhow!(
                "Member {field_name} doesn't exist in type (should have been handled by BadValue check)"
            )
        })?
        .0;

    procedure.write_instruction(
        Instruction::GetMember(GetMemberInstruction {
            index: field_index as u32,
        }),
        Some(&expression.info),
    );

    Ok(())
}

fn compile_function_expression(
    expression: &ast::FunctionExpressionNode,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    let tags = ctx.get_tags(expression.breadcrumbs());
    let captured_values = find_tags!(&tags, NodeTag::FunctionCapturesValue).collect_vec();
    for captured in &captured_values {
        compile_value_reference(&expression.info, &captured.value, procedure, ctx)?;
    }

    let function_procedure = compile_function(
        None,
        &expression.params,
        &expression.info,
        ctx,
        |function_procedure, ctx| compile_expression(&expression.body, function_procedure, ctx),
    )?;

    if let Some(error) = ctx.check_for_badtype_error(expression.breadcrumbs())? {
        compile_bad_value(expression.into(), error, procedure, ctx)?
    } else {
        ctx.procedures.push(function_procedure);
        let function_type = ctx
            .types
            .value_types
            .get(expression.breadcrumbs())
            .ok_or_else(|| anyhow!("Type not found for {}", expression.breadcrumbs()))?
            .to_result_assuming_inferred_ref()
            .map_err(|err| {
                anyhow!(
                    "Function type must be Known when getting to this codepath - actually was {:?}",
                    err
                )
            })?;
        let function_type_constant =
            procedure.add_constant(CompiledConstant::Type(function_type.clone()));
        procedure.write_instruction(
            Instruction::DefineFunction(DefineFunctionInstruction {
                procedure_index: (ctx.procedures.len() - 1) as u32,
                type_constant: function_type_constant,
                captured_values: captured_values.len() as u32,
            }),
            Some(&expression.info),
        );
    }
    Ok(())
}

fn compile_branch_expression(
    expression: &ast::BranchExpressionNode,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    ctx.scope_stack
        .push_back(Rc::new(RefCell::new(CompilerScope {
            scope_root: expression.breadcrumbs().clone(),
            scope_type: ScopeType::Body,
            open_loop: None,
            effect_count: 0,
            named_value_indices: HashMap::new(),
        })));
    let with_value_index = expression
        .with_value
        .as_ref()
        .map(|with_value| {
            compile_expression(&with_value, procedure, ctx)?;
            ctx.add_to_scope(with_value.breadcrumbs())
        })
        .transpose()?;

    let mut remaining_branch_jumps: Vec<JumpPlaceholder> = vec![];
    for branch in &expression.branches {
        match branch {
            ast::BranchOptionNode::If(branch) => {
                compile_expression(&branch.condition, procedure, ctx)?;
                let skip_body_instruction = procedure
                    .write_jump_if_false_placeholder(&branch.info, InstructionPhase::Execute);
                compile_body(&branch.body, procedure, ctx)?;
                remaining_branch_jumps.push(
                    procedure.write_jump_placeholder(branch.info(), InstructionPhase::Cleanup),
                );
                skip_body_instruction.fill_latest(procedure);
            }

            ast::BranchOptionNode::Is(branch) => {
                if let Some(with_value_index) = with_value_index {
                    procedure.write_instruction(
                        Instruction::ReadLocal(ReadLocalInstruction {
                            index: with_value_index as u32,
                        }),
                        Some(branch.info()),
                    );
                    compile_value_flow_reference(
                        (&branch.pattern.type_reference).into(),
                        procedure,
                        ctx,
                    )?;
                    procedure.write_instruction(
                        Instruction::IsAssignableTo(IsAssignableToInstruction {}),
                        Some(branch.info()),
                    );
                    let skip_body_instruction = procedure
                        .write_jump_if_false_placeholder(&branch.info, InstructionPhase::Execute);

                    ctx.scope_stack
                        .push_back(Rc::new(RefCell::new(CompilerScope::new(
                            branch.pattern.breadcrumbs().clone(),
                            ScopeType::Body,
                        ))));
                    procedure.write_instruction(
                        Instruction::ReadLocal(ReadLocalInstruction {
                            index: with_value_index.try_into()?,
                        }),
                        Some(branch.info()),
                    );
                    ctx.add_to_scope(&branch.pattern.info.breadcrumbs)?;
                    if let Some(name) = &branch.pattern.name {
                        let name_constant =
                            procedure.add_constant(CompiledConstant::String(name.text.clone()));
                        procedure.write_instruction(
                            Instruction::NameValue(NameValueInstruction {
                                name_constant,
                                variable: false,
                                local_index: None,
                            }),
                            Some(&branch.info),
                        );
                    }

                    compile_body(&branch.body, procedure, ctx)?;

                    procedure.write_instruction_with_phase(
                        Instruction::PopScope(PopScopeInstruction {
                            values: ctx
                                .scope_stack
                                .back()
                                .ok_or(anyhow!("no scope"))?
                                .borrow()
                                .size()
                                .try_into()?,
                        }),
                        Some(branch.info()),
                        InstructionPhase::Cleanup,
                    );
                    ctx.scope_stack.pop_back();
                    remaining_branch_jumps.push(
                        procedure.write_jump_placeholder(branch.info(), InstructionPhase::Cleanup),
                    );

                    skip_body_instruction.fill_latest(procedure);
                }
            }

            ast::BranchOptionNode::Else(branch) => {
                compile_body(&branch.body, procedure, ctx)?;
                remaining_branch_jumps.push(
                    procedure.write_jump_placeholder(branch.info(), InstructionPhase::Cleanup),
                );
            }
        }
    }
    let else_branch = expression.branches.iter().find_map(|it| match it {
        ast::BranchOptionNode::Else(branch) => Some(branch),
        _ => None,
    });
    if else_branch.is_none() {
        let return_type = ctx
            .types
            .value_types
            .get(expression.breadcrumbs())
            .cloned()
            .ok_or(anyhow!("no type found for branch expression"))?;
        let error = return_type.try_as_error_ref().cloned().unwrap_or_else(|| {
            LangError::MissingElseBranch(MissingElseBranchError { options: None }).pipe(Arc::new)
        });
        let error_const = procedure.add_constant(CompiledConstant::Error(ErrorConst {
            source_position: ErrorPosition::Source(SourcePosition {
                path: ctx.path.clone(),
                breadcrumbs: expression.breadcrumbs().clone(),
                position: expression.info.position,
            }),
            error: error,
        }));

        // If we're supposed to return an Action or NeverContinues, then this should be an immediate error
        // because the BadValue has nowhere to go
        let return_one_of = OneOfLangType::new_with_one(return_type.into());
        let should_report_error = return_one_of.options.len() == 0
            || return_one_of.options.iter().all(|option| {
                matches!(option, InferredType::InferenceVariable(_))
                    || matches!(option, InferredType::Error(_))
                    || option
                        .try_as_known_ref()
                        .map(|option| {
                            matches!(option.as_ref(), LangType::Action)
                                || matches!(option.as_ref(), LangType::NeverContinues)
                        })
                        .unwrap_or(false)
            });
        if should_report_error {
            compile_type_error(error_const, procedure);
        } else {
            procedure.write_instruction(
                Instruction::Literal(LiteralInstruction {
                    constant: error_const,
                }),
                Some(&expression.info),
            );
        }
    }

    for jump in remaining_branch_jumps {
        jump.fill_latest(procedure);
    }

    procedure.write_instruction_with_phase(
        Instruction::PopScope(PopScopeInstruction {
            values: ctx
                .scope_stack
                .back()
                .ok_or(anyhow!("no scope"))?
                .borrow()
                .size()
                .try_into()?,
        }),
        Some(expression.info()),
        InstructionPhase::Cleanup,
    );
    ctx.scope_stack.pop_back();

    // TODO: error handling
    Ok(())
}

fn compile_loop_expression(
    expression: &ast::LoopExpressionNode,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    let start_loop_placeholder =
        procedure.write_start_loop_placeholder(&expression.info, InstructionPhase::Execute);
    ctx.scope_stack.push_back(
        CompilerScope::new_with_loop(expression.breadcrumbs().clone(), ScopeType::Body, OpenLoop)
            .pipe(|it| Rc::new(RefCell::new(it))),
    );
    compile_body(&expression.body, procedure, ctx)?;
    ctx.scope_stack.pop_back();
    procedure.write_instruction(
        Instruction::ContinueLoop(ContinueLoopInstruction {}),
        Some(&expression.info),
    );
    start_loop_placeholder.fill_latest(procedure);

    return Ok(());
}

fn compile_return_expression(
    expression: &ast::ReturnExpressionNode,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    if let Some(value) = &expression.value {
        compile_expression(value, procedure, ctx)?;
    } else {
        procedure.write_instruction_with_phase(
            Instruction::PushAction(PushActionInstruction {}),
            Some(expression.info()),
            InstructionPhase::Setup,
        );
    }

    procedure.write_instruction(
        Instruction::Return(ReturnInstruction {}),
        Some(expression.info()),
    );

    Ok(())
}

fn compile_break_expression(
    expression: &ast::BreakExpressionNode,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    if let Some(with_value) = &expression.with_value {
        compile_expression(with_value, procedure, ctx)?;
    } else {
        procedure.write_instruction_with_phase(
            Instruction::PushAction(PushActionInstruction {}),
            Some(expression.info()),
            InstructionPhase::Setup,
        );
    }

    if let Some(error) = ctx.check_for_badtype_error(expression.breadcrumbs())? {
        procedure.write_instruction(
            Instruction::Pop(PopInstruction { number: 1 }),
            Some(expression.info()),
        );
        let error_const = add_error_constant(error.clone(), &expression.into(), procedure, ctx);
        compile_type_error(error_const, procedure);
        return Ok(());
    }

    let tags = ctx.get_tags(expression.breadcrumbs());
    let break_tag =
        find_tag!(&tags, NodeTag::BreaksLoop).ok_or(anyhow!("Could not find BreaksLoop tag"))?;
    let loop_index = ctx
        .scope_stack
        .iter()
        .rev()
        .filter(|it| it.borrow().open_loop.is_some())
        .enumerate()
        .find_map(|(i, scope)| {
            if scope.borrow().scope_root == break_tag.r#loop {
                Some(i)
            } else {
                None
            }
        })
        .ok_or(anyhow!(
            "Couldn't find an open loop (shouldn't have gotten this far)"
        ))?;

    procedure.write_instruction(
        Instruction::BreakLoop(BreakLoopInstruction {
            levels: (loop_index + 1) as i32,
        }),
        Some(expression.info()),
    );

    return Ok(());
}

fn compile_value_flow_reference(
    node: AnyAstNode,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    if let Some(error) = ctx.check_for_badtype_error(node.breadcrumbs())? {
        compile_bad_value(node, error, procedure, ctx)?;
        return Ok(());
    }

    let node_tags = ctx.get_tags(node.breadcrumbs());
    let comes_from = find_tag!(&node_tags, NodeTag::ValueComesFrom)
        .ok_or_else(|| anyhow!("No ValueComesFrom tag on {}", node.breadcrumbs()))?;
    let source_tags = ctx.get_tags(&comes_from.source);

    if let Some(it) = find_tag!(&source_tags, NodeTag::ReferencesFile) {
        compile_file_import_reference(node.info(), it, procedure, ctx)?;
        return Ok(());
    }

    if let Some(it) = find_tag!(&source_tags, NodeTag::TopLevelDeclaration) {
        compile_top_level_reference(node.info(), it, procedure, ctx)?;
        return Ok(());
    }

    if let Some(_) = find_tag!(&source_tags, NodeTag::DeclarationForScope) {
        compile_value_reference(node.info(), &comes_from.source, procedure, ctx)?;
        return Ok(());
    }

    Err(anyhow!(
        "Wasn't able to resolve identifier at {} to anything",
        node.breadcrumbs()
    ))
}

fn compile_top_level_reference(
    reference_node_info: &NodeInfo,
    tag: TopLevelDeclarationNodeTag,
    procedure: &mut Procedure,
    _ctx: &mut CompilerContext,
) -> Result<()> {
    let export_name_constant = procedure.add_constant(CompiledConstant::String(tag.name));
    procedure.write_instruction(
        Instruction::ImportSameFile(ImportSameFileInstruction {
            export_name_constant,
        }),
        Some(reference_node_info),
    );
    Ok(())
}

fn compile_file_import_reference(
    reference_node_info: &NodeInfo,
    tag: ReferencesFileNodeTag,
    procedure: &mut Procedure,
    _ctx: &mut CompilerContext,
) -> Result<()> {
    let file_path_constant = procedure.add_constant(CompiledConstant::String(tag.path.clone()));
    let export_name_constant = tag
        .export_name
        .map(|it| procedure.add_constant(CompiledConstant::String(it)));

    if let Some(export_name_constant) = export_name_constant {
        procedure.write_instruction(
            Instruction::Import(ImportInstruction {
                file_path_constant,
                export_name_constant,
            }),
            Some(reference_node_info),
        )
    } else {
        return Err(anyhow!(
            "Haven't implemented files as first-class objects yet"
        ));
    }
    Ok(())
}

fn compile_value_reference(
    reference_node_info: &NodeInfo,
    source: &Breadcrumbs,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    let value_reference = find_value_reference(source, ctx)?;
    procedure.write_instruction(
        if value_reference.effect_depth > 0 {
            Instruction::ReadLocalThroughEffectScope(ReadLocalThroughEffectScopeInstruction {
                index: value_reference.found_index.try_into()?,
                effect_depth: value_reference.effect_depth.try_into()?,
            })
        } else {
            Instruction::ReadLocal(ReadLocalInstruction {
                index: value_reference.found_index.try_into()?,
            })
        },
        Some(reference_node_info),
    );
    Ok(())
}

struct ValueReferenceResult {
    found_index: usize,
    effect_depth: usize,
}

fn find_value_reference(
    source: &Breadcrumbs,
    ctx: &CompilerContext,
) -> Result<ValueReferenceResult> {
    let mut effect_depth = 0;
    for scope in ctx.scope_stack.iter().rev() {
        if let Some(index) = scope.borrow().named_value_indices.get(source) {
            return Ok(ValueReferenceResult {
                found_index: *index as usize,
                effect_depth,
            });
        }
        if scope.borrow().scope_type == ScopeType::Effect {
            effect_depth += 1;
        }
    }
    Err(anyhow!("Couldn't find named value for {} in scope", source))
}

fn compile_bad_value(
    node: AnyAstNode,
    error: Arc<LangError>,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    let error_const = add_error_constant(error, &node, procedure, ctx);
    procedure.write_instruction(
        Instruction::Literal(LiteralInstruction {
            constant: error_const,
        }),
        Some(node.info()),
    );
    Ok(())
}

fn add_error_constant(
    error: Arc<LangError>,
    node: &AnyAstNode,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> u32 {
    procedure.add_constant(CompiledConstant::Error(ErrorConst {
        source_position: ErrorPosition::Source(SourcePosition {
            path: ctx.path.clone(),
            breadcrumbs: node.breadcrumbs().clone(),
            position: node.info().position,
        }),
        error,
    }))
}

fn compile_type_error(error_const: u32, procedure: &mut Procedure) {
    let file_path_constant = procedure.add_constant(CompiledConstant::String(
        "core/builtin.cau".to_owned().into(),
    ));
    let export_name_constant =
        procedure.add_constant(CompiledConstant::String("TypeError".to_owned().into()));
    procedure.write_instruction(
        Instruction::Import(ImportInstruction {
            file_path_constant,
            export_name_constant,
        }),
        None,
    );
    procedure.write_instruction(
        Instruction::Literal(LiteralInstruction {
            constant: error_const,
        }),
        None,
    );
    procedure.write_instruction(
        Instruction::Construct(ConstructInstruction { arity: 1 }),
        None,
    );
    procedure.write_instruction(Instruction::Cause(CauseInstruction {}), None);
}
