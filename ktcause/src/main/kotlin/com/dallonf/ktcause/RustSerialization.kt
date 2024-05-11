package com.dallonf.ktcause

import com.dallonf.ktcause.ast.Breadcrumbs
import com.dallonf.ktcause.ast.DocumentPosition
import com.dallonf.ktcause.ast.FileNode
import com.dallonf.ktcause.ast.NodeInfo
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

object RustSerialization {
    @OptIn(ExperimentalSerializationApi::class)
    val serializer by lazy {
        Json {
            prettyPrint = true
            prettyPrintIndent = "  "
        }
    }

    fun serializeAst(ast: FileNode): String {
        val astJson = buildJsonObject {
            put("info", serializeNodeInfo(ast.info))
        }

        return serializer.encodeToString(astJson)
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
        return buildJsonObject {
            put("entries", JsonArray(breadcrumbs.entries.map {
                when (it) {
                    is Breadcrumbs.BreadcrumbEntry.Index -> buildJsonObject {
                        put("Index", it.index)
                    }

                    is Breadcrumbs.BreadcrumbEntry.Name -> buildJsonObject {
                        put("Name", it.name)
                    }
                }
            }))
        }
    }
}

