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

        inline fun <reified T : NodeTag> getTagsOfType(breadcrumbs: Breadcrumbs): List<T> =
            getTags(breadcrumbs).mapNotNull { it as? T }

        fun hasTag(breadcrumbs: Breadcrumbs, vararg types: KClass<out NodeTag>): Boolean {
            return getTags(breadcrumbs).any { tag -> types.any { it.isInstance(tag) } }
        }

        fun nextScopeIndex(): Int {
            return scopeStack.sumOf { it.size() }
        }
    }

    private data class OpenLoop(
        val breadcrumbs: Breadcrumbs
    )

    private class CompilerScope(
        val scopeRoot: Breadcrumbs,
        val type: ScopeType,
        val openLoop: OpenLoop? = null,
        val stackPrefix: Int = 0,
        var effectCount: Int = 0
    ) {
        enum class ScopeType {
            BODY, FUNCTION, EFFECT
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
                    if (objectType is ConstraintValueLangType && objectType.valueType is InstanceValueLangType) {
                        types[objectType.valueType.canonicalType.id] = objectType.valueType.canonicalType
                        exports[declaration.name.text] =
                            CompiledFile.CompiledExport.Constraint(objectType.asConstraintReference())
                    } else if (error != null) {
                        exports[declaration.name.text] = CompiledFile.CompiledExport.Error(error)
                    } else {
                        error("Object declaration resolved to: $objectType")
                    }
                }

                is DeclarationNode.SignalType -> {
                    val signalType = resolved.getExpectedType(declaration.info.breadcrumbs)

                    val error = signalType.getRuntimeError()
                    if (signalType is ConstraintValueLangType && signalType.valueType is InstanceValueLangType) {
                        types[signalType.valueType.canonicalType.id] = signalType.valueType.canonicalType
                        exports[declaration.name.text] =
                            CompiledFile.CompiledExport.Constraint(signalType.asConstraintReference())
                    } else if (error != null) {
                        exports[declaration.name.text] = CompiledFile.CompiledExport.Error(error)
                    } else {
                        error("Signal declaration resolved to: $signalType")
                    }
                }

                is DeclarationNode.OptionType -> {
                    val optionType = resolved.getExpectedType(declaration.info.breadcrumbs)
                    val error = optionType.getRuntimeError()
                    if (optionType is ConstraintValueLangType && optionType.valueType is OptionValueLangType) {
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

        return CompiledFile(resolved.path, types, ctx.chunks, exports, resolved.debugContext())
    }

    private fun compileFunction(
        declaration: DeclarationNode.Function, ctx: CompilerContext
    ): CompiledFile.MutableInstructionChunk {
        val chunk = CompiledFile.MutableInstructionChunk()
        val functionScope = CompilerScope(declaration.info.breadcrumbs, CompilerScope.ScopeType.FUNCTION)
        val oldScopeStack = ctx.scopeStack.toList()
        ctx.scopeStack.clear() // brand-new scope for every function
        ctx.scopeStack.addLast(functionScope)

        for ((i, param) in declaration.params.withIndex()) {
            functionScope.namedValueIndices[param.info.breadcrumbs] = i
        }

        for ((i, captured) in ctx.getTagsOfType<NodeTag.CapturesValue>(declaration.info.breadcrumbs).withIndex()) {
            functionScope.namedValueIndices[captured.value] = i
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

            is BodyNode.SingleStatementBodyNode -> {
                compileStatement(body.statement, chunk, ctx, isLastStatement = true)
            }
        }
    }

    private fun compileBlock(
        block: BodyNode.BlockBodyNode,
        chunk: CompiledFile.MutableInstructionChunk,
        ctx: CompilerContext,
    ) {
        ctx.scopeStack.addLast(CompilerScope(block.info.breadcrumbs, CompilerScope.ScopeType.BODY))

        if (block.statements.isEmpty()) {
            chunk.writeInstruction(Instruction.PushAction)
        }

        for ((i, statement) in block.statements.withIndex()) {
            compileStatement(
                statement, chunk, ctx, isLastStatement = i == block.statements.lastIndex
            )
            if (ctx.resolved.getInferredType(statement.info.breadcrumbs) is NeverContinuesValueLangType) {
                ctx.scopeStack.removeLast()
                return
            }
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

            is StatementNode.SetStatement -> {
                compileSetStatement(statement, chunk, ctx)

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
            is DeclarationNode.Function -> {
                val capturedValues = ctx.getTagsOfType<NodeTag.CapturesValue>(declaration.info.breadcrumbs)
                for (captured in capturedValues) {
                    compileValueReference(captured.value, chunk, ctx)
                }

                val newChunk = compileFunction(declaration, ctx)

                ctx.resolved.checkForRuntimeErrors(declaration.info.breadcrumbs)?.let { error ->
                    compileBadValue(declaration, error, chunk, ctx)
                } ?: run {
                    ctx.chunks.add(newChunk.toInstructionChunk())
                    chunk.writeInstruction(
                        Instruction.DefineFunction(
                            chunkIndex = ctx.chunks.lastIndex, typeConstant = chunk.addConstant(
                                CompiledFile.CompiledConstant.TypeConst(
                                    ctx.resolved.getExpectedType(declaration.info.breadcrumbs)
                                )
                            ), capturedValues = capturedValues.size
                        )
                    )
                }
                ctx.scopeStack.last().namedValueIndices[declaration.info.breadcrumbs] = ctx.nextScopeIndex()
            }

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
                val rejectSignal = effectChunk.writeJumpIfFalsePlaceholder()
                compileBody(statement.body, effectChunk, ctx)
                effectChunk.writeInstruction(Instruction.FinishEffect)
                rejectSignal.fill(effectChunk.instructions.size)
            }
        }

        effectChunk.writeInstruction(Instruction.RejectSignal)

        ctx.scopeStack.removeLast()

        ctx.chunks.add(effectChunk.toInstructionChunk())
        chunk.writeInstruction(Instruction.RegisterEffect(ctx.chunks.lastIndex))
    }

    private fun compileSetStatement(
        statement: StatementNode.SetStatement, chunk: CompiledFile.MutableInstructionChunk, ctx: CompilerContext
    ) {
        compileExpression(statement.expression, chunk, ctx)

        ctx.resolved.checkForRuntimeErrors(statement.info.breadcrumbs)?.let { error ->
            chunk.writeInstruction(Instruction.Pop())
            compileBadValue(statement, error, chunk, ctx)

            when (error) {
                // these errors are recoverable
                is ErrorLangType.MismatchedType -> {}
                // others, like NotVariable... not so much
                else -> compileTypeErrorFromStackBadValue(chunk)
            }
            return
        }

        val tag = ctx.getTag<NodeTag.SetsVariable>(statement.info.breadcrumbs)!!
        val valueReference = findValueReference(tag.variable, ctx)
        if (valueReference.effectDepth > 0) {
            chunk.writeInstruction(
                Instruction.WriteLocalThroughEffectScope(
                    valueReference.effectDepth, valueReference.foundIndex
                )
            )
        } else {
            chunk.writeInstruction(
                Instruction.WriteLocal(valueReference.foundIndex)
            )
        }
    }

    private fun compileExpression(
        expression: ExpressionNode, chunk: CompiledFile.MutableInstructionChunk, ctx: CompilerContext
    ) {
        when (expression) {
            is ExpressionNode.BlockExpressionNode -> compileBlockExpression(expression, chunk, ctx)

            is ExpressionNode.BranchExpressionNode -> compileBranchExpression(expression, chunk, ctx)
            is ExpressionNode.LoopExpressionNode -> compileLoopExpression(expression, chunk, ctx)
            is ExpressionNode.CauseExpression -> compileCauseExpression(expression, chunk, ctx)
            is ExpressionNode.ReturnExpression -> compileReturnExpression(expression, chunk, ctx)
            is ExpressionNode.BreakExpression -> compileBreakExpression(expression, chunk, ctx)

            is ExpressionNode.CallExpression -> compileCallExpression(expression, chunk, ctx)
            is ExpressionNode.MemberExpression -> compileMemberExpression(expression, chunk, ctx)

            is ExpressionNode.IdentifierExpression -> compileIdentifierExpression(expression, chunk, ctx)
            is ExpressionNode.StringLiteralExpression -> chunk.writeLiteral(
                CompiledFile.CompiledConstant.StringConst(
                    expression.text
                )
            )

            is ExpressionNode.NumberLiteralExpression -> chunk.writeLiteral(
                CompiledFile.CompiledConstant.NumberConst(
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

    private fun compileIdentifierExpression(
        expression: ExpressionNode, chunk: CompiledFile.MutableInstructionChunk, ctx: CompilerContext
    ) {
        compileValueFlowReference(expression, chunk, ctx)

        ctx.resolved.getInferredType(expression.info.breadcrumbs).let {
            if (it is ActionValueLangType) {
                // special case: Action type references are automatically
                // converted to Action values so that you can use `Action`
                // as a keyword
                chunk.writeInstruction(Instruction.Pop(1))
                chunk.writeInstruction(Instruction.PushAction)
            }
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
        ctx.scopeStack.addLast(CompilerScope(expression.info.breadcrumbs, CompilerScope.ScopeType.BODY))
        val withValueIndex = expression.withValue?.let {
            compileExpression(it, chunk, ctx)
            val index = ctx.nextScopeIndex()
            ctx.scopeStack.last().namedValueIndices[it.info.breadcrumbs] = index
            index
        }

        val remainingBranchJumps = mutableListOf<CompiledFile.MutableInstructionChunk.JumpPlaceholder>()
        for (branch in expression.branches) {
            when (branch) {
                is BranchOptionNode.IfBranchOptionNode -> {
                    compileExpression(branch.condition, chunk, ctx)
                    val skipBodyInstruction = chunk.writeJumpIfFalsePlaceholder()
                    compileBody(branch.body, chunk, ctx)
                    remainingBranchJumps.add(chunk.writeJumpPlaceholder())
                    skipBodyInstruction.fill(chunk.instructions.size)
                }

                is BranchOptionNode.IsBranchOptionNode -> {
                    if (withValueIndex != null) {
                        chunk.writeInstruction(Instruction.ReadLocal(withValueIndex))
                        compileValueFlowReference(branch.pattern.typeReference, chunk, ctx)
                        chunk.writeInstruction(Instruction.IsAssignableTo)
                        val skipBodyInstruction = chunk.writeJumpIfFalsePlaceholder()

                        ctx.scopeStack.addLast(CompilerScope(expression.info.breadcrumbs, CompilerScope.ScopeType.BODY))
                        chunk.writeInstruction(Instruction.ReadLocal(withValueIndex))
                        ctx.scopeStack.last().namedValueIndices[branch.pattern.info.breadcrumbs] = ctx.nextScopeIndex()

                        compileBody(branch.body, chunk, ctx)

                        chunk.writeInstruction(Instruction.PopScope(ctx.scopeStack.last().size()))
                        ctx.scopeStack.removeLast()
                        remainingBranchJumps.add(chunk.writeJumpPlaceholder())

                        skipBodyInstruction.fill()
                    }
                }

                is BranchOptionNode.ElseBranchOptionNode -> compileBody(branch.body, chunk, ctx)
            }
        }
        val elseBranch = expression.branches.firstNotNullOfOrNull { it as? BranchOptionNode.ElseBranchOptionNode }
        if (elseBranch == null) {
            val returnType = ctx.resolved.getInferredType(expression.info.breadcrumbs)
            val error = returnType.getRuntimeError() ?: ErrorLangType.MissingElseBranch(
                null
            )
            chunk.writeLiteral(
                CompiledFile.CompiledConstant.ErrorConst(
                    SourcePosition.Source(
                        ctx.resolved.path, expression.info.breadcrumbs, expression.info.position
                    ), error
                )
            )

            // If we're supposed to return an Action or NeverContinues, then this should be an immediate error
            // because the BadValue has nowhere to go
            val returnOptions = returnType as? OptionValueLangType
            if (returnOptions?.let { returnOptionsNotNull ->
                    returnOptionsNotNull.options.asSequence().map { it.asValueType() }
                        .all { it is ValueLangType.Pending || it is ErrorLangType || it is ActionValueLangType || it is NeverContinuesValueLangType }
                } == true) {
                compileTypeErrorFromStackBadValue(chunk)
            }
        }

        for (jump in remainingBranchJumps) {
            jump.fill(chunk.instructions.size)
        }

        chunk.writeInstruction(Instruction.PopScope(ctx.scopeStack.last().size()))
        ctx.scopeStack.removeLast()

        (ctx.resolved.checkForRuntimeErrors(expression.info.breadcrumbs) as? ErrorLangType.ActionIncompatibleWithValueTypes)?.let {
            // This specific type of error should cause an immediate failure,
            // because you might have expected a side effect but gotten a value instead
            chunk.writeLiteral(
                CompiledFile.CompiledConstant.ErrorConst(
                    SourcePosition.Source(ctx.resolved.path, expression.info.breadcrumbs, expression.info.position), it
                )
            )
            compileTypeErrorFromStackBadValue(chunk)
        }
    }

    private fun compileLoopExpression(
        expression: ExpressionNode.LoopExpressionNode, chunk: CompiledFile.MutableInstructionChunk, ctx: CompilerContext
    ) {
        val startLoopPlaceholder = chunk.writeStartLoopPlaceholder()
        val openLoop = OpenLoop(expression.info.breadcrumbs)
        ctx.scopeStack.addLast(CompilerScope(expression.info.breadcrumbs, CompilerScope.ScopeType.BODY, openLoop))
        compileBody(expression.body, chunk, ctx)
        ctx.scopeStack.removeLast()
        chunk.writeInstruction(Instruction.ContinueLoop)
        startLoopPlaceholder.fill()
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
                NodeTag.DeclarationForScope::class,
            )
        ) {
            compileValueReference(comesFrom.source, chunk, ctx)
            return
        }


        error("Wasn't able to resolve identifier to anything")
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
        source: Breadcrumbs,
        chunk: CompiledFile.MutableInstructionChunk,
        ctx: CompilerContext,
    ) {
        val valueReference = findValueReference(source, ctx)
        chunk.writeInstruction(
            if (valueReference.effectDepth > 0) {
                Instruction.ReadLocalThroughEffectScope(valueReference.effectDepth, valueReference.foundIndex)
            } else {
                Instruction.ReadLocal(valueReference.foundIndex)
            }
        )
    }


    data class ValueReferenceResult(val foundIndex: Int, val effectDepth: Int)

    private fun findValueReference(source: Breadcrumbs, ctx: CompilerContext): ValueReferenceResult {
        var effectDepth = 0
        for (scope in ctx.scopeStack.reversed()) {
            val foundIndex = scope.namedValueIndices[source]
            if (foundIndex != null) {
                return ValueReferenceResult(foundIndex, effectDepth)
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
            compileTypeErrorFromStackBadValue(chunk)
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

        val errorPreventingCall = run {
            val error = ctx.resolved.checkForRuntimeErrors(expression.info.breadcrumbs)
            if (error is ErrorLangType.NotCallable) {
                return@run error
            }
            val calleeType = ctx.resolved.getInferredType(expression.callee.info.breadcrumbs)
            if (calleeType is ConstraintValueLangType || calleeType.getRuntimeError() != null) {
                return@run error
            }

            null
        }

        errorPreventingCall?.let {
            // Don't call; pop all the arguments and the callee off the stack and then push an error
            chunk.writeInstruction(Instruction.Pop(expression.parameters.size + 1))
            compileBadValue(expression, errorPreventingCall, chunk, ctx)
            return
        }

        when (val calleeType = ctx.resolved.getExpectedType(expression.callee.info.breadcrumbs)) {
            is ConstraintValueLangType -> {
                when (calleeType.valueType) {
                    is InstanceValueLangType -> {
                        if (calleeType.valueType.canonicalType.isUnique()) {
                            // ignore this, unique objects don't need to be constructed.
                            // Kiiind of abusing PopScope here to keep the value in place while popping
                            // any erroneous params
                            chunk.writeInstruction(Instruction.PopScope(expression.parameters.size))
                        } else {
                            when (calleeType.valueType.canonicalType) {
                                is CanonicalLangType.SignalCanonicalLangType -> chunk.writeInstruction(
                                    Instruction.Construct(
                                        arity = expression.parameters.size
                                    )
                                )

                                is CanonicalLangType.ObjectCanonicalLangType -> chunk.writeInstruction(
                                    Instruction.Construct(
                                        arity = expression.parameters.size
                                    )
                                )
                            }
                        }
                    }

                    else -> error("Can't construct a $calleeType")
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

    private fun compileReturnExpression(
        expression: ExpressionNode.ReturnExpression, chunk: CompiledFile.MutableInstructionChunk, ctx: CompilerContext
    ) {
        if (expression.value != null) {
            compileExpression(expression.value, chunk, ctx)
        } else {
            chunk.writeInstruction(Instruction.PushAction)
        }

        chunk.writeInstruction(Instruction.Return)
    }

    private fun compileBreakExpression(
        expression: ExpressionNode.BreakExpression, chunk: CompiledFile.MutableInstructionChunk, ctx: CompilerContext
    ) {
        if (expression.withValue != null) {
            compileExpression(expression.withValue, chunk, ctx)
        } else {
            chunk.writeInstruction(Instruction.PushAction)
        }

        ctx.resolved.checkForRuntimeErrors(expression.info.breadcrumbs)?.let { error ->
            chunk.writeInstruction(Instruction.Pop())
            chunk.writeLiteral(
                CompiledFile.CompiledConstant.ErrorConst(
                    SourcePosition.Source(
                        ctx.resolved.path, expression.info.breadcrumbs, expression.info.position
                    ), error
                )
            )
            compileTypeErrorFromStackBadValue(chunk)
            return
        }

        val breakTag = ctx.getTag<NodeTag.BreaksLoop>(expression.info.breadcrumbs)!!
        val loopIndex =
            ctx.scopeStack.reversed().filter { it.openLoop != null }.withIndex().firstNotNullOf { (i, scope) ->
                    if (scope.scopeRoot == breakTag.loop) i else null
                }

        chunk.writeInstruction(Instruction.BreakLoop(loopIndex + 1))
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

    private fun compileTypeErrorFromStackBadValue(chunk: CompiledFile.MutableInstructionChunk) {
        chunk.writeInstruction(
            Instruction.Import(
                filePathConstant = chunk.addConstant(CompiledFile.CompiledConstant.StringConst("core/builtin.cau")),
                exportNameConstant = chunk.addConstant(CompiledFile.CompiledConstant.StringConst("TypeError")),
            )
        )
        chunk.writeInstruction(Instruction.Construct(1))
        chunk.writeInstruction(Instruction.Cause)
    }
}

