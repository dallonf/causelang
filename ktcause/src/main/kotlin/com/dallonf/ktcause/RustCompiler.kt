package com.dallonf.ktcause

import com.dallonf.ktcause.ast.*
import com.dallonf.ktcause.gen.AstRustSerialization
import com.dallonf.ktcause.gen.TagsRustSerialization
import com.dallonf.ktcause.gen.rustCompilerSupportedTypes
import com.dallonf.ktcause.serialization.CompilerResultRustSerialization.deserializeRustCompilerResult
import com.dallonf.ktcause.serialization.LangTypeRustSerialization
import com.dallonf.ktcause.serialization.RustSerialization
import com.dallonf.ktcause.types.ActionValueLangType
import com.dallonf.ktcause.types.CanonicalLangType
import com.dallonf.ktcause.types.CanonicalLangTypeId
import com.dallonf.ktcause.types.ErrorLangType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.reflect.KClass

object RustCompiler {
    enum class Mode {
        /**
         * Always run the rscause compiler.
         * The tests that pass in this mode
         * show the true current capabilities of rscause.
         */
        ALWAYS,

        /**
         * Never run the rscause compiler, use ktcause instead.
         * All tests should pass in this mode and should be used
         * when performance and stability are important.
         * (... it's a toy language, I'm not sure when that would be)
         */
        NEVER,

        /**
         * Only run the rscause compiler if the given file is
         * within the current capabilities the rscause compiler.
         * All tests should pass in this mode.
         */
        IF_SUPPORTED,

        /**
         * Always run the rscause compiler, and throw an error
         * if it would not run in IF_SUPPORTED.
         * The tests that pass in this mode should match the tests
         * that pass in ALWAYS mode.
         *
         * This is a sanity check to make sure that the IF_SUPPORTED
         * detection logic is actually allowing rscause to run.
         */
        ASSERT_SUPPORTED,
    }

    private val MODE = Mode.IF_SUPPORTED
    private const val OUTPUT_TMP = false

    init {
        System.loadLibrary("rscause_jni")
    }

    fun shouldRunRustCompiler(
        path: String, ast: FileNode, analyzed: AnalyzedNode, otherFiles: Map<String, Resolver.ExternalFileDescriptor>
    ): Boolean {
        return when (MODE) {
            Mode.ALWAYS -> true
            Mode.NEVER -> false
            Mode.IF_SUPPORTED -> {
                val reasonsNotSupported = getReasonsNotSupported(ast, analyzed, path, otherFiles).toList()
                if (reasonsNotSupported.isNotEmpty()) {
                    println(
                        "NOTE: Falling back to Kotlin compiler:\n" + reasonsNotSupported.joinToString(
                            "\n"
                        )
                    )
                }
                reasonsNotSupported.none()
            }

            Mode.ASSERT_SUPPORTED -> {
                val reasonsNotSupported = getReasonsNotSupported(ast, analyzed, path, otherFiles).toList();
                if (reasonsNotSupported.isNotEmpty()) {
                    throw AssertionError(
                        "$path is not supported by the Rust compiler:\n" + reasonsNotSupported.joinToString(
                            "\n"
                        )
                    )
                }
                true
            }
        }
    }

    private fun getReasonsNotSupported(
        ast: FileNode, analyzed: AnalyzedNode, path: String, otherFiles: Map<String, Resolver.ExternalFileDescriptor>
    ): Sequence<String> = sequence {
        val incompatibleNodes = getIncompatibleNodeTypes(ast)
        yieldAll(incompatibleNodes.map { "Incompatible node type: $it" })

        val unsupportedImports = run {
            val imports = ast.allDescendants().mapNotNull { it as? ImportNode }
            imports.filter { it.path.path.startsWith("core/") }.filter { !supportedCoreImports.contains(it.path.path) }
        }
        yieldAll(unsupportedImports.map { "Unsupported import: ${it.path.path}" })

        val unsupportedIdentifiers = run {
            val identifiers = ast.allDescendants().mapNotNull { it as? IdentifierNode }
            identifiers.filter { unsupportedIdentifiers.contains(it.text) }
                // but don't worry about it if it's only imported
                .filter { it.allAncestors(ast).none { ancestor -> ancestor is ImportMappingNode } }
        }
        yieldAll(unsupportedIdentifiers.map { "Unsupported identifier: ${it.text}" })

        val unsupportedTypeAnnotations = ast.allDescendants().filter {
            when (it) {
                else -> false
            }
        }
        yieldAll(unsupportedTypeAnnotations.map { "Unsupported type annotation at ${it.info.breadcrumbs}" })

        val typeErrorsOnlyKtResolverWouldFind = run {
            val (resolvedFile, resolverErrors) = Resolver.resolveForFile(
                path, ast, analyzed, otherFiles
            )
            resolverErrors.mapNotNull {
                when (it.error) {
                    is ErrorLangType.NotCausable -> null
                    is ErrorLangType.MismatchedType -> null
                    is ErrorLangType.MissingElseBranch -> null
                    is ErrorLangType.ActionIncompatibleWithValueTypes -> null
                    is ErrorLangType.NotVariable -> null
                    is ErrorLangType.OuterVariable -> null
                    else -> it
                }
            }
        }
        yieldAll(typeErrorsOnlyKtResolverWouldFind.map { "Found type error that the Rust resolver can't output yet: $it" })
    }

