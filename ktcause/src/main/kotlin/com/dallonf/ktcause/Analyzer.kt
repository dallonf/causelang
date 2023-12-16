package com.dallonf.ktcause

import com.dallonf.ktcause.ast.*

data class AnalyzedNode(
    val nodeTags: MutableMap<Breadcrumbs, MutableList<NodeTag>> = mutableMapOf(),
    val filesReferenced: MutableSet<String> = mutableSetOf()
) {
    fun addTagWithoutInverse(breadcrumbs: Breadcrumbs, tag: NodeTag) {
        val tagList = nodeTags.getOrPut(breadcrumbs) { mutableListOf() }
        tagList.add(tag)
    }

    fun addTag(breadcrumbs: Breadcrumbs, tag: NodeTag) {
        val inverse = tag.inverse(breadcrumbs)
        addTagWithoutInverse(breadcrumbs, tag)
        if (inverse != null) {
            addTagWithoutInverse(inverse.first, inverse.second)
        }
    }

    fun addValueFlowTag(comesFrom: Breadcrumbs, goesTo: Breadcrumbs) {
        addTag(comesFrom, NodeTag.ValueGoesTo(goesTo))
    }

    fun addFileReference(path: String) {
        filesReferenced.add(path)
    }
}

sealed class NodeTag {
    abstract fun inverse(breadcrumbs: Breadcrumbs): Pair<Breadcrumbs, NodeTag>?

    data class ReferencesFile(val path: String, val exportName: String?) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    object BadFileReference : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    data class ValueComesFrom(val source: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(source, ValueGoesTo(destination = breadcrumbs))
    }

    data class ValueGoesTo(val destination: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(destination, ValueComesFrom(source = breadcrumbs))
    }

    data class SetsVariable(val variable: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(variable, VariableSetBy(statement = breadcrumbs))
    }

    data class VariableSetBy(val statement: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(statement, SetsVariable(variable = breadcrumbs))
    }

    data class UsesCapturedValue(val parentFunction: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    data class ValueCapturedByFunction(val function: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(function, FunctionCapturesValue(value = breadcrumbs))
    }

    data class FunctionCapturesValue(val value: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(value, ValueCapturedByFunction(function = breadcrumbs))
    }

    data class FunctionCanReturnTypeOf(val returnExpressionValue: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) =
            Pair(returnExpressionValue, ReturnsFromFunction(function = breadcrumbs))
    }

    data class ReturnsFromFunction(val function: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) =
            Pair(function, FunctionCanReturnTypeOf(returnExpressionValue = breadcrumbs))
    }

    data class FunctionCanReturnAction(val returnExpression: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(returnExpression, ActionReturn(function = breadcrumbs))

    }

    data class ActionReturn(val function: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) =
            Pair(function, FunctionCanReturnAction(returnExpression = breadcrumbs))
    }


    data class LoopBreaksAt(val breakExpression: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(breakExpression, BreaksLoop(loop = breadcrumbs))
    }

    data class BreaksLoop(val loop: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(loop, LoopBreaksAt(breakExpression = breadcrumbs))
    }

    object ReferenceNotInScope : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    data class DeclarationForScope(val scope: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) =
            Pair(scope, ScopeContainsDeclaration(declaration = breadcrumbs))
    }

    data class ScopeContainsDeclaration(val declaration: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(declaration, DeclarationForScope(scope = breadcrumbs))
    }

    data class TopLevelDeclaration(val name: String) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    data class ParameterForCall(val callExpression: Breadcrumbs, val index: UInt) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    data class CanonicalIdInfo(val parentName: String?, val index: UInt) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }
}


object Analyzer {
    private sealed interface ScopeItem {
        val origin: Breadcrumbs
    }

    private data class LocalScopeItem(override val origin: Breadcrumbs) : ScopeItem
    private data class TopLevelScopeItem(override val origin: Breadcrumbs) : ScopeItem
    private data class CapturedValueScopeItem(override val origin: Breadcrumbs) : ScopeItem

    private data class Scope(val items: MutableMap<String, ScopeItem> = mutableMapOf()) {
        fun extend(): Scope = Scope(items.toMutableMap())
    }

