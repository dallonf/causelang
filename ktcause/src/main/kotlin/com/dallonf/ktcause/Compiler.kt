package com.dallonf.ktcause

import com.dallonf.ktcause.ast.*
import com.dallonf.ktcause.types.*

object Compiler {
    private data class CompilerContext(
        val fileNode: FileNode,
        val analyzed: AnalyzedNode,
        val resolved: ResolvedFile,
        val scopeStack: ArrayDeque<CompilerScope> = ArrayDeque()
    ) {
        fun getTags(breadcrumbs: Breadcrumbs): List<NodeTag> = analyzed.nodeTags[breadcrumbs]!!

        inline fun <reified T : NodeTag> getTag(breadcrumbs: Breadcrumbs): T? =
            getTags(breadcrumbs).firstNotNullOfOrNull { it as? T }

        fun nextScopeIndex(): Int {
            return scopeStack.sumOf { it.namedValueIndices.size }
        }
    }

    private class CompilerScope(val scopeRoot: Breadcrumbs) {
        // indices are computed from the top of the current call frame
        val namedValueIndices = mutableMapOf<Breadcrumbs, Int>()
    }

    fun compile(fileNode: FileNode, analyzed: AnalyzedNode, resolved: ResolvedFile): CompiledFile {
        val types = mutableMapOf<CanonicalLangTypeId, CanonicalLangType>()
        val chunks = mutableListOf<CompiledFile.InstructionChunk>()
        val exports = mutableMapOf<String, CompiledFile.CompiledExport>()

        for (declaration in fileNode.declarations) {
            when (declaration) {
                is DeclarationNode.Import -> {}
                is DeclarationNode.Function -> {
                    val ctx = CompilerContext(fileNode, analyzed, resolved)
                    val chunk = compileFunction(declaration, ctx)
                    val functionType = resolved.getExpectedType(declaration.info.breadcrumbs)

                    chunks.add(chunk.toInstructionChunk())
                    exports[declaration.name.text] =
                        CompiledFile.CompiledExport.Function(chunks.lastIndex, functionType)
                }

                is DeclarationNode.NamedValue -> TODO()
            }
        }

        return CompiledFile(resolved.path, types, chunks, exports)
    }

    private fun compileFunction(
        declaration: DeclarationNode.Function, ctx: CompilerContext
    ): CompiledFile.MutableInstructionChunk {
        val chunk = CompiledFile.MutableInstructionChunk()
        when (declaration.body) {
            is BodyNode.BlockBody -> {
                compileBlock(declaration.body, chunk, ctx)

                // TODO: make sure this is the right type to return
                chunk.writeInstruction(Instruction.Return)
            }
        }
        return chunk
    }

    private fun compileBlock(
        block: BodyNode.BlockBody,
        chunk: CompiledFile.MutableInstructionChunk,
        ctx: CompilerContext,
    ) {
        ctx.scopeStack.addLast(CompilerScope(block.info.breadcrumbs))

        for ((i, statement) in block.statements.withIndex()) {
            compileStatement(
                statement, chunk, ctx, isLastStatement = i == block.statements.lastIndex
            )
        }

        val scope = ctx.scopeStack.removeLast()
        chunk.writeInstruction(Instruction.PopScope(scope.namedValueIndices.size))
    }

    private fun compileBadValue(
        node: AstNode, error: ErrorValueLangType, chunk: CompiledFile.MutableInstructionChunk, ctx: CompilerContext
    ) {
        chunk.writeLiteral(
            CompiledFile.CompiledConstant.ErrorConst(
                SourcePosition.Source(
                    ctx.resolved.path, node.info.breadcrumbs, node.info.position
                ),
                error,
            )
        )
    }

