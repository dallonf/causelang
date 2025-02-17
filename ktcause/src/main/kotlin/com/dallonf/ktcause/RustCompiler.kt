package com.dallonf.ktcause

import com.dallonf.ktcause.ast.*
import com.dallonf.ktcause.gen.AstRustSerialization
import com.dallonf.ktcause.serialization.CompilerResultRustSerialization.deserializeRustCompilerResult
import com.dallonf.ktcause.serialization.LangTypeRustSerialization
import com.dallonf.ktcause.serialization.RustSerialization
import com.dallonf.ktcause.types.CanonicalLangType
import com.dallonf.ktcause.types.CanonicalLangTypeId
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object RustCompiler {
    private const val OUTPUT_TMP = false

    init {
        System.loadLibrary("rscause_jni")
    }

    fun shouldRunRustCompiler(
        path: String, ast: FileNode, analyzed: AnalyzedNode, otherFiles: Map<String, Resolver.ExternalFileDescriptor>
    ): Boolean {
        return true
    }


    fun compile(
        path: String,
        ast: FileNode,
        tags: Map<Breadcrumbs, List<NodeTag>>,
        externalFiles: Map<String, Resolver.ExternalFileDescriptor>
    ): RustCompilerResult {
        val filteredCanonicalTypes = getCanonicalTypes(externalFiles)

        if (OUTPUT_TMP) {
            generateTestOutput("tmp", ast, tags, filteredCanonicalTypes, externalFiles)
        }
        val astJson = AstRustSerialization.serializeFile(ast).let { RustSerialization.encoder.encodeToString(it) }
        val tagsJson = RustSerialization.serializeNodeTagMap(tags).let { RustSerialization.encoder.encodeToString(it) }
        val canonicalTypesJson = LangTypeRustSerialization.serializeCanonicalTypeMap(filteredCanonicalTypes)
            .let { RustSerialization.encoder.encodeToString(it) }
        val externalFilesJson = RustSerialization.serializeExternalFileDescriptorMap(externalFiles)
            .let { RustSerialization.encoder.encodeToString(it) }

        val resultJson = compileInner(
            path, astJson, tagsJson, canonicalTypesJson, externalFilesJson
        )
        val result = deserializeRustCompilerResult(Json.parseToJsonElement(resultJson))
        return result
    }

    private fun getCanonicalTypes(externalFiles: Map<String, Resolver.ExternalFileDescriptor>): Map<CanonicalLangTypeId, CanonicalLangType> {
        val allEntries = externalFiles.flatMap { it.value.types.entries }
        val asPairs = allEntries.map { it.toPair() }
        return mapOf(*asPairs.toTypedArray())
    }

    private external fun compileInner(
        path: String, astJson: String, tagsJson: String, canonicalTypesJson: String, externalFilesJson: String
    ): String

    private fun generateTestOutput(
        testName: String,
        ast: FileNode,
        tags: Map<Breadcrumbs, List<NodeTag>>,
        canonicalTypes: Map<CanonicalLangTypeId, CanonicalLangType>,
        externalFiles: Map<String, Resolver.ExternalFileDescriptor>
    ) {
        val path = "../rscause/rscause_compiler/tests/fixtures/$testName"
        File("$path/ast.json").writeText(RustSerialization.encoder.encodeToString(AstRustSerialization.serializeFile(ast)))
        File("$path/tags.json").writeText(
            RustSerialization.encoder.encodeToString(
                RustSerialization.serializeNodeTagMap(
                    tags
                )
            )
        )
        File("$path/canonical_types.json").writeText(
            RustSerialization.encoder.encodeToString(
                LangTypeRustSerialization.serializeCanonicalTypeMap(
                    canonicalTypes
                )
            )
        )
        File("$path/external_files.json").writeText(
            RustSerialization.encoder.encodeToString(
                RustSerialization.serializeExternalFileDescriptorMap(
                    externalFiles
                )
            )
        )
    }


    data class RustCompilerResult(
        val compiledFile: CompiledFile, val errors: List<Resolver.ResolverError>
    )
}