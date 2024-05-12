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

    fun serializeDocumentPosition(dp: DocumentPosition): JsonElement {
        return buildJsonObject {
            put("line", dp.line)
            put("column", dp.column)
        }
    }

    fun serializeBreadcrumbs(breadcrumbs: Breadcrumbs): JsonElement {
        return JsonPrimitive(breadcrumbsToString(breadcrumbs))
    }

    private fun breadcrumbsToString(breadcrumbs: Breadcrumbs) = breadcrumbs.entries.joinToString(".") {
        when (it) {
            is Breadcrumbs.BreadcrumbEntry.Name -> toSnakeCase(it.name)
            is Breadcrumbs.BreadcrumbEntry.Index -> it.index.toString()
        }
    }

    fun toSnakeCase(name: String): String {
        val pattern = "(?<=.)[A-Z]".toRegex()
        return name.replace(pattern, "_$0").lowercase()
    }
}

