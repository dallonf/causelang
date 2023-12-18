package com.dallonf.ktcause.gen

import com.dallonf.ktcause.ast.*

val rustCompilerSupportedTypes = setOf(
    *listOf(
        IdentifierNode::class,
        IdentifierTypeReferenceNode::class,
        PatternNode::class,
        FunctionSignatureParameterNode::class,
        FunctionCallParameterNode::class,
        FileNode::class,
        ImportNode::class,
        ImportPathNode::class,
        ImportMappingNode::class,
        FunctionNode::class,
        NamedValueNode::class,
        BlockBodyNode::class,
        SingleStatementBodyNode::class,
        ExpressionStatementNode::class,
        DeclarationStatementNode::class,
        BranchExpressionNode::class,
        IfBranchOptionNode::class,
        IsBranchOptionNode::class,
        ElseBranchOptionNode::class,
        CauseExpressionNode::class,
        CallExpressionNode::class,
        IdentifierExpressionNode::class,
        StringLiteralExpressionNode::class,
        NumberLiteralExpressionNode::class,
    ).mapNotNull { it.simpleName }.toTypedArray()
)
