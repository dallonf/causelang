package com.dallonf.ktcause.gen.ast_nodes

import com.dallonf.ktcause.ast.NodeInfo
import com.dallonf.ktcause.ast.Breadcrumbs
import com.dallonf.ktcause.ast.BreadcrumbWalkChild
import java.math.BigDecimal

sealed interface AstNode {
    fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild>

    val info: NodeInfo

    fun findNode(breadcrumbs: Breadcrumbs): AstNode = BreadcrumbWalkChild.Node(this).findNode(breadcrumbs)

    fun allDescendants() = BreadcrumbWalkChild.Node(this).allDescendants()

    fun allAncestors(topAst: AstNode): Sequence<AstNode> {
        var currentBreadcrumbs = this.info.breadcrumbs.up()
        return sequence {
            while (!currentBreadcrumbs.isEmpty()) {
                val child = BreadcrumbWalkChild.Node(topAst).findBreadcrumbWalkNode(currentBreadcrumbs)
                if (child is BreadcrumbWalkChild.Node) {
                    yield(child.node)
                }
                currentBreadcrumbs = currentBreadcrumbs.up()
            }
        }
    }
}

sealed interface TypeReferenceNode : AstNode
sealed interface DeclarationNode : AstNode
sealed interface BodyNode : AstNode
sealed interface StatementNode : AstNode
sealed interface ExpressionNode : AstNode
sealed interface BranchOptionNode : AstNode

