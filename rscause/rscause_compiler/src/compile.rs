use std::cell::RefCell;
use std::collections::{HashMap, VecDeque};
use std::rc::Rc;
use std::sync::Arc;

use crate::ast::{AnyAstNode, AstNode, NodeInfo};
use crate::breadcrumbs::HasBreadcrumbs;
use crate::compiled_file::{CompiledConstant, ErrorConst};
use crate::error_types::{CompilerBugError, ErrorPosition, LangError, SourcePosition};
use crate::find_tag;
use crate::instructions::{
    CallFunctionInstruction, CauseInstruction, ConstructInstruction, DefineFunctionInstruction,
    ImportInstruction, Instruction, InstructionPhase, IsAssignableToInstruction,
    JumpIfFalseInstruction, JumpInstruction, LiteralInstruction, NameValueInstruction,
    NoOpInstruction, PopEffectsInstruction, PopInstruction, PopScopeInstruction,
    PushActionInstruction, ReadLocalInstruction, ReadLocalThroughEffectScopeInstruction,
    ReturnInstruction,
};
use crate::tags::ReferencesFileNodeTag;
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
    canonical_types: Arc<HashMap<Arc<CanonicalLangTypeId>, Arc<CanonicalLangType>>>,
    scope_stack: VecDeque<Rc<RefCell<CompilerScope>>>,
    node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
}
impl CompilerContext {
    fn next_scope_index(&mut self) -> usize {
        let mut index = 0;
        for scope in self.scope_stack.iter().rev() {
            index += scope.borrow().named_value_indices.len();
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

    fn check_for_runtime_error(&self, breadcrumbs: &Breadcrumbs) -> Result<Option<Arc<LangError>>> {
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
        .pipe(Ok)
    }
}

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

    fn size(&self) -> usize {
        self.named_value_indices.len()
    }
}

struct OpenLoop(Breadcrumbs);

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