    private data class AnalyzerContext(
        val path: String,
        val currentScope: Scope,
        val fileRootScope: Scope,
        val currentScopePosition: Breadcrumbs,
        val currentFunction: Breadcrumbs?,
        val currentLoop: Breadcrumbs?,
        val debugContext: Debug.DebugContext?,
        val canonicalNameStack: List<CanonicalNameParent>,
    ) {
        data class CanonicalNameParent(val parentName: String?, val numbers: MutableMap<String?, UInt> = mutableMapOf())

        fun clone(breadcrumbs: Breadcrumbs) = AnalyzerContext(
            path,
            currentScope.extend(),
            fileRootScope,
            breadcrumbs,
            currentFunction,
            currentLoop,
            debugContext,
            canonicalNameStack
        )
    }

    fun analyzeFile(filePath: String, astNode: FileNode, debugContext: Debug.DebugContext?): AnalyzedNode {
        val result = AnalyzedNode()
        val rootScope = Scope()

        val ctx = AnalyzerContext(
            filePath,
            currentScope = rootScope,
            fileRootScope = rootScope,
            currentScopePosition = astNode.info.breadcrumbs,
            currentFunction = null,
            currentLoop = null,
            debugContext,
            canonicalNameStack = listOf(AnalyzerContext.CanonicalNameParent(parentName = null))
        )

        // loop over top-level declarations to hoist them into file scope
        for (declaration in astNode.declarations) {
            getDeclarationsForScope(declaration)?.let {
                addDeclarationsToScope(
                    it.map { (name, position) -> name to TopLevelScopeItem(position) }, result, ctx
                )
                for ((name, scopeItem) in it) {
                    result.addTag(scopeItem, NodeTag.TopLevelDeclaration(name))
                }
            }
        }

        for (declaration in astNode.declarations) {
            analyzeDeclaration(declaration, result, ctx)
        }

        return result
    }

    private fun getDeclarationsForScope(
        declaration: DeclarationNode
    ): List<Pair<String, Breadcrumbs>>? {
        when (declaration) {
            is FunctionNode -> {
                return listOf(declaration.name.text to declaration.info.breadcrumbs)
            }

            is ImportNode -> {
                val list = declaration.mappings.map { mapping ->
                    val sourceName = mapping.sourceName.text
                    val rename = mapping.rename?.text
                    (rename ?: sourceName) to mapping.info.breadcrumbs
                }
                return list.ifEmpty { null }
            }

            is NamedValueNode -> {
                return listOf(declaration.name.text to declaration.info.breadcrumbs)
            }

            is ObjectType -> {
                return listOf(declaration.name.text to declaration.info.breadcrumbs)
            }

            is SignalType -> {
                return listOf(declaration.name.text to declaration.info.breadcrumbs)
            }

            is OptionType -> {
                return listOf(declaration.name.text to declaration.info.breadcrumbs)
            }
        }
    }

    private fun addDeclarationsToScope(
        declarations: List<Pair<String, ScopeItem>>, output: AnalyzedNode, scope: Scope, scopePosition: Breadcrumbs
    ) {
        for (declaration in declarations) {
            val (name, item) = declaration
            scope.items[name] = item
            output.addTag(item.origin, NodeTag.DeclarationForScope(scopePosition))
        }
    }

    private fun addDeclarationsToScope(
        declarations: List<Pair<String, ScopeItem>>, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        addDeclarationsToScope(declarations, output, ctx.currentScope, ctx.currentScopePosition)
    }

    private fun analyzeTypeReference(typeReference: TypeReferenceNode, output: AnalyzedNode, ctx: AnalyzerContext) {
        when (typeReference) {
            is IdentifierTypeReferenceNode -> {
                val scopeItem = ctx.currentScope.items[typeReference.identifier.text]
                if (scopeItem != null) {
                    output.addValueFlowTag(scopeItem.origin, typeReference.info.breadcrumbs)
                    if (scopeItem is CapturedValueScopeItem) {
                        captureValue(scopeItem.origin, typeReference.info.breadcrumbs, output, ctx)
                    }
                } else {
                    output.addTag(typeReference.info.breadcrumbs, NodeTag.ReferenceNotInScope)
                }
            }

            is FunctionTypeReferenceNode -> {
                for (param in typeReference.params) {
                    param.typeReference?.let { analyzeTypeReference(it, output, ctx) }
                }
                analyzeTypeReference(typeReference.returnType, output, ctx)
            }
        }
    }

