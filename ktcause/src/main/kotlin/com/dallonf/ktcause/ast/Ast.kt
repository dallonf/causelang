package com.dallonf.ktcause.ast

import com.dallonf.ktcause.gen.ast_nodes.AstNode
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
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
        // TODO: could probably implement as a specialization of findBreadcrumbWalkNode
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

    fun findBreadcrumbWalkNode(breadcrumbs: Breadcrumbs): BreadcrumbWalkChild {
        val (entry, remainingBreadcrumbs) = breadcrumbs.popStart()

        val foundChild = childNodes()[entry]

        return if (foundChild != null) {
            if (remainingBreadcrumbs.isEmpty()) {
                foundChild
            } else {
                foundChild.findBreadcrumbWalkNode(remainingBreadcrumbs)
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