package com.dallonf.ktcause

import com.dallonf.ktcause.ast.*
import com.dallonf.ktcause.types.PrimitiveValueLangType

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

    data class ValueComesFrom(val source: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(source, ValueGoesTo(destination = breadcrumbs))
    }

    data class ValueGoesTo(val destination: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(destination, ValueComesFrom(source = breadcrumbs))
    }

    data class Calls(val callee: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(callee, CalledBy(callExpression = breadcrumbs))
    }

    data class CalledBy(val callExpression: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(callExpression, Calls(callee = breadcrumbs))
    }

    data class CallsWithParameter(val parameter: Breadcrumbs, val index: Int) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) =
            Pair(parameter, ParameterForCall(callExpression = breadcrumbs, index = index))
    }

    data class ParameterForCall(val callExpression: Breadcrumbs, val index: Int) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) =
            Pair(callExpression, CallsWithParameter(parameter = breadcrumbs, index = index))
    }

    data class Causes(val signal: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(signal, CausedBy(causeExpression = breadcrumbs))
    }

    data class CausedBy(val causeExpression: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(causeExpression, Causes(signal = breadcrumbs))
    }

    data class IsPrimitiveValue(val primitiveType: PrimitiveValueLangType) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    data class IsFunction(val name: String?) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    data class FunctionCanReturnTypeOf(val returnExpression: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
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

    object Expression : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    data class NamedValue(val name: String, val value: Breadcrumbs, val typeDeclaration: Breadcrumbs?) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    data class TypeAnnotated(val annotation: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(annotation, AnnotatesTypeFor(value = breadcrumbs))
    }

    data class AnnotatesTypeFor(val value: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(value, TypeAnnotated(annotation = breadcrumbs))
    }
}


object Analyzer {
    private data class ScopeItem(val origin: Breadcrumbs)
    private data class Scope(val items: MutableMap<String, ScopeItem> = mutableMapOf()) {
        fun extend(): Scope = Scope(items.toMutableMap())
    }

    private data class AnalyzerContext(val currentScope: Scope, val currentScopePosition: Breadcrumbs) {
        fun withNewScope(breadcrumbs: Breadcrumbs, f: (AnalyzerContext) -> Unit) {
            val newCtx = clone(breadcrumbs)
            f(newCtx)
        }

        fun clone(breadcrumbs: Breadcrumbs) = AnalyzerContext(currentScope.extend(), breadcrumbs)
    }

    fun analyzeFile(astNode: FileNode): AnalyzedNode {
        val result = AnalyzedNode()
        val rootScope = Scope()

        val ctx = AnalyzerContext(
            currentScope = rootScope, currentScopePosition = astNode.info.breadcrumbs
        )

        // loop over top-level declarations to hoist them into file scope
        for (declaration in astNode.declarations) {
            getDeclarationsForScope(declaration)?.let {
                addDeclarationsToScope(
                    it,
                    result,
                    ctx
                )
            }
        }

        for (declaration in astNode.declarations) {
            analyzeDeclaration(declaration, result, ctx)
        }

        return result
    }

    private fun getDeclarationsForScope(
        declaration: DeclarationNode
    ): List<Pair<String, ScopeItem>>? {
        when (declaration) {
            is DeclarationNode.Function -> {
                return listOf(declaration.name.text to ScopeItem(declaration.info.breadcrumbs))
            }

            is DeclarationNode.Import -> {
                val list = declaration.mappings.map { mapping ->
                    val sourceName = mapping.sourceName.text
                    val rename = mapping.rename?.text
                    (rename ?: sourceName) to ScopeItem(mapping.info.breadcrumbs)
                }
                return list.ifEmpty { null }
            }

            is DeclarationNode.NamedValue -> {
                return listOf(declaration.name.text to ScopeItem(declaration.info.breadcrumbs))
            }
        }
    }

    private fun addDeclarationsToScope(
        declarations: List<Pair<String, ScopeItem>>,
        output: AnalyzedNode,
        scope: Scope,
        scopePosition: Breadcrumbs
    ) {
        for (declaration in declarations) {
            val (name, item) = declaration
            scope.items[name] = item
            output.addTag(item.origin, NodeTag.DeclarationForScope(scopePosition))
        }
    }

    private fun addDeclarationsToScope(
        declarations: List<Pair<String, ScopeItem>>,
        output: AnalyzedNode,
        ctx: AnalyzerContext
    ) {
        addDeclarationsToScope(declarations, output, ctx.currentScope, ctx.currentScopePosition)
    }

    private fun analyzeTypeReference(typeReference: TypeReferenceNode, output: AnalyzedNode, ctx: AnalyzerContext) {
        when (typeReference) {
            is TypeReferenceNode.IdentifierTypeReferenceNode -> {
                val scopeItem = ctx.currentScope.items[typeReference.identifier.text]
                if (scopeItem != null) {
                    output.addValueFlowTag(scopeItem.origin, typeReference.info.breadcrumbs)
                } else {
                    output.addTag(typeReference.info.breadcrumbs, NodeTag.ReferenceNotInScope)
                }
            }
        }
    }

    private fun analyzeDeclaration(declaration: DeclarationNode, output: AnalyzedNode, ctx: AnalyzerContext) {
        when (declaration) {
            is DeclarationNode.Import -> analyzeImportDeclaration(declaration, output, ctx)
            is DeclarationNode.Function -> analyzeFunctionDeclaration(declaration, output, ctx)
            is DeclarationNode.NamedValue -> analyzeNamedValueDeclaration(declaration, output, ctx)
        }
    }

