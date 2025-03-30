package com.dallonf.ktcause.serialization

import com.dallonf.ktcause.gen.LangErrorRustSerialization
import com.dallonf.ktcause.gen.LangErrorRustSerialization.serializeLangError
import com.dallonf.ktcause.types.*
import kotlinx.serialization.json.*

object LangTypeRustSerialization {

    // Type Mapping Notes:
    //  kt ValueLangType -> rs AnyOldResolvingLangType
    //  kt ResolvedValueLangType -> rs OldResolvingLangType
    //  kt OptionValueLangType -> rs OneOfOldResolvingLangType
    //  kt ConstraintValueLangType -> rs LangType::TypeReference


    fun serializeOldResolvingLangType(resolvedValueLangType: ResolvedValueLangType): JsonElement {
        return when (resolvedValueLangType) {
            is ConstraintValueLangType -> buildJsonObject {
                put(
                    "TypeReference", serializeAnyOldResolvingLangType(resolvedValueLangType.valueType)
                )
            }

            is ActionValueLangType -> JsonPrimitive("Action")
            is NeverContinuesValueLangType -> JsonPrimitive("NeverContinues")

            is InstanceValueLangType -> buildJsonObject {
                put("Instance", serializeInstanceLangType(resolvedValueLangType))
            }

            is FunctionValueLangType -> buildJsonObject {
                put("Function", serializeFunctionLangType(resolvedValueLangType))
            }

            is PrimitiveValueLangType -> buildJsonObject {
                put("Primitive", serializePrimitiveLangType(resolvedValueLangType))
            }

            is AnythingValueLangType -> JsonPrimitive("Anything")
            is AnySignalValueLangType -> JsonPrimitive("AnySignal")

            is OptionValueLangType -> buildJsonObject {
                put("OneOf", serializeOneOfOldResolvingLangType(resolvedValueLangType))
            }

            is StopgapDictionaryLangType -> JsonPrimitive("StopgapDictionary")
            is StopgapListLangType -> JsonPrimitive("StopgapList")

            is BadValueLangType -> JsonPrimitive("BadValue")

            else -> TODO("Unsupported lang type: ${resolvedValueLangType::class.simpleName}")
        }
    }

    fun deserializeConstraintValueLangType(langType: JsonElement): ConstraintValueLangType {
        val typeReference = deserializeValueLangType(langType)
        return ConstraintValueLangType(typeReference as ResolvedValueLangType)
    }

    fun deserializeResolvedValueLangType(langType: JsonElement): ResolvedValueLangType {
        if (langType is JsonObject) {
            langType["TypeReference"]?.let {
                return deserializeConstraintValueLangType(it)
            }

            langType["Instance"]?.let {
                return deserializeInstanceValueLangType(it)
            }

            langType["Function"]?.let {
                return deserializeFunctionValueLangType(it)
            }

            langType["Primitive"]?.let {
                return deserializePrimitiveValueLangType(it)
            }

            langType["OneOf"]?.let {
                return deserializeOptionValueLangType(it)
            }
        }

        if (langType is JsonPrimitive) {
            when (langType.content) {
                "Action" -> {
                    return ActionValueLangType
                }

                "Anything" -> {
                    return AnythingValueLangType
                }

                "AnySignal" -> {
                    return AnySignalValueLangType
                }

                "NeverContinues" -> {
                    return NeverContinuesValueLangType
                }

                "StopgapDictionary" -> {
                    return StopgapDictionaryLangType
                }

                "StopgapList" -> {
                    return StopgapListLangType
                }
            }
        }

        throw AssertionError("Unrecognized lang type: $langType")
    }


    fun serializeAnyOldResolvingLangType(valueLangType: ValueLangType): JsonElement {
        return when (valueLangType) {
            is ResolvedValueLangType -> buildJsonObject {
                put("Known", serializeOldResolvingLangType(valueLangType))
            }

            is ErrorLangType -> buildJsonObject {
                put("Error", serializeLangError(valueLangType))
            }

            ValueLangType.Pending -> throw AssertionError("Can't serialize a Kotlin Pending type")
        }
    }

    fun deserializeValueLangType(anyInferredLangType: JsonElement): ValueLangType {
        if (anyInferredLangType is JsonObject) {
            anyInferredLangType["Known"]?.let {
                return deserializeResolvedValueLangType(it)
            }

            anyInferredLangType["Error"]?.let {
                return LangErrorRustSerialization.deserializeErrorLangType(it)
            }

            anyInferredLangType["InferenceVariable"]?.let {
                return ValueLangType.Pending
            }
        }

        throw AssertionError("Unrecognized AnyInferredLangType: $anyInferredLangType")
    }


