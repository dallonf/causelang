package com.dallonf.ktcause.serialization

import com.dallonf.ktcause.NodeTag
import com.dallonf.ktcause.Resolver
import com.dallonf.ktcause.ast.*
import com.dallonf.ktcause.gen.TagsRustSerialization.serializeNodeTag
import com.dallonf.ktcause.types.ResolvedValueLangType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*

object RustSerialization {
    @OptIn(ExperimentalSerializationApi::class)
    val encoder by lazy {
        Json {
            prettyPrint = true
            prettyPrintIndent = "  "
        }
    }


    fun serializeNodeTagMap(tagMap: Map<Breadcrumbs, List<NodeTag>>): JsonElement {
        return buildJsonObject {
            tagMap.entries.forEach { entry ->
                put(breadcrumbsToString(entry.key), JsonArray(entry.value.map { serializeNodeTag(it) }))
            }
        }
    }

    fun serializeExternalFileDescriptorMap(map: Map<String, Resolver.ExternalFileDescriptor>): JsonElement {
        return buildJsonObject {
            map.entries.forEach { entry ->
                put(entry.key, serializeExternalFileDescriptor(entry.value))
            }
        }
    }

    fun serializeExternalFileDescriptor(fileDescriptor: Resolver.ExternalFileDescriptor): JsonElement {
        return buildJsonObject {
            put("exports", fileDescriptor.exports.mapValues { (_, type) ->
                require(type is ResolvedValueLangType)
                LangTypeRustSerialization.serializeLangType(type)
            }.let {
                JsonObject(it)
            })
        }
    }

    fun serializeNodeInfo(info: NodeInfo): JsonElement {
        return buildJsonObject {
            put("position", buildJsonObject {
                put("start", serializeDocumentPosition(info.position.start))
                put("end", serializeDocumentPosition(info.position.end))
            })
            put("breadcrumbs", serializeBreadcrumbs(info.breadcrumbs))
        }
    }

    fun deserializeNodeInfo(info: JsonElement): NodeInfo {
        require(info is JsonObject)
        val position = info["position"] as JsonObject
        val start = deserializeDocumentPosition(position["start"]!!)
        val end = deserializeDocumentPosition(position["end"]!!)
        val breadcrumbs = deserializeBreadcrumbs(info["breadcrumbs"]!!)

        return NodeInfo(DocumentRange(start, end), breadcrumbs)
    }

    fun serializeDocumentPosition(dp: DocumentPosition): JsonElement {
        return buildJsonObject {
            put("line", dp.line)
            put("column", dp.column)
        }
    }

    fun deserializeDocumentPosition(dp: JsonElement): DocumentPosition {
        require(dp is JsonObject)
        val line = (dp["line"] as JsonPrimitive).int
        val column = (dp["column"] as JsonPrimitive).int
        return DocumentPosition(line, column)
    }

    fun serializeBreadcrumbs(breadcrumbs: Breadcrumbs): JsonElement {
        return JsonPrimitive(breadcrumbsToString(breadcrumbs))
    }

    fun deserializeBreadcrumbs(breadcrumbs: JsonElement): Breadcrumbs {
        require(breadcrumbs is JsonPrimitive)
        return breadcrumbsFromString(breadcrumbs.content)
    }

    private fun breadcrumbsToString(breadcrumbs: Breadcrumbs) = breadcrumbs.entries.joinToString(".") {
        when (it) {
            is Breadcrumbs.BreadcrumbEntry.Name -> toSnakeCase(it.name)
            is Breadcrumbs.BreadcrumbEntry.Index -> it.index.toString()
        }
    }

    private fun breadcrumbsFromString(content: String): Breadcrumbs {
        val stringEntries = content.split('.')
        val entries = stringEntries.map {
            val number = it.toIntOrNull()
            number?.let { Breadcrumbs.BreadcrumbEntry.Index(number) } ?: Breadcrumbs.BreadcrumbEntry.Name(it)
        }
        return Breadcrumbs(entries)
    }

    private fun toSnakeCase(name: String): String {
        val pattern = "(?<=.)[A-Z]".toRegex()
        return name.replace(pattern, "_$0").lowercase()
    }

    private fun toCamelCase(name: String): String {
        val parts = name.split("_")
        val capitalizedParts = listOf(parts[0]) + parts.drop(1).map { part ->
            part.take(1).uppercase() + part.drop(1)
        }
        return capitalizedParts.joinToString()
    }
}

