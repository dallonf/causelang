package com.dallonf.ktcause

import com.dallonf.ktcause.ast.Breadcrumbs
import com.dallonf.ktcause.ast.FileNode
import com.dallonf.ktcause.types.CanonicalLangType
import com.dallonf.ktcause.types.CanonicalLangTypeId
import com.dallonf.ktcause.types.ErrorValueLangType
import com.dallonf.ktcause.types.ValueLangType

import com.dallonf.ktcause.ResolutionType.*

enum class ResolutionType {
    INFERRED, EXPECTED;
}


data class ResolutionKey(val type: ResolutionType, val breadcrumbs: Breadcrumbs)
data class ResolvedFile(
    val path: String,
    val resolvedTypes: Map<ResolutionKey, ValueLangType>,
    val canonicalTypes: Map<CanonicalLangTypeId, CanonicalLangType>
) {
    fun checkForRuntimeErrors(breadcrumbs: Breadcrumbs): ErrorValueLangType? {
        val expected = resolvedTypes[ResolutionKey(EXPECTED, breadcrumbs)]?.getRuntimeError()
        return expected ?: resolvedTypes[ResolutionKey(INFERRED, breadcrumbs)]!!.getRuntimeError()
    }
}

object Resolver {
    data class ExternalFileDescriptor(val exports: Map<String, ValueLangType>)

    fun resolveForFile(
        path: String, fileNode: FileNode, analyzed: AnalyzedNode, otherFiles: Map<String, ExternalFileDescriptor>
    ): ResolvedFile {
        val allOtherFiles = otherFiles.toMutableMap().also {
            val core = CoreDescriptors.coreBuiltinFile;
            it[core.first] = core.second
            it.putAll(CoreDescriptors.coreFiles)
        }.toMap()

        val nodeTags = analyzed.nodeTags

        val resolvedTypes = mutableMapOf<ResolutionKey, ValueLangType>()

        TODO()
    }
}