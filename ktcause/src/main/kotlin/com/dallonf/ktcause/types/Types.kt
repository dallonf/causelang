package com.dallonf.ktcause.types

import com.dallonf.ktcause.ast.SourcePosition
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = CanonicalLangTypeIdSerializer::class)
data class CanonicalLangTypeId(
    val path: String,
    val parentName: String? = null,
    val name: String?,
    val number: UByte,
) {
    override fun toString(): String {
        val name = name ?: "<anonymous>"
        val numberIfApplicable = if (number == 0.toUByte()) "" else number

        return if (parentName != null) {
            "${path}:${parentName}_${name}${numberIfApplicable}"
        } else {
            "${path}:${name}${numberIfApplicable}"
        }
    }
}

class CanonicalLangTypeIdSerializer : KSerializer<CanonicalLangTypeId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("CanonicalLangTypeId", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: CanonicalLangTypeId) = encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): CanonicalLangTypeId {
        TODO("Not yet implemented")
    }
}


@Serializable
sealed interface CanonicalLangType {
    val id: CanonicalLangTypeId

    fun isPending(): Boolean
    fun getError(): ErrorLangType?

    fun isUnique(): Boolean

    @Serializable
    @SerialName("Signal")
    data class SignalCanonicalLangType(
        override val id: CanonicalLangTypeId,
        val name: String,
        var fields: List<ObjectField>,
        var result: ConstraintReference,
    ) : CanonicalLangType {
        override fun isUnique() = fields.isEmpty()

        @Transient
        private var recursivePending: Int = 0
        override fun isPending(): Boolean {
            if (recursivePending > 0) return false
            recursivePending += 1
            return (result.isPending() || fields.any { it.valueConstraint.isPending() }).also {
                recursivePending -= 1
            }
        }

        @Transient
        private var recursiveError: Int = 0

        override fun getError(): ErrorLangType? {
            if (recursiveError > 0) return null
            recursiveError += 1
            return (result.getError()
                ?: fields.firstNotNullOfOrNull { it.valueConstraint.getError() }).also { recursiveError -= 1 }
        }
    }

    @Serializable
    @SerialName("Object")
    data class ObjectCanonicalLangType(
        override val id: CanonicalLangTypeId, var name: String, var fields: List<ObjectField>
    ) : CanonicalLangType {

        override fun isUnique() = fields.isEmpty()

        @Transient
        private var recursivePending: Int = 0
        override fun isPending(): Boolean {
            if (recursivePending > 0) return false
            recursivePending += 1
            return fields.any { it.valueConstraint.isPending() }.also {
                recursivePending -= 1
            }
        }

        @Transient
        private var recursiveError: Int = 0

        override fun getError(): ErrorLangType? {
            if (recursiveError > 0) return null
            recursiveError += 1
            return fields.firstNotNullOfOrNull { it.valueConstraint.getError() }.also { recursiveError -= 1 }
        }
    }

    @Serializable
    data class ObjectField(val name: String, val valueConstraint: ConstraintReference) {
        fun asLangParameter() = LangParameter(name, valueConstraint)
    }

    fun asConstraintReference() = ConstraintReference.ResolvedConstraint(InstanceValueLangType(this))
}

@Serializable
data class LangParameter(
    val name: String,
    val valueConstraint: ConstraintReference,
)

sealed interface ValueLangType {
    @Serializable
    @SerialName("Pending")
    object Pending : ValueLangType {
        override fun isPending() = true
    }

    fun isPending(): Boolean

    fun getRuntimeError(): ErrorLangType? = when (this) {
        is ErrorLangType -> this
        Pending -> ErrorLangType.NeverResolved
        is ResolvedValueLangType -> null
    }

    fun getError(): ErrorLangType? = null

    fun expectConstraint(): ValueLangType {
        return when (this) {
            is ConstraintValueLangType, is Pending, is ErrorLangType -> this
            is ResolvedValueLangType -> ErrorLangType.ValueUsedAsConstraint(this)
        }
    }

    fun asConstraintReference(): ConstraintReference {
        return when (this) {
            is Pending -> ConstraintReference.Pending
            is ErrorLangType -> ConstraintReference.Error(this)
            is ConstraintValueLangType -> ConstraintReference.ResolvedConstraint(this.valueType)
            is ResolvedValueLangType -> ConstraintReference.Error(ErrorLangType.ValueUsedAsConstraint(this))
        }
    }

    fun valueToConstraintReference(): ConstraintReference {
        return this.letIfResolved { it.toConstraint() }.asConstraintReference()
    }

