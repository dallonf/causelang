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
        val result: ConstraintLangType,
    ) : CanonicalLangType {
        fun getInstanceType() = InstanceValueLangType(this)

        override fun isUnique() = fields.isEmpty()

        override fun isPending() = result.isPending() || fields.any { it.valueConstraint.isPending() }
        override fun getError() = result.getError() ?: fields.firstNotNullOfOrNull { it.valueConstraint.getError() }
    }

    @Serializable
    @SerialName("Object")
    data class ObjectCanonicalLangType(
        override val id: CanonicalLangTypeId,
        val name: String,
        val fields: List<ObjectField>
    ) : CanonicalLangType {

        override fun isUnique() = fields.isEmpty()

        override fun isPending() = fields.any { it.valueConstraint.isPending() }
        override fun getError() = fields.firstNotNullOfOrNull { it.valueConstraint.getError() }
    }

    @Serializable
    data class ObjectField(val name: String, val valueConstraint: ConstraintLangType) {
        fun asLangParameter() = LangParameter(name, valueConstraint)
    }

    fun toPair() = id to this
}

@Serializable
data class LangParameter(
    val name: String,
    val valueConstraint: ConstraintLangType,
)

sealed interface LangType {
    @Serializable
    @SerialName("Pending")
    object Pending : LangType, ValueLangType, ConstraintLangType {
        override fun isPending() = true
    }

    fun isPending(): Boolean

    fun getRuntimeError(): ErrorLangType? = when (this) {
        is ErrorLangType -> this
        Pending -> ErrorLangType.NeverResolved
        is ResolvedValueLangType -> null
        is ResolvedConstraintLangType -> null
    }

    fun getError(): ErrorLangType? = null

    fun asValue(): ValueLangType = when (this) {
        is Pending, is ResolvedValueLangType, is ErrorLangType -> this as ValueLangType
        is ResolvedConstraintLangType -> ErrorLangType.ConstraintUsedAsValue(this)
    }

    fun asConstraint(): ConstraintLangType = when (this) {
        is Pending, is ResolvedConstraintLangType, is ErrorLangType -> this as ConstraintLangType
        is ResolvedValueLangType -> ErrorLangType.ValueUsedAsConstraint(this)
    }
}

sealed interface ValueLangType : LangType

sealed interface ConstraintLangType : LangType


sealed interface ErrorLangType : LangType, ValueLangType, ConstraintLangType {
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
    data class MismatchedType(val expected: ResolvedConstraintLangType, val actual: ResolvedValueLangType) :
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
        data class IncompatibleType(val type: LangType, val position: SourcePosition)
    }

    @Serializable
    @SerialName("ConstraintUsedAsValue")
    data class ConstraintUsedAsValue(val type: ResolvedConstraintLangType) : ErrorLangType

    @Serializable
    @SerialName("ValueUsedAsConstraint")
    data class ValueUsedAsConstraint(val type: ValueLangType) : ErrorLangType

    @Serializable
    @SerialName("DoesNotHaveAnyMembers")
    object DoesNotHaveAnyMembers : ErrorLangType

    @Serializable
    @SerialName("DoesNotHaveMember")
    object DoesNotHaveMember : ErrorLangType
}

sealed interface ResolvedValueLangType : ValueLangType {
    override fun isPending() = false

    fun isAssignableTo(constraint: ResolvedConstraintLangType): Boolean {
        return when (constraint) {
            is TypeReferenceConstraintLangType -> this is InstanceValueLangType && this.canonicalType.id == constraint.canonicalType.id
            is CanonicalLangType -> this is InstanceValueLangType && this.canonicalType.id == constraint.id
            is FunctionConstraintLangType -> this is FunctionValueLangType && this.returnConstraint == constraint.returnConstraint && this.params == constraint.params
            is PrimitiveConstraintLangType -> this is PrimitiveValueLangType && this.kind == constraint.kind
            is UniqueObjectLangType -> this is UniqueObjectLangType && this.canonicalType.id == constraint.canonicalType.id
            BadValueConstraintLangType -> this is BadValueLangType
            NeverContinuesConstraintLangType -> this is NeverContinuesValueLangType
        }
    }

    fun toConstraint(): ResolvedConstraintLangType
}

sealed interface ResolvedConstraintLangType : ConstraintLangType {
    override fun isPending() = false

    fun toInstanceType(): ResolvedValueLangType
}

@Serializable
@SerialName("Function")
data class FunctionValueLangType(
    val name: String?, val returnConstraint: ConstraintLangType, val params: List<LangParameter>
) : ResolvedValueLangType {
    override fun isPending(): Boolean = returnConstraint.isPending() || params.any() { it.valueConstraint.isPending() }

    override fun toConstraint() = FunctionConstraintLangType(returnConstraint, params)

    override fun getError(): ErrorLangType? =
        returnConstraint.getError() ?: params.firstNotNullOfOrNull { it.valueConstraint.getError() }
}

@Serializable
@SerialName("FunctionConstraint")
data class FunctionConstraintLangType(val returnConstraint: ConstraintLangType, val params: List<LangParameter>) :
    ResolvedConstraintLangType {

    override fun isPending(): Boolean = returnConstraint.isPending() || params.any() { it.valueConstraint.isPending() }

    override fun toInstanceType() = FunctionValueLangType(name = null, returnConstraint, params)

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
    fun toConstraintLangType() = PrimitiveConstraintLangType(this)
}

@Serializable
@SerialName("Primitive")
data class PrimitiveValueLangType(val kind: LangPrimitiveKind) : ResolvedValueLangType {
    override fun toConstraint(): ResolvedConstraintLangType = PrimitiveConstraintLangType(kind)
}


@Serializable
@SerialName("PrimitiveConstraint")
data class PrimitiveConstraintLangType(val kind: LangPrimitiveKind) : ResolvedConstraintLangType {
    override fun toInstanceType() = PrimitiveValueLangType(kind)
}

@Serializable
@SerialName("TypeReferenceConstraint")
data class TypeReferenceConstraintLangType(val canonicalType: CanonicalLangType) : ResolvedConstraintLangType {
    override fun toInstanceType() = InstanceValueLangType(canonicalType)

    override fun isPending() = canonicalType.isPending()
    override fun getError() = canonicalType.getError()
}

@Serializable
@SerialName("Instance")
data class InstanceValueLangType(val canonicalType: CanonicalLangType) : ResolvedValueLangType {
    override fun toConstraint() = TypeReferenceConstraintLangType(canonicalType)

    override fun isPending() = canonicalType.isPending()
    override fun getError() = canonicalType.getError()
}

@Serializable
@SerialName("UniqueObject")
data class UniqueObjectLangType(val canonicalType: CanonicalLangType) : ResolvedConstraintLangType,
    ResolvedValueLangType {
    override fun isPending() = canonicalType.isPending()

    override fun getError() = canonicalType.getError()

    override fun toInstanceType() = this

    override fun toConstraint() = this
}

@Serializable
@SerialName("BadValue")
object BadValueLangType : ResolvedValueLangType {
    override fun toConstraint() = BadValueConstraintLangType
}

@Serializable
@SerialName("BadValueConstraint")
object BadValueConstraintLangType : ResolvedConstraintLangType {
    override fun toInstanceType() = BadValueLangType
}

@Serializable
@SerialName("NeverContinues")
object NeverContinuesValueLangType : ResolvedValueLangType {
    override fun toConstraint() = NeverContinuesConstraintLangType
}

@Serializable
@SerialName("NeverContinuesConstraint")
object NeverContinuesConstraintLangType : ResolvedConstraintLangType {
    override fun toInstanceType() = NeverContinuesValueLangType
}