    private fun compileStatement(
        statement: StatementNode,
        chunk: CompiledFile.MutableInstructionChunk,
        ctx: CompilerContext,
        isLastStatement: Boolean
    ) {
        when (statement) {
            is StatementNode.ExpressionStatement -> {
                compileExpression(statement.expression, chunk, ctx)

                // TODO: emit compile error for mismatched type?

                if (!isLastStatement) {
                    chunk.writeInstruction(Instruction.Pop())
                }
            }

            is StatementNode.DeclarationStatement -> {
                compileLocalDeclaration(statement, chunk, ctx)
            }
        }
    }

    private fun compileLocalDeclaration(
        statement: StatementNode.DeclarationStatement, chunk: CompiledFile.MutableInstructionChunk, ctx: CompilerContext
    ) {
        when (val declaration = statement.declaration) {
            is DeclarationNode.Import -> {}
            is DeclarationNode.Function -> TODO()
            is DeclarationNode.NamedValue -> {
                compileExpression(declaration.value, chunk, ctx)
                ctx.resolved.checkForRuntimeErrors(declaration.info.breadcrumbs)?.let { error ->
                    chunk.writeInstruction(Instruction.Pop())
                    compileBadValue(declaration, error, chunk, ctx)
                }
                ctx.scopeStack.last().namedValueIndices[declaration.info.breadcrumbs] = ctx.nextScopeIndex()
            }
        }
    }

    private fun compileExpression(
        expression: ExpressionNode, chunk: CompiledFile.MutableInstructionChunk, ctx: CompilerContext
    ) {
        when (expression) {
            is ExpressionNode.BlockExpressionNode -> compileBlockExpression(expression, chunk, ctx)
            is ExpressionNode.IdentifierExpression -> compileIdentifierExpression(expression, chunk, ctx)
            is ExpressionNode.CauseExpression -> compileCauseExpression(expression, chunk, ctx)
            is ExpressionNode.CallExpression -> compileCallExpression(expression, chunk, ctx)
            is ExpressionNode.StringLiteralExpression -> chunk.writeLiteral(
                CompiledFile.CompiledConstant.StringConst(
                    expression.text
                )
            )

            is ExpressionNode.IntegerLiteralExpression -> chunk.writeLiteral(
                CompiledFile.CompiledConstant.IntegerConst(
                    expression.value
                )
            )
        }

        // TODO: this can be redundant since sometimes there's already a BadValue on the stack
        ctx.resolved.checkForRuntimeErrors(expression.info.breadcrumbs)?.let { error ->
            chunk.writeInstruction(Instruction.Pop())
            compileBadValue(expression, error, chunk, ctx)
        }
    }

    private fun compileBlockExpression(
        expression: ExpressionNode.BlockExpressionNode,
        chunk: CompiledFile.MutableInstructionChunk,
        ctx: CompilerContext
    ) {
        compileBlock(expression.block, chunk, ctx)
    }

    private fun compileIdentifierExpression(
        expression: ExpressionNode.IdentifierExpression,
        chunk: CompiledFile.MutableInstructionChunk,
        ctx: CompilerContext
    ) {
        ctx.resolved.checkForRuntimeErrors(expression.info.breadcrumbs)?.let { error ->
            compileBadValue(expression, error, chunk, ctx)
            return
        }

        val tags = ctx.getTags(expression.info.breadcrumbs)
        val comesFrom = tags.firstNotNullOf { (it as? NodeTag.ValueComesFrom) }

        ctx.getTag<NodeTag.ReferencesFile>(comesFrom.source)?.let {
            compileFileImportReference(it, chunk)
            return
        }

        ctx.getTag<NodeTag.TopLevelDeclaration>(comesFrom.source)?.let {
            compileTopLevelReference(it, chunk, ctx)
            return
        }

        (ctx.getTag<NodeTag.NamedValue>(comesFrom.source) ?: ctx.getTag<NodeTag.IsFunction>(comesFrom.source))?.let {
            compileNamedValueReference(comesFrom, chunk, ctx)
            return
        }

        // change this to an AssertionError when we're more stable
        TODO("Wasn't able to resolve identifier to anything")
    }

