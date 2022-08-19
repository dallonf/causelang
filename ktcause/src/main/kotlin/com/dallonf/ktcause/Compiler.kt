package com.dallonf.ktcause

import com.dallonf.ktcause.ast.*
import com.dallonf.ktcause.types.*
import kotlin.reflect.KClass

object Compiler {
    private data class CompilerContext(
        val fileNode: FileNode,
        val analyzed: AnalyzedNode,
        val resolved: ResolvedFile,
        val chunks: MutableList<CompiledFile.InstructionChunk>,
        val scopeStack: ArrayDeque<CompilerScope> = ArrayDeque(),
    ) {
        fun getTags(breadcrumbs: Breadcrumbs): List<NodeTag> = analyzed.nodeTags[breadcrumbs]!!

        inline fun <reified T : NodeTag> getTag(breadcrumbs: Breadcrumbs): T? =
            getTags(breadcrumbs).firstNotNullOfOrNull { it as? T }

        fun hasTag(breadcrumbs: Breadcrumbs, vararg types: KClass<out NodeTag>): Boolean {
            return getTags(breadcrumbs).any { tag -> types.any { it.isInstance(tag) } }
        }

        fun nextScopeIndex(): Int {
            return scopeStack.sumOf { it.size() }
        }
    }

    private class CompilerScope(
        val scopeRoot: Breadcrumbs, val type: ScopeType, val stackPrefix: Int = 0, var effectCount: Int = 0
    ) {
        enum class ScopeType {
            BODY, EFFECT
        }

        // indices are computed from the top of the current call frame
        val namedValueIndices = mutableMapOf<Breadcrumbs, Int>()

        fun size() = stackPrefix + namedValueIndices.size
    }

    fun compile(fileNode: FileNode, analyzed: AnalyzedNode, resolved: ResolvedFile): CompiledFile {
        val types = mutableMapOf<CanonicalLangTypeId, CanonicalLangType>()
        val exports = mutableMapOf<String, CompiledFile.CompiledExport>()
        val ctx = CompilerContext(fileNode, analyzed, resolved, chunks = mutableListOf())

        for (declaration in fileNode.declarations) {
            when (declaration) {
                is DeclarationNode.Import -> {}
                is DeclarationNode.Function -> {
                    val chunk = compileFunction(declaration, ctx)
                    val functionType = resolved.getExpectedType(declaration.info.breadcrumbs)

                    ctx.chunks.add(chunk.toInstructionChunk())
                    exports[declaration.name.text] =
                        CompiledFile.CompiledExport.Function(ctx.chunks.lastIndex, functionType)
                }

                is DeclarationNode.ObjectType -> {
                    val objectType = resolved.getExpectedType(declaration.info.breadcrumbs)

                    val error = objectType.getRuntimeError()
                    if (objectType is TypeReferenceConstraintLangType) {
                        types[objectType.canonicalType.id] = objectType.canonicalType
                        exports[declaration.name.text] = CompiledFile.CompiledExport.Type(objectType.canonicalType.id)
                    } else if (objectType is UniqueObjectLangType) {
                        types[objectType.canonicalType.id] = objectType.canonicalType
                        exports[declaration.name.text] = CompiledFile.CompiledExport.Type(objectType.canonicalType.id)
                    } else if (error != null) {
                        exports[declaration.name.text] = CompiledFile.CompiledExport.Error(error)
                    } else {
                        error("Object declaration resolved to: $objectType")
                    }
                }

                is DeclarationNode.SignalType -> {
                    val signalType = resolved.getExpectedType(declaration.info.breadcrumbs)

                    val error = signalType.getRuntimeError()
                    if (signalType is TypeReferenceConstraintLangType) {
                        types[signalType.canonicalType.id] = signalType.canonicalType
                        exports[declaration.name.text] = CompiledFile.CompiledExport.Type(signalType.canonicalType.id)
                    } else if (signalType is UniqueObjectLangType) {
                        types[signalType.canonicalType.id] = signalType.canonicalType
                        exports[declaration.name.text] = CompiledFile.CompiledExport.Type(signalType.canonicalType.id)
                    } else if (error != null) {
                        exports[declaration.name.text] = CompiledFile.CompiledExport.Error(error)
                    } else {
                        error("Signal declaration resolved to: $signalType")
                    }
                }

                is DeclarationNode.OptionType -> {
                    val optionType = resolved.getExpectedType(declaration.info.breadcrumbs)
                    val error = optionType.getRuntimeError()
                    if (optionType is OptionConstraintLangType) {
                        exports[declaration.name.text] = CompiledFile.CompiledExport.Value(null, optionType)
                    } else if (error != null) {
                        exports[declaration.name.text] = CompiledFile.CompiledExport.Error(error)
                    } else {
                        error("Option type declaration resolved to: $optionType")
                    }
                }

                is DeclarationNode.NamedValue -> TODO()
            }
        }

        return CompiledFile(resolved.path, types, ctx.chunks, exports)
    }