data class IdentifierNode(
  override val info: NodeInfo,
  val text: String,
): AstNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
    }
}
data class IdentifierTypeReferenceNode(
  override val info: NodeInfo,
  val identifier: IdentifierNode,
): TypeReferenceNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("identifier", identifier)
    }
}
data class FunctionTypeReferenceNode(
  override val info: NodeInfo,
  val params: List<FunctionSignatureParameterNode>,
  val returnType: TypeReferenceNode,
): TypeReferenceNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("params", params)
        put("returnType", returnType)
    }
}
data class PatternNode(
  override val info: NodeInfo,
  val name: IdentifierNode?,
  val typeReference: TypeReferenceNode,
): AstNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        if (name != null) {
          put("name", name)
        }
        put("typeReference", typeReference)
    }
}
data class FunctionSignatureParameterNode(
  override val info: NodeInfo,
  val name: IdentifierNode,
  val typeReference: TypeReferenceNode?,
): AstNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("name", name)
        if (typeReference != null) {
          put("typeReference", typeReference)
        }
    }
}
data class FunctionCallParameterNode(
  override val info: NodeInfo,
  val value: ExpressionNode,
): AstNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("value", value)
    }
}
data class FileNode(
  override val info: NodeInfo,
  val declarations: List<DeclarationNode>,
): AstNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("declarations", declarations)
    }
}
data class ImportNode(
  override val info: NodeInfo,
  val path: ImportPathNode,
  val mappings: List<ImportMappingNode>,
): DeclarationNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("path", path)
        put("mappings", mappings)
    }
}
data class ImportPathNode(
  override val info: NodeInfo,
  val path: String,
): AstNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
    }
}
data class ImportMappingNode(
  override val info: NodeInfo,
  val sourceName: IdentifierNode,
  val rename: IdentifierNode?,
): AstNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("sourceName", sourceName)
        if (rename != null) {
          put("rename", rename)
        }
    }
}
data class FunctionNode(
  override val info: NodeInfo,
  val name: IdentifierNode,
  val params: List<FunctionSignatureParameterNode>,
  val body: BodyNode,
  val returnType: TypeReferenceNode?,
): DeclarationNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("name", name)
        put("params", params)
        put("body", body)
        if (returnType != null) {
          put("returnType", returnType)
        }
    }
}
data class NamedValueNode(
  override val info: NodeInfo,
  val name: IdentifierNode,
  val typeAnnotation: TypeReferenceNode?,
  val value: ExpressionNode,
  val isVariable: Boolean,
): DeclarationNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("name", name)
        if (typeAnnotation != null) {
          put("typeAnnotation", typeAnnotation)
        }
        put("value", value)
    }
}
data class ObjectTypeNode(
  override val info: NodeInfo,
  val name: IdentifierNode,
  val fields: List<ObjectFieldNode>,
): DeclarationNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("name", name)
        put("fields", fields)
    }
}
data class SignalTypeNode(
  override val info: NodeInfo,
  val name: IdentifierNode,
  val fields: List<ObjectFieldNode>,
  val result: TypeReferenceNode?,
): DeclarationNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("name", name)
        put("fields", fields)
        if (result != null) {
          put("result", result)
        }
    }
}
data class ObjectFieldNode(
  override val info: NodeInfo,
  val name: IdentifierNode,
  val typeAnnotation: TypeReferenceNode,
): AstNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("name", name)
        put("typeAnnotation", typeAnnotation)
    }
}
data class OneOfTypeNode(
  override val info: NodeInfo,
  val name: IdentifierNode,
  val options: List<TypeReferenceNode>,
): DeclarationNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("name", name)
        put("options", options)
    }
}
data class BlockBodyNode(
  override val info: NodeInfo,
  val statements: List<StatementNode>,
  val result: ExpressionNode?,
): BodyNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("statements", statements)
        if (result != null) {
          put("result", result)
        }
    }
}
data class SingleExpressionBodyNode(
  override val info: NodeInfo,
  val expression: ExpressionNode,
): BodyNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("expression", expression)
    }
}
data class ExpressionStatementNode(
  override val info: NodeInfo,
  val expression: ExpressionNode,
): StatementNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("expression", expression)
    }
}
data class DeclarationStatementNode(
  override val info: NodeInfo,
  val declaration: DeclarationNode,
): StatementNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("declaration", declaration)
    }
}
data class EffectStatementNode(
  override val info: NodeInfo,
  val pattern: PatternNode,
  val body: BodyNode,
): StatementNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("pattern", pattern)
        put("body", body)
    }
}
data class SetStatementNode(
  override val info: NodeInfo,
  val identifier: IdentifierNode,
  val expression: ExpressionNode,
): StatementNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("identifier", identifier)
        put("expression", expression)
    }
}
data class GroupExpressionNode(
  override val info: NodeInfo,
  val expression: ExpressionNode,
): ExpressionNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("expression", expression)
    }
}
data class BlockExpressionNode(
  override val info: NodeInfo,
  val block: BlockBodyNode,
): ExpressionNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("block", block)
    }
}
data class FunctionExpressionNode(
  override val info: NodeInfo,
  val params: List<FunctionSignatureParameterNode>,
  val body: ExpressionNode,
  val returnType: TypeReferenceNode?,
): ExpressionNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("params", params)
        put("body", body)
        if (returnType != null) {
          put("returnType", returnType)
        }
    }
}
data class BranchExpressionNode(
  override val info: NodeInfo,
  val withValue: ExpressionNode?,
  val branches: List<BranchOptionNode>,
): ExpressionNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        if (withValue != null) {
          put("withValue", withValue)
        }
        put("branches", branches)
    }
}
data class IfBranchOptionNode(
  override val info: NodeInfo,
  val condition: ExpressionNode,
  val body: BodyNode,
): BranchOptionNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("condition", condition)
        put("body", body)
    }
}
data class IsBranchOptionNode(
  override val info: NodeInfo,
  val pattern: PatternNode,
  val body: BodyNode,
): BranchOptionNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("pattern", pattern)
        put("body", body)
    }
}
data class ElseBranchOptionNode(
  override val info: NodeInfo,
  val body: BodyNode,
): BranchOptionNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("body", body)
    }
}
data class LoopExpressionNode(
  override val info: NodeInfo,
  val body: BodyNode,
): ExpressionNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("body", body)
    }
}
data class CauseExpressionNode(
  override val info: NodeInfo,
  val signal: ExpressionNode,
): ExpressionNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("signal", signal)
    }
}
data class CallExpressionNode(
  override val info: NodeInfo,
  val callee: ExpressionNode,
  val parameters: List<FunctionCallParameterNode>,
): ExpressionNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("callee", callee)
        put("parameters", parameters)
    }
}
data class PipeCallExpressionNode(
  override val info: NodeInfo,
  val subject: ExpressionNode,
  val callee: ExpressionNode,
  val parameters: List<FunctionCallParameterNode>,
): ExpressionNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("subject", subject)
        put("callee", callee)
        put("parameters", parameters)
    }
}
data class MemberExpressionNode(
  override val info: NodeInfo,
  val objectExpression: ExpressionNode,
  val memberIdentifier: IdentifierNode,
): ExpressionNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("objectExpression", objectExpression)
        put("memberIdentifier", memberIdentifier)
    }
}
data class IdentifierExpressionNode(
  override val info: NodeInfo,
  val identifier: IdentifierNode,
): ExpressionNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        put("identifier", identifier)
    }
}
data class StringLiteralExpressionNode(
  override val info: NodeInfo,
  val text: String,
): ExpressionNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
    }
}
data class NumberLiteralExpressionNode(
  override val info: NodeInfo,
  val value: BigDecimal,
): ExpressionNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
    }
}
data class ReturnExpressionNode(
  override val info: NodeInfo,
  val value: ExpressionNode?,
): ExpressionNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        if (value != null) {
          put("value", value)
        }
    }
}
data class BreakExpressionNode(
  override val info: NodeInfo,
  val withValue: ExpressionNode?,
): ExpressionNode {
  override fun childNodes(): Map<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild> =
    buildMap {
        if (withValue != null) {
          put("withValue", withValue)
        }
    }
}

fun String.toBreadcrumbEntry() = Breadcrumbs.BreadcrumbEntry.Name(this)
fun Int.toBreadcrumbEntry() = Breadcrumbs.BreadcrumbEntry.Index(this)

internal fun MutableMap<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild>.put(key: String, value: AstNode) {
    put(key.toBreadcrumbEntry(), BreadcrumbWalkChild.Node(value))
}

internal fun MutableMap<Breadcrumbs.BreadcrumbEntry, BreadcrumbWalkChild>.put(
    key: String, value: List<AstNode>
) {
    put(key.toBreadcrumbEntry(), BreadcrumbWalkChild.List(value))
}