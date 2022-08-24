package com.dallonf.ktcause.types

import com.dallonf.ktcause.ast.SourcePosition
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
        val fields: List<ObjectField>,
        val result: ConstraintReference,
    ) : CanonicalLangType {
        override fun isUnique() = false

        override fun isPending() = result.isPending() || fields.any { it.valueConstraint.isPending() }
        override fun getError() = result.getError() ?: fields.firstNotNullOfOrNull { it.valueConstraint.getError() }
    }

    @Serializable
    @SerialName("Object")
    data class ObjectCanonicalLangType(
        override val id: CanonicalLangTypeId, val name: String, val fields: List<ObjectField>
    ) : CanonicalLangType {

        override fun isUnique() = fields.isEmpty()

        override fun isPending() = fields.any { it.valueConstraint.isPending() }
        override fun getError() = fields.firstNotNullOfOrNull { it.valueConstraint.getError() }
    }

    @Serializable
    data class ObjectField(val name: String, val valueConstraint: ConstraintReference) {
        fun asLangParameter() = LangParameter(name, valueConstraint)
    }

    fun toPair() = id to this
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
    data class MismatchedType(val expected: ConstraintValueLangType, val actual: ResolvedValueLangType) :
        ErrorLangType

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
    @SerialName("TooManyElseBranches")
    object TooManyElseBranches : ErrorLangType

    @Serializable
    @SerialName("MissingElseBranch")
    object MissingElseBranch : ErrorLangType

    @Serializable
    @SerialName("IncompatibleTypes")
    data class IncompatibleTypes(val types: List<IncompatibleType>) : ErrorLangType {
        @Serializable
        data class IncompatibleType(val type: ValueLangType, val position: SourcePosition)
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
}

sealed interface ResolvedValueLangType : ValueLangType {
    override fun isPending() = false

    fun isAssignableTo(constraint: ConstraintValueLangType): Boolean {
        return when (val constraintInstanceType = constraint.valueType) {
            is InstanceValueLangType -> this is InstanceValueLangType && this.canonicalType.id == constraintInstanceType.canonicalType.id
            is FunctionValueLangType -> this is FunctionValueLangType && this.returnConstraint == constraintInstanceType.returnConstraint && this.params == constraintInstanceType.params
            is PrimitiveValueLangType -> this is PrimitiveValueLangType && this.kind == constraintInstanceType.kind
            is UniqueObjectLangType -> this is UniqueObjectLangType && this.canonicalType.id == constraintInstanceType.canonicalType.id
            is OptionValueLangType -> constraintInstanceType.options.any {
                if (it is ConstraintReference.ResolvedConstraint) this.isAssignableTo(
                    it.asConstraintValue()
                ) else false
            }

            AnySignalValueLangType -> this is InstanceValueLangType && this.canonicalType is CanonicalLangType.SignalCanonicalLangType
            AnythingValueLangType -> true

            is ConstraintValueLangType -> this is ConstraintValueLangType && this.valueType.isAssignableTo(
                constraintInstanceType
            )

            BadValueLangType -> this is BadValueLangType
            NeverContinuesValueLangType -> this is NeverContinuesValueLangType
        }
    }

    fun toConstraint() = ConstraintValueLangType(this)
}

@Serializable
@SerialName("Constraint")
data class ConstraintValueLangType(val valueType: ResolvedValueLangType) : ResolvedValueLangType {
    override fun isPending() = valueType.isPending()
}

@Serializable
sealed class ConstraintReference {
    @Serializable
    @SerialName("Resolved")
    data class ResolvedConstraint(val valueType: ResolvedValueLangType) : ConstraintReference() {
        fun asConstraintValue() = valueType.toConstraint()
    }

    @Serializable
    @SerialName("Pending")
    object Pending : ConstraintReference()

    @Serializable
    @SerialName("Error")
    data class Error(val errorType: ErrorLangType) : ConstraintReference()

    fun isPending() =
        when (this) {
            is Pending -> true
            is ResolvedConstraint -> this.valueType.isPending()
            else -> false
        }


    fun getError() = when (this) {
        is Error -> this.errorType
        is ResolvedConstraint -> this.valueType.getError()
        else -> null
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
    @SerialName("String")
    STRING,

    @SerialName("Integer")
    INTEGER,

    @SerialName("Float")
    FLOAT,

    @SerialName("Boolean")
    BOOLEAN,

    @SerialName("Action")
    ACTION;

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
@SerialName("UniqueObject")
data class UniqueObjectLangType(val canonicalType: CanonicalLangType) : ResolvedValueLangType {
    override fun isPending() = canonicalType.isPending()

    override fun getError() = canonicalType.getError()
}

@Serializable
@SerialName("Option")
data class OptionValueLangType(val options: List<ConstraintReference>) : ResolvedValueLangType {

    override fun isPending() = options.any { it.isPending() }
    override fun getError() = options.firstNotNullOfOrNull { it.getError() }
}

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

