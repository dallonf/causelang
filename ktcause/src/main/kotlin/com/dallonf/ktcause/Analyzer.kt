package com.dallonf.ktcause

import com.dallonf.ktcause.ast.*
import com.dallonf.ktcause.types.LangPrimitiveKind

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

    data class GetsMember(val objectExpression: Breadcrumbs, val memberName: String) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    data class IsPrimitiveValue(val kind: LangPrimitiveKind) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    data class IsFunction(val name: String?) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    data class FunctionHasParam(val name: String, val param: Breadcrumbs, val typeReference: Breadcrumbs?) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) =
            Pair(param, ParamForFunction(name, function = breadcrumbs, typeReference))

    }

    data class ParamForFunction(val name: String, val function: Breadcrumbs, val typeReference: Breadcrumbs?) :
        NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) =
            Pair(function, FunctionHasParam(name, param = breadcrumbs, typeReference))
    }

    data class FunctionCanReturnTypeOf(val returnExpression: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    data class IsObjectType(val name: String) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    data class IsSignalType(val name: String, val resultTypeReference: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    data class TypeHasField(val name: String, val field: Breadcrumbs, val typeReference: Breadcrumbs) :
        NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) =
            Pair(field, FieldForObjectType(name, objectType = breadcrumbs, typeReference))
    }

    data class FieldForObjectType(val name: String, val objectType: Breadcrumbs, val typeReference: Breadcrumbs) :
        NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) =
            Pair(objectType, TypeHasField(name, field = breadcrumbs, typeReference))
    }

    data class IsOptionType(val name: String) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    data class OptionForOptionType(val optionType: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(optionType, OptionTypeHasOption(breadcrumbs))

    }

    data class OptionTypeHasOption(val typeReference: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(typeReference, OptionForOptionType(breadcrumbs))
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

    data class IsDeclarationStatement(val declaration: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    data class IsEffectStatement(val condition: Breadcrumbs, val effectBody: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    object IsExpression : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    data class IsNamedValue(val name: String, val value: Breadcrumbs, val typeDeclaration: Breadcrumbs?) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    object IsVariable : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    data class IsPattern(val name: String?, val typeReference: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    data class TypeAnnotated(val annotation: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(annotation, AnnotatesTypeFor(value = breadcrumbs))
    }

    data class AnnotatesTypeFor(val value: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(value, TypeAnnotated(annotation = breadcrumbs))
    }

    object IsBranch : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null
    }

    enum class BranchOptionType { IF, ELSE }

    data class BranchOptionFor(val branchExpression: Breadcrumbs, val type: BranchOptionType) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) =
            Pair(branchExpression, HasBranchOption(branchOption = breadcrumbs, type = type))
    }

    data class HasBranchOption(val branchOption: Breadcrumbs, val type: BranchOptionType) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) =
            Pair(branchOption, BranchOptionFor(branchExpression = breadcrumbs, type = type))
    }

    data class ConditionFor(val ifBranchOption: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(ifBranchOption, HasCondition(breadcrumbs))

    }

    data class HasCondition(val condition: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = Pair(condition, ConditionFor(breadcrumbs))
    }

    data class IsSetStatement(val sets: Breadcrumbs, val setTo: Breadcrumbs) : NodeTag() {
        override fun inverse(breadcrumbs: Breadcrumbs) = null

    }
}


object Analyzer {
    private data class ScopeItem(val origin: Breadcrumbs)
    private data class Scope(val items: MutableMap<String, ScopeItem> = mutableMapOf()) {
        fun extend(): Scope = Scope(items.toMutableMap())
    }

    private data class AnalyzerContext(
        val currentScope: Scope, val fileRootScope: Scope, val currentScopePosition: Breadcrumbs
    ) {
        fun clone(breadcrumbs: Breadcrumbs) = AnalyzerContext(currentScope.extend(), fileRootScope, breadcrumbs)
    }

