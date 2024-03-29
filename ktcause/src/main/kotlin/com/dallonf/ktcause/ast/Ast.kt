package com.dallonf.ktcause.ast

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal


data class DocumentPosition(val line: Int, val column: Int) {
    override fun toString(): String {
        return "${line}:${column}"
    }
}

@Serializable(with = DocumentRangeSerializer::class)
data class DocumentRange(val start: DocumentPosition, val end: DocumentPosition) {
    override fun toString(): String = "${start}-${end}"
}

private class DocumentRangeSerializer : KSerializer<DocumentRange> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DocumentRange", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: DocumentRange) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): DocumentRange {
        TODO("Not yet implemented")
    }

}

@Serializable(with = BreadcrumbsSerializer::class)
data class Breadcrumbs(val entries: List<BreadcrumbEntry>) {
    sealed interface BreadcrumbEntry {
        data class Index(val index: Int) : BreadcrumbEntry
        data class Name(val name: String) : BreadcrumbEntry
    }

    companion object {
        fun empty() = Breadcrumbs(emptyList())

        fun parse(input: String): Breadcrumbs {
            val entries = input.split(".")
                .map { entry -> entry.toIntOrNull()?.let { BreadcrumbEntry.Index(it) } ?: BreadcrumbEntry.Name(entry) }
            return Breadcrumbs(entries)
        }
    }

    fun append(entry: BreadcrumbEntry): Breadcrumbs = Breadcrumbs(entries + entry)

    fun appendName(name: String) = append(BreadcrumbEntry.Name(name))

    fun appendIndex(index: Int) = append(BreadcrumbEntry.Index(index))

    fun up(): Breadcrumbs {
        val newEntries = this.entries.toMutableList()
        newEntries.removeLast()
        return Breadcrumbs(newEntries)
    }

    fun isEmpty() = entries.isEmpty()

    fun popStart(): Pair<BreadcrumbEntry, Breadcrumbs> {
        val newEntries = entries.drop(1)
        return Pair(entries[0], Breadcrumbs(newEntries))
    }

    override fun toString(): String {
        val segments: List<String> = entries.map {
            when (it) {
                is BreadcrumbEntry.Index -> it.index.toString()
                is BreadcrumbEntry.Name -> it.name
            }
        }
        return segments.joinToString(".")
    }
}

fun String.toBreadcrumbEntry() = Breadcrumbs.BreadcrumbEntry.Name(this)
fun Int.toBreadcrumbEntry() = Breadcrumbs.BreadcrumbEntry.Index(this)

internal fun MutableMap<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild>.put(key: String, value: AstNode) {
    put(key.toBreadcrumbEntry(), AstNode.BreadcrumbWalkChild.Node(value))
}

internal fun MutableMap<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild>.put(
    key: String, value: List<AstNode>
) {
    put(key.toBreadcrumbEntry(), AstNode.BreadcrumbWalkChild.List(value))
}

class BreadcrumbsSerializer : KSerializer<Breadcrumbs> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Breadcrumbs", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Breadcrumbs) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Breadcrumbs {
        TODO("Not yet implemented")
    }
}

@Serializable
sealed class SourcePosition {
    @Serializable
    @SerialName("SourcePosition")
    data class Source(val path: String, val breadcrumbs: Breadcrumbs, val position: DocumentRange) : SourcePosition()

    @Serializable
    @SerialName("ExportPosition")
    data class Export(val path: String, val exportName: String) : SourcePosition()
}

data class NodeInfo(val position: DocumentRange, val breadcrumbs: Breadcrumbs)

sealed interface AstNode {

    sealed interface BreadcrumbWalkChild {
        fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild>

        data class Node(val node: AstNode) : BreadcrumbWalkChild {
            override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> = node.childNodes()
        }

        data class List(val list: kotlin.collections.List<AstNode>) : BreadcrumbWalkChild {
            override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> = mapOf(
                *list.mapIndexed { i, node ->
                    Breadcrumbs.BreadcrumbEntry.Index(i) to Node(
                        node
                    )
                }.toTypedArray()
            )
        }

        fun findNode(breadcrumbs: Breadcrumbs): AstNode {
            val (entry, remainingBreadcrumbs) = breadcrumbs.popStart()

            val foundChild = childNodes()[entry]

            return if (foundChild != null) {
                if (remainingBreadcrumbs.isEmpty()) {
                    when (foundChild) {
                        is BreadcrumbWalkChild.Node -> foundChild.node
                        is BreadcrumbWalkChild.List -> error("Can't stop a breadcrumb walk in the middle of a list")
                    }
                } else {
                    foundChild.findNode(remainingBreadcrumbs)
                }
            } else {
                error("Can't find key $entry for node: $this")
            }
        }