    private fun captureValue(
        valueOrigin: Breadcrumbs, usage: Breadcrumbs?, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        requireNotNull(ctx.currentFunction)
        val hasExistingCapture =
            output.nodeTags[valueOrigin]?.any { (it as? NodeTag.ValueCapturedByFunction)?.function == ctx.currentFunction }
                ?: false
        if (!hasExistingCapture) {
            output.addTag(valueOrigin, NodeTag.ValueCapturedByFunction(ctx.currentFunction))
        }
        if (usage != null) {
            output.addTag(usage, NodeTag.UsesCapturedValue(ctx.currentFunction))
        }
    }

    private fun analyzePattern(
        pattern: PatternNode,
        output: AnalyzedNode,
        ctx: AnalyzerContext,
    ) {
        pattern.name?.let {
            ctx.currentScope.items[it.text] = LocalScopeItem(pattern.info.breadcrumbs)
            output.addTag(
                pattern.info.breadcrumbs, NodeTag.DeclarationForScope(ctx.currentScopePosition)
            )
        }

        analyzeTypeReference(pattern.typeReference, output, ctx)
    }

    private fun analyzeDeclaration(declaration: DeclarationNode, output: AnalyzedNode, ctx: AnalyzerContext) {
        when (declaration) {
            is ImportNode -> analyzeImportDeclaration(declaration, output, ctx)
            is FunctionNode -> analyzeFunctionDeclaration(declaration, output, ctx)
            is NamedValueNode -> analyzeNamedValueDeclaration(declaration, output, ctx)
            is ObjectType -> analyzeObjectTypeDeclaration(declaration, output, ctx)
            is SignalType -> analyzeSignalTypeDeclaration(declaration, output, ctx)
            is OptionType -> analyzeOptionTypeDeclaration(declaration, output, ctx)
        }
    }

    private fun analyzeImportDeclaration(
        declaration: ImportNode, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        val importedPath = run {
            val it = declaration.path.path
            if (!it.endsWith(".cau")) {
                "$it.cau"
            } else {
                it
            }
        }
        val path = run {
            val filePathItems = ctx.path.split("/")
            val projectRoot = filePathItems.take(1)

            val importedPathItems = importedPath.split("/")
            var currentPath = if (importedPathItems[0].let { it == "." || it == ".." }) {
                filePathItems.dropLast(1)
            } else {
                emptyList()
            }

            for (importedPathItem in importedPathItems) {
                when (importedPathItem) {
                    "." -> {}
                    ".." -> {
                        if (currentPath == projectRoot) {
                            output.addTag(declaration.info.breadcrumbs, NodeTag.BadFileReference)
                            for (mappingNode in declaration.mappings) {
                                output.addValueFlowTag(declaration.info.breadcrumbs, mappingNode.info.breadcrumbs)
                            }
                            return
                        }
                        currentPath = currentPath.dropLast(1)
                    }

                    else -> currentPath = currentPath + listOf(importedPathItem)
                }
            }
            currentPath
        }.joinToString("/")

        output.addFileReference(path)
        output.addTag(declaration.info.breadcrumbs, NodeTag.ReferencesFile(path, null))

        for (mappingNode in declaration.mappings) {
            val sourceName = mappingNode.sourceName.text
            output.addTag(mappingNode.info.breadcrumbs, NodeTag.ReferencesFile(path, sourceName))
        }
    }


    private fun analyzeFunctionDeclaration(
        declaration: FunctionNode, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        val newCtx = analyzeFunctionParams(
            declaration.name.text, declaration.params, breadcrumbs = declaration.info.breadcrumbs, output, ctx
        )
        newCtx.currentScope.items[declaration.name.text] = LocalScopeItem(declaration.info.breadcrumbs)
        output.addTag(
            declaration.info.breadcrumbs, NodeTag.FunctionCanReturnTypeOf(
                declaration.body.info.breadcrumbs
            )
        )
        analyzeBody(declaration.body, output, newCtx)

        declaration.returnType?.let { analyzeTypeReference(it, output, ctx) }

        propagateCapturedValues(declaration.info.breadcrumbs, output, ctx)
    }

