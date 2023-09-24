use std::collections::HashMap;
use std::sync::Arc;

use crate::{
    ast_nodes as ast,
    compiled_file::{CompiledExport, CompiledFile, Procedure},
    lang_types::{FunctionLangType, LangType},
};
use anyhow::Result;
use thiserror::Error;

#[derive(Error, Debug)]
#[error("support for {0} is not implemented")]
pub struct TodoCompilerError(String);

trait CompileToProcedure {
    fn compile_procedure(&self, ctx: &mut CompilerContext) -> Result<Procedure>;
}

#[derive(Default)]
struct CompilerContext {
    procedures: Vec<Procedure>,
}

pub fn compile(ast: &ast::FileNode) -> Result<CompiledFile> {
    let mut ctx = CompilerContext::default();
    let mut exports: HashMap<Arc<String>, CompiledExport> = HashMap::new();

    for declaration in &ast.declarations {
        match declaration {
            ast::DeclarationNode::Import(_) => {}
            ast::DeclarationNode::Function(function) => {
                let procedure = function.compile_procedure(&mut ctx)?;
                let return_type = todo!();
                let function_type = LangType::Function(FunctionLangType {
                    name: function.name.text.clone(),
                    return_type: return_type,
                });

                ctx.procedures.push(procedure);
                exports.insert(
                    function.name.text.clone(),
                    CompiledExport::Function {
                        procedure_index: ctx.procedures.len() as u32 - 1,
                        function_type: function_type,
                    },
                );
            }
        }
    }
    todo!()
}

impl CompileToProcedure for ast::FunctionNode {
    fn compile_procedure(&self, ctx: &mut CompilerContext) -> Result<Procedure> {
        todo!()
    }
}