        fun allDescendants(): Sequence<AstNode> {
            return childNodes().values.asSequence().flatMap { thisNode ->
                sequence {
                    if (thisNode is Node) {
                        yield(thisNode.node)
                    }
                    yieldAll(thisNode.allDescendants())
                }
            }
        }
    }

    fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild>

    val info: NodeInfo

    fun findNode(breadcrumbs: Breadcrumbs): AstNode = BreadcrumbWalkChild.Node(this).findNode(breadcrumbs)

    fun allDescendants() = BreadcrumbWalkChild.Node(this).allDescendants()
}

data class Identifier(override val info: NodeInfo, val text: String) : AstNode {
    override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = mapOf()
}

data class FunctionCallParameterNode(override val info: NodeInfo, val value: ExpressionNode) : AstNode {
    override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> =
        buildMap { put("value", value) }
}

data class FunctionSignatureParameterNode(
    override val info: NodeInfo, val name: Identifier, val typeReference: TypeReferenceNode?
) : AstNode {
    override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
        put("name", name)
        if (typeReference != null) {
            put("typeReference", typeReference)
        }
    }
}


sealed interface TypeReferenceNode : AstNode {
    data class IdentifierTypeReferenceNode(override val info: NodeInfo, val identifier: Identifier) :
        TypeReferenceNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("identifier", identifier)
        }
    }

    data class FunctionTypeReferenceNode(
        override val info: NodeInfo,
        val params: List<FunctionSignatureParameterNode>,
        val returnType: TypeReferenceNode
    ) : TypeReferenceNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("params", params)
            put("returnType", returnType)
        }
    }
}

data class FileNode(override val info: NodeInfo, val declarations: List<DeclarationNode>) : AstNode {
    override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
        put("declarations", declarations)
    }
}

sealed interface DeclarationNode : AstNode {
    data class Import(override val info: NodeInfo, val path: PathNode, val mappings: List<MappingNode>) :
        DeclarationNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("path", path)
            put("mappings", mappings)
        }

        data class PathNode(override val info: NodeInfo, val path: String) : AstNode {
            override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = mapOf()
        }

        data class MappingNode(override val info: NodeInfo, val sourceName: Identifier, val rename: Identifier?) :
            AstNode {
            override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
                put("sourceName", sourceName)
                if (rename != null) {
                    put("rename", rename)
                }
            }
        }
    }

    data class Function(
        override val info: NodeInfo,
        val name: Identifier,
        val params: List<FunctionSignatureParameterNode>,
        val body: BodyNode,
        val returnType: TypeReferenceNode?
    ) : DeclarationNode {

        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("name", name)
            put("params", params)
            put("body", body)
            if (returnType != null) {
                put("returnType", returnType)
            }
        }
    }

    data class NamedValue(
        override val info: NodeInfo,
        val name: Identifier,
        val typeAnnotation: TypeReferenceNode?,
        val value: ExpressionNode,
        val isVariable: Boolean = false,
    ) : DeclarationNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("name", name)
            if (typeAnnotation != null) {
                put("typeAnnotation", typeAnnotation)
            }
            put("value", value)
        }
    }

    data class ObjectType(
        override val info: NodeInfo, val name: Identifier, val fields: List<ObjectField>?
    ) : DeclarationNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("name", name)
            if (fields != null) {
                put("fields", fields)
            }
        }
    }

    data class SignalType(
        override val info: NodeInfo,
        val name: Identifier,
        val fields: List<ObjectField>?,
        val result: TypeReferenceNode?
    ) : DeclarationNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("name", name)
            if (fields != null) {
                put("fields", fields)
            }
            if (result != null) {
                put("result", result)
            }
        }
    }

    data class ObjectField(
        override val info: NodeInfo, val name: Identifier, val typeConstraint: TypeReferenceNode
    ) : AstNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("name", name)
            put("typeConstraint", typeConstraint)
        }
    }

    data class OptionType(
        override val info: NodeInfo, val name: Identifier, val options: List<TypeReferenceNode>
    ) : DeclarationNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("name", name)
            put("options", options)
        }
    }
}

