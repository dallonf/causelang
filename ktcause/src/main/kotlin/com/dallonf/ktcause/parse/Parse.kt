package com.dallonf.ktcause.parse

import com.dallonf.ktcause.CoreDescriptors
import com.dallonf.ktcause.antlr.*
import com.dallonf.ktcause.antlr.CauseParser.*
import com.dallonf.ktcause.ast.*
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

private class ParserContext()

fun parse(source: String): FileNode {
    val tokens = CommonTokenStream(CauseLexer(CharStreams.fromString(source)))
    val tree = CauseParser(tokens).file()

    if (tree.exception != null) {
        throw tree.exception
    }

    val declarationsBreadcrumbs = Breadcrumbs.empty().appendName("declarations")
    val declarations = listOf(generateCoreBuiltinsImport(declarationsBreadcrumbs.appendIndex(0))) +
            tree.declaration()
                .mapIndexed { i, declaration ->
                    parseDeclaration(declaration, declarationsBreadcrumbs.appendIndex(i + 1), ParserContext())
                }

    return FileNode(NodeInfo(tree.getRange(), Breadcrumbs.empty()), declarations)
}

fun generateCoreBuiltinsImport(breadcrumbs: Breadcrumbs): DeclarationNode.Import {
    val (coreBuiltinFilepath, coreBuiltinFile) = CoreDescriptors.coreBuiltinFile
    val coreBuiltinNames = coreBuiltinFile.exports.map { (key, _) -> key }
    val position = DocumentRange(DocumentPosition(0, 0), DocumentPosition(0, 0))
    return DeclarationNode.Import(NodeInfo(position, breadcrumbs),
        path = DeclarationNode.Import.PathNode(NodeInfo(position, breadcrumbs.appendName("path")), coreBuiltinFilepath),
        mappings = coreBuiltinNames.mapIndexed { i, name ->
            val mappingBreadcrumbs = breadcrumbs.appendName("mappings").appendIndex(i)
            DeclarationNode.Import.MappingNode(
                NodeInfo(position, mappingBreadcrumbs),
                sourceName = Identifier(NodeInfo(position, mappingBreadcrumbs.appendName("sourceName")), name),
                rename = null
            )
        })
}

private fun parseIdentifier(token: Token, breadcrumbs: Breadcrumbs, ctx: ParserContext): Identifier {
    assert(token.type == IDENTIFIER)
    return Identifier(
        NodeInfo(token.getRange(), breadcrumbs), token.text
    )
}

private fun parseTypeReference(
    typeAnnotationRule: TypeReferenceContext, breadcrumbs: Breadcrumbs, ctx: ParserContext
): TypeReferenceNode {
    val identifier = parseIdentifier(typeAnnotationRule.IDENTIFIER().symbol, breadcrumbs, ctx)
    return TypeReferenceNode.IdentifierTypeReferenceNode(
        NodeInfo(typeAnnotationRule.getRange(), breadcrumbs), identifier
    )
}

private fun parseDeclaration(
    declaration: DeclarationContext, breadcrumbs: Breadcrumbs, ctx: ParserContext
): DeclarationNode {
    return when (val child = declaration.getChild(0)) {
        is ImportDeclarationContext -> parseImportDeclaration(child, breadcrumbs, ctx)
        is FunctionDeclarationContext -> parseFunctionDeclaration(child, breadcrumbs, ctx)
        is NamedValueDeclarationContext -> parseNamedValueDeclaration(child, breadcrumbs, ctx)
        else -> throw Error("unexpected declaration type: ${child.toString()}")
    }
}

private fun parseFunctionDeclaration(
    functionDeclaration: FunctionDeclarationContext,
    breadcrumbs: Breadcrumbs,
    ctx: ParserContext,
): DeclarationNode.Function {

    val name = parseIdentifier(functionDeclaration.IDENTIFIER().symbol, breadcrumbs.appendName("name"), ctx)
    val body = parseBody(functionDeclaration.body(), breadcrumbs.appendName("body"), ctx)

    return DeclarationNode.Function(
        NodeInfo(functionDeclaration.getRange(), breadcrumbs), name, body
    )
}