    fun <T> letIfResolved(f: (ResolvedValueLangType) -> T, otherwise: T): T {
        return if (this is ResolvedValueLangType) {
            f(this)
        } else {
            otherwise
        }
    }

    fun letIfResolved(f: (ResolvedValueLangType) -> ValueLangType): ValueLangType {
        return letIfResolved(f, this)
    }

    fun letIfError(f: (ErrorLangType) -> ValueLangType): ValueLangType {
        return if (this is ErrorLangType) {
            f(this)
        } else {
            this
        }
    }
}


sealed interface ErrorLangType : ValueLangType {
    override fun isPending() = false

    override fun getError() = this

    @Serializable
    @SerialName("NeverResolved")
    object NeverResolved : ErrorLangType

    @Serializable
    @SerialName("NotInScope")
    object NotInScope : ErrorLangType

    @Serializable
    @SerialName("FileNotFound")
    object FileNotFound : ErrorLangType

    @Serializable
    @SerialName("ImportPathInvalid")
    object ImportPathInvalid : ErrorLangType

    @Serializable
    @SerialName("ExportNotFound")
    object ExportNotFound : ErrorLangType

    @Serializable
    @SerialName("ProxyError")
    data class ProxyError(val actualError: ErrorLangType, val proxyChain: List<SourcePosition>) : ErrorLangType {
        companion object {
            fun from(error: ErrorLangType, source: SourcePosition): ProxyError {
                return if (error is ProxyError) {
                    ProxyError(
                        error.actualError, listOf(source) + error.proxyChain
                    )
                } else {
                    ProxyError(
                        error, listOf(
                            source
                        )
                    )
                }
            }
        }
    }

    @Serializable
    @SerialName("NotCallable")
    object NotCallable : ErrorLangType

    @Serializable
    @SerialName("NotCausable")
    object NotCausable : ErrorLangType

    @Serializable
    @SerialName("ImplementationTodo")
    data class ImplementationTodo(val description: String) : ErrorLangType

    @Serializable
    @SerialName("MismatchedType")
    data class MismatchedType(val expected: ConstraintValueLangType, val actual: ResolvedValueLangType) : ErrorLangType

    @Serializable
    @SerialName("MissingParameters")

    data class MissingParameters(val names: List<String>) : ErrorLangType

    @Serializable
    @SerialName("ExcessParameters")
    data class ExcessParameter(val expected: Int) : ErrorLangType

    @Serializable
    @SerialName("UnknownParameter")
    object UnknownParameter : ErrorLangType

    @Serializable
    @SerialName("MissingElseBranch")
    data class MissingElseBranch(val options: OptionValueLangType?) : ErrorLangType

    @Serializable
    @SerialName("UnreachableBranch")
    data class UnreachableBranch(val options: OptionValueLangType?) : ErrorLangType

    @Serializable
    @SerialName("ActionIncompatibleWithValueTypes")
    data class ActionIncompatibleWithValueTypes(
        val actions: List<SourcePosition>, val types: List<ValueType>
    ) : ErrorLangType {
        @Serializable
        data class ValueType(val type: ValueLangType, val position: SourcePosition)
    }

    @Serializable
    @SerialName("ConstraintUsedAsValue")
    data class ConstraintUsedAsValue(val type: ConstraintValueLangType) : ErrorLangType

    @Serializable
    @SerialName("ValueUsedAsConstraint")
    data class ValueUsedAsConstraint(val type: ValueLangType) : ErrorLangType

    @Serializable
    @SerialName("DoesNotHaveAnyMembers")
    object DoesNotHaveAnyMembers : ErrorLangType

    @Serializable
    @SerialName("DoesNotHaveMember")
    object DoesNotHaveMember : ErrorLangType

    @Serializable
    @SerialName("NotVariable")
    object NotVariable : ErrorLangType

    @Serializable
    @SerialName("OuterVariable")
    object OuterVariable : ErrorLangType

    @Serializable
    @SerialName("CannotBreakHere")
    object CannotBreakHere : ErrorLangType
}

sealed interface ResolvedValueLangType : ValueLangType {
    override fun isPending() = false

