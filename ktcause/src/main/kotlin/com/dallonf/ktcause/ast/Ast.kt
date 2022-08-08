package com.dallonf.ktcause.ast

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.math.exp


data class DocumentPosition(val line: Int, val column: Int) {
    override fun toString(): String {
        return "${line}:${column}"
    }
}

@Serializable(with = DocumentRangeSerializer::class)
data class DocumentRange(val start: DocumentPosition, val end: DocumentPosition)

private class DocumentRangeSerializer : KSerializer<DocumentRange> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DocumentRange", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: DocumentRange) {
        encoder.encodeString("${value.start}-${value.end}")
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
            override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
                mapOf(
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
    }

    fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild>

    val info: NodeInfo

    fun findNode(breadcrumbs: Breadcrumbs): AstNode = BreadcrumbWalkChild.Node(this).findNode(breadcrumbs)
}

data class Identifier(override val info: NodeInfo, val text: String) : AstNode {
    override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = mapOf()
}

sealed interface TypeReferenceNode : AstNode {
    data class IdentifierTypeReferenceNode(override val info: NodeInfo, val identifier: Identifier) :
        TypeReferenceNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("identifier", identifier)
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
        override val info: NodeInfo, val name: Identifier, val body: BodyNode /* TODO: params, return type */
    ) : DeclarationNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("name", name)
            put("body", body)
        }
    }

    data class NamedValue(
        override val info: NodeInfo,
        val name: Identifier,
        val typeAnnotation: TypeReferenceNode?,
        val value: ExpressionNode
    ) : DeclarationNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("name", name)
            if (typeAnnotation != null) {
                put("typeAnnotation", typeAnnotation)
            }
            put("value", value)
        }
    }
}

sealed interface BodyNode : AstNode {
    data class BlockBody(override val info: NodeInfo, val statements: List<StatementNode>) : BodyNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> =
            buildMap { put("statements", statements) }
    }
}

sealed interface StatementNode : AstNode {
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

    data class BlockExpressionNode(override val info: NodeInfo, val block: BodyNode.BlockBody) : ExpressionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> =
            buildMap { put("block", block) }

    }

    data class IdentifierExpression(override val info: NodeInfo, val identifier: Identifier) : ExpressionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> =
            buildMap { put("identifier", identifier) }
    }

    data class CauseExpression(override val info: NodeInfo, val signal: ExpressionNode) : ExpressionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> =
            buildMap { put("signal", signal) }

    }

    data class CallExpression(
        override val info: NodeInfo, val callee: ExpressionNode, val parameters: List<ParameterNode>
    ) : ExpressionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = buildMap {
            put("callee", callee)
            put("parameters", parameters)
        }

        data class ParameterNode(override val info: NodeInfo, val value: ExpressionNode) : AstNode {
            override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> =
                buildMap { put("value", value) }
        }
    }

    data class StringLiteralExpression(override val info: NodeInfo, val text: String) : ExpressionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = mapOf()
    }

    data class IntegerLiteralExpression(override val info: NodeInfo, val value: Long) : ExpressionNode {
        override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, AstNode.BreadcrumbWalkChild> = mapOf()
    }
}