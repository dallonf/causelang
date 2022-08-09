package com.dallonf.ktcause.types

import com.dallonf.ktcause.ast.SourcePosition
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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


sealed interface CanonicalLangType : ResolvedValueLangType {
    val id: CanonicalLangTypeId

    data class SignalCanonicalLangType(
        override val id: CanonicalLangTypeId,
        val name: String,
        val params: List<LangParameter>,
        val result: ValueLangType,
    ) : CanonicalLangType {
        override fun getInstanceType() = TypeReferenceValueLangType(this)
    }
}

@Serializable
data class LangParameter(
    val name: String,
    val valueType: ValueLangType,
)

sealed interface ValueLangType {
    @Serializable
    @SerialName("Pending")
    object Pending : ValueLangType

    fun isPending(): Boolean = when (this) {
        Pending -> true
        is FunctionValueLangType -> this.returnType.isPending()
        is ResolvedValueLangType -> false
        is ErrorValueLangType -> false
    }

    fun getRuntimeError(): ErrorValueLangType? = when (this) {
        is ErrorValueLangType -> this
        Pending -> ErrorValueLangType.NeverResolved
        is ResolvedValueLangType -> null
    }

    fun getError() = this as? ErrorValueLangType
}


sealed interface ErrorValueLangType : ValueLangType {
    @Serializable
    @SerialName("NeverResolved")
    object NeverResolved : ErrorValueLangType

    @Serializable
    @SerialName("NotInScope")
    object NotInScope : ErrorValueLangType

    @Serializable
    @SerialName("FileNotFound")
    object FileNotFound : ErrorValueLangType

    @Serializable
    @SerialName("ExportNotFound")
    object ExportNotFound : ErrorValueLangType

    @Serializable
    @SerialName("ProxyError")
    data class ProxyError(val actualError: ErrorValueLangType, val proxyChain: List<SourcePosition>) :
        ErrorValueLangType

    @Serializable
    @SerialName("NotCallable")
    object NotCallable : ErrorValueLangType

    @Serializable
    @SerialName("NotCausable")
    object NotCausable : ErrorValueLangType

    @Serializable
    @SerialName("ImplementationTodo")
    data class ImplementationTodo(val description: String) : ErrorValueLangType

    @Serializable
    @SerialName("NotATypeReference")
    data class NotATypeReference(val actual: ResolvedValueLangType) : ErrorValueLangType

    @Serializable
    @SerialName("MismatchedType")
    data class MismatchedType(val expected: ResolvedValueLangType, val actual: ResolvedValueLangType) :
        ErrorValueLangType

    @Serializable
    @SerialName("MissingParameters")

    data class MissingParameters(val names: List<String>) : ErrorValueLangType

    @Serializable
    @SerialName("ExcessParameters")
    data class ExcessParameter(val expected: Int) : ErrorValueLangType

    @Serializable
    @SerialName("UnknownParameter")
    object UnknownParameter : ErrorValueLangType

    @Serializable
    @SerialName("TooManyElseBranches")
    object TooManyElseBranches : ErrorValueLangType

    @Serializable
    @SerialName("MissingElseBranch")
    object MissingElseBranch : ErrorValueLangType

    @Serializable
    @SerialName("IncompatibleTypes")
    data class IncompatibleTypes(val types: List<IncompatibleType>) : ErrorValueLangType {
        @Serializable
        data class IncompatibleType(val type: ValueLangType, val position: SourcePosition)
    }
}

// TODO: more distinction between values and types?

sealed interface ResolvedValueLangType : ValueLangType {
    fun getInstanceType(): ResolvedValueLangType?
}

@Serializable
@SerialName("Function")
data class FunctionValueLangType(val name: String?, val returnType: ValueLangType, val params: List<LangParameter>) :
    ResolvedValueLangType {
    override fun getInstanceType() = null
}

@Serializable
@SerialName("FunctionType")
data class FunctionTypeValueLangType(val functionType: FunctionValueLangType) : ResolvedValueLangType {
    override fun getInstanceType() = functionType
}

@Serializable
@SerialName("Primitive")
data class PrimitiveValueLangType(val kind: LangPrimitiveKind) : ResolvedValueLangType {
    override fun getInstanceType() = null
}

@Serializable
enum class LangPrimitiveKind() {
    @SerialName("String")
    STRING,

    @SerialName("Integer")
    INTEGER,

    @SerialName("Float")
    FLOAT,

    @SerialName("Action")
    ACTION;

    fun toValueLangType() = PrimitiveValueLangType(this)
    fun toTypeValueLangType() = PrimitiveTypeValueLangType(this.toValueLangType())
}

@Serializable
@SerialName("PrimitiveType")
data class PrimitiveTypeValueLangType(val primitiveType: PrimitiveValueLangType) : ResolvedValueLangType {
    override fun getInstanceType() = primitiveType
}

@Serializable
@SerialName("TypeReference")
data class TypeReferenceValueLangType(val canonicalType: CanonicalLangType) : ResolvedValueLangType {
    override fun getInstanceType() = InstanceValueLangType(this)
}

@Serializable
@SerialName("Instance")
data class InstanceValueLangType(val type: TypeReferenceValueLangType) : ResolvedValueLangType {
    override fun getInstanceType() = null
}

@Serializable
@SerialName("BadValue")
object BadValueLangType : ResolvedValueLangType {
    override fun getInstanceType() = null
}

@Serializable
@SerialName("NeverContinues")
object NeverContinuesValueLangType : ResolvedValueLangType {
    override fun getInstanceType() = null
}