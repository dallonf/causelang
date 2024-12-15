package com.dallonf.ktcause.serialization

import com.dallonf.ktcause.CompiledFile
import com.dallonf.ktcause.Instruction
import com.dallonf.ktcause.Resolver
import com.dallonf.ktcause.RustCompiler
import com.dallonf.ktcause.ast.SourcePosition
import com.dallonf.ktcause.gen.InstructionRustSerialization
import com.dallonf.ktcause.gen.LangErrorRustSerialization
import com.dallonf.ktcause.types.CanonicalLangType
import com.dallonf.ktcause.types.CanonicalLangTypeId
import com.dallonf.ktcause.types.ErrorLangType
import com.dallonf.ktcause.types.FunctionValueLangType
import kotlinx.serialization.json.*
import org.apache.commons.numbers.fraction.BigFraction

object CompilerResultRustSerialization {
    fun deserializeRustCompilerResult(rustCompilerResult: JsonElement): RustCompiler.RustCompilerResult {
        require(rustCompilerResult is JsonObject)
        val compiledFile = deserializeCompiledFile(rustCompilerResult["compiled_file"]!!)
        val errors = (rustCompilerResult["errors"] as JsonArray).map { deserializeResolverError(it) }

        return RustCompiler.RustCompilerResult(compiledFile, errors)
    }

    fun deserializeResolverError(resolverError: JsonElement): Resolver.ResolverError {
        require(resolverError is JsonObject)
        val position = RustSerialization.deserializeSourcePositionSource(resolverError["position"]!!)
        val error = LangErrorRustSerialization.deserializeErrorLangType(resolverError["error"]!!)

        return Resolver.ResolverError(position, error)
    }

    fun deserializeCompiledFile(compiledFile: JsonElement): CompiledFile {
        require(compiledFile is JsonObject)
        val path = (compiledFile["path"] as JsonPrimitive).content
        val types = emptyMap<CanonicalLangTypeId, CanonicalLangType>()
        val procedures = (compiledFile["procedures"] as JsonArray).map { deserializeProcedure(it as JsonObject) }
        val exports = buildMap {
            val jsonExports = (compiledFile["exports"] as JsonObject)
            jsonExports.forEach { (key, export) -> put(key, deserializeCompiledExport(export as JsonObject)) }
        }

        return CompiledFile(path, types, procedures, exports)
    }

    fun deserializeCompiledExport(compiledExport: JsonElement): CompiledFile.CompiledExport {
        if (compiledExport is JsonObject) {
            compiledExport["Function"]?.let { function ->
                require(function is JsonObject)
                val index = (function["procedure_index"] as JsonPrimitive).int
                val type = function["function_type"].let functionType@{ inferredFunctionType ->
                    if (inferredFunctionType is JsonObject) {
                        inferredFunctionType["Known"]?.let {
                            return@functionType LangTypeRustSerialization.deserializeFunctionValueLangType(
                                it
                            )
                        }
                    }

                    throw AssertionError("Unrecognized InferredType for function export: $inferredFunctionType")
                }

                return CompiledFile.CompiledExport.Function(index, type)
            }
        }

        throw AssertionError("Unrecognized compiled export: $compiledExport")
    }


    fun deserializeProcedure(procedure: JsonElement): CompiledFile.Procedure {
        require(procedure is JsonObject)
        val identity = deserializeProcedureIdentity(procedure["identity"]!!)
        val constantTable = (procedure["constant_table"] as JsonArray).map { deserializeCompiledConstant(it) }
        val instructions =
            (procedure["instructions"] as JsonArray).map { InstructionRustSerialization.deserializeInstruction(it) }
        val sourceMap = (procedure["source_map"] as JsonArray?)?.let { array ->
            array.map {
                if (it is JsonNull) {
                    null
                } else {
                    deserializeProcedureInstructionMapping(it)
                }
            }
        }

        return CompiledFile.Procedure(identity, constantTable, instructions, sourceMap)
    }


    fun deserializeProcedureIdentity(procedureIdentity: JsonElement): CompiledFile.Procedure.ProcedureIdentity {
        if (procedureIdentity is JsonObject) {
            procedureIdentity["Function"]?.let {
                require(it is JsonObject)
                val name = (it["name"] as JsonPrimitive).content
                val declaration = RustSerialization.deserializeNodeInfo(it["declaration"]!!)

                return CompiledFile.Procedure.ProcedureIdentity.Function(name, declaration)
            }

            procedureIdentity["Effect"]?.let {
                require(it is JsonObject)
                val matchesType =
                    LangTypeRustSerialization.deserializeResolvedValueLangType(it["matches_type"]!!).valueToConstraintReference()
                val declaration = RustSerialization.deserializeNodeInfo(it["declaration"]!!)

                return CompiledFile.Procedure.ProcedureIdentity.Effect(matchesType, declaration)
            }
        }

        throw AssertionError("Can't parse as a procedure identity: $procedureIdentity")
    }

    fun deserializeCompiledConstant(compiledConstant: JsonElement): CompiledFile.CompiledConstant {
        if (compiledConstant is JsonObject) {
            compiledConstant["String"]?.let {
                return CompiledFile.CompiledConstant.StringConst((it as JsonPrimitive).content)
            }

            compiledConstant["Number"]?.let {
                val number = BigFraction.parse((it as JsonPrimitive).content)
                return CompiledFile.CompiledConstant.NumberConst(number)
            }

            compiledConstant["Error"]?.let {
                require(it is JsonObject)
                val sourcePosition = RustSerialization.deserializeSourcePosition(it["source_position"]!!)
                val error = LangErrorRustSerialization.deserializeErrorLangType(it["error"]!!)
                return CompiledFile.CompiledConstant.ErrorConst(sourcePosition, error)
            }

            compiledConstant["Type"]?.let {
                return CompiledFile.CompiledConstant.TypeConst(
                    LangTypeRustSerialization.deserializeResolvedValueLangType(
                        it
                    )
                )
            }
        }

        throw AssertionError("Can't parse as a compiled constant: $compiledConstant")
    }


    fun deserializeProcedureInstructionMapping(procedureInstructionMapping: JsonElement): CompiledFile.Procedure.InstructionMapping {
        require(procedureInstructionMapping is JsonObject)
        val nodeInfo = RustSerialization.deserializeNodeInfo(procedureInstructionMapping["node_info"]!!)
        val instructionPhase = when ((procedureInstructionMapping["phase"] as JsonPrimitive).content) {
            "Setup" -> CompiledFile.Procedure.InstructionPhase.SETUP
            "Execute" -> CompiledFile.Procedure.InstructionPhase.EXECUTE
            "Plumbing" -> CompiledFile.Procedure.InstructionPhase.PLUMBING
            "Cleanup" -> CompiledFile.Procedure.InstructionPhase.CLEANUP
            else -> throw AssertionError("Unrecognized instruction phase: ${procedureInstructionMapping["phase"]}")
        }

        return CompiledFile.Procedure.InstructionMapping(nodeInfo, instructionPhase)
    }
}