    private fun compileFunction(
        declaration: DeclarationNode.Function, ctx: CompilerContext
    ): CompiledFile.MutableInstructionChunk {
        val chunk = CompiledFile.MutableInstructionChunk()
        val functionScope = CompilerScope(declaration.info.breadcrumbs, CompilerScope.ScopeType.BODY)
        val oldScopeStack = ctx.scopeStack.toList()
        ctx.scopeStack.clear() // brand-new scope for every function
        ctx.scopeStack.addLast(functionScope)

        for ((i, param) in declaration.params.withIndex()) {
            functionScope.namedValueIndices[param.info.breadcrumbs] = i
        }
        compileBody(declaration.body, chunk, ctx)
        // TODO: make sure this is the right type to return
        chunk.writeInstruction(Instruction.Return)

        ctx.scopeStack.clear()
        ctx.scopeStack.addAll(oldScopeStack)
        return chunk
    }

    private fun compileBody(
        body: BodyNode, chunk: CompiledFile.MutableInstructionChunk, ctx: CompilerContext
    ) {
        when (body) {
            is BodyNode.BlockBodyNode -> {
                compileBlock(body, chunk, ctx)
            }

            is BodyNode.SingleExpressionBodyNode -> {
                compileExpression(body.expression, chunk, ctx)
            }
        }
    }

    private fun compileBlock(
        block: BodyNode.BlockBodyNode,
        chunk: CompiledFile.MutableInstructionChunk,
        ctx: CompilerContext,
    ) {
        ctx.scopeStack.addLast(CompilerScope(block.info.breadcrumbs, CompilerScope.ScopeType.BODY))

        for ((i, statement) in block.statements.withIndex()) {
            compileStatement(
                statement, chunk, ctx, isLastStatement = i == block.statements.lastIndex
            )
        }

        val scope = ctx.scopeStack.removeLast()
        chunk.writeInstruction(Instruction.PopEffects(scope.effectCount))
        chunk.writeInstruction(Instruction.PopScope(scope.size()))
    }

    private fun compileBadValue(
        node: AstNode, error: ErrorLangType, chunk: CompiledFile.MutableInstructionChunk, ctx: CompilerContext
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

                if (!isLastStatement) {
                    chunk.writeInstruction(Instruction.Pop())
                }
            }

            is StatementNode.DeclarationStatement -> {
                compileLocalDeclaration(statement, chunk, ctx)

                if (isLastStatement) {
                    chunk.writeInstruction(Instruction.PushAction)
                }
            }

