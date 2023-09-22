package com.dallonf.ktcause

import com.dallonf.ktcause.ast.FileNode
import com.dallonf.ktcause.gen.rustCompilerSupportedTypes

object RustCompiler {
    init {
        System.loadLibrary("rscause_jni")
    }

    external fun hello(): String

    fun canRunRustCompiler(ast: FileNode): Boolean {
        val allNodes = ast.allDescendants()
        return allNodes.all {
            val name = it::class.simpleName
            rustCompilerSupportedTypes.contains(name)
        }
    }
}