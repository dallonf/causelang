package com.dallonf.ktcause

import com.dallonf.ktcause.ResolutionType.*
import com.dallonf.ktcause.ast.*
import com.dallonf.ktcause.types.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

enum class ResolutionType {
    INFERRED, CONSTRAINT;
}

data class ResolutionKey(val type: ResolutionType, val breadcrumbs: Breadcrumbs)
data class ResolvedFile(
    val path: String,
    val resolvedTypes: Map<ResolutionKey, ValueLangType>,
    val canonicalTypes: Map<CanonicalLangTypeId, CanonicalLangType>,
    private val _debugContext: Debug.DebugContext? = null
)

object Resolver {
    @Serializable
    data class ResolverError(val position: SourcePosition.Source, val error: ErrorLangType) {
        fun debug(): String {
            return Debug.debugSerializer.encodeToString(this)
        }
    }

    internal fun List<ResolverError>.debug(): String {
        return Debug.debugSerializer.encodeToString(this)
    }


    data class ExternalFileDescriptor(
        val exports: Map<String, ValueLangType>, val types: Map<CanonicalLangTypeId, CanonicalLangType>
    )

}