    private fun compileTopLevelReference(
        comesFrom: NodeTag.TopLevelDeclaration, chunk: CompiledFile.MutableInstructionChunk, ctx: CompilerContext
    ) {
        chunk.writeInstruction(Instruction.ImportSameFile(chunk.addConstant(comesFrom.name)))
    }

    private fun compileFileImportReference(
        tag: NodeTag.ReferencesFile, chunk: CompiledFile.MutableInstructionChunk
    ) {
        val filePathConstant = chunk.addConstant(CompiledFile.CompiledConstant.StringConst(tag.path))
        val exportNameConstant = tag.exportName?.let {
            chunk.addConstant(CompiledFile.CompiledConstant.StringConst(it))
        }

        if (exportNameConstant == null) {
            TODO("Haven't implemented files as first-class objects yet")
        } else {
            chunk.writeInstruction(Instruction.Import(filePathConstant, exportNameConstant))
        }
    }

    private fun compileNamedValueReference(
        valueComesFrom: NodeTag.ValueComesFrom,
        chunk: CompiledFile.MutableInstructionChunk,
        ctx: CompilerContext,
    ) {
        for (scope in ctx.scopeStack.reversed()) {
            val foundIndex = scope.namedValueIndices[valueComesFrom.source]
            if (foundIndex != null) {
                chunk.writeInstruction(Instruction.ReadLocal(foundIndex))
                return
            }
        }
        error("Couldn't find named value in scope")
    }

    private fun compileCauseExpression(
        expression: ExpressionNode.CauseExpression, chunk: CompiledFile.MutableInstructionChunk, ctx: CompilerContext
    ) {
        compileExpression(expression.signal, chunk, ctx)

        ctx.resolved.checkForRuntimeErrors(expression.info.breadcrumbs)?.let { error ->
            chunk.writeInstruction(Instruction.Pop())
            compileBadValue(expression, error, chunk, ctx)
            chunk.writeInstruction(
                Instruction.Import(
                    filePathConstant = chunk.addConstant(CompiledFile.CompiledConstant.StringConst("core/builtin.cau")),
                    exportNameConstant = chunk.addConstant(CompiledFile.CompiledConstant.StringConst("TypeError")),
                )
            )
            chunk.writeInstruction(Instruction.Construct(1))
            chunk.writeInstruction(Instruction.Cause)
            return;
        }

        chunk.writeInstruction(Instruction.Cause)
    }

    private fun compileCallExpression(
        expression: ExpressionNode.CallExpression, chunk: CompiledFile.MutableInstructionChunk, ctx: CompilerContext
    ) {
        for (param in expression.parameters) {
            compileExpression(param.value, chunk, ctx)
            ctx.resolved.checkForRuntimeErrors(param.info.breadcrumbs)?.let { error ->
                chunk.writeInstruction(Instruction.Pop())
                compileBadValue(param, error, chunk, ctx)
            }
        }

        compileExpression(expression.callee, chunk, ctx)

        ctx.resolved.checkForRuntimeErrors(expression.info.breadcrumbs)?.let { error ->
            // Don't call; pop all the arguments and the callee off the stack and then push an error
            chunk.writeInstruction(Instruction.Pop(expression.parameters.size + 1))
            compileBadValue(expression, error, chunk, ctx)
            return
        }

        when (val calleeType = ctx.resolved.getExpectedType(expression.callee.info.breadcrumbs)) {
            is TypeReferenceValueLangType -> {
                when (calleeType.canonicalType) {
                    is CanonicalLangType.SignalCanonicalLangType -> chunk.writeInstruction(Instruction.Construct(arity = expression.parameters.size))
                }
            }

            is FunctionValueLangType -> {
                chunk.writeInstruction(Instruction.CallFunction(arity = expression.parameters.size))
            }
            // any other case should have been handled by the resolver as an
            // error on the CallExpression itself, which was checked above
            else -> throw AssertionError()
        }
    }
}