private fun parseImportDeclaration(
    importDeclaration: ImportDeclarationContext, breadcrumbs: Breadcrumbs, ctx: ParserContext
): DeclarationNode.Import {
    val pathToken = importDeclaration.PATH()
    val path = DeclarationNode.Import.PathNode(
        NodeInfo(pathToken.symbol.getRange(), breadcrumbs.appendName("path")), pathToken.text
    )

    val mappingRules = importDeclaration.importMappings().importMapping()
    val mappings = mappingRules.mapIndexed { i, mappingRule ->
        val iterator = mappingRule.children.ruleIterator()
        iterator.skip { (it is TerminalNode && (it.symbol.type == IMPORT || it.symbol.type == NEWLINE)) }

        val name = (iterator.next() as TerminalNode).symbol
        assert(name.type == IDENTIFIER)
        iterator.skipNewlines()
        val rename = (iterator.tryNext() as? TerminalNode)?.symbol
        if (rename != null) {
            assert(rename.type == IDENTIFIER)
        }

        val mappingBreadcrumbs = breadcrumbs.appendName("mappings").appendIndex(i)
        DeclarationNode.Import.MappingNode(
            NodeInfo(mappingRule.getRange(), mappingBreadcrumbs),
            parseIdentifier(name, mappingBreadcrumbs.appendName("sourceName"), ctx),
            rename?.let { parseIdentifier(it, mappingBreadcrumbs.appendName("rename"), ctx) },
        )
    }

    return DeclarationNode.Import(
        NodeInfo(
            importDeclaration.getRange(), breadcrumbs
        ), path, mappings
    )
}

private fun parseNamedValueDeclaration(
    namedValue: NamedValueDeclarationContext,
    breadcrumbs: Breadcrumbs,
    ctx: ParserContext,
): DeclarationNode.NamedValue {
    val iterator = namedValue.children.ruleIterator()
    iterator.skip { (it is TerminalNode && (it.symbol.type == LET || it.symbol.type == NEWLINE)) }

    val nameToken = (iterator.next() as TerminalNode).symbol
    val name = parseIdentifier(nameToken, breadcrumbs.appendName("name"), ctx)

    iterator.skip { (it is TerminalNode && (it.symbol.type == COLON || it.symbol.type == NEWLINE || it.symbol.type == EQUALS)) }
    val typeAnnotationMaybeRule = iterator.next()
    var valueRule = typeAnnotationMaybeRule

    val typeAnnotation = if (typeAnnotationMaybeRule is TypeReferenceContext) {
        val typeAnnotationRule: TypeReferenceContext = typeAnnotationMaybeRule
        iterator.skip { (it is TerminalNode && (it.symbol.type == EQUALS || it.symbol.type == NEWLINE)) }
        valueRule = iterator.next()
        parseTypeReference(typeAnnotationRule, breadcrumbs.appendName("typeAnnotation"), ctx)
    } else {
        null
    }

    val value = parseExpression(valueRule as ExpressionContext, breadcrumbs.appendName("value"), ctx)

    return DeclarationNode.NamedValue(
        NodeInfo(namedValue.getRange(), breadcrumbs), name, typeAnnotation, value
    )
}

private fun parseBody(
    body: BodyContext, breadcrumbs: Breadcrumbs, ctx: ParserContext
): BodyNode {
    return when (val child = body.getChild(0)) {
        is BlockContext -> parseBlock(child, breadcrumbs, ctx)
        is SingleExpressionBodyContext -> {
            val expression = parseExpression(child.expression(), breadcrumbs.appendName("expression"), ctx)
            BodyNode.SingleExpressionBodyNode(
                NodeInfo(child.getRange(), breadcrumbs),
                expression
            )
        }

        else -> error("unexpected body type")
    }
}


private fun parseBlock(
    blockBody: BlockContext, breadcrumbs: Breadcrumbs, ctx: ParserContext
): BodyNode.BlockBodyNode {
    val statementBreadcrumbs = breadcrumbs.appendName("statements")
    val statements = blockBody.statement().mapIndexed { i, statementRule ->
        parseStatement(
            statementRule, statementBreadcrumbs.appendIndex(i), ctx
        )
    }

    return BodyNode.BlockBodyNode(NodeInfo(blockBody.getRange(), breadcrumbs), statements)
}

private fun parseStatement(
    statementRule: StatementContext, breadcrumbs: Breadcrumbs, ctx: ParserContext
): StatementNode {
    return when (val child = statementRule.getChild(0)) {
        is ExpressionStatementContext -> parseExpressionStatement(child, breadcrumbs, ctx)
        is DeclarationStatementContext -> parseDeclarationStatement(child, breadcrumbs, ctx)
        else -> throw Error("unrecognized statement type")
    }
}

