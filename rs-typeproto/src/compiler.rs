use std::collections::HashMap;

use crate::analyzer::{AnalyzedNode, NodeTag};
use crate::ast::*;
use crate::compiled_file::{CompiledConstant, CompiledExport, CompiledFile, InstructionChunk};
use crate::instructions::Instruction;
use crate::resolver::{ResolutionType, ResolvedFile};
use crate::types::{CanonicalLangType, ResolvedValueLangType, ValueLangType};
use crate::vm::RuntimeBadValue;

pub struct CompilerInput<'a> {
    pub file_node: &'a AstNode<FileNode>,
    pub analyzed: &'a AnalyzedNode,
    pub resolved: &'a ResolvedFile,
}

pub struct CompilerContext<'a> {
    compiler_input: &'a CompilerInput<'a>,
}

impl<'a> CompilerContext<'a> {
    fn get_tags(&self, breadcrumbs: &Breadcrumbs) -> &[NodeTag] {
        &self
            .compiler_input
            .analyzed
            .node_tags
            .get(breadcrumbs)
            .expect("Missing tags")
    }
}

pub fn compile(input: CompilerInput) -> CompiledFile {
    let mut file = CompiledFile {
        path: input.resolved.path.to_owned(),
        types: HashMap::new(),
        chunks: vec![],
        exports: HashMap::new(),
    };

    for declaration in &input.file_node.node.declarations {
        match &declaration.node {
            DeclarationNode::Import(_) => {}
            DeclarationNode::Function(function_node) => {
                let ctx = CompilerContext {
                    compiler_input: &input,
                };
                let chunk =
                    compile_function(&declaration.with_node(function_node.to_owned()), &ctx);

                file.chunks.push(chunk);
                file.exports.insert(
                    function_node.name.node.0.to_owned(),
                    CompiledExport::Chunk(file.chunks.len() - 1),
                );
            }
            DeclarationNode::NamedValue(_) => todo!(),
        }
    }

    file
}

fn compile_function(
    node: &AstNode<FunctionDeclarationNode>,
    ctx: &CompilerContext,
) -> InstructionChunk {
    let mut chunk = InstructionChunk::default();

    match &node.node.body.node {
        BodyNode::BlockBody(block_body) => {
            for (i, statement) in block_body.statements.iter().enumerate() {
                compile_statement(
                    statement,
                    &mut chunk,
                    ctx,
                    i == block_body.statements.len() - 1,
                );
            }
            // TODO: make sure this is the right type to return
            chunk.write_instruction(Instruction::Return);
        }
    }

    chunk
}

fn compile_statement(
    statement: &AstNode<StatementNode>,
    chunk: &mut InstructionChunk,
    ctx: &CompilerContext,
    is_last_statement: bool,
) {
    match &statement.node {
        StatementNode::ExpressionStatement(expression_statement) => {
            compile_expression(&expression_statement.expression, chunk, ctx);

            // TODO: emit compile error for mismatched type

            if !is_last_statement {
                chunk.write_instruction(Instruction::Pop);
            }
        }
        StatementNode::DeclarationStatement(_) => todo!(),
    }
}

fn compile_expression(
    expression: &AstNode<ExpressionNode>,
    chunk: &mut InstructionChunk,
    ctx: &CompilerContext,
) {
    match &expression.node {
        ExpressionNode::IdentifierExpression(identifier) => {
            compile_identifier_expression(&expression.with_node(identifier.to_owned()), chunk, ctx);
        }
        ExpressionNode::CauseExpression(cause_expression) => {
            compile_cause_expression(
                &expression.with_node(cause_expression.to_owned()),
                chunk,
                ctx,
            );
        }
        ExpressionNode::CallExpression(call_expression) => {
            compile_call_expression(
                &expression.with_node(call_expression.to_owned()),
                chunk,
                ctx,
            );
        }
        ExpressionNode::StringLiteralExpression(string_literal) => {
            let constant =
                chunk.add_constant(CompiledConstant::String(string_literal.text.to_owned()));
            chunk.write_instruction(Instruction::Literal(constant));
        }
        ExpressionNode::IntegerLiteralExpression(_) => todo!(),
    }

    let expression_type = ctx
        .compiler_input
        .resolved
        .resolved_types
        .get(&(ResolutionType::Inferred, expression.breadcrumbs.to_owned()))
        .expect("unknown type of expression");

    if let Some(_) = expression_type.get_error() {
        chunk.write_instruction(Instruction::Pop);
        chunk.write_literal(CompiledConstant::Error(RuntimeBadValue {
            file_path: ctx.compiler_input.resolved.path.to_owned(),
            breadcrumbs: expression.breadcrumbs.to_owned(),
        }));
    }
}

