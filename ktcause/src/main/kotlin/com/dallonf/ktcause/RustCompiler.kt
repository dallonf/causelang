package com.dallonf.ktcause

import com.dallonf.ktcause.ast.FileNode
import com.dallonf.ktcause.gen.rustCompilerSupportedTypes
import java.io.File

object RustCompiler {
    init {
        System.loadLibrary("rscause_jni")
    }

    external fun hello(): String
    external fun logAst(ast: FileNode)

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