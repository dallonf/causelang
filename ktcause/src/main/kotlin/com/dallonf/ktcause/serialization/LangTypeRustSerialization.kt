package com.dallonf.ktcause.serialization

import com.dallonf.ktcause.gen.LangErrorRustSerialization
import com.dallonf.ktcause.types.*
import kotlinx.serialization.json.*

object LangTypeRustSerialization {

    // Type Mapping Notes:
    //  kt ValueLangType -> rs AnyInferredLangType
    //  kt ResolvedValueLangType -> rs LangType
    //  kt OptionValueLangType -> rs OneOfLangType
    //  kt ConstraintValueLangType -> rs LangType::TypeReference


    fun serializeLangType(resolvedValueLangType: ResolvedValueLangType): JsonElement {
        return when (resolvedValueLangType) {
            is ConstraintValueLangType -> buildJsonObject {
                put(
                    "TypeReference", serializeAnyInferredLangType(resolvedValueLangType.valueType)
                )
            }

            is ActionValueLangType -> JsonPrimitive("Action")

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

            is OptionValueLangType -> buildJsonObject {
                put("OneOf", serializeOneOfLangType(resolvedValueLangType))
            }

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
            if (langType.content == "Action") {
                return ActionValueLangType
            }

            if (langType.content == "Anything") {
                return AnythingValueLangType
            }
        }

        throw AssertionError("Unrecognized lang type: $langType")
    }


    fun serializeAnyInferredLangType(valueLangType: ValueLangType): JsonElement {
        return when (valueLangType) {
            is ResolvedValueLangType -> buildJsonObject {
                put("Known", serializeLangType(valueLangType))
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

            // TODO: Pending
        }

        throw AssertionError("Unrecognized AnyInferredLangType: $anyInferredLangType")
    }


    fun serializeLangError(errorLangType: ErrorLangType): JsonElement {
        return JsonPrimitive("TODO")
    }


    fun serializeInstanceLangType(instanceValueLangType: InstanceValueLangType): JsonElement {
        return buildJsonObject {
            put("type_id", serializeCanonicalLangTypeId(instanceValueLangType.canonicalTypeId))
        }
    }

    fun deserializeInstanceValueLangType(instanceLangType: JsonElement): InstanceValueLangType {
        TODO()
    }


    fun serializeFunctionLangType(functionValueLangType: FunctionValueLangType): JsonElement {
        return buildJsonObject {
            put("name", functionValueLangType.name)
            put("params", functionValueLangType.params.map { serializeLangParam(it) }.let { JsonArray(it) })
            put("return_type", serializeAnyInferredLangType(functionValueLangType.returnConstraint.asValueType()))
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
            put("value_type", serializeAnyInferredLangType(langParameter.valueConstraint.asValueType()))
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

    fun serializeOneOfLangType(optionValueLangType: OptionValueLangType): JsonElement {
        return buildJsonObject {
            put("options",
                optionValueLangType.options.map { serializeAnyInferredLangType(it.asValueType()) }
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

    fun serializeCanonicalTypeMap(map: Map<CanonicalLangTypeId, CanonicalLangType>): JsonElement {
        return buildJsonObject {
            for ((id, type) in map.entries) {
                put(serializeCanonicalLangTypeId(id), serializeCanonicalLangType(type))
            }
        }
    }

    private fun serializeCanonicalLangType(type: CanonicalLangType): JsonElement {
        fun serializeField(objectField: CanonicalLangType.ObjectField): JsonElement {
            return buildJsonObject {
                put("name", objectField.name)
                put("value_type", serializeAnyInferredLangType(objectField.valueConstraint.asValueType()))
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
                    put("result", serializeAnyInferredLangType(type.result.asValueType()))
                })
            }
        }
    }
}