fn compile_identifier_expression(
    identifier: &AstNode<IdentifierExpressionNode>,
    chunk: &mut InstructionChunk,
    ctx: &CompilerContext,
) {
    let tags = ctx.get_tags(&identifier.breadcrumbs);

    let comes_from = tags
        .iter()
        .find_map(|it| {
            if let NodeTag::ValueComesFrom(breadcrumbs) = it {
                Some(breadcrumbs)
            } else {
                None
            }
        })
        .expect("Identifier should have ValueComesFrom tag");

    let comes_from_tags = ctx.get_tags(comes_from);

    for tag in comes_from_tags {
        match tag {
            NodeTag::ReferencesFile { path, export_name } => {
                let file_path_constant =
                    chunk.add_constant(CompiledConstant::String(path.to_owned()));
                if let Some(export_name) = export_name {
                    let export_name_constant =
                        chunk.add_constant(CompiledConstant::String(export_name.to_owned()));
                    chunk.write_instruction(Instruction::Import {
                        file_path_constant,
                        export_name_constant,
                    });
                } else {
                    todo!("Haven't implemented files as first-class objects yet");
                }
                return;
            }
            _ => {}
        }
    }

    // change this to "unimplemented" when we're more stable
    todo!("Wasn't able to resolve identifier to anything");
}

fn compile_cause_expression(
    cause_expression: &AstNode<CauseExpressionNode>,
    chunk: &mut InstructionChunk,
    ctx: &CompilerContext,
) {
    compile_expression(&cause_expression.node.argument, chunk, ctx);

    if let Some(_error) = ctx
        .compiler_input
        .resolved
        .resolved_types
        .get(&(
            ResolutionType::Inferred,
            cause_expression.breadcrumbs.to_owned(),
        ))
        .expect("missing type for expression")
        .get_error()
    {
        chunk.write_instruction(Instruction::Pop);
        chunk.write_literal(CompiledConstant::Error(RuntimeBadValue {
            file_path: ctx.compiler_input.resolved.path.to_owned(),
            breadcrumbs: cause_expression.breadcrumbs.to_owned(),
        }));
        let file_path_constant =
            chunk.add_constant(CompiledConstant::String("core/builtin".to_owned()));
        let export_name_constant =
            chunk.add_constant(CompiledConstant::String("TypeError".to_owned()));
        chunk.write_instruction(Instruction::Import {
            file_path_constant,
            export_name_constant,
        });
        chunk.write_instruction(Instruction::Construct);
        chunk.write_instruction(Instruction::Cause);
    } else {
        chunk.write_instruction(Instruction::Cause);
    }
}

fn compile_call_expression(
    call_expression: &AstNode<CallExpressionNode>,
    chunk: &mut InstructionChunk,
    ctx: &CompilerContext,
) {
    // TODO: assuming all arguments are positional, and arity is correct
    for argument in &call_expression.node.arguments {
        compile_expression(&argument.node.value, chunk, ctx);
    }

    compile_expression(&call_expression.node.callee, chunk, ctx);

    let result_type = ctx
        .compiler_input
        .resolved
        .resolved_types
        .get(&(
            ResolutionType::Inferred,
            call_expression.breadcrumbs.to_owned(),
        ))
        .expect("result type unknown");

    if let Some(_) = result_type.get_error() {
        // Don't call; pop all the arguments and the callee off the stack and then push an error
        for _ in 0..(call_expression.node.arguments.len() + 1) {
            chunk.write_instruction(Instruction::Pop);
        }

        chunk.write_literal(CompiledConstant::Error(RuntimeBadValue {
            file_path: ctx.compiler_input.resolved.path.to_owned(),
            breadcrumbs: call_expression.breadcrumbs.to_owned(),
        }));
    } else {
        let callee_type = ctx
            .compiler_input
            .resolved
            .resolved_types
            .get(&(
                ResolutionType::Inferred,
                call_expression.node.callee.breadcrumbs.to_owned(),
            ))
            .expect("callee type unknown");

        match callee_type {
            ValueLangType::Resolved(ResolvedValueLangType::TypeReference(type_id)) => {
                let canonical = ctx
                    .compiler_input
                    .resolved
                    .canonical_types
                    .get(type_id)
                    .expect(format!("Missing type: {type_id}").as_str());

                match canonical {
                    CanonicalLangType::Signal(_) => {
                        chunk.write_instruction(Instruction::Construct);
                    }
                }
            }
            ValueLangType::Resolved(_) => todo!(),
            ValueLangType::Pending => unimplemented!(),
            ValueLangType::Error(_) => unimplemented!(),
        }
    }
}
