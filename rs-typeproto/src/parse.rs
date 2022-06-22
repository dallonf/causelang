use pest::{iterators::Pair, Parser, Span};
use pest_derive::Parser;

use crate::ast::*;

#[derive(Parser)]
#[grammar = "cause.pest"]
struct LangParser;

impl From<pest::Span<'_>> for DocumentRange {
    fn from(span: pest::Span) -> Self {
        DocumentRange {
            start: span.start_pos().into(),
            end: span.end_pos().into(),
        }
    }
}

impl From<pest::Position<'_>> for DocumentPosition {
    fn from(position: pest::Position) -> Self {
        let (line, col) = position.line_col();
        DocumentPosition {
            line: line,
            col: col,
        }
    }
}

type ParserError = String;

struct ParserContext {}

pub fn parse(source: &str) -> Result<AstNode<FileNode>, ParserError> {
    let mut parse_result = LangParser::parse(Rule::file, source).map_err(|err| err.to_string())?;
    let file_pair = parse_result.next().unwrap();
    let span = file_pair.as_span();

    let breadcrumbs = Breadcrumbs(vec![]);
    let ctx = ParserContext {};
    let declarations: Vec<AstNode<DeclarationNode>> = {
        let breadcrumbs = breadcrumbs.append_name("declarations");
        file_pair
            .into_inner()
            .enumerate()
            .map(|(i, pair)| {
                let breadcrumbs = breadcrumbs.append_index(i);
                Ok(emit_ast_node(
                    span.clone(),
                    &breadcrumbs,
                    parse_declaration(pair, &breadcrumbs, &ctx)?,
                ))
            })
            .collect::<Result<Vec<_>, ParserError>>()?
    };

    let file_node = AstNode {
        position: span.into(),
        breadcrumbs,
        node: FileNode { declarations },
    };

    Ok(file_node)
}

fn emit_ast_node<T>(span: Span<'_>, breadcrumbs: &Breadcrumbs, node: T) -> AstNode<T> {
    AstNode {
        position: span.into(),
        breadcrumbs: breadcrumbs.clone(),
        node,
    }
}

fn parse_declaration(
    pair: Pair<Rule>,
    breadcrumbs: &Breadcrumbs,
    ctx: &ParserContext,
) -> Result<DeclarationNode, ParserError> {
    match pair.as_rule() {
        Rule::function_declaration => Ok(DeclarationNode::Function(parse_function_declaration(
            pair,
            breadcrumbs,
            ctx,
        )?)),
        _ => unreachable!(),
    }
}

fn parse_function_declaration(
    pair: Pair<Rule>,
    breadcrumbs: &Breadcrumbs,
    ctx: &ParserContext,
) -> Result<FunctionDeclarationNode, ParserError> {
    todo!()
}
