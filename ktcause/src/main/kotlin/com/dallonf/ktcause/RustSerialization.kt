package com.dallonf.ktcause

import com.dallonf.ktcause.ast.FileNode
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

object RustSerialization {
    fun serializeAst(ast: FileNode): String {
        return Json.encodeToString(JsonObject(emptyMap()))
    }
}