    fun analyzeFile(astNode: FileNode): AnalyzedNode {
        val result = AnalyzedNode()
        val rootScope = Scope()

        val ctx = AnalyzerContext(
            currentScope = rootScope, fileRootScope = rootScope, currentScopePosition = astNode.info.breadcrumbs
        )

        // loop over top-level declarations to hoist them into file scope
        for (declaration in astNode.declarations) {
            getDeclarationsForScope(declaration)?.let {
                addDeclarationsToScope(
                    it, result, ctx
                )
                for ((name, scopeItem) in it) {
                    result.addTag(scopeItem.origin, NodeTag.TopLevelDeclaration(name))
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

            is DeclarationNode.ObjectType -> {
                return listOf(declaration.name.text to ScopeItem(declaration.info.breadcrumbs))
            }

            is DeclarationNode.SignalType -> {
                return listOf(declaration.name.text to ScopeItem(declaration.info.breadcrumbs))
            }

            is DeclarationNode.OptionType -> {
                return listOf(declaration.name.text to ScopeItem(declaration.info.breadcrumbs))
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
            is DeclarationNode.ObjectType -> analyzeObjectTypeDeclaration(declaration, output, ctx)
            is DeclarationNode.SignalType -> analyzeSignalTypeDeclaration(declaration, output, ctx)
            is DeclarationNode.OptionType -> analyzeOptionTypeDeclaration(declaration, output, ctx)
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
        val name = declaration.name.text

        output.addTag(declaration.info.breadcrumbs, NodeTag.IsFunction(name = name))

        for (param in declaration.params) {
            output.addTag(
                param.info.breadcrumbs, NodeTag.ParamForFunction(
                    name = param.name.text,
                    function = declaration.info.breadcrumbs,
                    typeReference = param.typeReference?.info?.breadcrumbs
                )
            )
            param.typeReference?.let { analyzeTypeReference(it, output, ctx) }
        }

        output.addTag(
            declaration.info.breadcrumbs, NodeTag.FunctionCanReturnTypeOf(
                declaration.body.info.breadcrumbs
            )
        )

        // brand-new scope for a function
        val newCtx = AnalyzerContext(ctx.fileRootScope.extend(), ctx.fileRootScope, declaration.body.info.breadcrumbs)
        for (param in declaration.params) {
            newCtx.currentScope.items[param.name.text] = ScopeItem(param.info.breadcrumbs)
        }
        analyzeBody(declaration.body, output, newCtx)
    }

    private fun analyzeNamedValueDeclaration(
        declaration: DeclarationNode.NamedValue, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        output.addTag(
            declaration.info.breadcrumbs, NodeTag.IsNamedValue(
                name = declaration.name.text,
                typeDeclaration = declaration.typeAnnotation?.info?.breadcrumbs,
                value = declaration.value.info.breadcrumbs
            )
        )
        output.addValueFlowTag(declaration.value.info.breadcrumbs, declaration.info.breadcrumbs)

        analyzeExpression(declaration.value, output, ctx)
        declaration.typeAnnotation?.let { typeAnnotation ->
            analyzeTypeReference(typeAnnotation, output, ctx)
            output.addTag(typeAnnotation.info.breadcrumbs, NodeTag.AnnotatesTypeFor(declaration.info.breadcrumbs))
        }

        if (declaration.isVariable) {
            output.addTag(declaration.info.breadcrumbs, NodeTag.IsVariable)
        }
    }

    private fun analyzeObjectTypeDeclaration(
        declaration: DeclarationNode.ObjectType, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        output.addTag(
            declaration.info.breadcrumbs, NodeTag.IsObjectType(
                name = declaration.name.text,
            )
        )

        declaration.fields?.let { fields ->
            for (field in fields) {
                output.addTag(
                    field.info.breadcrumbs, NodeTag.FieldForObjectType(
                        field.name.text,
                        objectType = declaration.info.breadcrumbs,
                        typeReference = field.typeConstraint.info.breadcrumbs,
                    )
                )
                analyzeTypeReference(field.typeConstraint, output, ctx)
            }
        }
    }

    private fun analyzeSignalTypeDeclaration(
        declaration: DeclarationNode.SignalType, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        output.addTag(
            declaration.info.breadcrumbs, NodeTag.IsSignalType(
                name = declaration.name.text,
                resultTypeReference = declaration.result.info.breadcrumbs
            )
        )

        declaration.fields?.let { fields ->
            for (field in fields) {
                output.addTag(
                    field.info.breadcrumbs, NodeTag.FieldForObjectType(
                        field.name.text,
                        objectType = declaration.info.breadcrumbs,
                        typeReference = field.typeConstraint.info.breadcrumbs,
                    )
                )
                analyzeTypeReference(field.typeConstraint, output, ctx)
            }
        }

        analyzeTypeReference(declaration.result, output, ctx)
    }

    private fun analyzeOptionTypeDeclaration(
        declaration: DeclarationNode.OptionType,
        output: AnalyzedNode,
        ctx: AnalyzerContext
    ) {
        output.addTag(declaration.info.breadcrumbs, NodeTag.IsOptionType(declaration.name.text))
        for (option in declaration.options) {
            analyzeTypeReference(option, output, ctx)
            output.addTag(option.info.breadcrumbs, NodeTag.OptionForOptionType(declaration.info.breadcrumbs))
        }
    }


    private fun analyzeBody(body: BodyNode, output: AnalyzedNode, ctx: AnalyzerContext) {
        when (body) {
            is BodyNode.BlockBodyNode -> {
                if (body.statements.isEmpty()) {
                    // if there are no statements, the block can only be Action-typed.
                    // Avoids issue with `statements.last()` below
                    output.addTag(body.info.breadcrumbs, NodeTag.IsPrimitiveValue(LangPrimitiveKind.ACTION))
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

                                currentCtx = AnalyzerContext(
                                    currentCtx.currentScope.extend(),
                                    currentCtx.fileRootScope,
                                    currentCtx.currentScopePosition
                                )
                                addDeclarationsToScope(declarations, output, currentCtx)
                            }
                            output.addTag(
                                statementNode.info.breadcrumbs,
                                NodeTag.IsDeclarationStatement(declaration = statementNode.declaration.info.breadcrumbs)
                            )
                        }

                        is StatementNode.EffectStatement -> {
                            val effectCtx = AnalyzerContext(
                                currentCtx.currentScope.extend(),
                                currentCtx.fileRootScope,
                                statementNode.info.breadcrumbs
                            )

                            statementNode.pattern.name?.let {
                                effectCtx.currentScope.items.put(
                                    it.text,
                                    ScopeItem(statementNode.pattern.info.breadcrumbs)
                                )
                            }

                            analyzeTypeReference(statementNode.pattern.typeReference, output, currentCtx)
                            output.addTag(
                                statementNode.pattern.info.breadcrumbs, NodeTag.IsPattern(
                                    name = statementNode.pattern.name?.text,
                                    typeReference = statementNode.pattern.typeReference.info.breadcrumbs
                                )
                            )
                            output.addTag(
                                statementNode.info.breadcrumbs, NodeTag.IsEffectStatement(
                                    condition = statementNode.pattern.info.breadcrumbs,
                                    effectBody = statementNode.body.info.breadcrumbs
                                )
                            )
                            analyzeBody(statementNode.body, output, effectCtx)
                        }

                        is StatementNode.SetStatement -> {
                            analyzeExpression(statementNode.expression, output, currentCtx)
                            val variable = currentCtx.currentScope.items[statementNode.identifier.text]
                            if (variable == null) {
                                output.addTag(statementNode.info.breadcrumbs, NodeTag.ReferenceNotInScope)
                            } else {
                                output.addTag(
                                    statementNode.info.breadcrumbs,
                                    NodeTag.IsSetStatement(
                                        sets = variable.origin,
                                        setTo = statementNode.expression.info.breadcrumbs
                                    )
                                )
                            }
                        }
                    }
                }
            }

            is BodyNode.SingleExpressionBodyNode -> {
                analyzeExpression(body.expression, output, ctx)
                output.addValueFlowTag(body.expression.info.breadcrumbs, body.info.breadcrumbs)
            }
        }
    }

    private fun analyzeExpression(expression: ExpressionNode, output: AnalyzedNode, ctx: AnalyzerContext) {
        when (expression) {
            is ExpressionNode.BlockExpressionNode -> analyzeBlockExpression(expression, output, ctx)
            is ExpressionNode.BranchExpressionNode -> analyzeBranchExpressionNode(expression, output, ctx)
            is ExpressionNode.IdentifierExpression -> analyzeIdentifierExpression(expression, output, ctx)
            is ExpressionNode.CauseExpression -> analyzeCauseExpression(expression, output, ctx)
            is ExpressionNode.CallExpression -> analyzeCallExpression(expression, output, ctx)
            is ExpressionNode.MemberExpression -> analyzeMemberExpression(expression, output, ctx)

            is ExpressionNode.StringLiteralExpression -> output.addTag(
                expression.info.breadcrumbs, NodeTag.IsPrimitiveValue(LangPrimitiveKind.STRING)
            )

            is ExpressionNode.IntegerLiteralExpression -> output.addTag(
                expression.info.breadcrumbs, NodeTag.IsPrimitiveValue(LangPrimitiveKind.INTEGER)
            )
        }
        output.addTag(expression.info.breadcrumbs, NodeTag.IsExpression)
    }

    private fun analyzeBranchExpressionNode(
        expression: ExpressionNode.BranchExpressionNode, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        output.addTag(expression.info.breadcrumbs, NodeTag.IsBranch)
        for (branchOption in expression.branches) {
            val type = when (branchOption) {
                is BranchOptionNode.IfBranchOptionNode -> {
                    analyzeExpression(branchOption.condition, output, ctx)
                    output.addTag(
                        branchOption.condition.info.breadcrumbs, NodeTag.ConditionFor(branchOption.info.breadcrumbs)
                    )
                    NodeTag.BranchOptionType.IF
                }

                is BranchOptionNode.ElseBranchOptionNode -> NodeTag.BranchOptionType.ELSE
            }
            output.addTag(expression.info.breadcrumbs, NodeTag.BranchOptionFor(branchOption.info.breadcrumbs, type))
            analyzeBody(branchOption.body, output, ctx)
        }
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
        } else {
            output.addTag(expression.info.breadcrumbs, NodeTag.ReferenceNotInScope)
        }
    }

    private fun analyzeCauseExpression(
        expression: ExpressionNode.CauseExpression, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        output.addTag(expression.info.breadcrumbs, NodeTag.Causes(expression.signal.info.breadcrumbs))
        analyzeExpression(expression.signal, output, ctx)
    }

    private fun analyzeCallExpression(
        expression: ExpressionNode.CallExpression, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        expression.parameters.forEachIndexed { i, parameterNode ->
            analyzeExpression(parameterNode.value, output, ctx)
            output.addValueFlowTag(parameterNode.value.info.breadcrumbs, parameterNode.info.breadcrumbs)
            output.addTag(parameterNode.info.breadcrumbs, NodeTag.ParameterForCall(expression.info.breadcrumbs, i))
        }

        analyzeExpression(expression.callee, output, ctx)
        output.addTag(expression.info.breadcrumbs, NodeTag.Calls(expression.callee.info.breadcrumbs))
    }

    private fun analyzeMemberExpression(
        expression: ExpressionNode.MemberExpression, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        analyzeExpression(expression.objectExpression, output, ctx)
        output.addTag(
            expression.info.breadcrumbs,
            NodeTag.GetsMember(expression.objectExpression.info.breadcrumbs, expression.memberIdentifier.text)
        )
    }
}