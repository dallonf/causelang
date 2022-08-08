package com.dallonf.ktcause.ast

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

data class DocumentPosition(val line: Int, val column: Int) {
    override fun toString(): String {
        return "${line}:${column}"
    }
}

data class DocumentRange(val start: DocumentPosition, val end: DocumentPosition)

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

class BreadcrumbsSerializer : KSerializer<Breadcrumbs> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Breadcrumbs", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Breadcrumbs) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Breadcrumbs {
        TODO("Not yet implemented")
    }
}

data class NodeInfo(val position: DocumentRange, val breadcrumbs: Breadcrumbs)

sealed interface AstNode {
    val info: NodeInfo
}

data class Identifier(override val info: NodeInfo, val text: String) : AstNode

sealed interface TypeReferenceNode : AstNode {
    data class IdentifierTypeReferenceNode(override val info: NodeInfo, val identifier: Identifier) : TypeReferenceNode
}

data class FileNode(override val info: NodeInfo, val declarations: List<DeclarationNode>) : AstNode

sealed interface DeclarationNode : AstNode {
    data class Import(override val info: NodeInfo, val path: PathNode, val mappings: List<MappingNode>) :
        DeclarationNode {
        data class PathNode(override val info: NodeInfo, val path: String) : AstNode
        data class MappingNode(override val info: NodeInfo, val sourceName: Identifier, val rename: Identifier?) :
            AstNode
    }

    data class Function(
        override val info: NodeInfo, val name: Identifier, val body: BodyNode /* TODO: params, return type */
    ) : DeclarationNode

    data class NamedValue(
        override val info: NodeInfo,
        val name: Identifier,
        val typeAnnotation: TypeReferenceNode?,
        val value: ExpressionNode
    ) : DeclarationNode
}

sealed interface BodyNode : AstNode {
    data class BlockBody(override val info: NodeInfo, val statements: List<StatementNode>) : BodyNode
}

sealed interface StatementNode : AstNode {
    data class ExpressionStatement(override val info: NodeInfo, val expression: ExpressionNode) : StatementNode
    data class DeclarationStatement(override val info: NodeInfo, val declaration: DeclarationNode) : StatementNode
}

sealed interface ExpressionNode : AstNode {
    data class IdentifierExpression(override val info: NodeInfo, val identifier: Identifier) : ExpressionNode
    data class CauseExpression(override val info: NodeInfo, val signal: ExpressionNode) : ExpressionNode
    data class CallExpression(
        override val info: NodeInfo, val callee: ExpressionNode, val parameters: List<ParameterNode>
    ) : ExpressionNode {
        data class ParameterNode(override val info: NodeInfo, val value: ExpressionNode) : AstNode
    }

    data class StringLiteralExpression(override val info: NodeInfo, val text: String) :
        ExpressionNode

    data class IntegerLiteralExpression(override val info: NodeInfo, val value: Long) : ExpressionNode
}