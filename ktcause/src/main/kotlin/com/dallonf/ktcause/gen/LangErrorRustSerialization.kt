package com.dallonf.ktcause.gen

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
    TODO()
  }
  
  fun deserializeImplementationTodoErrorLangType(error: JsonElement): ErrorLangType.ImplementationTodo {
    require(error is JsonObject)
    TODO()
  }
  
  fun deserializeMismatchedTypeErrorLangType(error: JsonElement): ErrorLangType.MismatchedType {
    require(error is JsonObject)
    TODO()
  }
  
  fun deserializeMissingParametersErrorLangType(error: JsonElement): ErrorLangType.MissingParameters {
    require(error is JsonObject)
    TODO()
  }
  
  fun deserializeExcessParametersErrorLangType(error: JsonElement): ErrorLangType.ExcessParameters {
    require(error is JsonObject)
    TODO()
  }
  
  fun deserializeMissingElseBranchErrorLangType(error: JsonElement): ErrorLangType.MissingElseBranch {
    require(error is JsonObject)
    TODO()
  }
  
  fun deserializeUnreachableBranchErrorLangType(error: JsonElement): ErrorLangType.UnreachableBranch {
    require(error is JsonObject)
    TODO()
  }
  
  fun deserializeActionIncompatibleWithValueTypesErrorLangType(error: JsonElement): ErrorLangType.ActionIncompatibleWithValueTypes {
    require(error is JsonObject)
    TODO()
  }
  
  fun deserializeConstraintUsedAsValueErrorLangType(error: JsonElement): ErrorLangType.ConstraintUsedAsValue {
    require(error is JsonObject)
    TODO()
  }
  
  fun deserializeValueUsedAsConstraintErrorLangType(error: JsonElement): ErrorLangType.ValueUsedAsConstraint {
    require(error is JsonObject)
    TODO()
  }
  
  fun deserializeCompilerBugErrorLangType(error: JsonElement): ErrorLangType.CompilerBug {
    require(error is JsonObject)
    TODO()
  }
}