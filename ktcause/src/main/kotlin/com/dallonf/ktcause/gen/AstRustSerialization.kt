package com.dallonf.ktcause.gen

import kotlinx.serialization.json.*
import com.dallonf.ktcause.gen.ast_nodes.*
import com.dallonf.ktcause.serialization.RustSerialization

object AstRustSerialization {
    fun serializeTypeReference(node: TypeReferenceNode): JsonElement {
        return when (node) {
            is IdentifierTypeReferenceNode -> buildJsonObject { put("Identifier", serializeIdentifierTypeReference(node)) }
            is FunctionTypeReferenceNode -> buildJsonObject { put("Function", serializeFunctionTypeReference(node)) }
            else -> TODO("Unknown node type: ${node::class.simpleName}")
        }
    }

    fun serializeDeclaration(node: DeclarationNode): JsonElement {
        return when (node) {
            is ImportNode -> buildJsonObject { put("Import", serializeImport(node)) }
            is FunctionNode -> buildJsonObject { put("Function", serializeFunction(node)) }
            is NamedValueNode -> buildJsonObject { put("NamedValue", serializeNamedValue(node)) }
            is ObjectTypeNode -> buildJsonObject { put("ObjectType", serializeObjectType(node)) }
            is SignalTypeNode -> buildJsonObject { put("SignalType", serializeSignalType(node)) }
            is OneOfTypeNode -> buildJsonObject { put("OneOfType", serializeOneOfType(node)) }
            else -> TODO("Unknown node type: ${node::class.simpleName}")
        }
    }

    fun serializeBody(node: BodyNode): JsonElement {
        return when (node) {
            is BlockBodyNode -> buildJsonObject { put("Block", serializeBlockBody(node)) }
            is SingleStatementBodyNode -> buildJsonObject { put("SingleStatement", serializeSingleStatementBody(node)) }
            else -> TODO("Unknown node type: ${node::class.simpleName}")
        }
    }

    fun serializeStatement(node: StatementNode): JsonElement {
        return when (node) {
            is ExpressionStatementNode -> buildJsonObject { put("Expression", serializeExpressionStatement(node)) }
            is DeclarationStatementNode -> buildJsonObject { put("Declaration", serializeDeclarationStatement(node)) }
            is EffectStatementNode -> buildJsonObject { put("Effect", serializeEffectStatement(node)) }
            is SetStatementNode -> buildJsonObject { put("Set", serializeSetStatement(node)) }
            else -> TODO("Unknown node type: ${node::class.simpleName}")
        }
    }

    fun serializeExpression(node: ExpressionNode): JsonElement {
        return when (node) {
            is GroupExpressionNode -> buildJsonObject { put("Group", serializeGroupExpression(node)) }
            is BlockExpressionNode -> buildJsonObject { put("Block", serializeBlockExpression(node)) }
            is FunctionExpressionNode -> buildJsonObject { put("Function", serializeFunctionExpression(node)) }
            is BranchExpressionNode -> buildJsonObject { put("Branch", serializeBranchExpression(node)) }
            is LoopExpressionNode -> buildJsonObject { put("Loop", serializeLoopExpression(node)) }
            is CauseExpressionNode -> buildJsonObject { put("Cause", serializeCauseExpression(node)) }
            is CallExpressionNode -> buildJsonObject { put("Call", serializeCallExpression(node)) }
            is PipeCallExpressionNode -> buildJsonObject { put("PipeCall", serializePipeCallExpression(node)) }
            is MemberExpressionNode -> buildJsonObject { put("Member", serializeMemberExpression(node)) }
            is IdentifierExpressionNode -> buildJsonObject { put("Identifier", serializeIdentifierExpression(node)) }
            is StringLiteralExpressionNode -> buildJsonObject { put("StringLiteral", serializeStringLiteralExpression(node)) }
            is NumberLiteralExpressionNode -> buildJsonObject { put("NumberLiteral", serializeNumberLiteralExpression(node)) }
            is ReturnExpressionNode -> buildJsonObject { put("Return", serializeReturnExpression(node)) }
            is BreakExpressionNode -> buildJsonObject { put("Break", serializeBreakExpression(node)) }
            else -> TODO("Unknown node type: ${node::class.simpleName}")
        }
    }

