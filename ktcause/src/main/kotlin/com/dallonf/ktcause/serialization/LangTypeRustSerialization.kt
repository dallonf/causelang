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
            put(
                "options",
                optionValueLangType.options.map { serializeAnyInferredLangType(it.asValueType()) }
                    .let { JsonArray(it) })
        }
    }
}