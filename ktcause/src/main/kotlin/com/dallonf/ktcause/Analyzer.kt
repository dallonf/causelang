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

    data class ValueCapturedBy(val function: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(function, CapturesValue(value = breadcrumbs))
    }

    data class CapturesValue(val value: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(value, ValueCapturedBy(function = breadcrumbs))
    }

    data class FunctionCanReturnTypeOf(val returnExpression: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) =
            Pair(returnExpression, ReturnsFromFunction(function = breadcrumbs))
    }

    data class ReturnsFromFunction(val function: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) =
            Pair(function, FunctionCanReturnTypeOf(returnExpression = breadcrumbs))
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

    data class ParameterForCall(val callExprssion: Breadcrumbs, val index: Int) : NodeTag() {
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
        val currentLoop: Breadcrumbs?
    ) {
        fun clone(breadcrumbs: Breadcrumbs) =
            AnalyzerContext(path, currentScope.extend(), fileRootScope, breadcrumbs, currentFunction, currentLoop)
    }

    fun analyzeFile(filePath: String, astNode: FileNode): AnalyzedNode {
        val result = AnalyzedNode()
        val rootScope = Scope()

        val ctx = AnalyzerContext(
            filePath,
            currentScope = rootScope,
            fileRootScope = rootScope,
            currentScopePosition = astNode.info.breadcrumbs,
            currentFunction = null,
            currentLoop = null
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
            is DeclarationNode.Function -> {
                return listOf(declaration.name.text to declaration.info.breadcrumbs)
            }

            is DeclarationNode.Import -> {
                val list = declaration.mappings.map { mapping ->
                    val sourceName = mapping.sourceName.text
                    val rename = mapping.rename?.text
                    (rename ?: sourceName) to mapping.info.breadcrumbs
                }
                return list.ifEmpty { null }
            }

            is DeclarationNode.NamedValue -> {
                return listOf(declaration.name.text to declaration.info.breadcrumbs)
            }

            is DeclarationNode.ObjectType -> {
                return listOf(declaration.name.text to declaration.info.breadcrumbs)
            }

            is DeclarationNode.SignalType -> {
                return listOf(declaration.name.text to declaration.info.breadcrumbs)
            }

            is DeclarationNode.OptionType -> {
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
            is TypeReferenceNode.IdentifierTypeReferenceNode -> {
                val scopeItem = ctx.currentScope.items[typeReference.identifier.text]
                if (scopeItem != null) {
                    output.addValueFlowTag(scopeItem.origin, typeReference.info.breadcrumbs)
                    // TODO: think harder about whether type references are actually captured
                    if (scopeItem is CapturedValueScopeItem) {
                        output.addTag(scopeItem.origin, NodeTag.ValueCapturedBy(ctx.currentFunction!!))
                        output.addTag(typeReference.info.breadcrumbs, NodeTag.UsesCapturedValue(ctx.currentFunction))
                    }
                } else {
                    output.addTag(typeReference.info.breadcrumbs, NodeTag.ReferenceNotInScope)
                }
            }
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
            is DeclarationNode.Import -> analyzeImportDeclaration(declaration, output, ctx)
            is DeclarationNode.Function -> analyzeFunctionDeclaration(declaration, output, ctx)
            is DeclarationNode.NamedValue -> analyzeNamedValueDeclaration(declaration, output, ctx)
            is DeclarationNode.ObjectType -> analyzeObjectTypeDeclaration(declaration, output, ctx)
            is DeclarationNode.SignalType -> analyzeSignalTypeDeclaration(declaration, output, ctx)
            is DeclarationNode.OptionType -> analyzeOptionTypeDeclaration(declaration, output, ctx)
        }
    }

    private fun analyzeImportDeclaration(
        declaration: DeclarationNode.Import, output: AnalyzedNode, ctx: AnalyzerContext
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
        declaration: DeclarationNode.Function, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        for (param in declaration.params) {
            param.typeReference?.let { analyzeTypeReference(it, output, ctx) }
        }

        output.addTag(
            declaration.info.breadcrumbs, NodeTag.FunctionCanReturnTypeOf(
                declaration.body.info.breadcrumbs
            )
        )

        val newCtx = AnalyzerContext(
            ctx.path,
            ctx.currentScope.copy(
                items = ctx.currentScope.items.mapValues { (key, value) ->
                    if (value is LocalScopeItem) {
                        CapturedValueScopeItem(value.origin)
                    } else {
                        value
                    }
                }.toMutableMap()
            ),
            ctx.fileRootScope,
            declaration.body.info.breadcrumbs,
            currentFunction = declaration.info.breadcrumbs,
            currentLoop = null,
        )
        for (param in declaration.params) {
            newCtx.currentScope.items[param.name.text] = LocalScopeItem(param.info.breadcrumbs)
            output.addTag(param.info.breadcrumbs, NodeTag.DeclarationForScope(newCtx.currentScopePosition))
        }
        analyzeBody(declaration.body, output, newCtx)
    }

    private fun analyzeNamedValueDeclaration(
        declaration: DeclarationNode.NamedValue, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        output.addValueFlowTag(declaration.value.info.breadcrumbs, declaration.info.breadcrumbs)

        analyzeExpression(declaration.value, output, ctx)
        declaration.typeAnnotation?.let { typeAnnotation ->
            analyzeTypeReference(typeAnnotation, output, ctx)
        }
    }

    private fun analyzeObjectTypeDeclaration(
        declaration: DeclarationNode.ObjectType, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        declaration.fields?.let { fields ->
            for (field in fields) {
                analyzeTypeReference(field.typeConstraint, output, ctx)
            }
        }
    }

    private fun analyzeSignalTypeDeclaration(
        declaration: DeclarationNode.SignalType, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        declaration.fields?.let { fields ->
            for (field in fields) {
                analyzeTypeReference(field.typeConstraint, output, ctx)
            }
        }

        analyzeTypeReference(declaration.result, output, ctx)
    }

    private fun analyzeOptionTypeDeclaration(
        declaration: DeclarationNode.OptionType, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        for (option in declaration.options) {
            analyzeTypeReference(option, output, ctx)
        }
    }


    private fun analyzeBody(body: BodyNode, output: AnalyzedNode, ctx: AnalyzerContext) {
        when (body) {
            is BodyNode.BlockBodyNode -> {
                var currentCtx = ctx.clone(body.info.breadcrumbs)

                for (statementNode in body.statements) {
                    currentCtx = analyzeStatement(statementNode, output, currentCtx)
                }
            }

            is BodyNode.SingleStatementBodyNode -> {
                analyzeStatement(body.statement, output, ctx)
                output.addValueFlowTag(body.statement.info.breadcrumbs, body.info.breadcrumbs)
            }
        }
    }

    private fun analyzeStatement(
        statementNode: StatementNode,
        output: AnalyzedNode,
        ctx: AnalyzerContext
    ): AnalyzerContext {
        when (statementNode) {
            is StatementNode.ExpressionStatement -> {
                output.addValueFlowTag(
                    statementNode.expression.info.breadcrumbs, statementNode.info.breadcrumbs
                )
                analyzeExpression(statementNode.expression, output, ctx)
            }

            is StatementNode.DeclarationStatement -> {
                getDeclarationsForScope(statementNode.declaration)?.let { declarations ->
                    analyzeDeclaration(statementNode.declaration, output, ctx)

                    val newCtx = AnalyzerContext(
                        ctx.path,
                        ctx.currentScope.extend(),
                        ctx.fileRootScope,
                        ctx.currentScopePosition,
                        ctx.currentFunction,
                        ctx.currentLoop,
                    )
                    addDeclarationsToScope(
                        declarations.map { (name, item) -> name to LocalScopeItem(item) },
                        output,
                        newCtx
                    )
                    return newCtx
                }
            }

            is StatementNode.EffectStatement -> {
                val effectCtx = AnalyzerContext(
                    ctx.path,
                    ctx.currentScope.extend(),
                    ctx.fileRootScope,
                    statementNode.info.breadcrumbs,
                    ctx.currentFunction,
                    ctx.currentLoop,
                )

                analyzePattern(statementNode.pattern, output, effectCtx)
                analyzeBody(statementNode.body, output, effectCtx)
            }

            is StatementNode.SetStatement -> {
                analyzeSetStatement(statementNode, output, ctx)
            }
        }
        return ctx
    }

    private fun analyzeSetStatement(
        statementNode: StatementNode.SetStatement, output: AnalyzedNode, ctx: AnalyzerContext
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
            is ExpressionNode.BlockExpressionNode -> analyzeBlockExpression(expression, output, ctx)

            is ExpressionNode.CauseExpression -> analyzeCauseExpression(expression, output, ctx)
            is ExpressionNode.BranchExpressionNode -> analyzeBranchExpressionNode(expression, output, ctx)
            is ExpressionNode.LoopExpressionNode -> analyzeLoopExpressionNode(expression, output, ctx)
            is ExpressionNode.ReturnExpression -> analyzeReturnExpression(expression, output, ctx)
            is ExpressionNode.BreakExpression -> analyzeBreakExpression(expression, output, ctx)

            is ExpressionNode.IdentifierExpression -> analyzeIdentifierExpression(expression, output, ctx)
            is ExpressionNode.StringLiteralExpression -> {}
            is ExpressionNode.NumberLiteralExpression -> {}

            is ExpressionNode.CallExpression -> analyzeCallExpression(expression, output, ctx)
            is ExpressionNode.MemberExpression -> analyzeMemberExpression(expression, output, ctx)
        }
    }

    private fun analyzeBranchExpressionNode(
        expression: ExpressionNode.BranchExpressionNode, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        expression.withValue?.let { analyzeExpression(it, output, ctx) }

        for (branchOption in expression.branches) {
            val newCtx = ctx.clone(branchOption.info.breadcrumbs)
            when (branchOption) {
                is BranchOptionNode.IfBranchOptionNode -> {
                    analyzeExpression(branchOption.condition, output, newCtx)
                }

                is BranchOptionNode.IsBranchOptionNode -> {
                    analyzePattern(branchOption.pattern, output, newCtx)
                }

                is BranchOptionNode.ElseBranchOptionNode -> {}
            }
            analyzeBody(branchOption.body, output, newCtx)
        }
    }

    private fun analyzeLoopExpressionNode(
        expression: ExpressionNode.LoopExpressionNode, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        analyzeBody(expression.body, output, ctx.copy(currentLoop = expression.info.breadcrumbs))
    }

    private fun analyzeBlockExpression(
        expression: ExpressionNode.BlockExpressionNode, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        analyzeBody(expression.block, output, ctx)
        output.addValueFlowTag(expression.block.info.breadcrumbs, expression.info.breadcrumbs)
    }


    private fun analyzeIdentifierExpression(
        expression: ExpressionNode.IdentifierExpression, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        val identifierText = expression.identifier.text
        val foundItem = ctx.currentScope.items[identifierText]
        if (foundItem != null) {
            output.addValueFlowTag(foundItem.origin, expression.info.breadcrumbs)
            if (foundItem is CapturedValueScopeItem) {
                output.addTag(foundItem.origin, NodeTag.ValueCapturedBy(ctx.currentFunction!!))
                output.addTag(expression.info.breadcrumbs, NodeTag.UsesCapturedValue(ctx.currentFunction))
            }
        } else {
            output.addTag(expression.info.breadcrumbs, NodeTag.ReferenceNotInScope)
        }
    }

    private fun analyzeCauseExpression(
        expression: ExpressionNode.CauseExpression, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        analyzeExpression(expression.signal, output, ctx)
    }

    private fun analyzeCallExpression(
        expression: ExpressionNode.CallExpression, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        for ((i, parameterNode) in expression.parameters.withIndex()) {
            analyzeExpression(parameterNode.value, output, ctx)
            output.addTag(parameterNode.info.breadcrumbs, NodeTag.ParameterForCall(expression.info.breadcrumbs, i))
        }

        analyzeExpression(expression.callee, output, ctx)
    }

    private fun analyzeMemberExpression(
        expression: ExpressionNode.MemberExpression, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        analyzeExpression(expression.objectExpression, output, ctx)
    }

    private fun analyzeReturnExpression(
        expression: ExpressionNode.ReturnExpression, output: AnalyzedNode, ctx: AnalyzerContext
    ) {

        expression.value?.let { value ->
            analyzeExpression(value, output, ctx)
            ctx.currentFunction?.let { output.addTag(value.info.breadcrumbs, NodeTag.ReturnsFromFunction(it)) }
        }
    }

    private fun analyzeBreakExpression(
        expression: ExpressionNode.BreakExpression, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        expression.withValue?.let { analyzeExpression(it, output, ctx) }
        ctx.currentLoop?.let { loop ->
            output.addTag(expression.info.breadcrumbs, NodeTag.BreaksLoop(loop))
        }
    }
}