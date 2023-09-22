package com.dallonf.ktcause.gen

import com.dallonf.ktcause.ast.*

val rustCompilerSupportedTypes = setOf(
    *listOf(
        IdentifierNode::class,
        IdentifierTypeReferenceNode::class,
        FunctionSignatureParameterNode::class,
        FunctionCallParameterNode::class,
        FileNode::class,
        FunctionNode::class,
        BlockBodyNode::class,
        ExpressionStatementNode::class,
        CauseExpressionNode::class,
        CallExpressionNode::class,
        IdentifierExpressionNode::class,
        StringLiteralExpressionNode::class,
    ).mapNotNull { it.simpleName }.toTypedArray()
)
