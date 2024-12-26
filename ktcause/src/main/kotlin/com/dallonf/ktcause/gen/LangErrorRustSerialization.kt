package com.dallonf.ktcause.gen

import com.dallonf.ktcause.serialization.LangTypeRustSerialization.deserializeConstraintValueLangType
import com.dallonf.ktcause.serialization.LangTypeRustSerialization.deserializeOptionValueLangType
import com.dallonf.ktcause.serialization.LangTypeRustSerialization.deserializeResolvedValueLangType
import com.dallonf.ktcause.serialization.LangTypeRustSerialization.deserializeValueLangType
import com.dallonf.ktcause.serialization.RustSerialization.deserializeSourcePosition
import com.dallonf.ktcause.serialization.RustSerialization.deserializeSourcePositionSource
import com.dallonf.ktcause.types.ErrorLangType
import kotlinx.serialization.json.*

object LangErrorRustSerialization {
  fun deserializeErrorLangType(langError: JsonElement): ErrorLangType {
    if (langError is JsonObject) {
      langError["ProxyError"]?.let {
        return deserializeProxyErrorErrorLangType(it)
      }
      langError["ImplementationTodo"]?.let {
        return deserializeImplementationTodoErrorLangType(it)
      }
      langError["MismatchedType"]?.let {
        return deserializeMismatchedTypeErrorLangType(it)
      }
      langError["MissingParameters"]?.let {
        return deserializeMissingParametersErrorLangType(it)
      }
      langError["ExcessParameters"]?.let {
        return deserializeExcessParametersErrorLangType(it)
      }
      langError["MissingElseBranch"]?.let {
        return deserializeMissingElseBranchErrorLangType(it)
      }
      langError["UnreachableBranch"]?.let {
        return deserializeUnreachableBranchErrorLangType(it)
      }
      langError["ActionIncompatibleWithValueTypes"]?.let {
        return deserializeActionIncompatibleWithValueTypesErrorLangType(it)
      }
      langError["ConstraintUsedAsValue"]?.let {
        return deserializeConstraintUsedAsValueErrorLangType(it)
      }
      langError["ValueUsedAsConstraint"]?.let {
        return deserializeValueUsedAsConstraintErrorLangType(it)
      }
      langError["CompilerBug"]?.let {
        return deserializeCompilerBugErrorLangType(it)
      }
    }

    if (langError is JsonPrimitive) {
      val errorName = langError.content
      return when (errorName) {
        "NeverResolved" -> ErrorLangType.NeverResolved
        "NotInScope" -> ErrorLangType.NotInScope
        "FileNotFound" -> ErrorLangType.FileNotFound
        "ImportPathInvalid" -> ErrorLangType.ImportPathInvalid
        "ExportNotFound" -> ErrorLangType.ExportNotFound
        "NotCallable" -> ErrorLangType.NotCallable
        "NotCausable" -> ErrorLangType.NotCausable
        "UnknownParameter" -> ErrorLangType.UnknownParameter
        "DoesNotHaveAnyMembers" -> ErrorLangType.DoesNotHaveAnyMembers
        "DoesNotHaveMember" -> ErrorLangType.DoesNotHaveMember
        "NotVariable" -> ErrorLangType.NotVariable
        "OuterVariable" -> ErrorLangType.OuterVariable
        "CannotBreakHere" -> ErrorLangType.CannotBreakHere
        "NotSupportedInRust" -> ErrorLangType.NotSupportedInRust
        else -> throw AssertionError("Unknown error type: $errorName")
      }
    }

    throw AssertionError("Can't parse as an error: $langError")
  }
  fun deserializeProxyErrorErrorLangType(error: JsonElement): ErrorLangType.ProxyError {
    require(error is JsonObject)
    return ErrorLangType.ProxyError(
      deserializeErrorLangType(error["actual_error"]!!),
      (error["proxy_chain"] as JsonArray).map { deserializeSourcePosition(it) },
    )
  }
  fun deserializeImplementationTodoErrorLangType(error: JsonElement): ErrorLangType.ImplementationTodo {
    require(error is JsonObject)
    return ErrorLangType.ImplementationTodo(
      (error["description"] as JsonPrimitive).content,
    )
  }
  fun deserializeMissingParametersErrorLangType(error: JsonElement): ErrorLangType.MissingParameters {
    require(error is JsonObject)
    return ErrorLangType.MissingParameters(
      (error["names"] as JsonArray).map { (it as JsonPrimitive).content },
    )
  }
  fun deserializeExcessParametersErrorLangType(error: JsonElement): ErrorLangType.ExcessParameters {
    require(error is JsonObject)
    return ErrorLangType.ExcessParameters(
      (error["expected"] as JsonPrimitive).int,
    )
  }
  fun deserializeMissingElseBranchErrorLangType(error: JsonElement): ErrorLangType.MissingElseBranch {
    require(error is JsonObject)
    return ErrorLangType.MissingElseBranch(
      error["options"]?.let { if (error["options"] is JsonNull) { null } else { deserializeOptionValueLangType(it) } },
    )
  }
  fun deserializeUnreachableBranchErrorLangType(error: JsonElement): ErrorLangType.UnreachableBranch {
    require(error is JsonObject)
    return ErrorLangType.UnreachableBranch(
      error["options"]?.let { if (error["options"] is JsonNull) { null } else { deserializeOptionValueLangType(it) } },
    )
  }
  fun deserializeConstraintUsedAsValueErrorLangType(error: JsonElement): ErrorLangType.ConstraintUsedAsValue {
    require(error is JsonObject)
    return ErrorLangType.ConstraintUsedAsValue(
      deserializeConstraintValueLangType(error["type"]!!),
    )
  }
  fun deserializeValueUsedAsConstraintErrorLangType(error: JsonElement): ErrorLangType.ValueUsedAsConstraint {
    require(error is JsonObject)
    return ErrorLangType.ValueUsedAsConstraint(
      deserializeValueLangType(error["type"]!!),
    )
  }
  fun deserializeCompilerBugErrorLangType(error: JsonElement): ErrorLangType.CompilerBug {
    require(error is JsonObject)
    return ErrorLangType.CompilerBug(
      (error["description"] as JsonPrimitive).content,
    )
  }

  fun deserializeMismatchedTypeErrorLangType(error: JsonElement): ErrorLangType {
    require(error is JsonObject)
    return ErrorLangType.MismatchedType(
      deserializeResolvedValueLangType(error["expected"]!!).toConstraint(),
      deserializeResolvedValueLangType(error["actual"]!!),
    )
  }

  private fun deserializeActionIncompatibleWithValueTypesErrorLangType(error: JsonElement): ErrorLangType {
    require(error is JsonObject)
    return ErrorLangType.ActionIncompatibleWithValueTypes(
      (error["actions"] as JsonArray).map { deserializeSourcePositionSource(it) },
      (error["types"] as JsonArray).map {
        require(it is JsonObject)
        ErrorLangType.ActionIncompatibleWithValueTypes.ValueType(
          deserializeResolvedValueLangType(it["type"]!!),
          deserializeSourcePositionSource(it["position"]!!),
        )
      },
    )
  }
}