    fun serializeInstanceLangType(instanceValueLangType: InstanceValueLangType): JsonElement {
        return buildJsonObject {
            put("type_id", serializeCanonicalLangTypeId(instanceValueLangType.canonicalTypeId))
        }
    }

    fun deserializeInstanceValueLangType(instanceLangType: JsonElement): InstanceValueLangType {
        require(instanceLangType is JsonObject)
        val canonicalTypeId = deserializeCanonicalLangTypeId((instanceLangType["type_id"]!! as JsonPrimitive).content)

        return InstanceValueLangType(canonicalTypeId)
    }


    fun serializeFunctionLangType(functionValueLangType: FunctionValueLangType): JsonElement {
        return buildJsonObject {
            put("name", functionValueLangType.name)
            put("params", functionValueLangType.params.map { serializeLangParam(it) }.let { JsonArray(it) })
            put("return_type", serializeAnyOldResolvingLangType(functionValueLangType.returnConstraint.asValueType()))
        }
    }

    fun deserializeFunctionValueLangType(functionLangType: JsonElement): FunctionValueLangType {
        require(functionLangType is JsonObject)
        val name = (functionLangType["name"] as JsonPrimitive).content
        val params = (functionLangType["params"] as JsonArray).map { deserializeLangParam(it) }
        val returnType = deserializeValueLangType(functionLangType["return_type"]!!)

        return FunctionValueLangType(name, returnType.valueToConstraintReference(), params)
    }

    fun serializeLangParam(langParameter: LangParameter): JsonElement {
        return buildJsonObject {
            put("name", langParameter.name)
            put("value_type", serializeAnyOldResolvingLangType(langParameter.valueConstraint.asValueType()))
        }
    }

    fun deserializeLangParam(langParameter: JsonElement): LangParameter {
        require(langParameter is JsonObject)
        val name = (langParameter["name"] as JsonPrimitive).content
        val valueConstraint = deserializeValueLangType(langParameter["value_type"]!!).valueToConstraintReference()
        return LangParameter(name, valueConstraint)
    }


    fun serializePrimitiveLangType(primitiveValueLangType: PrimitiveValueLangType): JsonElement {
        return when (primitiveValueLangType.kind) {
            LangPrimitiveKind.TEXT -> JsonPrimitive("Text")
            LangPrimitiveKind.NUMBER -> JsonPrimitive("Number")
        }
    }

    fun deserializePrimitiveValueLangType(primitiveLangType: JsonElement): PrimitiveValueLangType {
        if (primitiveLangType is JsonPrimitive) {
            if (primitiveLangType.content == "Text") {
                return PrimitiveValueLangType(LangPrimitiveKind.TEXT)
            }
            if (primitiveLangType.content == "Number") {
                return PrimitiveValueLangType(LangPrimitiveKind.NUMBER)
            }
        }

        throw AssertionError("Unrecognized primitive lang type: $primitiveLangType")
    }

    fun serializeOneOfOldResolvingLangType(optionValueLangType: OptionValueLangType): JsonElement {
        return buildJsonObject {
            put(
                "options",
                optionValueLangType.options.map { serializeAnyOldResolvingLangType(it.asValueType()) }
                    .let { JsonArray(it) })
        }
    }

    fun deserializeOptionValueLangType(oneOfLangType: JsonElement): OptionValueLangType {
        require(oneOfLangType is JsonObject)
        val options = (oneOfLangType["options"] as JsonArray).map { deserializeValueLangType(it) }
            .map { it.valueToConstraintReference() }
        return OptionValueLangType(options)
    }

    fun serializeCanonicalLangTypeId(canonicalLangTypeId: CanonicalLangTypeId): String {
        val nameWithFallback = canonicalLangTypeId.name ?: "$?"
        val fullName = if (canonicalLangTypeId.parentName != null) {
            "${canonicalLangTypeId.parentName}.$nameWithFallback"
        } else {
            nameWithFallback
        }
        val numberIfApplicable = if (canonicalLangTypeId.number == 0U) {
            ""
        } else {
            "_${canonicalLangTypeId.number}"
        }

        val category = when (canonicalLangTypeId.category) {
            CanonicalLangTypeId.CanonicalLangTypeIdCategory.OBJECT -> 'O'
            CanonicalLangTypeId.CanonicalLangTypeIdCategory.SIGNAL -> 'S'
        }

        val unique = if (canonicalLangTypeId.isUnique) {
            "!"
        } else {
            ""
        }

        return "${canonicalLangTypeId.path}:$category:$fullName$numberIfApplicable$unique"
    }

