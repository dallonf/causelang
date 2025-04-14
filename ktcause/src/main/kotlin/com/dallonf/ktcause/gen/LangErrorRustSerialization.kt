package com.dallonf.ktcause.gen

import com.dallonf.ktcause.serialization.LangTypeRustSerialization.deserializeConstraintValueLangType
import com.dallonf.ktcause.serialization.LangTypeRustSerialization.deserializeOptionValueLangType
import com.dallonf.ktcause.serialization.LangTypeRustSerialization.serializeOneOfLangType
import com.dallonf.ktcause.serialization.LangTypeRustSerialization.deserializeResolvedValueLangType
import com.dallonf.ktcause.serialization.LangTypeRustSerialization.deserializeValueLangType
import com.dallonf.ktcause.serialization.LangTypeRustSerialization.serializeFallibleLangType
import com.dallonf.ktcause.serialization.LangTypeRustSerialization.serializeLangType
import com.dallonf.ktcause.serialization.RustSerialization.deserializeSourcePosition
import com.dallonf.ktcause.serialization.RustSerialization.serializeErrorPosition
import com.dallonf.ktcause.serialization.RustSerialization.deserializeSourcePositionSource
import com.dallonf.ktcause.serialization.RustSerialization.serializeSourcePosition
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

    fun serializeLangError(errorLangType: ErrorLangType): JsonElement {
        return when (errorLangType) {
            is ErrorLangType.NeverResolved ->  JsonPrimitive("NeverResolved") 
            is ErrorLangType.NotInScope ->  JsonPrimitive("NotInScope") 
            is ErrorLangType.FileNotFound ->  JsonPrimitive("FileNotFound") 
            is ErrorLangType.ImportPathInvalid ->  JsonPrimitive("ImportPathInvalid") 
            is ErrorLangType.ExportNotFound ->  JsonPrimitive("ExportNotFound") 
            is ErrorLangType.ProxyError ->  buildJsonObject {
                put("ProxyError", serializeLangErrorProxyError(errorLangType))
            } 
            is ErrorLangType.NotCallable ->  JsonPrimitive("NotCallable") 
            is ErrorLangType.NotCausable ->  JsonPrimitive("NotCausable") 
            is ErrorLangType.ImplementationTodo ->  buildJsonObject {
                put("ImplementationTodo", serializeLangErrorImplementationTodo(errorLangType))
            } 
            is ErrorLangType.MismatchedType ->  buildJsonObject {
                put("MismatchedType", serializeLangErrorMismatchedType(errorLangType))
            } 
            is ErrorLangType.MissingParameters ->  buildJsonObject {
                put("MissingParameters", serializeLangErrorMissingParameters(errorLangType))
            } 
            is ErrorLangType.ExcessParameters ->  buildJsonObject {
                put("ExcessParameters", serializeLangErrorExcessParameters(errorLangType))
            } 
            is ErrorLangType.UnknownParameter ->  JsonPrimitive("UnknownParameter") 
            is ErrorLangType.MissingElseBranch ->  buildJsonObject {
                put("MissingElseBranch", serializeLangErrorMissingElseBranch(errorLangType))
            } 
            is ErrorLangType.UnreachableBranch ->  buildJsonObject {
                put("UnreachableBranch", serializeLangErrorUnreachableBranch(errorLangType))
            } 
            is ErrorLangType.ActionIncompatibleWithValueTypes ->  buildJsonObject {
                put("ActionIncompatibleWithValueTypes", serializeLangErrorActionIncompatibleWithValueTypes(errorLangType))
            } 
            is ErrorLangType.ConstraintUsedAsValue ->  buildJsonObject {
                put("ConstraintUsedAsValue", serializeLangErrorConstraintUsedAsValue(errorLangType))
            } 
            is ErrorLangType.ValueUsedAsConstraint ->  buildJsonObject {
                put("ValueUsedAsConstraint", serializeLangErrorValueUsedAsConstraint(errorLangType))
            } 
            is ErrorLangType.DoesNotHaveAnyMembers ->  JsonPrimitive("DoesNotHaveAnyMembers") 
            is ErrorLangType.DoesNotHaveMember ->  JsonPrimitive("DoesNotHaveMember") 
            is ErrorLangType.NotVariable ->  JsonPrimitive("NotVariable") 
            is ErrorLangType.OuterVariable ->  JsonPrimitive("OuterVariable") 
            is ErrorLangType.CannotBreakHere ->  JsonPrimitive("CannotBreakHere") 
            is ErrorLangType.NotSupportedInRust ->  JsonPrimitive("NotSupportedInRust") 
            is ErrorLangType.CompilerBug ->  buildJsonObject {
                put("CompilerBug", serializeLangErrorCompilerBug(errorLangType))
            } 
        }
    }

    fun deserializeProxyErrorErrorLangType(error: JsonElement): ErrorLangType.ProxyError {
        require(error is JsonObject)
        return ErrorLangType.ProxyError(
            deserializeErrorLangType(error["actual_error"]!!),
            (error["proxy_chain"] as JsonArray).map { deserializeSourcePosition(it) },
        )
    }

    fun serializeLangErrorProxyError(errorLangType: ErrorLangType.ProxyError): JsonElement {
        return buildJsonObject {
            put("actual_error", serializeLangError(errorLangType.actualError))
            put("proxy_chain", JsonArray(errorLangType.proxyChain.map { serializeErrorPosition(it) }))
        }
    }
    fun deserializeImplementationTodoErrorLangType(error: JsonElement): ErrorLangType.ImplementationTodo {
        require(error is JsonObject)
        return ErrorLangType.ImplementationTodo(
            (error["description"] as JsonPrimitive).content,
        )
    }

    fun serializeLangErrorImplementationTodo(errorLangType: ErrorLangType.ImplementationTodo): JsonElement {
        return buildJsonObject {
            put("description", JsonPrimitive(errorLangType.description))
        }
    }
    fun deserializeMissingParametersErrorLangType(error: JsonElement): ErrorLangType.MissingParameters {
        require(error is JsonObject)
        return ErrorLangType.MissingParameters(
            (error["names"] as JsonArray).map { (it as JsonPrimitive).content },
        )
    }

    fun serializeLangErrorMissingParameters(errorLangType: ErrorLangType.MissingParameters): JsonElement {
        return buildJsonObject {
            put("names", JsonArray(errorLangType.names.map { JsonPrimitive(it) }))
        }
    }
    fun deserializeExcessParametersErrorLangType(error: JsonElement): ErrorLangType.ExcessParameters {
        require(error is JsonObject)
        return ErrorLangType.ExcessParameters(
            (error["expected"] as JsonPrimitive).int,
        )
    }

    fun serializeLangErrorExcessParameters(errorLangType: ErrorLangType.ExcessParameters): JsonElement {
        return buildJsonObject {
            put("expected", JsonPrimitive(errorLangType.expected))
        }
    }
    fun deserializeMissingElseBranchErrorLangType(error: JsonElement): ErrorLangType.MissingElseBranch {
        require(error is JsonObject)
        return ErrorLangType.MissingElseBranch(
            error["options"]?.let { if (error["options"] is JsonNull) { null } else { deserializeOptionValueLangType(it) } },
        )
    }

    fun serializeLangErrorMissingElseBranch(errorLangType: ErrorLangType.MissingElseBranch): JsonElement {
        return buildJsonObject {
            put("options", errorLangType.options?.let { serializeOneOfLangType(it) } ?: JsonNull)
        }
    }
    fun deserializeUnreachableBranchErrorLangType(error: JsonElement): ErrorLangType.UnreachableBranch {
        require(error is JsonObject)
        return ErrorLangType.UnreachableBranch(
            error["options"]?.let { if (error["options"] is JsonNull) { null } else { deserializeOptionValueLangType(it) } },
        )
    }

    fun serializeLangErrorUnreachableBranch(errorLangType: ErrorLangType.UnreachableBranch): JsonElement {
        return buildJsonObject {
            put("options", errorLangType.options?.let { serializeOneOfLangType(it) } ?: JsonNull)
        }
    }
    fun deserializeConstraintUsedAsValueErrorLangType(error: JsonElement): ErrorLangType.ConstraintUsedAsValue {
        require(error is JsonObject)
        return ErrorLangType.ConstraintUsedAsValue(
            deserializeResolvedValueLangType(error["type"]!!),
        )
    }

    fun serializeLangErrorConstraintUsedAsValue(errorLangType: ErrorLangType.ConstraintUsedAsValue): JsonElement {
        return buildJsonObject {
            put("type", serializeLangType(errorLangType.type))
        }
    }
    fun deserializeValueUsedAsConstraintErrorLangType(error: JsonElement): ErrorLangType.ValueUsedAsConstraint {
        require(error is JsonObject)
        return ErrorLangType.ValueUsedAsConstraint(
            deserializeValueLangType(error["type"]!!),
        )
    }

    fun serializeLangErrorValueUsedAsConstraint(errorLangType: ErrorLangType.ValueUsedAsConstraint): JsonElement {
        return buildJsonObject {
            put("type", serializeFallibleLangType(errorLangType.type))
        }
    }
    fun deserializeCompilerBugErrorLangType(error: JsonElement): ErrorLangType.CompilerBug {
        require(error is JsonObject)
        return ErrorLangType.CompilerBug(
            (error["description"] as JsonPrimitive).content,
        )
    }

    fun serializeLangErrorCompilerBug(errorLangType: ErrorLangType.CompilerBug): JsonElement {
        return buildJsonObject {
            put("description", JsonPrimitive(errorLangType.description))
        }
    }

    fun deserializeMismatchedTypeErrorLangType(error: JsonElement): ErrorLangType {
        require(error is JsonObject)
        return ErrorLangType.MismatchedType(
            deserializeResolvedValueLangType(error["expected"]!!).toConstraint(),
            deserializeResolvedValueLangType(error["actual"]!!),
        )
    }

    fun serializeLangErrorMismatchedType(errorLangType: ErrorLangType.MismatchedType): JsonElement {
        return buildJsonObject {
            put("expected",  serializeLangType(errorLangType.expected.valueType))
            put("actual",  serializeLangType(errorLangType.actual))
        }
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

    private fun serializeLangErrorActionIncompatibleWithValueTypes(errorLangType: ErrorLangType.ActionIncompatibleWithValueTypes): JsonElement {
        return buildJsonObject {
            put("actions", JsonArray(errorLangType.actions.map {
                serializeSourcePosition(it)
            }))
            errorLangType.types?.let { types ->
                put("types", JsonArray(types.map {
                    buildJsonObject {
                        put("type", serializeFallibleLangType(it.type))
                        put("position", serializeSourcePosition(it.position))
                    }
                }))
            }
        }
    }
}