package com.dallonf.ktcause.serialization

import com.dallonf.ktcause.types.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

object LangTypeRustSerialization {
    fun serializeLangType(type: ValueLangType): JsonElement {
        return JsonNull
    }
}