    private fun analyzeImportDeclaration(
        declaration: DeclarationNode.Import, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        val path = run {
            val it = declaration.path.path
            if (!it.endsWith(".cau")) {
                "$it.cau"
            } else {
                it
            }
        }

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
        ctx.withNewScope(declaration.info.breadcrumbs) { newCtx ->
            val name = declaration.name.text
            // the function itself goes in the scope to allow recursion
            newCtx.currentScope.items[name] = ScopeItem(declaration.info.breadcrumbs)

            output.addTag(declaration.info.breadcrumbs, NodeTag.IsFunction(name = name))
            output.addTag(
                declaration.info.breadcrumbs, NodeTag.FunctionCanReturnTypeOf(
                    declaration.body.info.breadcrumbs
                )
            )

            analyzeBody(declaration.body, output, newCtx)
        }
    }

    private fun analyzeNamedValueDeclaration(
        declaration: DeclarationNode.NamedValue, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        output.addTag(
            declaration.info.breadcrumbs, NodeTag.NamedValue(
                name = declaration.name.text,
                typeDeclaration = declaration.typeAnnotation?.info?.breadcrumbs,
                value = declaration.value.info.breadcrumbs
            )
        )

        analyzeExpression(declaration.value, output, ctx)
        declaration.typeAnnotation?.let { typeAnnotation ->
            analyzeTypeReference(typeAnnotation, output, ctx)
            output.addTag(typeAnnotation.info.breadcrumbs, NodeTag.AnnotatesTypeFor(declaration.value.info.breadcrumbs))
        }
    }

    private fun analyzeBody(body: BodyNode, output: AnalyzedNode, ctx: AnalyzerContext) {
        when (body) {
            is BodyNode.BlockBody -> {
                if (body.statements.isEmpty()) {
                    // if there are no statements, the block can only be Action-typed.
                    // Avoids issue with `statements.last()` below
                    output.addTag(body.info.breadcrumbs, NodeTag.IsPrimitiveValue(PrimitiveValueLangType.ACTION))
                } else {
                    // a block's return type is the last expression... or the type of any returns? hmmmmmm
                    val lastStatementBreadcrumbs = body.statements.last().info.breadcrumbs
                    output.addValueFlowTag(lastStatementBreadcrumbs, body.info.breadcrumbs)
                }

                var currentCtx = ctx.clone(body.info.breadcrumbs)

                for (statementNode in body.statements) {
                    when (statementNode) {
                        is StatementNode.ExpressionStatement -> {
                            output.addValueFlowTag(
                                statementNode.expression.info.breadcrumbs, statementNode.info.breadcrumbs
                            )
                            analyzeExpression(statementNode.expression, output, currentCtx)
                        }

                        is StatementNode.DeclarationStatement -> {
                            getDeclarationsForScope(statementNode.declaration)?.let { declarations ->
                                analyzeDeclaration(statementNode.declaration, output, currentCtx)

                                currentCtx =
                                    AnalyzerContext(currentCtx.currentScope.extend(), currentCtx.currentScopePosition)
                                addDeclarationsToScope(declarations, output, currentCtx)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun analyzeExpression(expression: ExpressionNode, output: AnalyzedNode, ctx: AnalyzerContext) {
        when (expression) {
            is ExpressionNode.IdentifierExpression -> analyzeIdentifierExpression(expression, output, ctx)
            is ExpressionNode.CauseExpression -> analyzeCauseExpression(expression, output, ctx)
            is ExpressionNode.CallExpression -> analyzeCallExpression(expression, output, ctx)
            is ExpressionNode.StringLiteralExpression -> output.addTag(
                expression.info.breadcrumbs,
                NodeTag.IsPrimitiveValue(PrimitiveValueLangType.STRING)
            )

            is ExpressionNode.IntegerLiteralExpression -> output.addTag(
                expression.info.breadcrumbs,
                NodeTag.IsPrimitiveValue(PrimitiveValueLangType.INTEGER)
            )
        }
        output.addTag(expression.info.breadcrumbs, NodeTag.Expression)
    }


    private fun analyzeIdentifierExpression(
        expression: ExpressionNode.IdentifierExpression,
        output: AnalyzedNode,
        ctx: AnalyzerContext
    ) {
        val identifierText = expression.identifier.text
        val foundItem = ctx.currentScope.items[identifierText]
        if (foundItem != null) {
            output.addValueFlowTag(foundItem.origin, expression.info.breadcrumbs)
        } else {
            output.addTag(expression.info.breadcrumbs, NodeTag.ReferenceNotInScope)
        }
    }

    private fun analyzeCauseExpression(
        expression: ExpressionNode.CauseExpression,
        output: AnalyzedNode,
        ctx: AnalyzerContext
    ) {
        output.addTag(expression.info.breadcrumbs, NodeTag.Causes(expression.signal.info.breadcrumbs))
        analyzeExpression(expression.signal, output, ctx)
    }

    private fun analyzeCallExpression(
        expression: ExpressionNode.CallExpression,
        output: AnalyzedNode,
        ctx: AnalyzerContext
    ) {
        expression.parameters.forEachIndexed { i, parameterNode ->
            analyzeExpression(parameterNode.value, output, ctx)
            output.addValueFlowTag(parameterNode.value.info.breadcrumbs, parameterNode.info.breadcrumbs)
            output.addTag(parameterNode.info.breadcrumbs, NodeTag.ParameterForCall(expression.info.breadcrumbs, i))
        }

        analyzeExpression(expression.callee, output, ctx)
        output.addTag(expression.info.breadcrumbs, NodeTag.Calls(expression.callee.info.breadcrumbs))
    }


}