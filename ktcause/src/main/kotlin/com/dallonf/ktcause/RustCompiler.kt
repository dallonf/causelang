package com.dallonf.ktcause

import com.dallonf.ktcause.ast.Breadcrumbs
import com.dallonf.ktcause.ast.FileNode
import com.dallonf.ktcause.gen.rustCompilerSupportedTypes
import com.dallonf.ktcause.types.ActionValueLangType
import com.dallonf.ktcause.types.CanonicalLangType
import com.dallonf.ktcause.types.CanonicalLangTypeId

object RustCompiler {
    enum class Mode {
        ALWAYS,
        NEVER,
        IF_SUPPORTED,
    }
    private val mode = Mode.NEVER

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

    fun shouldRunRustCompiler(ast: FileNode): Boolean {
        return when (mode) {
            Mode.ALWAYS -> true
            Mode.NEVER -> false
            Mode.IF_SUPPORTED -> {
                val incompatibleNodes = getIncompatibleNodeTypes(ast)
                !incompatibleNodes.any()
            }
        }
    }

    fun compile(
        path: String,
        ast: FileNode,
        tags: Map<Breadcrumbs, List<NodeTag>>,
        externalFiles: Map<String, Resolver.ExternalFileDescriptor>
    ): CompiledFile {
        val filteredCanonicalTypes = run {
            val allEntries = externalFiles.flatMap { it.value.types.entries }
                // only supported core types for now
                .filter { it.key.name == "Debug" }
            val asPairs = allEntries.map { it.toPair() }
            mapOf(*asPairs.toTypedArray())
        }
        val filteredExternalFiles = externalFiles.mapValues { (key, value) ->
            if (key == "core/builtin.cau") {
                val filteredExports = value.exports.mapValues { (exportKey, exportValue) ->
                    // only supported core exports for now
                    // all others are just Actions
                    if (exportKey == "Debug" || exportKey == "Action") {
                        exportValue
                    } else {
                        ActionValueLangType
                    }
                }
                Resolver.ExternalFileDescriptor(filteredExports, value.types)
            } else {
                value
            }
        }
        val filteredTags = tags.mapValues { (breadcrumbs, tags) ->
            tags.filter {
                when (it) {
                    is NodeTag.ReferencesFile -> true
                    is NodeTag.BadFileReference -> true
                    is NodeTag.ValueGoesTo -> true
                    is NodeTag.ValueComesFrom -> true
                    is NodeTag.FunctionCanReturnTypeOf -> true
                    is NodeTag.ReturnsFromFunction -> true
                    is NodeTag.FunctionCanReturnAction -> true
                    is NodeTag.ActionReturn -> true
                    else -> false
                }
            }
        }

        return compileInner(path, ast, filteredTags, filteredCanonicalTypes, filteredExternalFiles)
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

    private external fun compileInner(
        path: String,
        ast: FileNode,
        tags: Map<Breadcrumbs, List<NodeTag>>,
        canonicalTypes: Map<CanonicalLangTypeId, CanonicalLangType>,
        externalFiles: Map<String, Resolver.ExternalFileDescriptor>
    ): CompiledFile
}