package com.dallonf.ktcause

import com.dallonf.ktcause.CompiledFile.Procedure.InstructionPhase
import com.dallonf.ktcause.CompiledFile.Procedure.ProcedureIdentity
import com.dallonf.ktcause.ast.*
import com.dallonf.ktcause.types.*
import org.apache.commons.numbers.fraction.BigFraction
import kotlin.math.exp
import kotlin.reflect.KClass

object Compiler {
    private data class CompilerContext(
        val fileNode: FileNode,
        val analyzed: AnalyzedNode,
        val resolved: ResolvedFile,
        val procedures: MutableList<CompiledFile.Procedure>,
        val scopeStack: ArrayDeque<CompilerScope> = ArrayDeque(),
    ) {
        fun getTags(breadcrumbs: Breadcrumbs): List<NodeTag> = analyzed.nodeTags[breadcrumbs] ?: emptyList()

        inline fun <reified T : NodeTag> getTag(breadcrumbs: Breadcrumbs): T? =
            getTags(breadcrumbs).firstNotNullOfOrNull { it as? T }

        inline fun <reified T : NodeTag> getTagsOfType(breadcrumbs: Breadcrumbs): List<T> =
            getTags(breadcrumbs).mapNotNull { it as? T }

        fun hasTag(breadcrumbs: Breadcrumbs, vararg types: KClass<out NodeTag>): Boolean {
            return getTags(breadcrumbs).any { tag -> types.any { it.isInstance(tag) } }
        }

        fun nextScopeIndex(): Int {
            var index = 0
            for (scope in scopeStack.reversed()) {
                index += scope.size()
                if (scope.type != CompilerScope.ScopeType.BODY) break
            }
            return index
        }

        fun addToScope(breadcrumbs: Breadcrumbs): Int {
            val index = nextScopeIndex()
            val currentScope = scopeStack.last().namedValueIndices
            if (currentScope.containsKey(breadcrumbs)) {
                throw AssertionError("$breadcrumbs already exists on scope!")
            }
            currentScope[breadcrumbs] = index
            return index
        }
    }

    private data class OpenLoop(
        val breadcrumbs: Breadcrumbs
    )

    private class CompilerScope(
        val scopeRoot: Breadcrumbs, val type: ScopeType, val openLoop: OpenLoop? = null, var effectCount: Int = 0
    ) {
        enum class ScopeType {
            BODY, FUNCTION, EFFECT
        }

        // indices are computed from the top of the current call frame
        val namedValueIndices = mutableMapOf<Breadcrumbs, Int>()

        fun size() = namedValueIndices.size
    }

