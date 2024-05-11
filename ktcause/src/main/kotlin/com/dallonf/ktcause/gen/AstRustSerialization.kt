package com.dallonf.ktcause.gen

import kotlinx.serialization.json.*
import com.dallonf.ktcause.ast.*
import com.dallonf.ktcause.RustSerialization

object AstRustSerialization {
    fun serializeTypeReference(node: TypeReferenceNode): JsonElement {
        return when (node) {
            is IdentifierTypeReferenceNode -> buildJsonObject { put("IdentifierTypeReference", serializeIdentifierTypeReference(node)) }
            else -> TODO("Unknown node type: ${node::class.simpleName}")
        }
    }

    fun serializeDeclaration(node: DeclarationNode): JsonElement {
        return when (node) {
            is ImportNode -> buildJsonObject { put("Import", serializeImport(node)) }
            is FunctionNode -> buildJsonObject { put("Function", serializeFunction(node)) }
            is NamedValueNode -> buildJsonObject { put("NamedValue", serializeNamedValue(node)) }
            else -> TODO("Unknown node type: ${node::class.simpleName}")
        }
    }

    fun serializeBody(node: BodyNode): JsonElement {
        return when (node) {
            is BlockBodyNode -> buildJsonObject { put("BlockBody", serializeBlockBody(node)) }
            is SingleStatementBodyNode -> buildJsonObject { put("SingleStatementBody", serializeSingleStatementBody(node)) }
            else -> TODO("Unknown node type: ${node::class.simpleName}")
        }
    }

    fun serializeStatement(node: StatementNode): JsonElement {
        return when (node) {
            is ExpressionStatementNode -> buildJsonObject { put("ExpressionStatement", serializeExpressionStatement(node)) }
            is DeclarationStatementNode -> buildJsonObject { put("DeclarationStatement", serializeDeclarationStatement(node)) }
            else -> TODO("Unknown node type: ${node::class.simpleName}")
        }
    }

    fun serializeExpression(node: ExpressionNode): JsonElement {
        return when (node) {
            is BranchExpressionNode -> buildJsonObject { put("BranchExpression", serializeBranchExpression(node)) }
            is CauseExpressionNode -> buildJsonObject { put("CauseExpression", serializeCauseExpression(node)) }
            is CallExpressionNode -> buildJsonObject { put("CallExpression", serializeCallExpression(node)) }
            is IdentifierExpressionNode -> buildJsonObject { put("IdentifierExpression", serializeIdentifierExpression(node)) }
            is StringLiteralExpressionNode -> buildJsonObject { put("StringLiteralExpression", serializeStringLiteralExpression(node)) }
            is NumberLiteralExpressionNode -> buildJsonObject { put("NumberLiteralExpression", serializeNumberLiteralExpression(node)) }
            else -> TODO("Unknown node type: ${node::class.simpleName}")
        }
    }

    fun serializeBranchOption(node: BranchOptionNode): JsonElement {
        return when (node) {
            is IfBranchOptionNode -> buildJsonObject { put("IfBranchOption", serializeIfBranchOption(node)) }
            is IsBranchOptionNode -> buildJsonObject { put("IsBranchOption", serializeIsBranchOption(node)) }
            is ElseBranchOptionNode -> buildJsonObject { put("ElseBranchOption", serializeElseBranchOption(node)) }
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

    fun serializePattern(node: PatternNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            if (node.name != null) {
            put("name", serializeIdentifier(node.name))
            }
            put("typeReference", serializeTypeReference(node.typeReference))
        }
    }

    fun serializeFunctionSignatureParameter(node: FunctionSignatureParameterNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("name", serializeIdentifier(node.name))
            if (node.typeReference != null) {
            put("typeReference", serializeTypeReference(node.typeReference))
            }
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
            put("sourceName", serializeIdentifier(node.sourceName))
            if (node.rename != null) {
            put("rename", serializeIdentifier(node.rename))
            }
        }
    }

    fun serializeFunction(node: FunctionNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("name", serializeIdentifier(node.name))
            put("params", JsonArray(node.params.map { serializeFunctionSignatureParameter(it) }))
            put("body", serializeBody(node.body))
            if (node.returnType != null) {
            put("returnType", serializeTypeReference(node.returnType))
            }
        }
    }

    fun serializeNamedValue(node: NamedValueNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            put("name", serializeIdentifier(node.name))
            if (node.typeAnnotation != null) {
            put("typeAnnotation", serializeTypeReference(node.typeAnnotation))
            }
            put("value", serializeExpression(node.value))
            put("isVariable", node.isVariable)
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

    fun serializeBranchExpression(node: BranchExpressionNode): JsonElement {
        return buildJsonObject {
            put("info", RustSerialization.serializeNodeInfo(node.info))
            if (node.withValue != null) {
            put("withValue", serializeExpression(node.withValue))
            }
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
            put("value", node.value)
        }
    }

}