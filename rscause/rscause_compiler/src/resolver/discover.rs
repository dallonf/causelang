use tap::Pipe;

use super::resolving_lang_types::{
    ResolvingCanonicalLangType, ResolvingLangTypeValue, ResolvingLangTypesContext,
};
use crate::{
    ast::{self, AnyAstNode, BreadcrumbTreeNode, StringLiteralExpressionNode},
    breadcrumbs::{Breadcrumbs, HasBreadcrumbs},
    compiled_file::ExternalFileDescriptor,
    lang_types::{self, CanonicalLangTypeId, PrimitiveLangType},
    resolver::resolving_lang_types::ResolvingLangType,
    tags::NodeTag,
};
use std::{cell::RefCell, collections::HashMap, rc::Rc, sync::Arc};

pub fn discover_types(
    path: Arc<String>,
    file: Arc<ast::FileNode>,
    node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
    canonical_types: &HashMap<Arc<CanonicalLangTypeId>, Arc<lang_types::CanonicalLangType>>,
    external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>>,
) -> anyhow::Result<()> {
    let resolving_types_ctx = ResolvingLangTypesContext::new()
        .pipe(RefCell::new)
        .pipe(Rc::new);

    let mut ctx = DiscoverTypesContext {
        path,
        file: file.clone(),
        node_tags,
        // TODO
        canonical_types: Default::default(),
        external_files,
        resolving_types_ctx,
    };

    let descendants = BreadcrumbTreeNode::from(&file.clone()).descendants();
    for descendant in &descendants {
        let node_type = discover_type_for_any_ast_node(descendant, &mut ctx)?;
        if let Some(node_type) = node_type {
            let mut resolving_types_ctx = ctx.resolving_types_ctx.try_borrow_mut()?;
            resolving_types_ctx.track_type(
                Some(
                    super::resolving_lang_types::ResolvingLangTypeSource::Breadcrumb(
                        descendant.breadcrumbs().to_owned(),
                    ),
                ),
                node_type,
            );
        }
    }
    Ok(())
}

struct DiscoverTypesContext {
    path: Arc<String>,
    file: Arc<ast::FileNode>,
    node_tags: Arc<HashMap<Breadcrumbs, Vec<NodeTag>>>,
    canonical_types: HashMap<Arc<CanonicalLangTypeId>, Arc<ResolvingCanonicalLangType>>,
    external_files: Arc<HashMap<Arc<String>, ExternalFileDescriptor>>,

    resolving_types_ctx: Rc<RefCell<ResolvingLangTypesContext>>,
}

fn discover_type_for_any_ast_node(
    node: &AnyAstNode,
    ctx: &mut DiscoverTypesContext,
) -> anyhow::Result<Option<ResolvingLangTypeValue>> {
    match node {
        AnyAstNode::Identifier(identifier_node) => todo!(),
        AnyAstNode::IdentifierTypeReference(identifier_type_reference_node) => todo!(),
        AnyAstNode::FunctionTypeReference(function_type_reference_node) => todo!(),
        AnyAstNode::Pattern(pattern_node) => todo!(),
        AnyAstNode::FunctionSignatureParameter(function_signature_parameter_node) => todo!(),
        AnyAstNode::FunctionCallParameter(function_call_parameter_node) => todo!(),
        AnyAstNode::File(file_node) => todo!(),
        AnyAstNode::Import(import_node) => todo!(),
        AnyAstNode::ImportPath(import_path_node) => todo!(),
        AnyAstNode::ImportMapping(import_mapping_node) => todo!(),
        AnyAstNode::Function(function_node) => todo!(),
        AnyAstNode::NamedValue(named_value_node) => todo!(),
        AnyAstNode::ObjectType(object_type_node) => todo!(),
        AnyAstNode::SignalType(signal_type_node) => todo!(),
        AnyAstNode::ObjectField(object_field_node) => todo!(),
        AnyAstNode::OneOfType(one_of_type_node) => todo!(),
        AnyAstNode::BlockBody(block_body_node) => todo!(),
        AnyAstNode::SingleExpressionBody(single_expression_body_node) => todo!(),
        AnyAstNode::ExpressionStatement(expression_statement_node) => todo!(),
        AnyAstNode::DeclarationStatement(declaration_statement_node) => todo!(),
        AnyAstNode::EffectStatement(effect_statement_node) => todo!(),
        AnyAstNode::GroupExpression(group_expression_node) => todo!(),
        AnyAstNode::BlockExpression(block_expression_node) => todo!(),
        AnyAstNode::FunctionExpression(function_expression_node) => todo!(),
        AnyAstNode::BranchExpression(branch_expression_node) => todo!(),
        AnyAstNode::IfBranchOption(if_branch_option_node) => todo!(),
        AnyAstNode::IsBranchOption(is_branch_option_node) => todo!(),
        AnyAstNode::ElseBranchOption(else_branch_option_node) => todo!(),
        AnyAstNode::LoopExpression(loop_expression_node) => todo!(),
        AnyAstNode::SetExpression(set_expression_node) => todo!(),
        AnyAstNode::CauseExpression(cause_expression_node) => todo!(),
        AnyAstNode::CallExpression(call_expression_node) => todo!(),
        AnyAstNode::PipeCallExpression(pipe_call_expression_node) => todo!(),
        AnyAstNode::MemberExpression(member_expression_node) => todo!(),
        AnyAstNode::IdentifierExpression(identifier_expression_node) => todo!(),
        AnyAstNode::StringLiteralExpression(node) => {
            Some(discover_type_for_string_literal_expression(node, ctx)?)
        }
        AnyAstNode::NumberLiteralExpression(number_literal_expression_node) => todo!(),
        AnyAstNode::ReturnExpression(return_expression_node) => todo!(),
        AnyAstNode::BreakExpression(break_expression_node) => todo!(),
    }
    .pipe(Ok)
}

fn discover_type_for_string_literal_expression(
    node: &StringLiteralExpressionNode,
    ctx: &mut DiscoverTypesContext,
) -> anyhow::Result<ResolvingLangTypeValue> {
    Ok(ResolvingLangTypeValue::Known(Ok(
        ResolvingLangType::Primitive(PrimitiveLangType::Text),
    )))
}
