package com.dallonf.ktcause.parse

import com.dallonf.ktcause.antlr.*
import com.dallonf.ktcause.antlr.CauseParser.*
import com.dallonf.ktcause.ast.*
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.TerminalNode

private class ParserContext()

fun parse(source: String): FileNode {
    val tokens = CommonTokenStream(CauseLexer(CharStreams.fromString(source)))
    val tree = CauseParser(tokens).file()

    val declarationsBreadcrumbs = Breadcrumbs.empty().appendName("declarations")
    val declarations = tree.declaration().mapIndexed { i, declaration ->
        parseDeclaration(declaration, declarationsBreadcrumbs.appendIndex(i), ParserContext())
    }

    return FileNode(NodeInfo(tree.getRange(), Breadcrumbs.empty()), declarations)
}

private fun parseDeclaration(
    declaration: DeclarationContext,
    breadcrumbs: Breadcrumbs,
    ctx: ParserContext
): DeclarationNode {
    return when (val child = declaration.getChild(0)) {
        is ImportDeclarationContext -> parseImportDeclaration(child, breadcrumbs, ctx)
        else -> throw Error("unexpected declaration type: ${child.toString()}")
    }
}

private fun parseImportDeclaration(
    importDeclaration: ImportDeclarationContext,
    breadcrumbs: Breadcrumbs,
    ctx: ParserContext
): DeclarationNode.Import {
    val pathToken = importDeclaration.PATH()
    val path = DeclarationNode.Import.PathNode(
        NodeInfo(pathToken.symbol.getRange(), breadcrumbs.appendName("path")),
        pathToken.text
    )

    val mappingRules = importDeclaration.importMappings().importMapping()
    val mappings = mappingRules.mapIndexed { i, mappingRule ->
        val iterator = mappingRule.children.iterator()
        val name = (iterator.next() as TerminalNode).symbol
        assert(name.type == IDENTIFIER)
        val rename = (iterator.tryNext() as TerminalNode?)?.symbol
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
            importDeclaration.getRange(),
            breadcrumbs
        ), path, mappings
    )
}

private fun parseIdentifier(token: Token, breadcrumbs: Breadcrumbs, ctx: ParserContext): Identifier {
    assert(token.type == IDENTIFIER)
    return Identifier(
        NodeInfo(token.getRange(), breadcrumbs),
        token.text
    )
}

private fun ParserRuleContext.getRange(): DocumentRange {
    return DocumentRange(
        DocumentPosition(this.start.line, this.start.charPositionInLine),
        DocumentPosition(this.stop.line, this.stop.charPositionInLine)
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

private fun <T> Iterator<T>.tryNext(): T? = if (this.hasNext()) {
    this.next()
} else {
    null
}