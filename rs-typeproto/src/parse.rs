use pest::Parser;
use pest_derive::Parser;

use crate::ast::*;

#[derive(Parser)]
#[grammar = "cause.pest"]
struct LangParser;

pub fn parse(source: &str) -> Result<AstNode<FileNode>, String> {
    let mut parse_result = LangParser::parse(Rule::file, source).map_err(|err| err.to_string())?;
    let file_pair = parse_result.next().unwrap();

    let breadcrumbs = Breadcrumbs(vec![]);
    let declarations: Vec<AstNode<DeclarationNode>> = { todo!() };

    let file_node = AstNode {
        position: file_pair.as_span().into(),
        breadcrumbs,
        node: FileNode { declarations },
    };

    Ok(file_node)
}

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
