package com.dallonf.ktcause

import com.dallonf.ktcause.ast.*
import com.dallonf.ktcause.gen.AstRustSerialization
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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.internal.writeJson
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

    private val mode = Mode.IF_SUPPORTED

    init {
        System.loadLibrary("rscause_jni")
    }

    fun shouldRunRustCompiler(
        path: String, ast: FileNode, analyzed: AnalyzedNode, otherFiles: Map<String, Resolver.ExternalFileDescriptor>
    ): Boolean {
        return when (mode) {
            Mode.ALWAYS -> true
            Mode.NEVER -> false
            Mode.IF_SUPPORTED -> {
                getReasonsNotSupported(ast, analyzed, path, otherFiles).none()
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

        val functionDeclarationsWithParameters = run {
            val functions = ast.allDescendants().mapNotNull { it as? FunctionNode }
            functions.filter { it.params.isNotEmpty() }
        }
        yieldAll(functionDeclarationsWithParameters.map { "Declared function by the name of ${it.name.text} has parameters" })

        val unsupportedImports = run {
            val imports = ast.allDescendants().mapNotNull { it as? ImportNode }
            imports.filter { !supportedCoreImports.contains(it.path.path) }
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
                is FunctionNode -> it.returnType != null
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
                    else -> it
                }
            }
        }
        yieldAll(typeErrorsOnlyKtResolverWouldFind.map { "Found type error that the Rust resolver can't output yet: $it" })
    }

    val supportedCoreImports = setOf("core/builtin.cau", "core/math")
    private val supportedCoreBuiltins = setOf("Debug", "Action", "Text", "Number", "equals")
    private val unsupportedIdentifiers = setOf("AssumptionBroken")

    fun compile(
        path: String,
        ast: FileNode,
        tags: Map<Breadcrumbs, List<NodeTag>>,
        externalFiles: Map<String, Resolver.ExternalFileDescriptor>
    ): RustCompilerResult {
        val filteredCanonicalTypes = getFilteredCanonicalTypes(externalFiles)
        val filteredExternalFiles = getFilteredExternalFiles(externalFiles)
        val filteredTags = getFilteredTags(tags)

//        generateTestOutput("tmp", ast, filteredTags, filteredCanonicalTypes, filteredExternalFiles);
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
            // only supported core types for now
            .filter { supportedCoreBuiltins.contains(it.key.name) }
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
                Resolver.ExternalFileDescriptor(filteredExports, value.types)
            } else {
                value
            }
        }

    fun getFilteredTags(tags: Map<Breadcrumbs, List<NodeTag>>) = tags.mapValues { (breadcrumbs, tags) ->
        tags.filter {
            when (it) {
                is NodeTag.ReferencesFile -> true
                is NodeTag.BadFileReference -> true
                is NodeTag.ValueGoesTo -> true
                is NodeTag.ValueComesFrom -> true
                is NodeTag.FunctionCanReturnTypeOf -> true
                is NodeTag.ReturnsFromFunction -> true
                is NodeTag.FunctionCanReturnAction -> true
                is NodeTag.ActionReturn -> true
                is NodeTag.DeclarationForScope -> true
                is NodeTag.ScopeContainsDeclaration -> true
                is NodeTag.TopLevelDeclaration -> true
                else -> false
            }
        }
    }

    private val otherUnsupportedNodeTypes: List<String> =
        listOf<KClass<out Any>>(IsBranchOptionNode::class).mapNotNull { it.simpleName }

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


    external fun rsSerializeAst(ast: FileNode): String
    private external fun rsSerializeTagsInner(tags: Map<Breadcrumbs, List<NodeTag>>): String
    fun rsSerializeTags(tags: Map<Breadcrumbs, List<NodeTag>>): String {
        return rsSerializeTagsInner(getFilteredTags(tags))
    }

    private external fun rsSerializeExternalFilesInner(externalFiles: Map<String, Resolver.ExternalFileDescriptor>): String
    fun rsSerializeExternalFiles(externalFiles: Map<String, Resolver.ExternalFileDescriptor>): String {
        return rsSerializeExternalFilesInner(externalFiles)
    }

    private external fun rsSerializeCanonicalTypesInner(canonicalTypes: Map<CanonicalLangTypeId, CanonicalLangType>): String
    fun rsSerializeCanonicalTypes(canonicalTypes: Map<CanonicalLangTypeId, CanonicalLangType>): String {
        return rsSerializeCanonicalTypesInner(canonicalTypes)
    }

    data class RustCompilerResult(
        val compiledFile: CompiledFile, val errors: List<Resolver.ResolverError>
    )
}