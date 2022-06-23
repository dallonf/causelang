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
type ParserResult<T> = Result<T, ParserError>;

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
            .filter_map(|(i, pair)| {
                if let Rule::EOI = pair.as_rule() {
                    None
                } else {
                    Some(parse_declaration(pair, breadcrumbs.append_index(i), &ctx))
                }
            })
            .collect::<ParserResult<Vec<_>>>()?
    };

    let file_node = AstNode {
        position: span.into(),
        breadcrumbs,
        node: FileNode { declarations },
    };

    Ok(file_node)
}

fn parse_identifier(
    pair: Pair<Rule>,
    breadcrumbs: Breadcrumbs,
    _ctx: &ParserContext,
) -> ParserResult<AstNode<Identifier>> {
    Ok(AstNode::new(
        Identifier(pair.as_str().to_string()),
        pair.as_span(),
        breadcrumbs,
    ))
}

fn parse_declaration(
    pair: Pair<Rule>,
    breadcrumbs: Breadcrumbs,
    ctx: &ParserContext,
) -> ParserResult<AstNode<DeclarationNode>> {
    match pair.as_rule() {
        Rule::function_declaration => Ok(parse_function_declaration(pair, breadcrumbs, ctx)?
            .map(|it| DeclarationNode::Function(it))),
        other => unreachable!("unexpected rule: {:?}", other),
    }
}

fn parse_function_declaration(
    pair: Pair<Rule>,
    breadcrumbs: Breadcrumbs,
    ctx: &ParserContext,
) -> ParserResult<AstNode<FunctionDeclarationNode>> {
    let span = pair.as_span();
    let mut inner = pair.into_inner();

    let name_pair = inner.next().unwrap();
    let name = parse_identifier(name_pair, breadcrumbs.append_name("name"), ctx)?;

    let body_pair = inner.next().unwrap();
    let body = parse_body(body_pair, breadcrumbs.append_name("body"), ctx)?;

    Ok(AstNode::new(
        FunctionDeclarationNode { name, body },
        span,
        breadcrumbs,
    ))
}

fn parse_body(
    body_pair: Pair<Rule>,
    breadcrumbs: Breadcrumbs,
    ctx: &ParserContext,
) -> ParserResult<AstNode<BodyNode>> {
    // TODO, just a placeholder
    Ok(AstNode::new(
        BodyNode::BlockBody(BlockBodyNode { statements: vec![] }),
        body_pair.as_span(),
        breadcrumbs,
    ))
}