sealed interface BodyNode : AstNode {
    data class BlockBodyNode(override val info: NodeInfo, val statements: List<StatementNode>) : BodyNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> =
            buildMap { put("statements", statements) }
    }

    data class SingleStatementBodyNode(override val info: NodeInfo, val statement: StatementNode) : BodyNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("statement", statement)
        }
    }
}

sealed interface StatementNode : AstNode {
    data class EffectStatement(override val info: NodeInfo, val pattern: PatternNode, val body: BodyNode) :
        StatementNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("pattern", pattern)
            put("body", body)
        }
    }

    data class SetStatement(override val info: NodeInfo, val identifier: Identifier, val expression: ExpressionNode) :
        StatementNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("identifier", identifier)
            put("expression", expression)
        }
    }

    data class ExpressionStatement(override val info: NodeInfo, val expression: ExpressionNode) : StatementNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("expression", expression)
        }
    }

    data class DeclarationStatement(override val info: NodeInfo, val declaration: DeclarationNode) : StatementNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("declaration", declaration)
        }
    }
}

sealed interface ExpressionNode : AstNode {

    data class GroupExpressionNode(override val info: NodeInfo, val expression: ExpressionNode) : ExpressionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> =
            buildMap { put("expression", expression) }
    }

    data class BlockExpressionNode(override val info: NodeInfo, val block: BodyNode.BlockBodyNode) : ExpressionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> =
            buildMap { put("block", block) }

    }

    data class FunctionExpressionNode(
        override val info: NodeInfo,
        val params: List<FunctionSignatureParameterNode>,
        val body: ExpressionNode,
        val returnType: TypeReferenceNode? = null,
    ) : ExpressionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("params", params)
            put("body", body)
            returnType?.let { put("returnType", it) }
        }
    }

    data class BranchExpressionNode(
        override val info: NodeInfo, val withValue: ExpressionNode?, val branches: List<BranchOptionNode>
    ) : ExpressionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("branches", branches)
            withValue?.let { put("withValue", it) }
        }
    }

    data class LoopExpressionNode(override val info: NodeInfo, val body: BodyNode) : ExpressionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("body", body)
        }
    }

    data class IdentifierExpression(override val info: NodeInfo, val identifier: Identifier) : ExpressionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> =
            buildMap { put("identifier", identifier) }
    }

    data class MemberExpression(
        override val info: NodeInfo, val objectExpression: ExpressionNode, val memberIdentifier: Identifier
    ) : ExpressionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("objectExpression", objectExpression)
            put("memberIdentifier", memberIdentifier)
        }
    }

    data class CauseExpression(override val info: NodeInfo, val signal: ExpressionNode) : ExpressionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> =
            buildMap { put("signal", signal) }

    }


    data class CallExpression(
        override val info: NodeInfo, val callee: ExpressionNode, val parameters: List<FunctionCallParameterNode>
    ) : ExpressionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("callee", callee)
            put("parameters", parameters)
        }


    }

    data class PipeCallExpression(
        override val info: NodeInfo,
        val subject: ExpressionNode,
        val callee: ExpressionNode,
        val parameters: List<FunctionCallParameterNode>
    ) : ExpressionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("subject", subject)
            put("callee", callee)
            put("parameters", parameters)
        }
    }

    data class StringLiteralExpression(override val info: NodeInfo, val text: String) : ExpressionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = mapOf()
    }


    data class NumberLiteralExpression(override val info: NodeInfo, val value: BigDecimal) : ExpressionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = mapOf()
    }

    data class ReturnExpression(override val info: NodeInfo, val value: ExpressionNode?) : ExpressionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            value?.let { put("value", it) }
        }
    }

    data class BreakExpression(override val info: NodeInfo, val withValue: ExpressionNode? = null) : ExpressionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            withValue?.let { put("withValue", it) }
        }
    }
}

sealed interface BranchOptionNode : AstNode {
    val body: BodyNode

    data class IfBranchOptionNode(
        override val info: NodeInfo, val condition: ExpressionNode, override val body: BodyNode
    ) : BranchOptionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("condition", condition)
            put("body", body)
        }
    }

    data class IsBranchOptionNode(
        override val info: NodeInfo, val pattern: PatternNode, override val body: BodyNode
    ) : BranchOptionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("pattern", pattern)
            put("body", body)
        }
    }

    data class ElseBranchOptionNode(override val info: NodeInfo, override val body: BodyNode) : BranchOptionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("body", body)
        }
    }

}

data class PatternNode(override val info: NodeInfo, val name: Identifier?, val typeReference: TypeReferenceNode) :
    AstNode {
    override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
        if (name != null) {
            put("name", name)
        }
        put("typeReference", typeReference)
    }
}