    val supportedCoreImports = setOf("core/builtin.cau", "core/math", "core/text")
    private val supportedCoreBuiltins =
        setOf("Debug", "AssumptionBroken", "Action", "Anything", "AnySignal", "Text", "Number", "equals")
    private val unsupportedIdentifiers: Set<String> = setOf()

    fun compile(
        path: String,
        ast: FileNode,
        tags: Map<Breadcrumbs, List<NodeTag>>,
        externalFiles: Map<String, Resolver.ExternalFileDescriptor>
    ): RustCompilerResult {
        val filteredExternalFiles = getFilteredExternalFiles(externalFiles)
        val filteredCanonicalTypes = getFilteredCanonicalTypes(filteredExternalFiles)
        val filteredTags = getFilteredTags(tags)

        if (OUTPUT_TMP) {
            generateTestOutput("tmp", ast, filteredTags, filteredCanonicalTypes, filteredExternalFiles)
        }
        val astJson = AstRustSerialization.serializeFile(ast).let { RustSerialization.encoder.encodeToString(it) }
        val tagsJson =
            RustSerialization.serializeNodeTagMap(filteredTags).let { RustSerialization.encoder.encodeToString(it) }
        val canonicalTypesJson = LangTypeRustSerialization.serializeCanonicalTypeMap(filteredCanonicalTypes)
            .let { RustSerialization.encoder.encodeToString(it) }
        val externalFilesJson = RustSerialization.serializeExternalFileDescriptorMap(filteredExternalFiles)
            .let { RustSerialization.encoder.encodeToString(it) }

        val resultJson = compileInner(
            path, astJson, tagsJson, canonicalTypesJson, externalFilesJson
        )
        val result = deserializeRustCompilerResult(Json.parseToJsonElement(resultJson))
        return result
    }

    fun getFilteredCanonicalTypes(externalFiles: Map<String, Resolver.ExternalFileDescriptor>): Map<CanonicalLangTypeId, CanonicalLangType> {
        val allEntries = externalFiles.flatMap { it.value.types.entries }
        val asPairs = allEntries.map { it.toPair() }
        return mapOf(*asPairs.toTypedArray())
    }

    fun getFilteredExternalFiles(externalFiles: Map<String, Resolver.ExternalFileDescriptor>) =
        externalFiles.mapValues { (key, value) ->
            if (key == "core/builtin.cau") {
                val filteredExports = value.exports.mapValues { (exportKey, exportValue) ->
                    // only supported core exports for now
                    // all others are just Actions
                    if (supportedCoreBuiltins.contains(exportKey)) {
                        exportValue
                    } else {
                        ActionValueLangType
                    }
                }
                val filteredTypes = value.types.filter { supportedCoreBuiltins.contains(it.key.name) }
                Resolver.ExternalFileDescriptor(filteredExports, filteredTypes)
            } else {
                value
            }
        }

    fun getFilteredTags(tags: Map<Breadcrumbs, List<NodeTag>>) = tags.mapValues { (breadcrumbs, tags) ->
        tags.filter { TagsRustSerialization.isNodeTagSupported(it) }
    }

    private val otherUnsupportedNodeTypes: List<String> = listOf<KClass<out Any>>().mapNotNull { it.simpleName }

    private fun getIncompatibleNodeTypes(ast: FileNode): Sequence<String> {
        val allNodes = ast.allDescendants()
        return allNodes.mapNotNull {
            val name = it::class.simpleName
            if (rustCompilerSupportedTypes.contains(name) && !otherUnsupportedNodeTypes.contains(name)) {
                null
            } else {
                name
            }
        }
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