private fun parseDeclarationStatement(
    declarationStatement: DeclarationStatementContext, breadcrumbs: Breadcrumbs, ctx: ParserContext
): StatementNode.DeclarationStatement {
    return StatementNode.DeclarationStatement(
        NodeInfo(declarationStatement.getRange(), breadcrumbs),
        parseDeclaration(declarationStatement.declaration(), breadcrumbs.appendName("declaration"), ctx)
    )
}

private fun parseExpressionStatement(
    expressionStatement: ExpressionStatementContext, breadcrumbs: Breadcrumbs, ctx: ParserContext
): StatementNode.ExpressionStatement {
    return StatementNode.ExpressionStatement(
        NodeInfo(expressionStatement.getRange(), breadcrumbs),
        parseExpression(expressionStatement.expression(), breadcrumbs.appendName("expression"), ctx)
    )
}


private fun parseExpression(
    expressionContext: ExpressionContext, breadcrumbs: Breadcrumbs, ctx: ParserContext
): ExpressionNode {
    val mainExpression = { innerBreadcrumbs: Breadcrumbs ->
        when (val child = expressionContext.getChild(0)) {
            is BlockExpressionContext -> parseBlockExpression(child, innerBreadcrumbs, ctx)
            is BranchExpressionContext -> parseBranchExpression(child, innerBreadcrumbs, ctx)
            is CauseExpressionContext -> parseCauseExpression(child, innerBreadcrumbs, ctx)
            is StringLiteralExpressionContext -> parseStringLiteralExpression(child, innerBreadcrumbs, ctx)
            is IntegerLiteralExpressionContext -> parseIntegerLiteralExpression(child, innerBreadcrumbs, ctx)
            is IdentifierExpressionContext -> parseIdentifierExpression(child, innerBreadcrumbs, ctx)
            else -> throw Error("unexpected expression type")
        }
    }

    val suffixContainer = expressionContext.getChild(1) as? ExpressionSuffixContext
    return when (val suffix = suffixContainer?.getChild(0)) {
        is CallExpressionSuffixContext -> parseCallExpressionSuffix(
            suffix, breadcrumbs, mainExpression, ctx
        )

        null -> mainExpression(breadcrumbs)
        else -> throw Error("unexpected call expression suffix")
    }
}

private fun parseBlockExpression(
    expression: BlockExpressionContext,
    breadcrumbs: Breadcrumbs,
    ctx: ParserContext
): ExpressionNode.BlockExpressionNode {
    return ExpressionNode.BlockExpressionNode(
        NodeInfo(expression.getRange(), breadcrumbs),
        parseBlock(expression.block(), breadcrumbs.appendName("block"), ctx)
    )
}

private fun parseBranchExpression(
    expression: BranchExpressionContext,
    breadcrumbs: Breadcrumbs,
    ctx: ParserContext
): ExpressionNode {
    val options = expression.branchOption()
        .mapIndexed { i, it -> parseBranchOption(it, breadcrumbs.appendName("branches").appendIndex(i), ctx) }

    return ExpressionNode.BranchExpressionNode(
        NodeInfo(expression.getRange(), breadcrumbs),
        options
    )
}

private fun parseBranchOption(
    branchOption: BranchOptionContext,
    breadcrumbs: Breadcrumbs,
    ctx: ParserContext
): BranchOptionNode {
    val info = NodeInfo(branchOption.getRange(), breadcrumbs)
    return when (val child = branchOption.getChild(0)) {
        is IfBranchOptionContext -> {
            BranchOptionNode.IfBranchOptionNode(
                info,
                parseExpression(child.expression(), breadcrumbs.appendName("condition"), ctx),
                parseBody(child.body(), breadcrumbs.appendName("body"), ctx),
            )
        }

        is ElseBranchOptionContext -> {
            BranchOptionNode.ElseBranchOptionNode(
                info,
                parseBody(child.body(), breadcrumbs.appendName("body"), ctx)
            )
        }

        else -> error("Unexpected branch option type")
    }
}

