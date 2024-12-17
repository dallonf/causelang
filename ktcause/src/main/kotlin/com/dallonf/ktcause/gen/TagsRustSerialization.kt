package com.dallonf.ktcause.gen

import kotlinx.serialization.json.*
import com.dallonf.ktcause.NodeTag
import com.dallonf.ktcause.serialization.RustSerialization

object TagsRustSerialization {
  fun serializeNodeTag(tag: NodeTag): JsonElement {
    return when (tag) {
      is NodeTag.ReferencesFile -> buildJsonObject {
        put("ReferencesFile", serializeReferencesFileTag(tag))
      }
      is NodeTag.CanonicalIdInfo -> buildJsonObject {
        put("CanonicalIdInfo", serializeCanonicalIdInfoTag(tag))
      }
      is NodeTag.BadFileReference -> buildJsonObject {
        put("BadFileReference", serializeBadFileReferenceTag(tag))
      }
      is NodeTag.TopLevelDeclaration -> buildJsonObject {
        put("TopLevelDeclaration", serializeTopLevelDeclarationTag(tag))
      }
      is NodeTag.ValueGoesTo -> buildJsonObject {
        put("ValueGoesTo", serializeValueGoesToTag(tag))
      }
      is NodeTag.ValueComesFrom -> buildJsonObject {
        put("ValueComesFrom", serializeValueComesFromTag(tag))
      }
      is NodeTag.FunctionCanReturnTypeOf -> buildJsonObject {
        put("FunctionCanReturnTypeOf", serializeFunctionCanReturnTypeOfTag(tag))
      }
      is NodeTag.ReturnsFromFunction -> buildJsonObject {
        put("ReturnsFromFunction", serializeReturnsFromFunctionTag(tag))
      }
      is NodeTag.FunctionCanReturnAction -> buildJsonObject {
        put("FunctionCanReturnAction", serializeFunctionCanReturnActionTag(tag))
      }
      is NodeTag.ActionReturn -> buildJsonObject {
        put("ActionReturn", serializeActionReturnTag(tag))
      }
      is NodeTag.DeclarationForScope -> buildJsonObject {
        put("DeclarationForScope", serializeDeclarationForScopeTag(tag))
      }
      is NodeTag.ScopeContainsDeclaration -> buildJsonObject {
        put("ScopeContainsDeclaration", serializeScopeContainsDeclarationTag(tag))
      }
      else -> TODO("Unknown tag type: ${tag::class.simpleName}")
    }
  }

  fun serializeReferencesFileTag(tag: NodeTag.ReferencesFile): JsonElement {
    return buildJsonObject {
      put("path", tag.path)
      put("export_name", tag.exportName)
    }
  }
  fun serializeCanonicalIdInfoTag(tag: NodeTag.CanonicalIdInfo): JsonElement {
    return buildJsonObject {
      put("parent_name", tag.parentName)
      put("index", tag.index)
    }
  }
  fun serializeBadFileReferenceTag(tag: NodeTag.BadFileReference): JsonElement {
    return buildJsonObject {
    }
  }
  fun serializeTopLevelDeclarationTag(tag: NodeTag.TopLevelDeclaration): JsonElement {
    return buildJsonObject {
      put("name", tag.name)
    }
  }
  fun serializeValueGoesToTag(tag: NodeTag.ValueGoesTo): JsonElement {
    return buildJsonObject {
      put("destination", RustSerialization.serializeBreadcrumbs(tag.destination))
    }
  }
  fun serializeValueComesFromTag(tag: NodeTag.ValueComesFrom): JsonElement {
    return buildJsonObject {
      put("source", RustSerialization.serializeBreadcrumbs(tag.source))
    }
  }
  fun serializeFunctionCanReturnTypeOfTag(tag: NodeTag.FunctionCanReturnTypeOf): JsonElement {
    return buildJsonObject {
      put("return_expression_value", RustSerialization.serializeBreadcrumbs(tag.returnExpressionValue))
    }
  }
  fun serializeReturnsFromFunctionTag(tag: NodeTag.ReturnsFromFunction): JsonElement {
    return buildJsonObject {
      put("function", RustSerialization.serializeBreadcrumbs(tag.function))
    }
  }
  fun serializeFunctionCanReturnActionTag(tag: NodeTag.FunctionCanReturnAction): JsonElement {
    return buildJsonObject {
      put("return_expression", RustSerialization.serializeBreadcrumbs(tag.returnExpression))
    }
  }
  fun serializeActionReturnTag(tag: NodeTag.ActionReturn): JsonElement {
    return buildJsonObject {
      put("function", RustSerialization.serializeBreadcrumbs(tag.function))
    }
  }
  fun serializeDeclarationForScopeTag(tag: NodeTag.DeclarationForScope): JsonElement {
    return buildJsonObject {
      put("scope", RustSerialization.serializeBreadcrumbs(tag.scope))
    }
  }
  fun serializeScopeContainsDeclarationTag(tag: NodeTag.ScopeContainsDeclaration): JsonElement {
    return buildJsonObject {
      put("declaration", RustSerialization.serializeBreadcrumbs(tag.declaration))
    }
  }
}