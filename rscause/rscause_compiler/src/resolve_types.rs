use crate::ast_nodes as ast;
use crate::breadcrumbs::{Breadcrumbs, HasBreadcrumbs};
use crate::lang_types::{InferredType, LangType};
use std::collections::HashMap;

pub struct ResolveTypesContext {
    value_types: HashMap<Breadcrumbs, InferredType<LangType>>,
}

pub fn resolve_types(file: &ast::FileNode) -> ResolveTypesContext {
    let mut ctx = ResolveTypesContext {
        value_types: HashMap::new(),
    };
    for declaration in &file.declarations {
        declaration.resolve_types(&mut ctx);
    }
    ctx
}

trait ResolveTypes {
    fn resolve_types(&self, ctx: &mut ResolveTypesContext);
}

impl ResolveTypes for ast::DeclarationNode {
    fn resolve_types(&self, ctx: &mut ResolveTypesContext) {
        match self {
            ast::DeclarationNode::Import(node) => node.resolve_types(ctx),
            ast::DeclarationNode::Function(node) => node.resolve_types(ctx),
        }
    }
}

impl ResolveTypes for ast::ImportNode {
    fn resolve_types(&self, ctx: &mut ResolveTypesContext) {
        for mapping in &self.mappings {
            mapping.resolve_types(ctx);
        }
    }
}

impl ResolveTypes for ast::ImportMappingNode {
    fn resolve_types(&self, ctx: &mut ResolveTypesContext) {
        todo!()
    }
}

impl ResolveTypes for ast::FunctionNode {
    fn resolve_types(&self, ctx: &mut ResolveTypesContext) {
        todo!()
    }
}