    fun compile(fileNode: FileNode, analyzed: AnalyzedNode, resolved: ResolvedFile): CompiledFile {
        val types = mutableMapOf<CanonicalLangTypeId, CanonicalLangType>()
        val exports = mutableMapOf<String, CompiledFile.CompiledExport>()
        val ctx = CompilerContext(fileNode, analyzed, resolved, procedures = mutableListOf())

        for (declaration in fileNode.declarations) {
            when (declaration) {
                is DeclarationNode.Import -> {}
                is DeclarationNode.Function -> {
                    val procedure = compileFunctionDeclaration(declaration, ctx)
                    val functionType = resolved.getExpectedType(declaration.info.breadcrumbs)

                    ctx.procedures.add(procedure.toProcedure())
                    exports[declaration.name.text] =
                        CompiledFile.CompiledExport.Function(ctx.procedures.lastIndex, functionType)
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

        return CompiledFile(resolved.path, types, ctx.procedures, exports, resolved.debugContext())
    }

    private fun compileFunctionDeclaration(
        declaration: DeclarationNode.Function, ctx: CompilerContext
    ): CompiledFile.MutableProcedure {
        return compileFunction(
            declaration.name.text, declaration.params, declaration.info, ctx
        ) { procedure ->
            compileBody(declaration.body, procedure, ctx)
            ctx.resolved.checkForRuntimeErrors(declaration.body.info.breadcrumbs)?.let {
                procedure.writeInstruction(Instruction.Pop(), declaration.body.info, InstructionPhase.CLEANUP)
                compileBadValue(declaration.body, it, procedure, ctx)
            }
        }
    }

    private fun compileFunction(
        name: String?,
        params: List<FunctionSignatureParameterNode>,
        nodeInfo: NodeInfo,
        ctx: CompilerContext,
        compileBody: (CompiledFile.MutableProcedure) -> Unit
    ): CompiledFile.MutableProcedure {
        val procedure = CompiledFile.MutableProcedure(ProcedureIdentity.Function(name, nodeInfo))
        val functionScope = CompilerScope(nodeInfo.breadcrumbs, CompilerScope.ScopeType.FUNCTION)
        val oldScopeStack = ctx.scopeStack.toList()
        ctx.scopeStack.clear() // brand-new scope for every function
        ctx.scopeStack.addLast(functionScope)

        ctx.addToScope(nodeInfo.breadcrumbs) // the function itself is on the stack
        for (param in params) {
            ctx.addToScope(param.info.breadcrumbs)
            procedure.writeInstruction(
                Instruction.NameValue(
                    procedure.addConstant(param.name.text),
                    variable = false,
                    localIndex = ctx.scopeStack.last().size() - 1
                ), param.info
            )
        }
        for (captured in ctx.getTagsOfType<NodeTag.FunctionCapturesValue>(nodeInfo.breadcrumbs)) {
            ctx.addToScope(captured.value)
        }


        compileBody(procedure)
        assert(ctx.scopeStack.last() == functionScope)

        procedure.writeInstruction(Instruction.Return, nodeInfo, InstructionPhase.CLEANUP)

        ctx.scopeStack.clear()
        ctx.scopeStack.addAll(oldScopeStack)
        return procedure
    }

    private fun compileBody(
        body: BodyNode, procedure: CompiledFile.MutableProcedure, ctx: CompilerContext
    ) {
        when (body) {
            is BodyNode.BlockBodyNode -> {
                compileBlock(body, procedure, ctx)
            }

            is BodyNode.SingleStatementBodyNode -> {
                compileStatement(body.statement, procedure, ctx, isLastStatement = true)
            }
        }
    }

    private fun compileBlock(
        block: BodyNode.BlockBodyNode,
        procedure: CompiledFile.MutableProcedure,
        ctx: CompilerContext,
    ) {
        ctx.scopeStack.addLast(CompilerScope(block.info.breadcrumbs, CompilerScope.ScopeType.BODY))

        if (block.statements.isEmpty()) {
            procedure.writeInstruction(Instruction.PushAction, block.info)
        }

        for ((i, statement) in block.statements.withIndex()) {
            compileStatement(
                statement, procedure, ctx, isLastStatement = i == block.statements.lastIndex
            )
            if (ctx.resolved.getInferredType(statement.info.breadcrumbs) is NeverContinuesValueLangType) {
                ctx.scopeStack.removeLast()
                return
            }
        }

        val scope = ctx.scopeStack.removeLast()
        procedure.writeInstruction(Instruction.PopEffects(scope.effectCount), block.info, InstructionPhase.CLEANUP)
        procedure.writeInstruction(Instruction.PopScope(scope.size()), block.info, InstructionPhase.CLEANUP)
    }

    private fun compileBadValue(
        node: AstNode, error: ErrorLangType, procedure: CompiledFile.MutableProcedure, ctx: CompilerContext
    ) {
        procedure.writeInstruction(
            Instruction.Literal(makeErrorConst(error, node, procedure, ctx)),
            node.info,
        )
    }

    private fun makeErrorConst(
        error: ErrorLangType,
        node: AstNode,
        procedure: CompiledFile.MutableProcedure,
        ctx: CompilerContext,
    ) = procedure.addConstant(
        CompiledFile.CompiledConstant.ErrorConst(
            SourcePosition.Source(
                ctx.resolved.path, node.info.breadcrumbs, node.info.position
            ),
            error,
        )
    )

    private fun compileStatement(
        statement: StatementNode,
        procedure: CompiledFile.MutableProcedure,
        ctx: CompilerContext,
        isLastStatement: Boolean
    ) {
        when (statement) {
            is StatementNode.ExpressionStatement -> {
                compileExpression(statement.expression, procedure, ctx)

                if (!isLastStatement) {
                    procedure.writeInstruction(Instruction.Pop(), statement.info, InstructionPhase.CLEANUP)
                }
            }

            is StatementNode.DeclarationStatement -> {
                compileLocalDeclaration(statement, procedure, ctx)

                if (isLastStatement) {
                    procedure.writeInstruction(Instruction.PushAction, statement.info, InstructionPhase.CLEANUP)
                }
            }

            is StatementNode.EffectStatement -> {
                compileEffectStatement(statement, procedure, ctx)

                if (isLastStatement) {
                    procedure.writeInstruction(Instruction.PushAction, statement.info, InstructionPhase.CLEANUP)
                }
            }

            is StatementNode.SetStatement -> {
                compileSetStatement(statement, procedure, ctx)

                if (isLastStatement) {
                    procedure.writeInstruction(Instruction.PushAction, statement.info, InstructionPhase.CLEANUP)
                }
            }
        }
    }

    private fun compileLocalDeclaration(
        statement: StatementNode.DeclarationStatement, procedure: CompiledFile.MutableProcedure, ctx: CompilerContext
    ) {
        when (val declaration = statement.declaration) {
            is DeclarationNode.Import -> {}
            is DeclarationNode.ObjectType, is DeclarationNode.SignalType, is DeclarationNode.OptionType -> {
                val type = ctx.resolved.getInferredType(declaration.info.breadcrumbs)
                type.getRuntimeError().let {
                    if (it != null) {
                        compileBadValue(declaration, it, procedure, ctx)
                    } else {
                        procedure.writeLiteral(
                            CompiledFile.CompiledConstant.TypeConst(
                                (type as ConstraintValueLangType).valueType
                            ), declaration.info
                        )
                    }
                }
                val name = when (declaration) {
                    is DeclarationNode.ObjectType -> declaration.name.text
                    is DeclarationNode.OptionType -> declaration.name.text
                    is DeclarationNode.SignalType -> declaration.name.text
                    else -> throw AssertionError()
                }
                ctx.addToScope(declaration.info.breadcrumbs)
                procedure.writeInstruction(Instruction.NameValue(procedure.addConstant(name)), declaration.info)
            }

            is DeclarationNode.Function -> {
                val capturedValues = ctx.getTagsOfType<NodeTag.FunctionCapturesValue>(declaration.info.breadcrumbs)
                for (captured in capturedValues) {
                    compileValueReference(declaration.info, captured.value, procedure, ctx)
                }

                val newProcedure = compileFunctionDeclaration(declaration, ctx)

                ctx.resolved.checkForRuntimeErrors(declaration.info.breadcrumbs)?.let { error ->
                    compileBadValue(declaration, error, procedure, ctx)
                } ?: run {
                    ctx.procedures.add(newProcedure.toProcedure())
                    procedure.writeInstruction(
                        Instruction.DefineFunction(
                            procedureIndex = ctx.procedures.lastIndex, typeConstant = procedure.addConstant(
                                CompiledFile.CompiledConstant.TypeConst(
                                    ctx.resolved.getExpectedType(declaration.info.breadcrumbs) as ResolvedValueLangType
                                )
                            ), capturedValues = capturedValues.size
                        ), declaration.info
                    )
                }
                ctx.addToScope(declaration.info.breadcrumbs)
                procedure.writeInstruction(
                    Instruction.NameValue(procedure.addConstant(declaration.name.text)), declaration.info
                )
            }

            is DeclarationNode.NamedValue -> {
                compileExpression(declaration.value, procedure, ctx)
                ctx.resolved.checkForRuntimeErrors(declaration.info.breadcrumbs)?.let { error ->
                    procedure.writeInstruction(Instruction.Pop(), declaration.info)
                    compileBadValue(declaration, error, procedure, ctx)
                }
                ctx.addToScope(declaration.info.breadcrumbs)
                procedure.writeInstruction(
                    Instruction.NameValue(
                        procedure.addConstant(declaration.name.text), declaration.isVariable
                    ), declaration.info
                )
            }
        }
    }

    private fun compileEffectStatement(
        statement: StatementNode.EffectStatement, procedure: CompiledFile.MutableProcedure, ctx: CompilerContext
    ) {
        val matchingType =
            ctx.resolved.getInferredType(statement.pattern.typeReference.info.breadcrumbs).asConstraintReference()
        val effectProcedure = CompiledFile.MutableProcedure(ProcedureIdentity.Effect(matchingType, statement.info))
        ctx.scopeStack.last().effectCount += 1

        ctx.scopeStack.addLast(
            CompilerScope(statement.info.breadcrumbs, CompilerScope.ScopeType.EFFECT)
        )
        ctx.addToScope(statement.pattern.info.breadcrumbs)
        statement.pattern.name?.text?.let {
            effectProcedure.writeInstruction(
                Instruction.NameValue(
                    effectProcedure.addConstant(
                        it
                    )
                ), statement.pattern.info
            )
        }

        // Check the condition
        ctx.resolved.checkForRuntimeErrors(statement.pattern.typeReference.info.breadcrumbs).let { error ->
            if (error == null) {
                effectProcedure.writeInstruction(Instruction.ReadLocal(0), statement.info)
                compileValueFlowReference(statement.pattern.typeReference, effectProcedure, ctx)
                effectProcedure.writeInstruction(Instruction.IsAssignableTo, statement.info)
                val rejectSignal = effectProcedure.writeJumpIfFalsePlaceholder(statement.info)
                compileBody(statement.body, effectProcedure, ctx)
                effectProcedure.writeInstruction(Instruction.FinishEffect, statement.info, InstructionPhase.CLEANUP)
                rejectSignal.fill(effectProcedure.instructions.size)
            }
        }

        effectProcedure.writeInstruction(Instruction.RejectSignal, statement.info, InstructionPhase.CLEANUP)

        ctx.scopeStack.removeLast()

        ctx.procedures.add(effectProcedure.toProcedure())
        procedure.writeInstruction(Instruction.RegisterEffect(ctx.procedures.lastIndex), statement.info)
    }

    private fun compileSetStatement(
        statement: StatementNode.SetStatement, procedure: CompiledFile.MutableProcedure, ctx: CompilerContext
    ) {
        compileExpression(statement.expression, procedure, ctx)

        ctx.resolved.checkForRuntimeErrors(statement.info.breadcrumbs)?.let { error ->
            procedure.writeInstruction(Instruction.Pop(), statement.info)
            val errorConst = makeErrorConst(error, statement, procedure, ctx)

            when (error) {
                // these errors are recoverable
                is ErrorLangType.MismatchedType -> {
                    procedure.writeInstruction(Instruction.Literal(errorConst), nodeInfo = statement.info)
                }
                // others, like NotVariable... not so much
                else -> compileTypeError(errorConst, procedure)
            }
            return
        }

        val tag = ctx.getTag<NodeTag.SetsVariable>(statement.info.breadcrumbs)!!
        val valueReference = findValueReference(tag.variable, ctx)
        if (valueReference.effectDepth > 0) {
            procedure.writeInstruction(
                Instruction.WriteLocalThroughEffectScope(
                    valueReference.effectDepth, valueReference.foundIndex
                ), statement.info
            )
        } else {
            procedure.writeInstruction(
                Instruction.WriteLocal(valueReference.foundIndex), statement.info
            )
        }
    }

    private fun compileExpression(
        expression: ExpressionNode, procedure: CompiledFile.MutableProcedure, ctx: CompilerContext
    ) {
        when (expression) {
            is ExpressionNode.GroupExpressionNode -> compileExpression(expression.expression, procedure, ctx)

            is ExpressionNode.BlockExpressionNode -> compileBlockExpression(expression, procedure, ctx)

            is ExpressionNode.FunctionExpressionNode -> compileFunctionExpression(expression, procedure, ctx)

            is ExpressionNode.BranchExpressionNode -> compileBranchExpression(expression, procedure, ctx)
            is ExpressionNode.LoopExpressionNode -> compileLoopExpression(expression, procedure, ctx)
            is ExpressionNode.CauseExpression -> compileCauseExpression(expression, procedure, ctx)
            is ExpressionNode.ReturnExpression -> compileReturnExpression(expression, procedure, ctx)
            is ExpressionNode.BreakExpression -> compileBreakExpression(expression, procedure, ctx)

            is ExpressionNode.CallExpression -> compileCallExpression(expression, procedure, ctx)
            is ExpressionNode.MemberExpression -> compileMemberExpression(expression, procedure, ctx)
            is ExpressionNode.PipeCallExpression -> compilePipeCallExpression(expression, procedure, ctx)

            is ExpressionNode.IdentifierExpression -> compileIdentifierExpression(expression, procedure, ctx)
            is ExpressionNode.StringLiteralExpression -> procedure.writeLiteral(
                CompiledFile.CompiledConstant.StringConst(
                    expression.text
                ), expression.info
            )

            is ExpressionNode.NumberLiteralExpression -> {
                val numerator = expression.value.unscaledValue()
                val denominator = 10.toBigInteger().pow(expression.value.scale())
                val fraction = BigFraction.of(numerator, denominator)
                procedure.writeLiteral(
                    CompiledFile.CompiledConstant.NumberConst(
                        fraction
                    ), expression.info
                )
            }
        }

        // TODO: this can be redundant since sometimes there's already a BadValue on the stack
        ctx.resolved.checkForRuntimeErrors(expression.info.breadcrumbs)?.let { error ->
            procedure.writeInstruction(Instruction.Pop(), expression.info)
            compileBadValue(expression, error, procedure, ctx)
        }
    }

    private fun compileIdentifierExpression(
        expression: ExpressionNode, procedure: CompiledFile.MutableProcedure, ctx: CompilerContext
    ) {
        compileValueFlowReference(expression, procedure, ctx)

        ctx.resolved.getInferredType(expression.info.breadcrumbs).let {
            if (it is ActionValueLangType) {
                // special case: Action type references are automatically
                // converted to Action values so that you can use `Action`
                // as a keyword
                procedure.writeInstruction(Instruction.Pop(1), expression.info)
                procedure.writeInstruction(Instruction.PushAction, expression.info)
            }
        }
    }

    private fun compileBlockExpression(
        expression: ExpressionNode.BlockExpressionNode, procedure: CompiledFile.MutableProcedure, ctx: CompilerContext
    ) {
        compileBlock(expression.block, procedure, ctx)
    }

    private fun compileFunctionExpression(
        expression: ExpressionNode.FunctionExpressionNode,
        procedure: CompiledFile.MutableProcedure,
        ctx: CompilerContext
    ) {
        val capturedValues = ctx.getTagsOfType<NodeTag.FunctionCapturesValue>(expression.info.breadcrumbs)
        for (captured in capturedValues) {
            compileValueReference(expression.info, captured.value, procedure, ctx)
        }

        val functionProcedure =
            compileFunction(name = null, expression.params, expression.info, ctx) { functionProcedure ->
                compileExpression(expression.body, functionProcedure, ctx)
            }

        ctx.resolved.checkForRuntimeErrors(expression.info.breadcrumbs)?.let { error ->
            compileBadValue(expression, error, procedure, ctx)
        } ?: run {
            ctx.procedures.add(functionProcedure.toProcedure())
            procedure.writeInstruction(
                Instruction.DefineFunction(
                    procedureIndex = ctx.procedures.lastIndex, typeConstant = procedure.addConstant(
                        CompiledFile.CompiledConstant.TypeConst(
                            ctx.resolved.getExpectedType(expression.info.breadcrumbs) as ResolvedValueLangType
                        )
                    ), capturedValues = capturedValues.size
                ), expression.info
            )
        }
    }

    private fun compileBranchExpression(
        expression: ExpressionNode.BranchExpressionNode, procedure: CompiledFile.MutableProcedure, ctx: CompilerContext
    ) {
        ctx.scopeStack.addLast(CompilerScope(expression.info.breadcrumbs, CompilerScope.ScopeType.BODY))
        val withValueIndex = expression.withValue?.let {
            compileExpression(it, procedure, ctx)
            ctx.addToScope(it.info.breadcrumbs)
        }

        val remainingBranchJumps = mutableListOf<CompiledFile.MutableProcedure.JumpPlaceholder>()
        for (branch in expression.branches) {
            when (branch) {
                is BranchOptionNode.IfBranchOptionNode -> {
                    compileExpression(branch.condition, procedure, ctx)
                    val skipBodyInstruction = procedure.writeJumpIfFalsePlaceholder(branch.info)
                    compileBody(branch.body, procedure, ctx)
                    remainingBranchJumps.add(procedure.writeJumpPlaceholder(branch.info, InstructionPhase.CLEANUP))
                    skipBodyInstruction.fill(procedure.instructions.size)
                }

                is BranchOptionNode.IsBranchOptionNode -> {
                    if (withValueIndex != null) {
                        procedure.writeInstruction(Instruction.ReadLocal(withValueIndex), branch.info)
                        compileValueFlowReference(branch.pattern.typeReference, procedure, ctx)
                        procedure.writeInstruction(Instruction.IsAssignableTo, branch.info)
                        val skipBodyInstruction = procedure.writeJumpIfFalsePlaceholder(branch.info)

                        ctx.scopeStack.addLast(CompilerScope(expression.info.breadcrumbs, CompilerScope.ScopeType.BODY))
                        procedure.writeInstruction(Instruction.ReadLocal(withValueIndex), branch.info)
                        ctx.addToScope(branch.pattern.info.breadcrumbs)
                        branch.pattern.name?.text?.let {
                            procedure.writeInstruction(
                                Instruction.NameValue(
                                    procedure.addConstant(
                                        it
                                    )
                                ), branch.pattern.info
                            )
                        }

                        compileBody(branch.body, procedure, ctx)

                        procedure.writeInstruction(
                            Instruction.PopScope(ctx.scopeStack.last().size()), branch.info, InstructionPhase.CLEANUP
                        )
                        ctx.scopeStack.removeLast()
                        remainingBranchJumps.add(procedure.writeJumpPlaceholder(branch.info, InstructionPhase.CLEANUP))

                        skipBodyInstruction.fill()
                    }
                }

                is BranchOptionNode.ElseBranchOptionNode -> compileBody(branch.body, procedure, ctx)
            }
        }
        val elseBranch = expression.branches.firstNotNullOfOrNull { it as? BranchOptionNode.ElseBranchOptionNode }
        if (elseBranch == null) {
            val returnType = ctx.resolved.getInferredType(expression.info.breadcrumbs)
            val error = returnType.getRuntimeError() ?: ErrorLangType.MissingElseBranch(
                null
            )
            val errorConst = makeErrorConst(error, expression, procedure, ctx)

            // If we're supposed to return an Action or NeverContinues, then this should be an immediate error
            // because the BadValue has nowhere to go
            val returnOptions = returnType as? OptionValueLangType
            if (returnOptions?.let { returnOptionsNotNull ->
                    returnOptionsNotNull.options.asSequence().map { it.asValueType() }
                        .all { it is ValueLangType.Pending || it is ErrorLangType || it is ActionValueLangType || it is NeverContinuesValueLangType }
                } == true) {
                compileTypeError(errorConst, procedure)
            } else {
                procedure.writeInstruction(Instruction.Literal(errorConst), expression.info)
            }
        }

        for (jump in remainingBranchJumps) {
            jump.fill(procedure.instructions.size)
        }

        procedure.writeInstruction(
            Instruction.PopScope(ctx.scopeStack.last().size()), expression.info, InstructionPhase.CLEANUP
        )
        ctx.scopeStack.removeLast()

        (ctx.resolved.checkForRuntimeErrors(expression.info.breadcrumbs) as? ErrorLangType.ActionIncompatibleWithValueTypes)?.let {
            // This specific type of error should cause an immediate failure,
            // because you might have expected a side effect but gotten a value instead
            compileTypeError(
                makeErrorConst(it, expression, procedure, ctx), procedure
            )
        }
    }

    private fun compileLoopExpression(
        expression: ExpressionNode.LoopExpressionNode, procedure: CompiledFile.MutableProcedure, ctx: CompilerContext
    ) {
        val startLoopPlaceholder = procedure.writeStartLoopPlaceholder(expression.info)
        val openLoop = OpenLoop(expression.info.breadcrumbs)
        ctx.scopeStack.addLast(CompilerScope(expression.info.breadcrumbs, CompilerScope.ScopeType.BODY, openLoop))
        compileBody(expression.body, procedure, ctx)
        ctx.scopeStack.removeLast()
        procedure.writeInstruction(Instruction.ContinueLoop, expression.info)
        startLoopPlaceholder.fill()
    }

    private fun compileValueFlowReference(
        node: AstNode,
        procedure: CompiledFile.MutableProcedure,
        ctx: CompilerContext,
    ) {
        ctx.resolved.checkForRuntimeErrors(node.info.breadcrumbs)?.let { error ->
            compileBadValue(node, error, procedure, ctx)
            return
        }

        val comesFrom = ctx.getTag<NodeTag.ValueComesFrom>(node.info.breadcrumbs)!!

        ctx.getTag<NodeTag.ReferencesFile>(comesFrom.source)?.let {
            compileFileImportReference(node.info, it, procedure)
            return
        }

        ctx.getTag<NodeTag.TopLevelDeclaration>(comesFrom.source)?.let {
            compileTopLevelReference(node.info, it, procedure, ctx)
            return
        }

        if (ctx.hasTag(
                comesFrom.source,
                NodeTag.DeclarationForScope::class,
            )
        ) {
            compileValueReference(node.info, comesFrom.source, procedure, ctx)
            return
        }


        error("Wasn't able to resolve identifier to anything")
    }

    private fun compileTopLevelReference(
        referenceNodeInfo: NodeInfo,
        comesFrom: NodeTag.TopLevelDeclaration,
        procedure: CompiledFile.MutableProcedure,
        ctx: CompilerContext
    ) {
        procedure.writeInstruction(Instruction.ImportSameFile(procedure.addConstant(comesFrom.name)), referenceNodeInfo)
    }

    private fun compileFileImportReference(
        referenceNodeInfo: NodeInfo, tag: NodeTag.ReferencesFile, procedure: CompiledFile.MutableProcedure
    ) {
        val filePathConstant = procedure.addConstant(CompiledFile.CompiledConstant.StringConst(tag.path))
        val exportNameConstant = tag.exportName?.let {
            procedure.addConstant(CompiledFile.CompiledConstant.StringConst(it))
        }

        if (exportNameConstant == null) {
            TODO("Haven't implemented files as first-class objects yet")
        } else {
            procedure.writeInstruction(Instruction.Import(filePathConstant, exportNameConstant), referenceNodeInfo)
        }
    }

    private fun compileValueReference(
        referenceNodeInfo: NodeInfo,
        source: Breadcrumbs,
        procedure: CompiledFile.MutableProcedure,
        ctx: CompilerContext,
    ) {
        val valueReference = findValueReference(source, ctx)
        procedure.writeInstruction(
            if (valueReference.effectDepth > 0) {
                Instruction.ReadLocalThroughEffectScope(valueReference.effectDepth, valueReference.foundIndex)
            } else {
                Instruction.ReadLocal(valueReference.foundIndex)
            },
            referenceNodeInfo,
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
        expression: ExpressionNode.CauseExpression, procedure: CompiledFile.MutableProcedure, ctx: CompilerContext
    ) {
        compileExpression(expression.signal, procedure, ctx)

        ctx.resolved.checkForRuntimeErrors(expression.info.breadcrumbs)?.let { error ->
            procedure.writeInstruction(Instruction.Pop(), expression.info)
            compileTypeError(
                makeErrorConst(error, expression, procedure, ctx), procedure
            )
            return;
        }

        procedure.writeInstruction(Instruction.Cause, expression.info)
    }

    private fun compileCallExpression(
        expression: ExpressionNode.CallExpression, procedure: CompiledFile.MutableProcedure, ctx: CompilerContext
    ) {
        compileExpression(expression.callee, procedure, ctx)

        for (param in expression.parameters) {
            compileExpression(param.value, procedure, ctx)
            ctx.resolved.checkForRuntimeErrors(param.info.breadcrumbs)?.let { error ->
                procedure.writeInstruction(Instruction.Pop(), expression.info)
                compileBadValue(param, error, procedure, ctx)
            }
        }

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
            // Don't call; pop all the arguments and the callee off the stack
            // and then raise an error
            procedure.writeInstruction(Instruction.Pop(expression.parameters.size + 1), expression.info)
            compileTypeError(makeErrorConst(errorPreventingCall, expression, procedure, ctx), procedure)
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
                            procedure.writeInstruction(
                                Instruction.PopScope(expression.parameters.size), expression.info
                            )
                        } else {
                            when (calleeType.valueType.canonicalType) {
                                is CanonicalLangType.SignalCanonicalLangType -> procedure.writeInstruction(
                                    Instruction.Construct(
                                        arity = expression.parameters.size
                                    ), expression.info
                                )

                                is CanonicalLangType.ObjectCanonicalLangType -> procedure.writeInstruction(
                                    Instruction.Construct(
                                        arity = expression.parameters.size
                                    ), expression.info
                                )
                            }
                        }
                    }

                    is StopgapDictionaryLangType -> {
                        procedure.writeInstruction(
                            Instruction.Construct(
                                arity = expression.parameters.size
                            ), expression.info
                        )
                    }

                    else -> error("Can't construct a $calleeType")
                }
            }

            is FunctionValueLangType -> {
                procedure.writeInstruction(
                    Instruction.CallFunction(arity = expression.parameters.size), nodeInfo = expression.info
                )
            }

            // any other case should have been handled by the resolver as an
            // error on the CallExpression itself, which was checked above
            else -> throw AssertionError()
        }
    }

    private fun compilePipeCallExpression(
        expression: ExpressionNode.PipeCallExpression, procedure: CompiledFile.MutableProcedure, ctx: CompilerContext
    ) {
        // TODO: pretty much all of this is the same as compileCallExpression
        compileExpression(expression.subject, procedure, ctx)
        compileExpression(expression.callee, procedure, ctx)

        procedure.writeInstruction(Instruction.Swap, nodeInfo = expression.info, phase = InstructionPhase.PLUMBING)

        for (param in expression.parameters) {
            compileExpression(param.value, procedure, ctx)
            ctx.resolved.checkForRuntimeErrors(param.info.breadcrumbs)?.let { error ->
                procedure.writeInstruction(Instruction.Pop(), expression.info)
                compileBadValue(param, error, procedure, ctx)
            }
        }

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
            // Don't call; pop all the arguments and the callee off the stack
            // and then raise an error
            procedure.writeInstruction(Instruction.Pop(expression.parameters.size + 1), expression.info)
            compileTypeError(makeErrorConst(errorPreventingCall, expression, procedure, ctx), procedure)
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
                            procedure.writeInstruction(
                                Instruction.PopScope(expression.parameters.size), expression.info
                            )
                        } else {
                            when (calleeType.valueType.canonicalType) {
                                is CanonicalLangType.SignalCanonicalLangType -> procedure.writeInstruction(
                                    Instruction.Construct(
                                        arity = expression.parameters.size
                                    ), expression.info
                                )

                                is CanonicalLangType.ObjectCanonicalLangType -> procedure.writeInstruction(
                                    Instruction.Construct(
                                        arity = expression.parameters.size
                                    ), expression.info
                                )
                            }
                        }
                    }

                    is StopgapDictionaryLangType -> {
                        procedure.writeInstruction(
                            Instruction.Construct(
                                arity = expression.parameters.size
                            ), expression.info
                        )
                    }

                    else -> error("Can't construct a $calleeType")
                }
            }

            is FunctionValueLangType -> {
                procedure.writeInstruction(
                    Instruction.CallFunction(arity = expression.parameters.size + 1), nodeInfo = expression.info
                )
            }

            // any other case should have been handled by the resolver as an
            // error on the CallExpression itself, which was checked above
            else -> throw AssertionError()
        }
    }

    private fun compileReturnExpression(
        expression: ExpressionNode.ReturnExpression, procedure: CompiledFile.MutableProcedure, ctx: CompilerContext
    ) {
        if (expression.value != null) {
            compileExpression(expression.value, procedure, ctx)
        } else {
            procedure.writeInstruction(Instruction.PushAction, expression.info, InstructionPhase.SETUP)
        }

        procedure.writeInstruction(Instruction.Return, expression.info)
    }

    private fun compileBreakExpression(
        expression: ExpressionNode.BreakExpression, procedure: CompiledFile.MutableProcedure, ctx: CompilerContext
    ) {
        if (expression.withValue != null) {
            compileExpression(expression.withValue, procedure, ctx)
        } else {
            procedure.writeInstruction(Instruction.PushAction, expression.info, InstructionPhase.SETUP)
        }

        ctx.resolved.checkForRuntimeErrors(expression.info.breadcrumbs)?.let { error ->
            procedure.writeInstruction(Instruction.Pop(), expression.info)
            compileTypeError(makeErrorConst(error, expression, procedure, ctx), procedure)
            return
        }

        val breakTag = ctx.getTag<NodeTag.BreaksLoop>(expression.info.breadcrumbs)!!
        val loopIndex =
            ctx.scopeStack.reversed().filter { it.openLoop != null }.withIndex().firstNotNullOf { (i, scope) ->
                if (scope.scopeRoot == breakTag.loop) i else null
            }

        procedure.writeInstruction(Instruction.BreakLoop(loopIndex + 1), expression.info)
    }

    private fun compileMemberExpression(
        expression: ExpressionNode.MemberExpression, procedure: CompiledFile.MutableProcedure, ctx: CompilerContext
    ) {
        compileExpression(expression.objectExpression, procedure, ctx)

        ctx.resolved.checkForRuntimeErrors(expression.info.breadcrumbs)?.let { error ->
            procedure.writeInstruction(Instruction.Pop(1), expression.info)
            compileBadValue(expression, error, procedure, ctx)
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

        procedure.writeInstruction(Instruction.GetMember(fieldIndex), expression.info)
    }

    private fun compileTypeError(errorConst: Int, procedure: CompiledFile.MutableProcedure) {
        procedure.writeInstruction(
            Instruction.Import(
                filePathConstant = procedure.addConstant(CompiledFile.CompiledConstant.StringConst("core/builtin.cau")),
                exportNameConstant = procedure.addConstant(CompiledFile.CompiledConstant.StringConst("TypeError")),
            ), nodeInfo = null
        )
        procedure.writeInstruction(Instruction.Literal(errorConst), nodeInfo = null)
        procedure.writeInstruction(
            Instruction.Construct(1), nodeInfo = null
        )
        procedure.writeInstruction(
            Instruction.Cause, nodeInfo = null
        )
    }
}

