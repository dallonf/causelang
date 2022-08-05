package com.dallonf.ktcause.types

import com.dallonf.ktcause.ast.Breadcrumbs

data class CanonicalLangTypeId(
    val path: String,
    val parentName: String? = null,
    val name: String?,
    val number: Byte,
) {
    override fun toString(): String {
        val name = name ?: "<anonymous>"

        return if (parentName != null) {
            "${path}:${parentName}_${name}${number}"
        } else {
            "${path}:${name}${number}"
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

data class LangParameter(
    val name: String,
    val valueType: ValueLangType,
)

sealed interface ValueLangType {
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

sealed interface ErrorSourcePosition {
    data class SameFile(val path: String, val breadcrumbs: Breadcrumbs) : ErrorSourcePosition
    data class FileImport(val path: String, val exportName: String) : ErrorSourcePosition
}

sealed interface ErrorValueLangType : ValueLangType {
    object NeverResolved : ErrorValueLangType
    object NotInScope : ErrorValueLangType
    object FileNotFound : ErrorValueLangType
    object ExportNotFound : ErrorValueLangType
    data class ProxyError(val causedBy: ErrorSourcePosition) : ErrorValueLangType
    object NotCallable : ErrorValueLangType
    object NotCausable : ErrorValueLangType
    data class ImplementationTodo(val description: String) : ErrorValueLangType
    data class NotATypeReference(val actual: ResolvedValueLangType) : ErrorValueLangType
    data class MismatchedType(val expected: ResolvedValueLangType, val actual: ResolvedValueLangType) :
        ErrorValueLangType

    data class MissingParameters(val names: List<String>) : ErrorValueLangType
    data class ExcessParameter(val expected: Int) : ErrorValueLangType
    object UnknownParameter : ErrorValueLangType
}

// TODO: more distinction between values and types?

sealed interface ResolvedValueLangType : ValueLangType {
    fun getInstanceType(): ResolvedValueLangType?
}

data class FunctionValueLangType(val name: String?, val returnType: ValueLangType, val params: List<LangParameter>) :
    ResolvedValueLangType {
    override fun getInstanceType() = null
}

data class FunctionTypeValueLangType(val functionType: FunctionValueLangType) : ResolvedValueLangType {
    override fun getInstanceType() = functionType
}

enum class PrimitiveValueLangType : ResolvedValueLangType {
    STRING, INTEGER, FLOAT, ACTION;

    override fun getInstanceType() = null
}

data class PrimitiveTypeValueLangType(val primitiveType: PrimitiveValueLangType) : ResolvedValueLangType {
    override fun getInstanceType() = primitiveType
}

data class TypeReferenceValueLangType(val canonicalType: CanonicalLangType) : ResolvedValueLangType {
    override fun getInstanceType() = InstanceValueLangType(this)
}

data class InstanceValueLangType(val type: TypeReferenceValueLangType) : ResolvedValueLangType {
    override fun getInstanceType() = null
}

object BadValueLangType : ResolvedValueLangType {
    override fun getInstanceType() = null
}

object NeverContinuesValueLangType : ResolvedValueLangType {
    override fun getInstanceType() = null
}