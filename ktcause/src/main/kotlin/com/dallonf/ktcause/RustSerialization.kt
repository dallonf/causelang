package com.dallonf.ktcause

import com.dallonf.ktcause.ast.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

object RustSerialization {
    @OptIn(ExperimentalSerializationApi::class)
    val encoder by lazy {
        Json {
            prettyPrint = true
            prettyPrintIndent = "  "
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
        return JsonPrimitive(breadcrumbs.entries.map {
            when (it) {
                is Breadcrumbs.BreadcrumbEntry.Name -> toSnakeCase(it.name)
                is Breadcrumbs.BreadcrumbEntry.Index -> it.index.toString()
            }
        }.joinToString("."))
    }

    fun toSnakeCase(name: String): String {
        val pattern = "(?<=.)[A-Z]".toRegex()
        return name.replace(pattern, "_$0").lowercase()
    }
}