    private fun propagateCapturedValues(
        functionDefinition: Breadcrumbs, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        val tagsForFunction = output.nodeTags[functionDefinition] ?: emptyList()
        val capturedValues = tagsForFunction.mapNotNull { it as? NodeTag.FunctionCapturesValue }
        for (capturedValue in capturedValues) {
            val matchingScopeItem = ctx.currentScope.items.values.find { it.origin == capturedValue.value }
            if (matchingScopeItem is CapturedValueScopeItem) {
                captureValue(matchingScopeItem.origin, null, output, ctx)
            }
        }
    }

    private fun analyzeFunctionParams(
        functionName: String?,
        params: List<FunctionSignatureParameterNode>,
        breadcrumbs: Breadcrumbs,
        output: AnalyzedNode,
        ctx: AnalyzerContext
    ): AnalyzerContext {
        for (param in params) {
            param.typeReference?.let { analyzeTypeReference(it, output, ctx) }
        }
        val currentCanonicalParent = ctx.canonicalNameStack.last()
        val newCanonicalNameStack = if (functionName != null) {
            val newName = currentCanonicalParent.parentName?.let { "$it.$functionName" } ?: functionName
            ctx.canonicalNameStack + listOf(AnalyzerContext.CanonicalNameParent(newName))
        } else {
            ctx.canonicalNameStack
        }
        val newCtx = AnalyzerContext(
            ctx.path,
            ctx.currentScope.copy(
                items = ctx.currentScope.items.mapValues { (_, value) ->
                    if (value is LocalScopeItem) {
                        CapturedValueScopeItem(value.origin)
                    } else {
                        value
                    }
                }.toMutableMap()
            ),
            ctx.fileRootScope,
            currentScopePosition = breadcrumbs,
            currentFunction = breadcrumbs,
            currentLoop = null,
            ctx.debugContext,
            newCanonicalNameStack,
        )
        for (param in params) {
            newCtx.currentScope.items[param.name.text] = LocalScopeItem(param.info.breadcrumbs)
            output.addTag(param.info.breadcrumbs, NodeTag.DeclarationForScope(newCtx.currentScopePosition))
        }
        return newCtx
    }

    private fun analyzeNamedValueDeclaration(
        declaration: NamedValueNode, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        output.addValueFlowTag(declaration.value.info.breadcrumbs, declaration.info.breadcrumbs)

        analyzeExpression(declaration.value, output, ctx)
        declaration.typeAnnotation?.let { typeAnnotation ->
            analyzeTypeReference(typeAnnotation, output, ctx)
        }
    }

    private fun analyzeObjectTypeDeclaration(
        declaration: ObjectType, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        tagCanonicalTypeId(declaration.name.text, declaration.info.breadcrumbs, ctx, output)

        declaration.fields?.let { fields ->
            for (field in fields) {
                analyzeTypeReference(field.typeConstraint, output, ctx)
            }
        }
    }

    private fun analyzeSignalTypeDeclaration(
        declaration: SignalType, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        tagCanonicalTypeId(declaration.name.text, declaration.info.breadcrumbs, ctx, output)

        declaration.fields?.let { fields ->
            for (field in fields) {
                analyzeTypeReference(field.typeConstraint, output, ctx)
            }
        }

        declaration.result?.let { analyzeTypeReference(it, output, ctx) }
    }

    private fun tagCanonicalTypeId(
        name: String?, breadcrumbs: Breadcrumbs, ctx: AnalyzerContext, output: AnalyzedNode
    ) {
        val canonicalInfo = ctx.canonicalNameStack.last()
        val number = canonicalInfo.numbers.putIfAbsent(name, 0u) ?: 0u
        canonicalInfo.numbers[name] = number + 1u
        output.addTag(breadcrumbs, NodeTag.CanonicalIdInfo(canonicalInfo.parentName, number))
    }

    private fun analyzeOptionTypeDeclaration(
        declaration: OptionType, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        for (option in declaration.options) {
            analyzeTypeReference(option, output, ctx)
        }
    }