    fun serializeBranchOption(node: BranchOptionNode): JsonElement {
        return when (node) {
            is IfBranchOptionNode -> buildJsonObject { put("If", serializeIfBranchOption(node)) }
            is IsBranchOptionNode -> buildJsonObject { put("Is", serializeIsBranchOption(node)) }
            is ElseBranchOptionNode -> buildJsonObject { put("Else", serializeElseBranchOption(node)) }
            else -> TODO("Unknown node type: ${node::class.simpleName}")
        }
    }


    fun serializeIdentifier(node: IdentifierNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("text", node.text)
        }
    }

    fun serializeIdentifierTypeReference(node: IdentifierTypeReferenceNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("identifier", serializeIdentifier(node.identifier))
        }
    }

    fun serializeFunctionTypeReference(node: FunctionTypeReferenceNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("params", JsonArray(node.params.map { serializeFunctionSignatureParameter(it) }))
            put("return_type", serializeTypeReference(node.returnType))
        }
    }

    fun serializePattern(node: PatternNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("name", node.name?.let { serializeIdentifier(it)} ?: JsonNull)
            put("type_reference", serializeTypeReference(node.typeReference))
        }
    }

    fun serializeFunctionSignatureParameter(node: FunctionSignatureParameterNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("name", serializeIdentifier(node.name))
            put("type_reference", node.typeReference?.let { serializeTypeReference(it)} ?: JsonNull)
        }
    }

    fun serializeFunctionCallParameter(node: FunctionCallParameterNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("value", serializeExpression(node.value))
        }
    }

    fun serializeFile(node: FileNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("declarations", JsonArray(node.declarations.map { serializeDeclaration(it) }))
        }
    }

    fun serializeImport(node: ImportNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("path", serializeImportPath(node.path))
            put("mappings", JsonArray(node.mappings.map { serializeImportMapping(it) }))
        }
    }

    fun serializeImportPath(node: ImportPathNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("path", node.path)
        }
    }

    fun serializeImportMapping(node: ImportMappingNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("source_name", serializeIdentifier(node.sourceName))
            put("rename", node.rename?.let { serializeIdentifier(it)} ?: JsonNull)
        }
    }

    fun serializeFunction(node: FunctionNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("name", serializeIdentifier(node.name))
            put("params", JsonArray(node.params.map { serializeFunctionSignatureParameter(it) }))
            put("body", serializeBody(node.body))
            put("return_type", node.returnType?.let { serializeTypeReference(it)} ?: JsonNull)
        }
    }

    fun serializeNamedValue(node: NamedValueNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("name", serializeIdentifier(node.name))
            put("type_annotation", node.typeAnnotation?.let { serializeTypeReference(it)} ?: JsonNull)
            put("value", serializeExpression(node.value))
            put("is_variable", node.isVariable)
        }
    }

    fun serializeObjectType(node: ObjectTypeNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("name", serializeIdentifier(node.name))
            put("fields", JsonArray(node.fields.map { serializeObjectField(it) }))
        }
    }

    fun serializeSignalType(node: SignalTypeNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("name", serializeIdentifier(node.name))
            put("fields", JsonArray(node.fields.map { serializeObjectField(it) }))
            put("result", node.result?.let { serializeTypeReference(it)} ?: JsonNull)
        }
    }

    fun serializeObjectField(node: ObjectFieldNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("name", serializeIdentifier(node.name))
            put("type_annotation", serializeTypeReference(node.typeAnnotation))
        }
    }

    fun serializeOneOfType(node: OneOfTypeNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("name", serializeIdentifier(node.name))
            put("options", JsonArray(node.options.map { serializeTypeReference(it) }))
        }
    }

    fun serializeBlockBody(node: BlockBodyNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("statements", JsonArray(node.statements.map { serializeStatement(it) }))
        }
    }

    fun serializeSingleStatementBody(node: SingleStatementBodyNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("statement", serializeStatement(node.statement))
        }
    }

    fun serializeExpressionStatement(node: ExpressionStatementNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("expression", serializeExpression(node.expression))
        }
    }

    fun serializeDeclarationStatement(node: DeclarationStatementNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("declaration", serializeDeclaration(node.declaration))
        }
    }

    fun serializeEffectStatement(node: EffectStatementNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("pattern", serializePattern(node.pattern))
            put("body", serializeBody(node.body))
        }
    }

    fun serializeSetStatement(node: SetStatementNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("identifier", serializeIdentifier(node.identifier))
            put("expression", serializeExpression(node.expression))
        }
    }

    fun serializeGroupExpression(node: GroupExpressionNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("expression", serializeExpression(node.expression))
        }
    }

    fun serializeBlockExpression(node: BlockExpressionNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("block", serializeBlockBody(node.block))
        }
    }

    fun serializeFunctionExpression(node: FunctionExpressionNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("params", JsonArray(node.params.map { serializeFunctionSignatureParameter(it) }))
            put("body", serializeExpression(node.body))
            put("return_type", node.returnType?.let { serializeTypeReference(it)} ?: JsonNull)
        }
    }

    fun serializeBranchExpression(node: BranchExpressionNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("with_value", node.withValue?.let { serializeExpression(it)} ?: JsonNull)
            put("branches", JsonArray(node.branches.map { serializeBranchOption(it) }))
        }
    }

    fun serializeIfBranchOption(node: IfBranchOptionNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("condition", serializeExpression(node.condition))
            put("body", serializeBody(node.body))
        }
    }

    fun serializeIsBranchOption(node: IsBranchOptionNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("pattern", serializePattern(node.pattern))
            put("body", serializeBody(node.body))
        }
    }

    fun serializeElseBranchOption(node: ElseBranchOptionNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("body", serializeBody(node.body))
        }
    }

    fun serializeLoopExpression(node: LoopExpressionNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("body", serializeBody(node.body))
        }
    }

    fun serializeCauseExpression(node: CauseExpressionNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("signal", serializeExpression(node.signal))
        }
    }

    fun serializeCallExpression(node: CallExpressionNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("callee", serializeExpression(node.callee))
            put("parameters", JsonArray(node.parameters.map { serializeFunctionCallParameter(it) }))
        }
    }

    fun serializePipeCallExpression(node: PipeCallExpressionNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("subject", serializeExpression(node.subject))
            put("callee", serializeExpression(node.callee))
            put("parameters", JsonArray(node.parameters.map { serializeFunctionCallParameter(it) }))
        }
    }

    fun serializeMemberExpression(node: MemberExpressionNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("object_expression", serializeExpression(node.objectExpression))
            put("member_identifier", serializeIdentifier(node.memberIdentifier))
        }
    }

    fun serializeIdentifierExpression(node: IdentifierExpressionNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("identifier", serializeIdentifier(node.identifier))
        }
    }

    fun serializeStringLiteralExpression(node: StringLiteralExpressionNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("text", node.text)
        }
    }

    fun serializeNumberLiteralExpression(node: NumberLiteralExpressionNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("value", node.value.toPlainString())
        }
    }

    fun serializeReturnExpression(node: ReturnExpressionNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("value", node.value?.let { serializeExpression(it)} ?: JsonNull)
        }
    }

    fun serializeBreakExpression(node: BreakExpressionNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("with_value", node.withValue?.let { serializeExpression(it)} ?: JsonNull)
        }
    }

}