    fn write_jump_placeholder(
        &mut self,
        node_info: &NodeInfo,
        phase: InstructionPhase,
    ) -> JumpPlaceholder {
        self.instructions
            .push(Instruction::NoOp(NoOpInstruction {}));
        // TODO: sourcemap
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
        // TODO: sourcemap
        let index = self.instructions.len() - 1;
        JumpPlaceholder {
            index,
            make_instruction: Box::new(|instruction| {
                Instruction::JumpIfFalse(JumpIfFalseInstruction { instruction })
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
    let mut ctx = CompilerContext {
        path: path.clone(),
        procedures: Vec::new(),
        canonical_types,
        types,
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
            ast::DeclarationNode::NamedValue(_) => { /* TODO */ }
        }
    }

    Ok(CompiledFile {
        path,
        procedures: ctx.procedures,
        exports,
    })
}

fn compile_function_declaration(
    function: &ast::FunctionNode,
    ctx: &mut CompilerContext,
) -> Result<Procedure> {
    compile_function(
        function.name.text.clone(),
        &function.params,
        &function.info,
        ctx,
        |procedure, ctx| {
            compile_body(&function.body, procedure, ctx)?;
            // TODO: report errors
            Ok(())
        },
    )
}

fn compile_function(
    name: Arc<String>,
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
    // TODO: handle closure captures

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
        ast::BodyNode::Block(block) => compile_block(block, procedure, ctx),
        ast::BodyNode::SingleStatement(body) => {
            compile_statement(&body.statement, procedure, ctx, true)
        }
    }
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

    if block.statements.is_empty() {
        procedure.write_instruction(
            Instruction::PushAction(PushActionInstruction {}),
            Some(&block.info),
        );
    }

    for (i, statement) in block.statements.iter().enumerate() {
        compile_statement(statement, procedure, ctx, i == block.statements.len() - 1)?;
        // TODO: deal with NeverContinues
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
    is_last_statement: bool,
) -> Result<()> {
    match statement {
        ast::StatementNode::Expression(statement) => {
            compile_expression(&statement.expression, procedure, ctx)?;
            if !is_last_statement {
                procedure.write_instruction_with_phase(
                    Instruction::Pop(PopInstruction { number: 1 }),
                    Some(&statement.info),
                    InstructionPhase::Cleanup,
                );
            }
        }
        ast::StatementNode::Declaration(statement) => {
            compile_local_declaration(statement, procedure, ctx)?;
            if is_last_statement {
                procedure.write_instruction_with_phase(
                    Instruction::PushAction(PushActionInstruction {}),
                    Some(&statement.info),
                    InstructionPhase::Cleanup,
                );
            }
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
        ast::DeclarationNode::Import(_) => todo!(),
        ast::DeclarationNode::Function(function) => {
            // TODO: captured values
            let new_procedure = compile_function_declaration(&function, ctx)?;

            if let Some(error) = ctx.check_for_runtime_error(function.breadcrumbs())? {
                compile_bad_value(function.into(), error, procedure, ctx)?;
            } else {
                ctx.procedures.push(new_procedure);
                let type_constant = procedure.add_constant(CompiledConstant::Type(
                    ctx.types
                        .value_types
                        .get(function.breadcrumbs())
                        .cloned()
                        .ok_or(anyhow!("missing type for {}", function.breadcrumbs()))?
                        .to_result()
                        .expect("cannot be an error due to check above"),
                ));
                procedure.write_instruction(
                    Instruction::DefineFunction(DefineFunctionInstruction {
                        type_constant,
                        procedure_index: ctx.procedures.len() as u32 - 1,
                        captured_values: 0,
                    }),
                    Some(function.info()),
                )
            }
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
            if let Some(error) = ctx.check_for_runtime_error(named_value.breadcrumbs())? {
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

fn compile_expression(
    expression: &ast::ExpressionNode,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    match expression {
        ast::ExpressionNode::Branch(expression) => {
            compile_branch_expression(&expression, procedure, ctx)
        }
        ast::ExpressionNode::Cause(expression) => {
            compile_cause_expression(expression.clone(), procedure, ctx)
        }
        ast::ExpressionNode::Call(expression) => {
            compile_call_expression(expression, procedure, ctx)
        }
        ast::ExpressionNode::Identifier(expression) => {
            compile_identifier_expression(expression.clone(), procedure, ctx)
        }
        ast::ExpressionNode::StringLiteral(expression) => {
            let constant =
                procedure.add_constant(CompiledConstant::String(expression.text.clone()));
            procedure.write_instruction(
                Instruction::Literal(LiteralInstruction { constant }),
                Some(&expression.info),
            );
            Ok(())
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
            Ok(())
        }
    }
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

    if let Some(error) = ctx.check_for_runtime_error(&expression.breadcrumbs())? {
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
        // TODO: handle errors
    }

    // TODO: handle an error preventing the call

    let callee_type = ctx
        .types
        .value_types
        .get(expression.callee.breadcrumbs())
        .cloned()
        .ok_or_else(|| anyhow!("No type for callee at {}", expression.callee.breadcrumbs()))?
        .to_result()
        .map_err(|_| anyhow!("Callee type is an error"))?;

    match callee_type.as_ref() {
        LangType::TypeReference(type_reference) => {
            // TODO: handle unique types
            let canonical_type = type_reference
                .clone()
                .to_result()
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
                arity: expression.parameters.len() as u32,
            }),
            Some(&expression.info),
        ),
        _ => return Err(anyhow!("Callee {callee_type:?} is not callable")),
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
        todo!("error reporting in branch expression with no else");
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

fn compile_value_flow_reference(
    node: AnyAstNode,
    procedure: &mut Procedure,
    ctx: &mut CompilerContext,
) -> Result<()> {
    // TODO: check for errors/badvalue

    let node_tags = ctx.get_tags(node.breadcrumbs());
    let comes_from = find_tag!(&node_tags, NodeTag::ValueComesFrom)
        .ok_or_else(|| anyhow!("No ValueComesFrom tag on {}", node.breadcrumbs()))?;
    let source_tags = ctx.get_tags(&comes_from.source);

    if let Some(it) = find_tag!(&source_tags, NodeTag::ReferencesFile) {
        compile_file_import_reference(node.info(), it, procedure, ctx)?;
        return Ok(());
    }

    // TODO: top-level declarations

    if let Some(_) = find_tag!(&source_tags, NodeTag::DeclarationForScope) {
        compile_value_reference(node.info(), &comes_from.source, procedure, ctx)?;
        return Ok(());
    }

    dbg!(&ctx.node_tags);

    Err(anyhow!(
        "Wasn't able to resolve identifier at {} to anything",
        node.breadcrumbs()
    ))
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
