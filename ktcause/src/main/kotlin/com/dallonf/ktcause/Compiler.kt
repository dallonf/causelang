package com.dallonf.ktcause

import com.dallonf.ktcause.ast.*
import com.dallonf.ktcause.types.*

object Compiler {
    private data class CompilerContext(
        val fileNode: FileNode,
        val analyzed: AnalyzedNode,
        val resolved: ResolvedFile,
    ) {
        fun getTags(breadcrumbs: Breadcrumbs): List<NodeTag> = analyzed.nodeTags[breadcrumbs]!!
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

        return CompiledFile(resolved.path, types, chunks, exports, resolved)
    }

    private fun compileFunction(
        declaration: DeclarationNode.Function, ctx: CompilerContext
    ): CompiledFile.MutableInstructionChunk {
        val chunk = CompiledFile.MutableInstructionChunk()
        when (declaration.body) {
            is BodyNode.BlockBody -> {
                for ((i, statement) in declaration.body.statements.withIndex()) {
                    compileStatement(
                        statement, chunk, ctx, isLastStatement = i == declaration.body.statements.lastIndex
                    )
                }
                // TODO: make sure this is the right type to return
                chunk.writeInstruction(Instruction.Return)
            }
        }
        return chunk
    }

    private fun compileBadValue(
        node: AstNode, error: ErrorValueLangType, chunk: CompiledFile.MutableInstructionChunk, ctx: CompilerContext
    ) {
        chunk.writeLiteral(
            CompiledFile.CompiledConstant.ErrorConst(
                ctx.resolved.path,
                node.info.breadcrumbs,
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
                    chunk.writeInstruction(Instruction.Pop)
                }
            }

            is StatementNode.DeclarationStatement -> TODO()
        }
    }

    private fun compileExpression(
        expression: ExpressionNode, chunk: CompiledFile.MutableInstructionChunk, ctx: CompilerContext
    ) {
        when (expression) {
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
            chunk.writeInstruction(Instruction.Pop)
            compileBadValue(expression, error, chunk, ctx)
        }
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
        val comesFrom = tags.firstNotNullOf { (it as? NodeTag.ValueComesFrom)?.source }
        val comesFromTags = ctx.getTags(comesFrom)

        for (tag in comesFromTags) {
            when (tag) {
                is NodeTag.ReferencesFile -> {
                    val filePathConstant = chunk.addConstant(CompiledFile.CompiledConstant.StringConst(tag.path))
                    val exportNameConstant = tag.exportName?.let {
                        chunk.addConstant(CompiledFile.CompiledConstant.StringConst(it))
                    }

                    if (exportNameConstant == null) {
                        TODO("Haven't implemented files as first-class objects yet")
                    } else {
                        chunk.writeInstruction(Instruction.Import(filePathConstant, exportNameConstant))
                    }
                    return
                }

                else -> {}
            }
        }

        // change this to an AssertionError when we're more stable
        TODO("Wasn't able to resolve identifier to anything")
    }

    private fun compileCauseExpression(
        expression: ExpressionNode.CauseExpression, chunk: CompiledFile.MutableInstructionChunk, ctx: CompilerContext
    ) {
        compileExpression(expression.signal, chunk, ctx)

        ctx.resolved.checkForRuntimeErrors(expression.info.breadcrumbs)?.let { error ->
            chunk.writeInstruction(Instruction.Pop)
            compileBadValue(expression, error, chunk, ctx)
            chunk.writeInstruction(
                Instruction.Import(
                    filePathConstant = chunk.addConstant(CompiledFile.CompiledConstant.StringConst("core/builtin.cau")),
                    exportNameConstant = chunk.addConstant(CompiledFile.CompiledConstant.StringConst("TypeError")),
                )
            )
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
                chunk.writeInstruction(Instruction.Pop)
                compileBadValue(param, error, chunk, ctx)
            }
        }

        compileExpression(expression.callee, chunk, ctx)

        ctx.resolved.checkForRuntimeErrors(expression.info.breadcrumbs)?.let { error ->
            // Don't call; pop all the arguments and the callee off the stack and then push an error
            for (i in 0..expression.parameters.size + 1) {
                chunk.writeInstruction(Instruction.Pop)
            }
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