private fun parseStringLiteralExpression(
    expression: StringLiteralExpressionContext, breadcrumbs: Breadcrumbs, ctx: ParserContext
): ExpressionNode.StringLiteralExpression {
    val quotedText = expression.STRING_LITERAL().text
    val unquoted = quotedText.subSequence(1 until quotedText.length - 1)

    return ExpressionNode.StringLiteralExpression(
        NodeInfo(expression.getRange(), breadcrumbs), unquoted.toString()
    )
}

private fun parseIntegerLiteralExpression(
    expression: IntegerLiteralExpressionContext, breadcrumbs: Breadcrumbs, ctx: ParserContext
): ExpressionNode.IntegerLiteralExpression {
    val text = expression.INT_LITERAL().text
    val number = text.replace("_", "").toLong()

    return ExpressionNode.IntegerLiteralExpression(
        NodeInfo(expression.getRange(), breadcrumbs), number
    )
}

private fun parseCauseExpression(
    expression: CauseExpressionContext, breadcrumbs: Breadcrumbs, ctx: ParserContext
): ExpressionNode.CauseExpression {
    val signal = parseExpression(expression.expression(), breadcrumbs.appendName("signal"), ctx)

    return ExpressionNode.CauseExpression(
        NodeInfo(expression.getRange(), breadcrumbs), signal
    )
}

private fun parseIdentifierExpression(
    expression: IdentifierExpressionContext, breadcrumbs: Breadcrumbs, ctx: ParserContext
): ExpressionNode.IdentifierExpression {
    val identifier = parseIdentifier(expression.IDENTIFIER().symbol, breadcrumbs.appendName("identifier"), ctx)
    return ExpressionNode.IdentifierExpression(
        NodeInfo(expression.getRange(), breadcrumbs), identifier
    )
}

private fun parseCallExpressionSuffix(
    suffix: CallExpressionSuffixContext,
    breadcrumbs: Breadcrumbs,
    mainExpression: (Breadcrumbs) -> ExpressionNode,
    ctx: ParserContext
): ExpressionNode.CallExpression {
    val callee = mainExpression(breadcrumbs.appendName("callee"))

    val paramsBreadcrumbs = breadcrumbs.appendName("parameters")
    val params = suffix.callParam().mapIndexed { i, paramContainer ->
        when (val param = paramContainer.getChild(0)) {
            is CallPositionalParameterContext -> parsePositionalParameter(param, paramsBreadcrumbs.appendIndex(i), ctx)
            else -> throw Error("Unexpected call parameter type")
        }
    }

    return ExpressionNode.CallExpression(
        NodeInfo(suffix.getRange(), breadcrumbs), callee, params
    )
}

private fun parsePositionalParameter(
    param: CallPositionalParameterContext, breadcrumbs: Breadcrumbs, ctx: ParserContext
): ExpressionNode.CallExpression.ParameterNode {
    val value = parseExpression(param.expression(), breadcrumbs.appendName("value"), ctx)
    return ExpressionNode.CallExpression.ParameterNode(
        NodeInfo(param.getRange(), breadcrumbs), value
    )
}

private fun ParserRuleContext.getRange(): DocumentRange {
    return DocumentRange(
        DocumentPosition(this.start.line, this.start.charPositionInLine),
        this.stop.getRange().end
    )
}

private fun Token.getRange(): DocumentRange {
    val start = DocumentPosition(this.line, this.charPositionInLine)
    val lines = this.text.split("\n")
    val end = if (lines.size == 1) {
        DocumentPosition(start.line, start.column + lines.first().length)
    } else {
        DocumentPosition(start.line + lines.size - 1, start.column + lines.last().length)
    }

    return DocumentRange(start, end)
}

private class RuleIterator<T>(val list: List<T>) {
    var currentIndex = 0
        private set

    fun hasNext() = currentIndex < list.size

    fun next(): T {
        val value = list[currentIndex]
        currentIndex += 1
        return value
    }

    fun tryNext(): T? = if (this.hasNext()) {
        this.next()
    } else {
        null
    }

    fun peek(): T? = if (this.hasNext()) {
        list[currentIndex]
    } else {
        null
    }

    fun skip(f: (T?) -> Boolean) {
        while (f(peek())) {
            next()
        }
    }
}

private fun <T> List<T>.ruleIterator() = RuleIterator(this)

private fun RuleIterator<ParseTree>.skipNewlines() {
    skip { it is TerminalNode && it.symbol.type == NEWLINE }
}