    fun isAssignableTo(constraint: ConstraintValueLangType): Boolean {
        return when (val constraintInstanceType = constraint.valueType) {
            is InstanceValueLangType -> {
                this is InstanceValueLangType && this.canonicalType.id == constraintInstanceType.canonicalType.id ||
                        // handle unique types
                        (this is ConstraintValueLangType && this.tryGetCanonicalType()
                            ?.let { it.isUnique() && it.id == constraintInstanceType.canonicalType.id } ?: false)
            }

            is FunctionValueLangType -> {
                this is FunctionValueLangType && run {
                    val paramsMatch = this.params == constraintInstanceType.params
                    val returnConstraintMatches = run {
                        val returnValue = this.returnConstraint.asValueType() as? ResolvedValueLangType
                        val constraintReturnConstraint = constraintInstanceType.returnConstraint.asConstraintValue() as? ConstraintValueLangType

                        returnValue != null && constraintReturnConstraint != null && returnValue.isAssignableTo(constraintReturnConstraint)
                    }

                    paramsMatch && returnConstraintMatches
                }
            }

            is PrimitiveValueLangType -> this is PrimitiveValueLangType && this.kind == constraintInstanceType.kind
            is OptionValueLangType -> constraintInstanceType.isSupersetOf(this)

            AnySignalValueLangType -> this is InstanceValueLangType && this.canonicalType is CanonicalLangType.SignalCanonicalLangType
            AnythingValueLangType -> true

            ActionValueLangType -> this is ActionValueLangType
            BadValueLangType -> this is BadValueLangType
            NeverContinuesValueLangType -> this is NeverContinuesValueLangType

            is ConstraintValueLangType -> this is ConstraintValueLangType && this.valueType.isAssignableTo(
                constraintInstanceType
            )
        }
    }

    fun toConstraint() = ConstraintValueLangType(this)
}

@Serializable
@SerialName("Constraint")
data class ConstraintValueLangType(val valueType: ResolvedValueLangType) : ResolvedValueLangType {
    override fun isPending() = valueType.isPending()

    fun tryGetCanonicalType(): CanonicalLangType? {
        return if (valueType is InstanceValueLangType) {
            valueType.canonicalType
        } else {
            null
        }
    }
}

@Serializable
sealed class ConstraintReference {
    @Serializable
    @SerialName("Resolved")
    data class ResolvedConstraint(val valueType: ResolvedValueLangType) : ConstraintReference() {
        fun asResolvedConstraintValue() = valueType.toConstraint()
    }

    @Serializable
    @SerialName("Pending")
    object Pending : ConstraintReference()

    @Serializable
    @SerialName("Error")
    data class Error(val errorType: ErrorLangType) : ConstraintReference()

    fun isPending() = when (this) {
        is Pending -> true
        is ResolvedConstraint -> this.valueType.isPending()
        else -> false
    }


    fun getError() = when (this) {
        is Error -> this.errorType
        is ResolvedConstraint -> this.valueType.getError()
        else -> null
    }

    fun asConstraintValue() = when (this) {
        is ResolvedConstraint -> this.asResolvedConstraintValue()
        is Pending -> ValueLangType.Pending
        is Error -> this.errorType
    }

    fun asValueType() = when (this) {
        is ResolvedConstraint -> this.valueType
        is Pending -> ValueLangType.Pending
        is Error -> this.errorType
    }
}

@Serializable
@SerialName("Function")
data class FunctionValueLangType(
    val name: String?, val returnConstraint: ConstraintReference, val params: List<LangParameter>
) : ResolvedValueLangType {
    override fun isPending(): Boolean = returnConstraint.isPending() || params.any() { it.valueConstraint.isPending() }

    override fun getError(): ErrorLangType? =
        returnConstraint.getError() ?: params.firstNotNullOfOrNull { it.valueConstraint.getError() }
}

@Serializable
enum class LangPrimitiveKind() {
    @SerialName("Text")
    TEXT,

    @SerialName("Number")
    NUMBER;

    fun toValueLangType() = PrimitiveValueLangType(this)
    fun toConstraintLangType() = ConstraintValueLangType(toValueLangType())
}

@Serializable
@SerialName("Primitive")
data class PrimitiveValueLangType(val kind: LangPrimitiveKind) : ResolvedValueLangType

@Serializable
@SerialName("Instance")
data class InstanceValueLangType(val canonicalType: CanonicalLangType) : ResolvedValueLangType {

    override fun isPending() = canonicalType.isPending()
    override fun getError() = canonicalType.getError()
}

@Serializable
@SerialName("Option")
data class OptionValueLangType(val options: List<ConstraintReference>) : ResolvedValueLangType {

    companion object {
        fun from(type: ValueLangType): OptionValueLangType {
            return if (type is OptionValueLangType) {
                type
            } else {
                OptionValueLangType(listOf(type.valueToConstraintReference()))
            }
        }
    }

