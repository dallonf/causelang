package com.dallonf.ktcause

import com.dallonf.ktcause.ast.Breadcrumbs
import com.dallonf.ktcause.ast.FileNode
import com.dallonf.ktcause.gen.rustCompilerSupportedTypes
import com.dallonf.ktcause.types.CanonicalLangType
import com.dallonf.ktcause.types.CanonicalLangTypeId

object RustCompiler {
    init {
        System.loadLibrary("rscause_jni")
    }

    external fun hello(): String
    external fun logAst(ast: FileNode)

    external fun logResolvedTypes(
        path: String,
        ast: FileNode,
        tags: Map<Breadcrumbs, List<NodeTag>>,
        canonicalTypes: Map<CanonicalLangTypeId, CanonicalLangType>,
        externalFiles: Map<String, Resolver.ExternalFileDescriptor>
    )

    fun canRunRustCompiler(ast: FileNode): Boolean {
        val incompatibleNodes = getIncompatibleNodeTypes(ast)
        return !incompatibleNodes.any()
    }

    private fun getIncompatibleNodeTypes(ast: FileNode): Sequence<String> {
        val allNodes = ast.allDescendants()
        return allNodes.mapNotNull {
            val name = it::class.simpleName
            if (rustCompilerSupportedTypes.contains(name)) {
                null
            } else {
                name
            }
        }
    }
}