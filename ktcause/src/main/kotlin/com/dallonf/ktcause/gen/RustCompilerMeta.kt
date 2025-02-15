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
        ObjectTypeNode::class,
        SignalTypeNode::class,
        ObjectFieldNode::class,
        OneOfTypeNode::class,
        BlockBodyNode::class,
        SingleStatementBodyNode::class,
        ExpressionStatementNode::class,
        DeclarationStatementNode::class,
        GroupExpressionNode::class,
        BlockExpressionNode::class,
        EffectStatementNode::class,
        SetStatementNode::class,
        BranchExpressionNode::class,
        IfBranchOptionNode::class,
        IsBranchOptionNode::class,
        ElseBranchOptionNode::class,
        LoopExpressionNode::class,
        CauseExpressionNode::class,
        CallExpressionNode::class,
        MemberExpressionNode::class,
        IdentifierExpressionNode::class,
        StringLiteralExpressionNode::class,
        NumberLiteralExpressionNode::class,
        ReturnExpressionNode::class,
        BreakExpressionNode::class,
    ).mapNotNull { it.simpleName }.toTypedArray()
)