            is StatementNode.EffectStatement -> {
                compileEffectStatement(statement, chunk, ctx)

                if (isLastStatement) {
                    chunk.writeInstruction(Instruction.PushAction)
                }
            }
        }
    }

    private fun compileLocalDeclaration(
        statement: StatementNode.DeclarationStatement, chunk: CompiledFile.MutableInstructionChunk, ctx: CompilerContext
    ) {
        when (val declaration = statement.declaration) {
            is DeclarationNode.Import -> {}
            is DeclarationNode.ObjectType -> {}
            is DeclarationNode.SignalType -> {}
            is DeclarationNode.OptionType -> {}
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

    private fun compileEffectStatement(
        statement: StatementNode.EffectStatement, chunk: CompiledFile.MutableInstructionChunk, ctx: CompilerContext
    ) {
        val effectChunk = CompiledFile.MutableInstructionChunk()
        ctx.scopeStack.last().effectCount += 1

        ctx.scopeStack.addLast(
            CompilerScope(statement.info.breadcrumbs, CompilerScope.ScopeType.EFFECT)
        )
        ctx.scopeStack.last().namedValueIndices[statement.pattern.info.breadcrumbs] = 0

        // Check the condition
        ctx.resolved.checkForRuntimeErrors(statement.pattern.typeReference.info.breadcrumbs).let { error ->
            if (error == null) {
                effectChunk.writeInstruction(Instruction.ReadLocal(0))
                compileValueFlowReference(statement.pattern.typeReference, effectChunk, ctx)
                effectChunk.writeInstruction(Instruction.IsAssignableTo)
                val rejectEffectJump = effectChunk.writeInstruction(Instruction.NoOp)
                compileBody(statement.body, effectChunk, ctx)
                effectChunk.writeInstruction(Instruction.FinishEffect)
                effectChunk.instructions[rejectEffectJump] = Instruction.JumpIfFalse(effectChunk.instructions.size)
            }
        }

        effectChunk.writeInstruction(Instruction.RejectSignal)

        ctx.scopeStack.removeLast()

        ctx.chunks.add(effectChunk.toInstructionChunk())
        chunk.writeInstruction(Instruction.RegisterEffect(ctx.chunks.lastIndex))
    }


    private fun compileExpression(
        expression: ExpressionNode, chunk: CompiledFile.MutableInstructionChunk, ctx: CompilerContext
    ) {
        when (expression) {
            is ExpressionNode.BlockExpressionNode -> compileBlockExpression(expression, chunk, ctx)
            is ExpressionNode.BranchExpressionNode -> compileBranchExpression(expression, chunk, ctx)
            is ExpressionNode.IdentifierExpression -> compileValueFlowReference(expression, chunk, ctx)
            is ExpressionNode.CauseExpression -> compileCauseExpression(expression, chunk, ctx)
            is ExpressionNode.CallExpression -> compileCallExpression(expression, chunk, ctx)
            is ExpressionNode.MemberExpression -> compileMemberExpression(expression, chunk, ctx)

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

    private fun compileBranchExpression(
        expression: ExpressionNode.BranchExpressionNode,
        chunk: CompiledFile.MutableInstructionChunk,
        ctx: CompilerContext
    ) {
        val elseBranch = expression.branches.firstNotNullOfOrNull { it as? BranchOptionNode.ElseBranchOptionNode }
        val ifBranches = expression.branches.mapNotNull { it as? BranchOptionNode.IfBranchOptionNode }

        val remainingBranchJumps = mutableListOf<Int>()
        for (ifBranch in ifBranches) {
            compileExpression(ifBranch.condition, chunk, ctx)
            val skipBodyInstruction = chunk.writeInstruction(Instruction.NoOp)
            compileBody(ifBranch.body, chunk, ctx)
            remainingBranchJumps.add(chunk.writeInstruction(Instruction.NoOp))
            chunk.instructions[skipBodyInstruction] = Instruction.JumpIfFalse(chunk.instructions.size)
        }
        if (elseBranch != null) {
            compileBody(elseBranch.body, chunk, ctx)
        } else {
            chunk.writeInstruction(Instruction.PushAction)
        }

        for (jump in remainingBranchJumps) {
            chunk.instructions[jump] = Instruction.Jump(chunk.instructions.size)
        }
    }

    private fun compileValueFlowReference(
        node: AstNode,
        chunk: CompiledFile.MutableInstructionChunk,
        ctx: CompilerContext,
    ) {
        ctx.resolved.checkForRuntimeErrors(node.info.breadcrumbs)?.let { error ->
            compileBadValue(node, error, chunk, ctx)
            return
        }

        val comesFrom = ctx.getTag<NodeTag.ValueComesFrom>(node.info.breadcrumbs)!!

        ctx.getTag<NodeTag.ReferencesFile>(comesFrom.source)?.let {
            compileFileImportReference(it, chunk)
            return
        }

        ctx.getTag<NodeTag.TopLevelDeclaration>(comesFrom.source)?.let {
            compileTopLevelReference(it, chunk, ctx)
            return
        }

        if (ctx.hasTag(
                comesFrom.source,
                NodeTag.IsNamedValue::class,
                NodeTag.IsFunction::class,
                NodeTag.ParamForFunction::class,
                NodeTag.IsPattern::class,
            )
        ) {
            compileValueReference(comesFrom, chunk, ctx)
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

    private fun compileValueReference(
        valueComesFrom: NodeTag.ValueComesFrom,
        chunk: CompiledFile.MutableInstructionChunk,
        ctx: CompilerContext,
    ) {
        var effectDepth = 0
        for (scope in ctx.scopeStack.reversed()) {
            val foundIndex = scope.namedValueIndices[valueComesFrom.source]
            if (foundIndex != null) {
                chunk.writeInstruction(
                    if (effectDepth > 0) {
                        Instruction.ReadLocalThroughEffectScope(effectDepth, foundIndex)
                    } else {
                        Instruction.ReadLocal(foundIndex)
                    }
                )
                return
            }
            if (scope.type == CompilerScope.ScopeType.EFFECT) {
                effectDepth += 1
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
            is TypeReferenceConstraintLangType -> {
                when (calleeType.canonicalType) {
                    is CanonicalLangType.SignalCanonicalLangType -> chunk.writeInstruction(Instruction.Construct(arity = expression.parameters.size))
                    is CanonicalLangType.ObjectCanonicalLangType -> chunk.writeInstruction(Instruction.Construct(arity = expression.parameters.size))
                }
            }

            is FunctionValueLangType -> {
                chunk.writeInstruction(Instruction.CallFunction(arity = expression.parameters.size))
            }

            is UniqueObjectLangType -> {
                // ignore this, unique objects don't need to be constructed.
                // Kiiind of abusing PopScope here to keep the value in place while popping
                // any erroneous params
                chunk.writeInstruction(Instruction.PopScope(expression.parameters.size))
            }

            // any other case should have been handled by the resolver as an
            // error on the CallExpression itself, which was checked above
            else -> throw AssertionError()
        }
    }

    private fun compileMemberExpression(
        expression: ExpressionNode.MemberExpression, chunk: CompiledFile.MutableInstructionChunk, ctx: CompilerContext
    ) {
        compileExpression(expression.objectExpression, chunk, ctx)

        ctx.resolved.checkForRuntimeErrors(expression.info.breadcrumbs)?.let { error ->
            chunk.writeInstruction(Instruction.Pop(1))
            compileBadValue(expression, error, chunk, ctx)
            return
        }

        // TODO: it'd be nice if we could pick this info up from the resolver,
        // which already had to do all this work to determine the type
        val objType =
            ctx.resolved.getInferredType(expression.objectExpression.info.breadcrumbs) as InstanceValueLangType
        val fields = when (val canonical = objType.canonicalType) {
            is CanonicalLangType.ObjectCanonicalLangType -> canonical.fields
            is CanonicalLangType.SignalCanonicalLangType -> canonical.fields
        }
        val fieldIndex = fields.indexOfFirst { it.name == expression.memberIdentifier.text }

        chunk.writeInstruction(Instruction.GetMember(fieldIndex))
    }
}