    private fun analyzeBody(body: BodyNode, output: AnalyzedNode, ctx: AnalyzerContext) {
        when (body) {
            is BlockBodyNode -> {
                var currentCtx = ctx.clone(body.info.breadcrumbs)

                for (statementNode in body.statements) {
                    currentCtx = analyzeStatement(statementNode, output, currentCtx)
                }
            }

            is SingleStatementBodyNode -> {
                analyzeStatement(body.statement, output, ctx)
                output.addValueFlowTag(body.statement.info.breadcrumbs, body.info.breadcrumbs)
            }
        }
    }

    private fun analyzeStatement(
        statementNode: StatementNode, output: AnalyzedNode, ctx: AnalyzerContext
    ): AnalyzerContext {
        when (statementNode) {
            is ExpressionStatementNode -> {
                output.addValueFlowTag(
                    statementNode.expression.info.breadcrumbs, statementNode.info.breadcrumbs
                )
                analyzeExpression(statementNode.expression, output, ctx)
            }

            is DeclarationStatementNode -> {
                getDeclarationsForScope(statementNode.declaration)?.let { declarations ->
                    analyzeDeclaration(statementNode.declaration, output, ctx)

                    val newCtx = AnalyzerContext(
                        ctx.path,
                        ctx.currentScope.extend(),
                        ctx.fileRootScope,
                        ctx.currentScopePosition,
                        ctx.currentFunction,
                        ctx.currentLoop,
                        ctx.debugContext,
                        ctx.canonicalNameStack,
                    )
                    addDeclarationsToScope(
                        declarations.map { (name, item) -> name to LocalScopeItem(item) }, output, newCtx
                    )
                    return newCtx
                }
            }

            is EffectStatementNode -> {
                val effectCtx = AnalyzerContext(
                    ctx.path,
                    ctx.currentScope.extend(),
                    ctx.fileRootScope,
                    statementNode.info.breadcrumbs,
                    ctx.currentFunction,
                    ctx.currentLoop,
                    ctx.debugContext,
                    ctx.canonicalNameStack,
                )

                analyzePattern(statementNode.pattern, output, effectCtx)
                analyzeBody(statementNode.body, output, effectCtx)
            }

            is SetStatementNode -> {
                analyzeSetStatement(statementNode, output, ctx)
            }
        }
        return ctx
    }

    private fun analyzeSetStatement(
        statementNode: SetStatementNode, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        analyzeExpression(statementNode.expression, output, ctx)
        val variable = ctx.currentScope.items[statementNode.identifier.text]
        if (variable == null) {
            output.addTag(statementNode.info.breadcrumbs, NodeTag.ReferenceNotInScope)
        } else {
            output.addTag(statementNode.info.breadcrumbs, NodeTag.SetsVariable(variable.origin))
            if (variable is CapturedValueScopeItem) {
                output.addTag(statementNode.info.breadcrumbs, NodeTag.UsesCapturedValue(ctx.currentFunction!!))
            }
        }
    }

    private fun analyzeExpression(expression: ExpressionNode, output: AnalyzedNode, ctx: AnalyzerContext) {
        when (expression) {
            is GroupExpressionNode -> analyzeExpression(expression.expression, output, ctx)

            is BlockExpressionNode -> analyzeBlockExpression(expression, output, ctx)
            is FunctionExpressionNode -> analyzeFunctionExpression(expression, output, ctx)

            is CauseExpressionNode -> analyzeCauseExpression(expression, output, ctx)
            is BranchExpressionNode -> analyzeBranchExpressionNode(expression, output, ctx)
            is LoopExpressionNode -> analyzeLoopExpressionNode(expression, output, ctx)
            is ReturnExpression -> analyzeReturnExpression(expression, output, ctx)
            is BreakExpression -> analyzeBreakExpression(expression, output, ctx)

            is IdentifierExpressionNode -> analyzeIdentifierExpression(expression, output, ctx)
            is StringLiteralExpressionNode -> {}
            is NumberLiteralExpression -> {}

            is CallExpressionNode -> analyzeCallExpression(expression, output, ctx)
            is MemberExpression -> analyzeMemberExpression(expression, output, ctx)
            is PipeCallExpression -> analyzePipeCallExpression(expression, output, ctx)
        }
    }

