package com.dallonf.ktcause

import com.dallonf.ktcause.ast.Breadcrumbs
import com.dallonf.ktcause.ast.DeclarationNode
import com.dallonf.ktcause.ast.FileNode
import com.dallonf.ktcause.ast.TypeReferenceNode
import com.dallonf.ktcause.types.PrimitiveLangValueType

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

    data class IsPrimitiveValue(val primitiveType: PrimitiveLangValueType) : NodeTag() {
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
    private data class Scope(val items: Map<String, ScopeItem> = mutableMapOf())

    //    {
//        fun withNewScope(breadcrumbs: Breadcrumbs, f: (MutableMap<String, ScopeItem>) -> Unit) {
//
//        }
//    }
    private data class AnalyzerContext(val currentScope: Scope, val currentScopePosition: Breadcrumbs)

    fun analyzeFile(astNode: FileNode): AnalyzedNode {
        val result = AnalyzedNode()
        val rootScopeItems = mutableMapOf<String, ScopeItem>()

        // loop over top-level declarations to hoist them into file scope
        for (declaration in astNode.declarations) {
            addDeclarationToScopeMap(declaration, rootScopeItems)
        }

        val ctx = AnalyzerContext(
            currentScope = Scope(rootScopeItems), currentScopePosition = astNode.info.breadcrumbs
        )

        for (declaration in astNode.declarations) {
            analyzeDeclaration(declaration, result, ctx)
        }

        return result
    }

    private fun addDeclarationToScopeMap(
        declaration: DeclarationNode, scopeItems: MutableMap<String, ScopeItem>
    ): Boolean {
        when (declaration) {
            is DeclarationNode.Function -> {
                scopeItems[declaration.name.text] = ScopeItem(declaration.info.breadcrumbs)
                return true
            }

            is DeclarationNode.Import -> {
                for (mapping in declaration.mappings) {
                    val sourceName = mapping.sourceName.text
                    val rename = mapping.rename?.text
                    scopeItems[rename ?: sourceName] = ScopeItem(mapping.info.breadcrumbs)
                }
                return true
            }

            is DeclarationNode.NamedValue -> {
                scopeItems[declaration.name.text] = ScopeItem(declaration.info.breadcrumbs)
                return true
            }
        }
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
        TODO("Not yet implemented")
    }

    private fun analyzeNamedValueDeclaration(
        declaration: DeclarationNode.NamedValue, output: AnalyzedNode, ctx: AnalyzerContext
    ) {
        TODO("Not yet implemented")
    }

}