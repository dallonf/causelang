package com.dallonf.ktcause

import com.dallonf.ktcause.ast.*
import com.dallonf.ktcause.gen.rustCompilerSupportedTypes
import com.dallonf.ktcause.types.ActionValueLangType
import com.dallonf.ktcause.types.CanonicalLangType
import com.dallonf.ktcause.types.CanonicalLangTypeId
import com.dallonf.ktcause.types.ErrorLangType

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

    private val mode = Mode.ASSERT_SUPPORTED

    init {
        System.loadLibrary("rscause_jni")
    }

    fun shouldRunRustCompiler(
        path: String, ast: FileNode, analyzed: AnalyzedNode,
        otherFiles: Map<String, Resolver.ExternalFileDescriptor>
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
        ast: FileNode,
        analyzed: AnalyzedNode,
        path: String,
        otherFiles: Map<String, Resolver.ExternalFileDescriptor>
    ): Sequence<String> = sequence {
        val incompatibleNodes = getIncompatibleNodeTypes(ast)
        yieldAll(incompatibleNodes.map { "Incompatible node type: $it" })

        val identifiersReferencingTopLevelDeclarations = run {
            val identifiers = ast.allDescendants().mapNotNull { it as? IdentifierExpressionNode }
            identifiers.mapNotNull { identifierExpression ->
                val valueComesFrom =
                    analyzed.nodeTags[identifierExpression.info.breadcrumbs]?.firstNotNullOfOrNull { it as? NodeTag.ValueComesFrom }
                val sourceTags = valueComesFrom?.let { analyzed.nodeTags[valueComesFrom.source] } ?: emptyList()

                sourceTags.firstNotNullOfOrNull { it as? NodeTag.TopLevelDeclaration }?.let {
                    if (sourceTags.none { sourceTag -> sourceTag is NodeTag.ReferencesFile }) {
                        "Node at ${identifierExpression.info.breadcrumbs} references a (non-import) top-level declaration: $it"
                    } else {
                        null
                    }
                }
            }
        }
        yieldAll(identifiersReferencingTopLevelDeclarations)

        val functionDeclarationsWithParameters = run {
            val functions = ast.allDescendants().mapNotNull { it as? FunctionNode }
            functions.filter { it.params.isNotEmpty() }
        }
        yieldAll(functionDeclarationsWithParameters.map { "Declared function by the name of ${it.name.text} has parameters" })

        val unsupportedImports = run {
            val imports = ast.allDescendants().mapNotNull { it as? ImportNode }
            imports.filter { !listOf("core/builtin.cau", "core/math").contains(it.path.path) }
        }
        yieldAll(unsupportedImports.map { "Unsupported import: ${it.path.path}" })

        val unsupportedTypeAnnotations = ast.allDescendants().filter {
            when (it) {
                is FunctionNode -> it.returnType != null
//                is NamedValueNode -> it.typeAnnotation != null
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

    private val supportedCoreExports = setOf("Debug", "Action", "Text", "Number", "equals")

    fun compile(
        path: String,
        ast: FileNode,
        tags: Map<Breadcrumbs, List<NodeTag>>,
        externalFiles: Map<String, Resolver.ExternalFileDescriptor>
    ): RustCompilerResult {
        val filteredCanonicalTypes = run {
            val allEntries = externalFiles.flatMap { it.value.types.entries }
                // only supported core types for now
                .filter { supportedCoreExports.contains(it.key.name) }
            val asPairs = allEntries.map { it.toPair() }
            mapOf(*asPairs.toTypedArray())
        }
        val filteredExternalFiles = externalFiles.mapValues { (key, value) ->
            if (key == "core/builtin.cau") {
                val filteredExports = value.exports.mapValues { (exportKey, exportValue) ->
                    // only supported core exports for now
                    // all others are just Actions
                    if (supportedCoreExports.contains(exportKey)) {
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
        val filteredTags = tags.mapValues { (breadcrumbs, tags) ->
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
                    else -> false
                }
            }
        }

//        generateTestOutput("tmp", ast, filteredTags, filteredCanonicalTypes, filteredExternalFiles);
        return compileInner(path, ast, filteredTags, filteredCanonicalTypes, filteredExternalFiles)
    }

    private val otherUnsupportedNodeTypes = listOf(BranchExpressionNode::class).mapNotNull { it.simpleName }

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
        path: String,
        ast: FileNode,
        tags: Map<Breadcrumbs, List<NodeTag>>,
        canonicalTypes: Map<CanonicalLangTypeId, CanonicalLangType>,
        externalFiles: Map<String, Resolver.ExternalFileDescriptor>
    ): RustCompilerResult

    private external fun generateTestOutput(
        testName: String,
        ast: FileNode,
        tags: Map<Breadcrumbs, List<NodeTag>>,
        canonicalTypes: Map<CanonicalLangTypeId, CanonicalLangType>,
        externalFiles: Map<String, Resolver.ExternalFileDescriptor>
    )

    data class RustCompilerResult(
        val compiledFile: CompiledFile,
        val errors: List<Resolver.ResolverError>
    )
}