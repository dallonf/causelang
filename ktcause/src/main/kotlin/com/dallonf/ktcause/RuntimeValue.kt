package com.dallonf.ktcause

import com.dallonf.ktcause.ast.SourcePosition
import com.dallonf.ktcause.types.*
import kotlinx.serialization.json.*

sealed class RuntimeValue {
    object Action : RuntimeValue()

    data class BadValue(val position: SourcePosition, val error: ErrorLangType) : RuntimeValue()

    data class String(val value: kotlin.String) : RuntimeValue()
    data class Number(val value: Double) : RuntimeValue()
    data class Count(val value: Long) : RuntimeValue()

    // TODO: probably want to make it harder to make an invalid RuntimeObject
    data class RuntimeObject(val typeDescriptor: CanonicalLangType, val values: List<RuntimeValue>) : RuntimeValue()

    data class RuntimeTypeConstraint(val valueType: ResolvedValueLangType) : RuntimeValue() {
        fun tryGetCanonicalType(): CanonicalLangType? {
            return if (valueType is InstanceValueLangType) {
                valueType.canonicalType
            } else {
                null
            }
        }
    }

    // TODO: definitely don't want these to come from anywhere but the core modules
    data class NativeFunction(val name: kotlin.String, val function: (List<RuntimeValue>) -> RuntimeValue) :
        RuntimeValue()

    data class Function(
        val name: kotlin.String?,
        val file: CompiledFile,
        val chunkIndex: Int,
        val type: FunctionValueLangType,
        val capturedValues: List<RuntimeValue>
    ) : RuntimeValue()

    fun isAssignableTo(constraint: ConstraintValueLangType): kotlin.Boolean {
        return when (val valueType = constraint.valueType) {
            is ActionValueLangType -> this is Action

            is FunctionValueLangType -> this is Function && this.type.isAssignableTo(constraint)
            is PrimitiveValueLangType -> when (valueType.kind) {
                LangPrimitiveKind.STRING -> this is String
                LangPrimitiveKind.COUNT -> this is Count
                LangPrimitiveKind.NUMBER -> this is Number
            }

            is OptionValueLangType -> valueType.options.any {
                this.isAssignableTo(it)
            }

            is AnySignalValueLangType -> this is RuntimeObject && this.typeDescriptor is CanonicalLangType.SignalCanonicalLangType
            is AnythingValueLangType -> true

            is BadValueLangType -> this is BadValue

            is InstanceValueLangType -> (this is RuntimeObject && this.typeDescriptor.id == valueType.canonicalType.id) || (valueType.canonicalType.isUnique() && this is RuntimeTypeConstraint && (this.tryGetCanonicalType()
                ?.let { it.id == valueType.canonicalType.id } ?: false))

            // this could _theoretically_ be a thing in some scenarios, but none that I can think of off the top of
            // my head
            is ConstraintValueLangType -> TODO()


            // No associated runtime values
            NeverContinuesValueLangType -> false
        }
    }

    fun isAssignableTo(constraint: ConstraintReference): kotlin.Boolean = when (constraint) {
        is ConstraintReference.Pending -> false
        is ConstraintReference.Error -> false
        is ConstraintReference.ResolvedConstraint -> isAssignableTo(constraint.asResolvedConstraintValue())
    }

    fun validate(): RuntimeValue {
        return when (this) {
            is Action, is BadValue, is String, is Count, is Number, is RuntimeTypeConstraint, is NativeFunction, is Function -> this
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
            is Action, is String, is Count, is Number, is RuntimeTypeConstraint, is NativeFunction, is Function -> true
            is RuntimeObject -> this.values.all { it.isValid() }
        }
    }

    internal open fun toJson(): JsonElement {
        return when (this) {
            is Action -> buildJsonObject {
                put("#type", JsonPrimitive("Action"))
            }

            is BadValue -> buildJsonObject {
                put("#type", "BadValue")
                put("position", Debug.debugSerializer.encodeToJsonElement(this@RuntimeValue.position))
                put("error", Debug.debugSerializer.encodeToJsonElement(this@RuntimeValue.error))
            }

            is Number -> JsonPrimitive(this.value)
            is Count -> JsonPrimitive(this.value)

            is NativeFunction -> buildJsonObject {
                put("#type", "NativeFunction")
                put("name", this@RuntimeValue.name)
            }

            is Function -> buildJsonObject {
                put("#type", "Function")
                if (name != null) {
                    put("name", name)
                }
            }

            is RuntimeObject -> {
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

            is RuntimeTypeConstraint -> buildJsonObject {
                put("#type", "RuntimeTypeConstraint")
                val idShortcut = (this@RuntimeValue.valueType as? InstanceValueLangType)?.let {
                    when (val canonicalType = it.canonicalType) {
                        is CanonicalLangType.ObjectCanonicalLangType -> canonicalType.id
                        is CanonicalLangType.SignalCanonicalLangType -> canonicalType.id
                    }
                }

                if (idShortcut != null) {
                    put("id", idShortcut.toString())
                } else {
                    put("descriptor", Debug.debugSerializer.encodeToJsonElement(this@RuntimeValue.valueType))
                }
            }

            is String -> JsonPrimitive(this.value)
        }
    }
}
