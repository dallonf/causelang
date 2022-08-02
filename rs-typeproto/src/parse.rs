use std::borrow::Cow;

use pest::{iterators::Pair, Parser};
use pest_derive::Parser;

use crate::{ast::*, core_descriptors::core_builtin_file};

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

    let breadcrumbs = Breadcrumbs::empty();
    let ctx = ParserContext {};
    let declarations: Vec<AstNode<DeclarationNode>> = {
        let breadcrumbs = breadcrumbs.append_name("declarations");
        let mut declarations =
            vec![
                generate_core_globals_import(breadcrumbs.append_index(0).to_owned())
                    .map(|it| DeclarationNode::Import(it)),
            ];
        let mut parsed_declarations = file_pair
            .into_inner()
            .enumerate()
            .filter_map(|(i, pair)| {
                if let Rule::EOI = pair.as_rule() {
                    None
                } else {
                    Some(parse_declaration(
                        pair,
                        breadcrumbs.append_index(i + 1),
                        &ctx,
                    ))
                }
            })
            .collect::<ParserResult<Vec<_>>>()?;
        declarations.append(&mut parsed_declarations);
        declarations
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

fn generate_core_globals_import(breadcrumbs: Breadcrumbs) -> AstNode<ImportDeclarationNode> {
    let core_global_file = core_builtin_file();
    let core_global_ids: Vec<_> = core_global_file.1.exports.iter().map(|it| it.0).collect();
    AstNode::new(
        ImportDeclarationNode {
            path: AstNode::new(
                ImportPathNode(core_global_file.0.to_owned()),
                DocumentRange::default(),
                breadcrumbs.append_name("path"),
            ),
            mappings: core_global_ids
                .into_iter()
                .enumerate()
                .map(|(index, id)| {
                    AstNode::new(
                        ImportMappingNode {
                            source_name: AstNode::new(
                                Identifier(id.to_owned()),
                                DocumentRange::default(),
                                breadcrumbs
                                    .append_name("mappings")
                                    .append_index(index)
                                    .append_name("source_name"),
                            ),
                            rename: None,
                        },
                        DocumentRange::default(),
                        breadcrumbs.append_name("mappings").append_index(index),
                    )
                })
                .collect(),
        },
        DocumentRange::default(),
        breadcrumbs,
    )
}

fn parse_type_reference(
    pair: Pair<Rule>,
    breadcrumbs: Breadcrumbs,
    ctx: &ParserContext,
) -> ParserResult<AstNode<TypeReferenceNode>> {
    let node = pair.into_inner().next().unwrap();

    match node.as_rule() {
        Rule::identifier => {
            Ok(parse_identifier(node, breadcrumbs, ctx)?
                .map(|it| TypeReferenceNode::Identifier(it)))
        }
        _ => unreachable!(),
    }
}

fn parse_declaration(
    pair: Pair<Rule>,
    breadcrumbs: Breadcrumbs,
    ctx: &ParserContext,
) -> ParserResult<AstNode<DeclarationNode>> {
    match pair.as_rule() {
        Rule::import_declaration => {
            Ok(parse_import_declaration(pair, breadcrumbs, ctx)?
                .map(|it| DeclarationNode::Import(it)))
        }
        Rule::function_declaration => Ok(parse_function_declaration(pair, breadcrumbs, ctx)?
            .map(|it| DeclarationNode::Function(it))),
        Rule::named_value_declaration => Ok(parse_named_value_declaration(pair, breadcrumbs, ctx)?
            .map(|it| DeclarationNode::NamedValue(it))),
        other => unreachable!("unexpected rule: {:?}", other),
    }
}

fn parse_import_declaration(
    pair: Pair<Rule>,
    breadcrumbs: Breadcrumbs,
    ctx: &ParserContext,
) -> ParserResult<AstNode<ImportDeclarationNode>> {
    let span = pair.as_span();
    let mut inner = pair.into_inner();

    let path = inner.next().unwrap();

    let mappings = inner
        .enumerate()
        .map(|(i, mapping_pair)| {
            let span = mapping_pair.as_span();
            let mut inner = mapping_pair.into_inner();

            let source_pair = inner.next().unwrap();
            let rename_pair = inner.next();

            let mapping_breadcrumbs = breadcrumbs.append_name("mappings").append_index(i);
            Ok(AstNode::new(
                ImportMappingNode {
                    source_name: parse_identifier(
                        source_pair,
                        mapping_breadcrumbs.append_name("source_name"),
                        ctx,
                    )?,
                    rename: {
                        let optionally_parsed = rename_pair.map(|rename_pair| {
                            parse_identifier(
                                rename_pair,
                                mapping_breadcrumbs.append_name("rename"),
                                ctx,
                            )
                        });
                        match optionally_parsed {
                            Some(Err(err)) => Err(err),
                            Some(Ok(it)) => Ok(Some(it)),
                            None => Ok(None),
                        }
                    }?,
                },
                span,
                mapping_breadcrumbs,
            ))
        })
        .collect::<ParserResult<Vec<_>>>()?;

    Ok(AstNode::new(
        ImportDeclarationNode {
            path: AstNode::new(
                ImportPathNode(path.as_str().to_string()),
                path.as_span(),
                breadcrumbs.append_name("path"),
            ),
            mappings,
        },
        span,
        breadcrumbs,
    ))
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

fn parse_named_value_declaration(
    pair: Pair<Rule>,
    breadcrumbs: Breadcrumbs,
    ctx: &ParserContext,
) -> ParserResult<AstNode<NamedValueDeclarationNode>> {
    let span = pair.as_span();
    let mut inner = pair.into_inner();

    let name_pair = inner.next().unwrap();
    let name = parse_identifier(name_pair, breadcrumbs.append_name("name"), ctx)?;

    let type_annotation_maybe_pair = inner.next().unwrap();
    let mut value_pair = Cow::Borrowed(&type_annotation_maybe_pair);
    let type_annotation = if let Rule::type_reference = type_annotation_maybe_pair.as_rule() {
        value_pair = Cow::Owned(inner.next().unwrap());
        Some(parse_type_reference(
            type_annotation_maybe_pair.to_owned(),
            breadcrumbs.append_name("type_annotation"),
            ctx,
        )?)
    } else {
        None
    };

    let value = parse_expression(
        value_pair.into_owned(),
        breadcrumbs.append_name("value"),
        ctx,
    )?;

    let named_value = NamedValueDeclarationNode {
        name,
        type_annotation,
        value,
    };

    let ast_node = AstNode::new(named_value, span, breadcrumbs);

    Ok(ast_node)
}

fn parse_body(
    pair: Pair<Rule>,
    breadcrumbs: Breadcrumbs,
    ctx: &ParserContext,
) -> ParserResult<AstNode<BodyNode>> {
    match pair.as_rule() {
        Rule::block_body => {
            Ok(parse_block_body(pair, breadcrumbs, ctx)?.map(|it| BodyNode::BlockBody(it)))
        }
        _ => unreachable!(),
    }
}

fn parse_block_body(
    pair: Pair<Rule>,
    breadcrumbs: Breadcrumbs,
    ctx: &ParserContext,
) -> ParserResult<AstNode<BlockBodyNode>> {
    let statement_breadcrumbs = breadcrumbs.append_name("statements");
    let span = pair.as_span();
    let statements = pair
        .into_inner()
        .enumerate()
        .map(|(i, statement_pair)| {
            parse_statement(statement_pair, statement_breadcrumbs.append_index(i), ctx)
        })
        .collect::<ParserResult<Vec<_>>>()?;

    Ok(AstNode::new(
        BlockBodyNode { statements },
        span,
        breadcrumbs,
    ))
}

fn parse_statement(
    pair: Pair<Rule>,
    breadcrumbs: Breadcrumbs,
    ctx: &ParserContext,
) -> ParserResult<AstNode<StatementNode>> {
    match pair.as_rule() {
        Rule::expression_statement => Ok(parse_expression_statement(pair, breadcrumbs, ctx)?
            .map(|it| StatementNode::ExpressionStatement(it))),
        Rule::declaration_statement => Ok(parse_declaration_statement(pair, breadcrumbs, ctx)?
            .map(|it| StatementNode::DeclarationStatement(it))),
        _ => unreachable!(),
    }
}

fn parse_declaration_statement(
    pair: Pair<Rule>,
    breadcrumbs: Breadcrumbs,
    ctx: &ParserContext,
) -> ParserResult<AstNode<DeclarationStatementNode>> {
    let span = pair.as_span();
    let declaration = parse_declaration(
        pair.into_inner().next().unwrap(),
        breadcrumbs.append_name("declaration"),
        ctx,
    )?;
    Ok(AstNode::new(
        DeclarationStatementNode { declaration },
        span,
        breadcrumbs,
    ))
}

fn parse_expression_statement(
    pair: Pair<Rule>,
    breadcrumbs: Breadcrumbs,
    ctx: &ParserContext,
) -> ParserResult<AstNode<ExpressionStatementNode>> {
    let span = pair.as_span();
    let expression = parse_expression(
        pair.into_inner().next().unwrap(),
        breadcrumbs.append_name("expression"),
        ctx,
    )?;
    Ok(AstNode::new(
        ExpressionStatementNode { expression },
        span,
        breadcrumbs,
    ))
}

fn parse_expression(
    pair: Pair<Rule>,
    breadcrumbs: Breadcrumbs,
    ctx: &ParserContext,
) -> ParserResult<AstNode<ExpressionNode>> {
    let mut inner = pair.into_inner();

    let main_expression_pair = inner.next().unwrap().clone();
    let main_expression = {
        |breadcrumbs: Breadcrumbs| -> ParserResult<_> {
            match main_expression_pair.as_rule() {
                Rule::string_literal_expression => {
                    Ok(
                        parse_string_literal_expression(main_expression_pair, breadcrumbs, ctx)?
                            .map(|it| ExpressionNode::StringLiteralExpression(it)),
                    )
                }
                Rule::integer_literal_expression => {
                    Ok(
                        parse_integer_literal_expression(main_expression_pair, breadcrumbs, ctx)?
                            .map(|it| ExpressionNode::IntegerLiteralExpression(it)),
                    )
                }
                Rule::cause_expression => {
                    Ok(
                        parse_cause_expression(main_expression_pair, breadcrumbs, ctx)?
                            .map(|it| ExpressionNode::CauseExpression(it)),
                    )
                }
                Rule::identifier_expression => {
                    Ok(
                        parse_identifier_expression(main_expression_pair, breadcrumbs, ctx)?
                            .map(|it| ExpressionNode::IdentifierExpression(it)),
                    )
                }
                err => unreachable!("{:?}", err),
            }
        }
    };

    let suffix_pair = inner.next();
    match suffix_pair.map(|it| (it.as_rule(), it)) {
        Some((Rule::call_expression_suffix, suffix_pair)) => Ok(parse_call_expression_suffix(
            suffix_pair,
            breadcrumbs,
            Box::new(main_expression),
            ctx,
        )?
        .map(|it| ExpressionNode::CallExpression(it))),
        None => main_expression(breadcrumbs),
        _ => unreachable!(),
    }
}

fn parse_string_literal_expression(
    pair: Pair<Rule>,
    breadcrumbs: Breadcrumbs,
    _ctx: &ParserContext,
) -> ParserResult<AstNode<StringLiteralExpressionNode>> {
    let span = pair.as_span();
    let inner = pair.into_inner().next().unwrap();

    Ok(AstNode::new(
        StringLiteralExpressionNode {
            text_range: inner.as_span().into(),
            text: inner.as_str().to_string(),
        },
        span,
        breadcrumbs,
    ))
}

fn parse_integer_literal_expression(
    pair: Pair<Rule>,
    breadcrumbs: Breadcrumbs,
    _ctx: &ParserContext,
) -> ParserResult<AstNode<IntegerLiteralExpressionNode>> {
    let text = pair.as_str();

    let number: Result<i64, _> = text.replace("_", "").parse();

    Ok(AstNode::new(
        IntegerLiteralExpressionNode {
            value: number.unwrap(),
        },
        pair.as_span(),
        breadcrumbs,
    ))
}

fn parse_cause_expression(
    pair: Pair<Rule>,
    breadcrumbs: Breadcrumbs,
    ctx: &ParserContext,
) -> ParserResult<AstNode<CauseExpressionNode>> {
    let span = pair.as_span();
    let argument_pair = pair.into_inner().next().unwrap();
    let argument = parse_expression(argument_pair, breadcrumbs.append_name("argument"), ctx)?;
    Ok(AstNode::new(
        CauseExpressionNode {
            argument: Box::new(argument),
        },
        span,
        breadcrumbs,
    ))
}

fn parse_identifier_expression(
    pair: Pair<Rule>,
    breadcrumbs: Breadcrumbs,
    ctx: &ParserContext,
) -> ParserResult<AstNode<IdentifierExpressionNode>> {
    let span = pair.as_span();
    let identifier_pair = pair.into_inner().next().unwrap();
    let identifier = parse_identifier(identifier_pair, breadcrumbs.append_name("identifier"), ctx)?;
    Ok(AstNode::new(
        IdentifierExpressionNode { identifier },
        span,
        breadcrumbs,
    ))
}

fn parse_call_expression_suffix(
    pair: Pair<Rule>,
    breadcrumbs: Breadcrumbs,
    main_expression: impl FnOnce(Breadcrumbs) -> ParserResult<AstNode<ExpressionNode>>,
    ctx: &ParserContext,
) -> ParserResult<AstNode<CallExpressionNode>> {
    let span = pair.as_span();

    let callee = main_expression(breadcrumbs.append_name("callee"))?;

    let arguments_breadcrumbs = breadcrumbs.append_name("arguments");
    let arguments = pair
        .into_inner()
        .enumerate()
        .map(|(i, argument_pair)| match argument_pair.as_rule() {
            Rule::positional_argument => {
                parse_positional_argument(argument_pair, arguments_breadcrumbs.append_index(i), ctx)
            }
            _ => unreachable!(),
        })
        .collect::<ParserResult<Vec<_>>>()?;

    Ok(AstNode::new(
        CallExpressionNode {
            callee: Box::new(callee),
            arguments,
        },
        span,
        breadcrumbs,
    ))
}

fn parse_positional_argument(
    argument_pair: Pair<Rule>,
    breadcrumbs: Breadcrumbs,
    ctx: &ParserContext,
) -> ParserResult<AstNode<CallExpressionArgumentNode>> {
    let span = argument_pair.as_span();
    let value = parse_expression(
        argument_pair.into_inner().next().unwrap(),
        breadcrumbs.append_name("value"),
        ctx,
    )?;

    Ok(AstNode::new(
        CallExpressionArgumentNode { value: value },
        span,
        breadcrumbs,
    ))
}
