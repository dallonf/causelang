package com.dallonf.ktcause.serialization

import com.dallonf.ktcause.CompiledFile
import com.dallonf.ktcause.Resolver
import com.dallonf.ktcause.RustCompiler
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

object CompilerResultRustSerialization {
    fun deserializeRustCompilerResult(rustCompilerResult: JsonObject): RustCompiler.RustCompilerResult {
        val compiledFile = deserializeCompiledFile(rustCompilerResult["compiled_file"] as JsonObject)
        val errors = (rustCompilerResult["errors"] as JsonArray).map { deserializeResolverError(it as JsonObject) }

        return RustCompiler.RustCompilerResult(compiledFile, errors)
    }

    private fun deserializeResolverError(resolverError: JsonObject): Resolver.ResolverError {
        TODO()
    }

    private fun deserializeCompiledFile(compiledFile: JsonObject): CompiledFile {
        TODO()
    }

}