package com.dallonf.ktcause

import com.dallonf.ktcause.ast.SourcePosition
import com.dallonf.ktcause.types.*
import kotlinx.serialization.json.*

sealed class RuntimeValue {
    object Action : RuntimeValue()
    data class BadValue(val position: SourcePosition, val error: ErrorLangType) : RuntimeValue()

    data class String(val value: kotlin.String) : RuntimeValue()
    data class Integer(val value: Long) : RuntimeValue()
    data class Float(val value: Double) : RuntimeValue()
    data class Boolean(val value: kotlin.Boolean) : RuntimeValue()

    // TODO: probably want to make it harder to make an invalid RuntimeObject
    data class RuntimeObject(val typeDescriptor: CanonicalLangType, val values: List<RuntimeValue>) : RuntimeValue()

    data class RuntimeTypeReference(val type: ConstraintLangType) : RuntimeValue()

    data class RuntimeUniqueObject(val typeId: CanonicalLangTypeId) : RuntimeValue()

    // TODO: definitely don't want these to come from anywhere but the core modules
    data class NativeFunction(val name: kotlin.String, val function: (List<RuntimeValue>) -> RuntimeValue) :
        RuntimeValue()

    data class Function(
        val name: kotlin.String?,
        val file: CompiledFile,
        val chunkIndex: Int,
        val type: FunctionValueLangType,
    ) : RuntimeValue()

    fun isAssignableTo(constraint: ConstraintLangType): kotlin.Boolean {
        return when (constraint) {
            is LangType.Pending -> false
            is ErrorLangType -> false

            is FunctionConstraintLangType -> this is Function && this.type.isAssignableTo(constraint)
            is PrimitiveConstraintLangType -> when (constraint.kind) {
                LangPrimitiveKind.STRING -> this is String
                LangPrimitiveKind.INTEGER -> this is Integer
                LangPrimitiveKind.FLOAT -> this is Float
                LangPrimitiveKind.BOOLEAN -> this is Boolean
                LangPrimitiveKind.ACTION -> this is Action
            }

            is OptionConstraintLangType -> constraint.options.any {
                if (it is ResolvedConstraintLangType)
                    this.isAssignableTo(it)
                else false
            }

            is AnySignalConstraintLangType -> this is RuntimeObject && this.typeDescriptor is CanonicalLangType.SignalCanonicalLangType
            is AnythingConstraintLangType -> true

            is UniqueObjectLangType -> this is RuntimeUniqueObject && this.typeId == constraint.canonicalType.id

            is BadValueConstraintLangType -> this is BadValue

            is TypeReferenceConstraintLangType -> this is RuntimeObject && this.typeDescriptor.id == constraint.canonicalType.id


            // No associated runtime values
            NeverContinuesConstraintLangType -> false
        }
    }

    fun validate(): RuntimeValue {
        return when (this) {
            is BadValue, is Action, is String, is Integer, is Float, is Boolean, is RuntimeTypeReference, is NativeFunction, is Function, is RuntimeUniqueObject -> this
            is RuntimeObject -> {
                // TODO: we shouldn't make a brand new object if it's all valid
                val newValues = mutableListOf<RuntimeValue>()
                for (value in values) {
                    when (val validatedValue = value.validate()) {
                        is BadValue -> return validatedValue
                        else -> newValues.add(validatedValue)
                    }
                }

                this.copy(values = newValues)
            }
        }
    }

    fun isValid(): kotlin.Boolean {
        return when (this) {
            is BadValue -> false
            is Action, is String, is Integer, is Float, is Boolean, is RuntimeTypeReference, is NativeFunction, is Function, is RuntimeUniqueObject -> true
            is RuntimeObject -> this.values.all { it.isValid() }
        }
    }

    internal open fun toJson(): JsonElement {
        return when (this) {
            is RuntimeValue.Action -> buildJsonObject {
                put("#type", JsonPrimitive("Action"))
            }

            is RuntimeValue.BadValue -> buildJsonObject {
                put("#type", "BadValue")
                put("position", Debug.debugSerializer.encodeToJsonElement(this@RuntimeValue.position))
                put("error", Debug.debugSerializer.encodeToJsonElement(this@RuntimeValue.error))
            }

            is RuntimeValue.Float -> JsonPrimitive(this.value)
            is RuntimeValue.Integer -> JsonPrimitive(this.value)
            is RuntimeValue.Boolean -> JsonPrimitive(this.value)

            is RuntimeValue.NativeFunction -> buildJsonObject {
                put("#type", "NativeFunction")
                put("name", this@RuntimeValue.name)
            }

            is RuntimeValue.Function -> buildJsonObject {
                put("#type", "Function")
                if (name != null) {
                    put("name", name)
                }
            }

            is RuntimeValue.RuntimeObject -> {
                val objectTypeFields = when (val type = this.typeDescriptor) {
                    is CanonicalLangType.SignalCanonicalLangType -> type.fields
                    is CanonicalLangType.ObjectCanonicalLangType -> type.fields
                }

                val values = this.values
                buildJsonObject {
                    put("#type", JsonPrimitive(this@RuntimeValue.typeDescriptor.id.toString()))
                    for ((i, objectTypeField) in objectTypeFields.withIndex()) {
                        put(objectTypeField.name, values[i].toJson())
                    }
                }
            }

            is RuntimeValue.RuntimeTypeReference -> buildJsonObject {
                put("#type", "RuntimeTypeReference")
                if (this@RuntimeValue.type is TypeReferenceConstraintLangType) {
                    val id = when (val canonicalType = this@RuntimeValue.type.canonicalType) {
                        is CanonicalLangType.ObjectCanonicalLangType -> canonicalType.id
                        is CanonicalLangType.SignalCanonicalLangType -> canonicalType.id
                    }
                    put("id", id.toString())
                } else {
                    put("descriptor", Debug.debugSerializer.encodeToJsonElement(this@RuntimeValue.type))
                }
            }

            is RuntimeUniqueObject -> buildJsonObject {
                put("#type", "RuntimeUniqueObject")
                put("id", this@RuntimeValue.typeId.toString())
            }

            is RuntimeValue.String -> JsonPrimitive(this.value)
        }
    }
}