    override fun isPending() = options.any { it.isPending() }
    override fun getError() = options.firstNotNullOfOrNull { it.getError() }

    fun isSupersetOf(otherType: ResolvedValueLangType): Boolean {
        val possibleValues = when (otherType) {
            is OptionValueLangType -> otherType.simplify().options.map { option -> option.asValueType() }
            else -> listOf(otherType)
        }

        var pendingTrue = false
        val result = possibleValues.all { possibleValue ->
            when (possibleValue) {
                is ValueLangType.Pending -> {
                    pendingTrue = true
                    true
                }

                is ErrorLangType -> false
                is ResolvedValueLangType -> options.any { option ->
                    if (option is ConstraintReference.ResolvedConstraint) possibleValue.isAssignableTo(option.asResolvedConstraintValue()) else false
                }
            }
        }
        if (result && pendingTrue) {
            error("Value is pending; can't tell if it was truly assignable")
        }
        return result
    }

    fun simplify(): OptionValueLangType {
        var allPossibleValues = options.flatMap {
            when (val value = it.asValueType()) {
                is OptionValueLangType -> value.simplify().options.asSequence()
                else -> sequenceOf(it)
            }
        }

        allPossibleValues = if (allPossibleValues.size > 1) {
            val notDuplicated = mutableListOf<ConstraintReference>()
            for (possibleValue in allPossibleValues) {
                val isDuplicate = notDuplicated.any { checkDuplicate ->
                    if (possibleValue.asValueType() == checkDuplicate.asValueType()) {
                        true
                    } else {
                        val possibleValueResolved = possibleValue.asValueType() as? ResolvedValueLangType
                        val checkDuplicatedConstraint = checkDuplicate.asConstraintValue() as? ConstraintValueLangType
                        possibleValueResolved != null && checkDuplicatedConstraint != null && possibleValueResolved.isAssignableTo(
                            checkDuplicatedConstraint
                        )
                    }
                }
                if (!isDuplicate) {
                    notDuplicated.add(possibleValue)
                }
            }
            notDuplicated
        } else {
            allPossibleValues
        }

        // if NeverContinues is part of the option, but only a part, then remove it
        val nonNeverContinues = allPossibleValues.filter { it.asValueType() !is NeverContinuesValueLangType }
        if (nonNeverContinues.isNotEmpty()) {
            allPossibleValues = nonNeverContinues
        }

        return OptionValueLangType(allPossibleValues)
    }

    fun simplifyToValue(): ValueLangType {
        val simplified = simplify()
        return if (simplified.options.size == 1) {
            simplified.options[0].asValueType()
        } else {
            simplified
        }
    }

    fun narrow(otherType: ValueLangType): OptionValueLangType {
        val possibleValues = when (otherType) {
            is OptionValueLangType -> otherType.simplify().options.map { option -> option.asValueType() }
            else -> listOf(otherType)
        }

        val remainingOptions = options.filter { option ->
            val optionValue = option.asValueType()
            if (optionValue is ResolvedValueLangType) {
                possibleValues.none { possibleValue ->
                    if (possibleValue is ResolvedValueLangType) {
                        optionValue.isAssignableTo(possibleValue.toConstraint())
                    } else {
                        // don't count error or pending
                        false
                    }
                }
            } else {
                // keep error and pending options around
                true
            }
        }

        return OptionValueLangType(remainingOptions)
    }

    fun expand(otherType: ValueLangType): OptionValueLangType {
        val newValues = options + listOf(otherType.letIfResolved { it.toConstraint() }.asConstraintReference())
        return OptionValueLangType(newValues).simplify()
    }

    fun isEmpty(): Boolean {
        return options.isEmpty()
    }

    fun proxyAllErrors(sourcePosition: SourcePosition.Source): ValueLangType {
        return if (this.getError() != null) {
            OptionValueLangType(this.options.map { option ->
                option.asConstraintValue().letIfError { ErrorLangType.ProxyError.from(it, sourcePosition) }
                    .asConstraintReference()
            })
        } else {
            this
        }
    }

}

@Serializable
@SerialName("Action")
object ActionValueLangType : ResolvedValueLangType

@Serializable
@SerialName("Anything")
object AnythingValueLangType : ResolvedValueLangType

@Serializable
@SerialName("AnySignal")
object AnySignalValueLangType : ResolvedValueLangType


@Serializable
@SerialName("BadValue")
object BadValueLangType : ResolvedValueLangType

@Serializable
@SerialName("NeverContinues")
object NeverContinuesValueLangType : ResolvedValueLangType

