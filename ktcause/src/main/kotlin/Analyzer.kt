package com.dallonf.ktcause

import com.dallonf.ktcause.ast.Breadcrumbs

data class AnalyzedNode(
    val nodeTags: MutableMap<Breadcrumbs, MutableList<NodeTag>> = mutableMapOf(),
    val filesReference: MutableSet<String> = mutableSetOf()
) {

}

sealed class NodeTag {
    data class ReferencesFile(val path: String, val exportName: String?) : NodeTag()
    data class ValueComesFrom(val source: Breadcrumbs) : NodeTag()
    data class ValueGoesTo(val destination: Breadcrumbs) : NodeTag()
    data class Calls(val callee: Breadcrumbs) : NodeTag()
    data class CalledBy(val callExpression: Breadcrumbs) : NodeTag()
    data class CallsWithParameter(val parameter: Breadcrumbs, val index: Int): NodeTag()
    data class ParameterForCall(val callExpression: Breadcrumbs, val index: Int) : NodeTag()
    data class Causes(val signal: Breadcrumbs) : NodeTag()
    data class CausedBy(val causeExpression: Breadcrumbs) : NodeTag()
//    data class IsPrimitiveValue()
}

object Analyzer {

}