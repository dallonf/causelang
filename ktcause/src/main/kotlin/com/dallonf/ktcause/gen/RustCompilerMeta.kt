package com.dallonf.ktcause.gen

import com.dallonf.ktcause.ast.*

val rustCompilerSupportedTypes = setOf(
    *listOf(
        FileNode::class
    ).mapNotNull { it.simpleName }.toTypedArray()
)