    fun deserializeCanonicalLangTypeId(canonicalLangTypeId: String): CanonicalLangTypeId {
        var (path, categoryStr, combinedName) = canonicalLangTypeId.split(":")

        val category = when (categoryStr) {
            "O" -> CanonicalLangTypeId.CanonicalLangTypeIdCategory.OBJECT
            "S" -> CanonicalLangTypeId.CanonicalLangTypeIdCategory.SIGNAL
            else -> throw AssertionError("Unrecognized canonical type category indicator: $categoryStr")
        }
        val unique = if (combinedName.endsWith("!")) {
            combinedName = combinedName.removeSuffix("!")
            true
        } else {
            false
        }
        val numberSuffixRegex = Regex("""_(\d+)$""")
        val number = numberSuffixRegex.find(combinedName)?.let { match ->
            combinedName = combinedName.removeSuffix(match.value)
            match.groups[0]!!.value.toUInt()
        } ?: 0U

        val (parentName, maybeName) = if (combinedName.contains(".")) {
            combinedName.split(".")
        } else {
            listOf(null, combinedName)
        }
        val name = if (maybeName == "$?") {
            null
        } else {
            maybeName
        }

        return CanonicalLangTypeId(path, parentName, name, number, category, unique)
    }

    fun serializeCanonicalTypeMap(map: Map<CanonicalLangTypeId, CanonicalLangType>): JsonElement {
        return buildJsonObject {
            for ((id, type) in map.entries) {
                put(serializeCanonicalLangTypeId(id), serializeCanonicalLangType(type))
            }
        }
    }

    fun deserializeCanonicalTypeMap(map: JsonElement): Map<CanonicalLangTypeId, CanonicalLangType> {
        return (map as JsonObject).map {
            val id = deserializeCanonicalLangTypeId(it.key)
            val type = deserializeCanonicalLangType(it.value)
            id to type
        }.toMap()
    }

    private fun serializeCanonicalLangType(type: CanonicalLangType): JsonElement {
        fun serializeField(objectField: CanonicalLangType.ObjectField): JsonElement {
            return buildJsonObject {
                put("name", objectField.name)
                put("value_type", serializeAnyOldResolvingLangType(objectField.valueConstraint.asValueType()))
            }
        }

        return when (type) {
            is CanonicalLangType.ObjectCanonicalLangType -> buildJsonObject {
                put("Object", buildJsonObject {
                    put("type_id", serializeCanonicalLangTypeId(type.id))
                    put("fields", type.fields.map { serializeField(it) }.let {
                        JsonArray(it)
                    })
                })
            }

            is CanonicalLangType.SignalCanonicalLangType -> buildJsonObject {
                put("Signal", buildJsonObject {
                    put("type_id", serializeCanonicalLangTypeId(type.id))
                    put("fields", type.fields.map { serializeField(it) }.let {
                        JsonArray(it)
                    })
                    put("result", serializeAnyOldResolvingLangType(type.result.asValueType()))
                })
            }
        }
    }

    fun deserializeCanonicalLangType(type: JsonElement): CanonicalLangType {
        fun deserializeField(field: JsonElement): CanonicalLangType.ObjectField {
            require(field is JsonObject)
            val name = (field["name"] as JsonPrimitive).content
            val valueConstraint = deserializeValueLangType(field["value_type"]!!).valueToConstraintReference()
            return CanonicalLangType.ObjectField(name, valueConstraint)
        }

        require(type is JsonObject)

        type["Object"]?.let { objectType ->
            require(objectType is JsonObject)
            val typeId = deserializeCanonicalLangTypeId((objectType["type_id"] as JsonPrimitive).content)
            val fields = (objectType["fields"] as JsonArray).map { deserializeField(it) }
            return CanonicalLangType.ObjectCanonicalLangType(typeId, typeId.name!!, fields)
        }

        type["Signal"]?.let { signalType ->
            require(signalType is JsonObject)
            val typeId = deserializeCanonicalLangTypeId((signalType["type_id"] as JsonPrimitive).content)
            val fields = (signalType["fields"] as JsonArray).map { deserializeField(it) }
            val result = deserializeValueLangType(signalType["result"]!!).valueToConstraintReference()
            return CanonicalLangType.SignalCanonicalLangType(typeId, typeId.name!!, fields, result)
        }

        throw AssertionError("Unrecognized canonical type: $type")
    }
}