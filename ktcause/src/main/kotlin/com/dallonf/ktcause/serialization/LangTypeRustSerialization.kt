package com.dallonf.ktcause.serialization

import com.dallonf.ktcause.types.*
import kotlinx.serialization.json.*

object LangTypeRustSerialization {

    // Type Mapping Notes:
    //  kt ValueLangType -> rs AnyInferredLangType
    //  kt ResolvedValueLangType -> rs LangType
    //  kt OptionValueLangType -> rs OneOfLangType


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

    fun deserializeResolvedValueLangType(langType: JsonElement): ResolvedValueLangType {
        TODO()
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

    fun serializeLangError(errorLangType: ErrorLangType): JsonElement {
        return JsonPrimitive("TODO")
    }


    fun serializeInstanceLangType(instanceValueLangType: InstanceValueLangType): JsonElement {
        return buildJsonObject {
            put("type_id", serializeCanonicalLangTypeId(instanceValueLangType.canonicalTypeId))
        }
    }


    fun serializeFunctionLangType(functionValueLangType: FunctionValueLangType): JsonElement {
        return buildJsonObject {
            put("name", functionValueLangType.name)
            // TODO: params
            put("return_type", serializeAnyInferredLangType(functionValueLangType.returnConstraint.asValueType()))
        }
    }

    fun serializePrimitiveLangType(primitiveValueLangType: PrimitiveValueLangType): JsonElement {
        return when (primitiveValueLangType.kind) {
            LangPrimitiveKind.TEXT -> JsonPrimitive("Text")
            LangPrimitiveKind.NUMBER -> JsonPrimitive("Number")
        }
    }

    fun serializeOneOfLangType(optionValueLangType: OptionValueLangType): JsonElement {
        return buildJsonObject {
            put("options",
                optionValueLangType.options.map { serializeAnyInferredLangType(it.asValueType()) }
                    .let { JsonArray(it) })
        }
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