package com.dallonf.ktcause.serialization

import com.dallonf.ktcause.types.*
import kotlinx.serialization.json.*

object LangTypeRustSerialization {

    // Type Mapping Notes:
    //  kt ValueLangType -> rs AnyInferredLangType
    //  kt ResolvedValueLangType -> rs LangType
    //  kt OptionValueLangType -> rs OneOfLangType

    private fun serializeCanonicalLangTypeId(canonicalLangTypeId: CanonicalLangTypeId): JsonElement {
        return buildJsonObject {
            put("path", canonicalLangTypeId.path)
            put("parent_name", canonicalLangTypeId.parentName)
            put("name", canonicalLangTypeId.name)
            put("number", canonicalLangTypeId.number.toInt())
            put("category", serializeCanonicalLangTypeCategory(canonicalLangTypeId.category))
            put("is_unique", canonicalLangTypeId.isUnique)
        }
    }

    private fun serializeCanonicalLangTypeCategory(category: CanonicalLangTypeId.CanonicalLangTypeIdCategory): JsonElement {
        return when (category) {
            CanonicalLangTypeId.CanonicalLangTypeIdCategory.OBJECT -> JsonPrimitive("Object")
            CanonicalLangTypeId.CanonicalLangTypeIdCategory.SIGNAL -> JsonPrimitive("Signal")
        }
    }


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
        return JsonPrimitive("TODO")
    }

    private fun serializePrimitiveLangType(primitiveValueLangType: PrimitiveValueLangType): JsonElement {
        return JsonPrimitive("TODO")
    }

    private fun serializeOneOfLangType(optionValueLangType: OptionValueLangType): JsonElement {
        return JsonPrimitive("TODO")
    }
}