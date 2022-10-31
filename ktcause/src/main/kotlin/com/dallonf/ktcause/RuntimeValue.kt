package com.dallonf.ktcause

import com.dallonf.ktcause.ast.SourcePosition
import com.dallonf.ktcause.types.*
import kotlinx.serialization.json.*
import org.apache.commons.numbers.fraction.BigFraction

sealed class RuntimeValue {
    object Action : RuntimeValue() {
        override fun typeOf() = ActionValueLangType
    }

    data class BadValue(val position: SourcePosition?, val error: ErrorLangType) : RuntimeValue() {
        override fun typeOf() = error
    }

    data class Text(val value: String) : RuntimeValue() {
        override fun typeOf() = LangPrimitiveKind.TEXT.toValueLangType()
    }

    data class Number(val value: BigFraction) : RuntimeValue() {
        constructor(value: Double) : this(BigFraction.from(value))
        constructor(value: Long) : this(BigFraction.of(value.toBigInteger()))

        override fun typeOf() = LangPrimitiveKind.NUMBER.toValueLangType()

        override fun equals(other: Any?): Boolean {
            if (other !is Number) return false
            return value.compareTo(other.value) == 0
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        fun asDouble(): Double {
            return value.toDouble()
        }

        fun asLong(): Long {
            return value.toLong()
        }
    }

    data class StopgapDictionary(val map: Map<String, RuntimeValue> = emptyMap()) : RuntimeValue() {
        override fun typeOf() = StopgapDictionaryLangType
    }

    data class StopgapList(val values: List<RuntimeValue> = emptyList()) : RuntimeValue() {
        override fun typeOf() = StopgapListLangType
    }

    // TODO: probably want to make it harder to make an invalid RuntimeObject
    data class RuntimeObject(val typeDescriptor: CanonicalLangType, val values: List<RuntimeValue>) : RuntimeValue() {
        override fun typeOf() = InstanceValueLangType(typeDescriptor)
    }

    data class RuntimeTypeConstraint(val valueType: ResolvedValueLangType) : RuntimeValue() {
        fun tryGetCanonicalType(): CanonicalLangType? {
            return if (valueType is InstanceValueLangType) {
                valueType.canonicalType
            } else {
                null
            }
        }

        override fun typeOf() = ConstraintValueLangType(valueType)
    }

    data class NativeFunction internal constructor(
        val name: String, val function: (List<RuntimeValue>) -> RuntimeValue, val type: FunctionValueLangType,
    ) : RuntimeValue() {
        override fun typeOf() = type
    }

    data class Function(
        val name: String?,
        val file: CompiledFile,
        val procedureIndex: Int,
        val type: FunctionValueLangType,
        val capturedValues: List<Pair<RuntimeValue, String?>>
    ) : RuntimeValue() {
        override fun typeOf() = type
    }

    companion object {
        fun fromExport(
            file: CompiledFile, exportName: String
        ): RuntimeValue {
            val export =
                requireNotNull(file.exports[exportName]) { "The file ${file.path} doesn't export anything (at least non-private) called $exportName." }

            val value = when (export) {
                is CompiledFile.CompiledExport.Constraint -> {
                    val valueType = export.constraint.asValueType()
                    valueType.getRuntimeError()?.let {
                        BadValue(SourcePosition.Export(file.path, exportName), it)
                    } ?: RuntimeTypeConstraint(valueType as ResolvedValueLangType)
                }

                is CompiledFile.CompiledExport.Function -> {
                    if (export.type is FunctionValueLangType) {
                        val functionName = export.type.name ?: exportName
                        Function(
                            functionName, file, export.procedureIndex, export.type, capturedValues = listOf()
                        )
                    } else {
                        BadValue(
                            SourcePosition.Export(
                                file.path, exportName
                            ), export.type.getRuntimeError()!!
                        )
                    }
                }

                is CompiledFile.CompiledExport.NativeFunction -> {
                    NativeFunction(export.type.name ?: exportName, export.function, export.type)
                }

                is CompiledFile.CompiledExport.Value -> TODO()
                is CompiledFile.CompiledExport.Error -> {
                    BadValue(SourcePosition.Export(file.path, exportName), export.error)
                }
            }
            return value
        }
    }

    fun isAssignableTo(constraint: ConstraintValueLangType): Boolean {
        return when (val valueType = constraint.valueType) {
            is ActionValueLangType -> this is Action

            is FunctionValueLangType -> this is Function && this.type.isAssignableTo(constraint)
            is PrimitiveValueLangType -> when (valueType.kind) {
                LangPrimitiveKind.TEXT -> this is Text
                LangPrimitiveKind.NUMBER -> this is Number
            }

            is OptionValueLangType -> valueType.options.any {
                this.isAssignableTo(it)
            }

            is StopgapDictionaryLangType -> this is StopgapDictionary
            is StopgapListLangType -> this is StopgapList

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

    fun isAssignableTo(constraint: ConstraintReference): Boolean = when (constraint) {
        is ConstraintReference.Pending -> false
        is ConstraintReference.Error -> false
        is ConstraintReference.ResolvedConstraint -> isAssignableTo(constraint.asResolvedConstraintValue())
    }

    fun validate(): RuntimeValue {
        return when (this) {
            is Action, is BadValue, is Text, is Number, is RuntimeTypeConstraint, is NativeFunction, is Function -> this
            is RuntimeObject -> {
                for (value in values) {
                    when (val validatedValue = value.validate()) {
                        is BadValue -> return validatedValue
                        else -> {}
                    }
                }

                this
            }

            is StopgapDictionary -> {
                for (value in this.map.values) {
                    when (val validatedValue = value.validate()) {
                        is BadValue -> return validatedValue
                        else -> {}
                    }
                }

                this
            }

            is StopgapList -> {
                for (value in this.values) {
                    when (val validatedValue = value.validate()) {
                        is BadValue -> return validatedValue
                        else -> {}
                    }
                }

                this
            }
        }
    }

    fun isValid(): Boolean {
        return when (this) {
            is BadValue -> false
            is Action, is Text, is Number, is RuntimeTypeConstraint, is NativeFunction, is Function -> true
            is RuntimeObject -> this.values.all { it.isValid() }
            is StopgapDictionary -> this.map.values.all { it.isValid() }
            is StopgapList -> this.values.all { it.isValid() }
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

            is StopgapDictionary -> {
                buildJsonObject {
                    put("#type", "StopgapDictionary")
                    for ((key, value) in map.entries) {
                        put(key, value.toJson())
                    }
                }
            }

            is StopgapList -> {
                buildJsonObject {
                    put("#type", "StopgapList")
                    put("values", JsonArray(values.map { it.toJson() }))
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

            is Text -> JsonPrimitive(this.value)
        }
    }

    abstract fun typeOf(): ValueLangType
}