    private fun analyzeBranchExpressionNode(
        expression: BranchExpressionNode, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        expression.withValue?.let { analyzeExpression(it, output, ctx) }

        for (branchOption in expression.branches) {
            val newCtx = ctx.clone(branchOption.info.breadcrumbs)
            when (branchOption) {
                is IfBranchOptionNode -> {
                    analyzeExpression(branchOption.condition, output, newCtx)
                }

                is IsBranchOptionNode -> {
                    analyzePattern(branchOption.pattern, output, newCtx)
                }

                is ElseBranchOptionNode -> {}
            }
            analyzeBody(branchOption.body, output, newCtx)
        }
    }

    private fun analyzeLoopExpressionNode(
        expression: LoopExpressionNode, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        analyzeBody(expression.body, output, ctx.copy(currentLoop = expression.info.breadcrumbs))
    }

    private fun analyzeBlockExpression(
        expression: BlockExpressionNode, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        analyzeBody(expression.block, output, ctx)
        output.addValueFlowTag(expression.block.info.breadcrumbs, expression.info.breadcrumbs)
    }

    private fun analyzeFunctionExpression(
        expression: FunctionExpressionNode, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        val newCtx = analyzeFunctionParams(
            functionName = null, expression.params, breadcrumbs = expression.info.breadcrumbs, output, ctx
        )
        analyzeExpression(expression.body, output, newCtx)
        output.addTag(expression.info.breadcrumbs, NodeTag.FunctionCanReturnTypeOf(expression.body.info.breadcrumbs))

        expression.returnType?.let { analyzeTypeReference(it, output, ctx) }

        propagateCapturedValues(expression.info.breadcrumbs, output, ctx)
    }


    private fun analyzeIdentifierExpression(
        expression: IdentifierExpressionNode, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        val identifierText = expression.identifier.text
        val foundItem = ctx.currentScope.items[identifierText]
        if (foundItem != null) {
            output.addValueFlowTag(foundItem.origin, expression.info.breadcrumbs)
            if (foundItem is CapturedValueScopeItem) {
                captureValue(foundItem.origin, expression.info.breadcrumbs, output, ctx)
            }
        } else {
            output.addTag(expression.info.breadcrumbs, NodeTag.ReferenceNotInScope)
        }
    }

    private fun analyzeCauseExpression(
        expression: CauseExpressionNode, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        analyzeExpression(expression.signal, output, ctx)
    }

    private fun analyzeCallExpression(
        expression: CallExpressionNode, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        analyzeExpression(expression.callee, output, ctx)

        for ((i, parameterNode) in expression.parameters.withIndex()) {
            analyzeExpression(parameterNode.value, output, ctx)
            output.addTag(
                parameterNode.info.breadcrumbs,
                NodeTag.ParameterForCall(expression.info.breadcrumbs, i.toUInt())
            )
        }
    }

    private fun analyzeMemberExpression(
        expression: MemberExpression, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        analyzeExpression(expression.objectExpression, output, ctx)
    }

    private fun analyzePipeCallExpression(
        expression: PipeCallExpression, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        analyzeExpression(expression.subject, output, ctx)

        analyzeExpression(expression.callee, output, ctx)

        for ((i, parameterNode) in expression.parameters.withIndex()) {
            analyzeExpression(parameterNode.value, output, ctx)
            output.addTag(
                parameterNode.info.breadcrumbs, NodeTag.ParameterForCall(
                    expression.info.breadcrumbs,
                    (i + 1).toUInt()
                )
            )
        }
    }


    private fun analyzeReturnExpression(
        expression: ReturnExpression, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        val value = expression.value
        if (value != null) {
            analyzeExpression(value, output, ctx)
            ctx.currentFunction?.let { output.addTag(value.info.breadcrumbs, NodeTag.ReturnsFromFunction(it)) }
        } else {
            ctx.currentFunction?.let { output.addTag(expression.info.breadcrumbs, NodeTag.ActionReturn(it)) }
        }
    }

    private fun analyzeBreakExpression(
        expression: BreakExpression, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        expression.withValue?.let { analyzeExpression(it, output, ctx) }
        ctx.currentLoop?.let { loop ->
            output.addTag(expression.info.breadcrumbs, NodeTag.BreaksLoop(loop))
        }
    }
}