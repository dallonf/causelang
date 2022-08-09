package com.dallonf.ktcause

import com.dallonf.ktcause.ast.Breadcrumbs
import com.dallonf.ktcause.ast.FileNode

import com.dallonf.ktcause.ResolutionType.*
import com.dallonf.ktcause.ast.SourcePosition
import com.dallonf.ktcause.types.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

enum class ResolutionType {
    INFERRED, EXPECTED;
}

data class ResolutionKey(val type: ResolutionType, val breadcrumbs: Breadcrumbs)
data class ResolvedFile(
    val path: String,
    val resolvedTypes: Map<ResolutionKey, ValueLangType>,
    val canonicalTypes: Map<CanonicalLangTypeId, CanonicalLangType>,
) {
    fun checkForRuntimeErrors(breadcrumbs: Breadcrumbs): ErrorValueLangType? {
        val expected = resolvedTypes[ResolutionKey(EXPECTED, breadcrumbs)]?.getRuntimeError()
        return expected ?: resolvedTypes[ResolutionKey(INFERRED, breadcrumbs)]!!.getRuntimeError()
    }

    fun getExpectedType(breadcrumbs: Breadcrumbs): ValueLangType {
        return resolvedTypes[ResolutionKey(EXPECTED, breadcrumbs)] ?: getInferredType(breadcrumbs)
    }

    fun getInferredType(breadcrumbs: Breadcrumbs): ValueLangType = resolvedTypes[ResolutionKey(
        INFERRED, breadcrumbs
    )]!!
}

object Resolver {
    @Serializable
    data class ResolverError(val position: SourcePosition.Source, val error: ErrorValueLangType)

    internal fun List<ResolverError>.debug(): String {
        return Debug.debugSerializer.encodeToString(this)
    }


    data class ExternalFileDescriptor(val exports: Map<String, ValueLangType>)

    fun resolveForFile(
        path: String, fileNode: FileNode, analyzed: AnalyzedNode, otherFiles: Map<String, ExternalFileDescriptor>
    ): Pair<ResolvedFile, List<ResolverError>> {
        val allOtherFiles = otherFiles.toMutableMap().also {
            val core = CoreDescriptors.coreBuiltinFile;
            it[core.first] = core.second
            it.putAll(CoreDescriptors.coreFiles)
        }.toMap()

        val nodeTags = analyzed.nodeTags

        val resolvedTypes = mutableMapOf<ResolutionKey, ValueLangType>()
        val knownCanonicalTypes = mutableMapOf<CanonicalLangTypeId, CanonicalLangType>()

        // Seed the crawler with all expressions, top-level declarations,
        // and explicit type annotations
        run {
            for ((nodeBreadcrumbs, tags) in nodeTags) {
                fun track(resolutionType: ResolutionType) {
                    resolvedTypes[ResolutionKey(resolutionType, nodeBreadcrumbs)] = ValueLangType.Pending
                }
                for (tag in tags) {
                    when (tag) {
                        is NodeTag.Expression -> track(INFERRED)
                        is NodeTag.TypeAnnotated -> {
                            track(INFERRED)
                            track(EXPECTED)
                        }

                        is NodeTag.ParameterForCall -> {
                            track(INFERRED)
                            track(EXPECTED)
                        }

                        is NodeTag.TopLevelDeclaration -> {
                            track(INFERRED)
                        }

                        else -> {}
                    }
                }
            }
        }

        fun getSourcePosition(breadcrumbs: Breadcrumbs) = SourcePosition.Source(
            path, breadcrumbs, fileNode.findNode(breadcrumbs).info.position
        )



        while (true) {
            val iterationResolvedReferences = mutableListOf<Pair<ResolutionKey, ValueLangType>>()
            fun getResolvedTypeOf(breadcrumbs: Breadcrumbs): ValueLangType {
                val found = resolvedTypes[ResolutionKey(EXPECTED, breadcrumbs)] ?: resolvedTypes[ResolutionKey(
                    INFERRED, breadcrumbs
                )]

                return if (found != null) {
                    found
                } else {
                    iterationResolvedReferences.add(
                        ResolutionKey(
                            INFERRED, breadcrumbs
                        ) to ValueLangType.Pending
                    )
                    ValueLangType.Pending
                }
            }

            val pendingReferences = resolvedTypes.asSequence().mapNotNull { if (it.value.isPending()) it.key else null }
            for (pendingKey in pendingReferences) {
                fun resolveWith(langType: ValueLangType) {
                    iterationResolvedReferences.add(pendingKey to langType)
                }

                fun resolveWithProxyError(error: ErrorValueLangType, breadcrumbs: Breadcrumbs) {
                    val sourcePosition = getSourcePosition(breadcrumbs)
                    resolveWith(
                        if (error is ErrorValueLangType.ProxyError) {
                            ErrorValueLangType.ProxyError(
                                error.actualError, listOf(sourcePosition) + error.proxyChain
                            )
                        } else {
                            ErrorValueLangType.ProxyError(
                                error, listOf(
                                    sourcePosition
                                )
                            )
                        }
                    )
                }

                val pendingNodeTags = nodeTags[pendingKey.breadcrumbs]
                pendingNodeTags?.forEach eachTag@{ tag ->
                    when (pendingKey.type) {
                        INFERRED -> when (tag) {
                            is NodeTag.ValueComesFrom -> {
                                when (val sourceType = getResolvedTypeOf(tag.source)) {
                                    is ValueLangType.Pending -> {}
                                    is ResolvedValueLangType -> resolveWith(sourceType)
                                    is ErrorValueLangType -> resolveWithProxyError(sourceType, tag.source)
                                }
                            }

                            is NodeTag.Calls -> {
                                when (val calleeType = getResolvedTypeOf(tag.callee)) {
                                    is ValueLangType.Pending -> {}
                                    is FunctionValueLangType -> {
                                        resolveWith(calleeType.returnType)
                                    }

                                    is TypeReferenceValueLangType -> {
                                        when (calleeType.canonicalType) {
                                            is CanonicalLangType.SignalCanonicalLangType -> {
                                                val signal = calleeType.canonicalType
                                                val params = arrayOfNulls<ValueLangType>(signal.params.size)
                                                val parameterTags = nodeTags[pendingKey.breadcrumbs]?.mapNotNull {
                                                    (it as? NodeTag.CallsWithParameter)
                                                }
                                                parameterTags?.forEach { (parameterBreadcrumbs, index) ->
                                                    val argumentType = getResolvedTypeOf(parameterBreadcrumbs)
                                                    if (argumentType is ErrorValueLangType) {
                                                        resolveWithProxyError(argumentType, parameterBreadcrumbs)
                                                        return@eachTag
                                                    }
                                                    params[index] = argumentType
                                                }

                                                val foundParams = mutableListOf<ValueLangType>()
                                                val missingParams = mutableListOf<LangParameter>()
                                                for ((i, param) in params.withIndex()) {
                                                    if (param != null) {
                                                        foundParams.add(param)
                                                    } else {
                                                        missingParams.add(signal.params[i])
                                                    }
                                                }

                                                if (missingParams.isNotEmpty()) {
                                                    resolveWith(ErrorValueLangType.MissingParameters(missingParams.map { it.name }))
                                                } else if (foundParams.all { !it.isPending() }) {
                                                    resolveWith(InstanceValueLangType(TypeReferenceValueLangType(signal)))
                                                }
                                            }
                                        }
                                    }

                                    is ResolvedValueLangType -> resolveWith(ErrorValueLangType.NotCallable)
                                    is ErrorValueLangType -> resolveWithProxyError(calleeType, tag.callee)
                                }
                            }

                            is NodeTag.Causes -> when (val signalType = getResolvedTypeOf(tag.signal)) {
                                is ValueLangType.Pending -> {}
                                is InstanceValueLangType -> {
                                    when (val signalCanonicalType = signalType.type.canonicalType) {
                                        is CanonicalLangType.SignalCanonicalLangType -> {
                                            resolveWith(signalCanonicalType.result)
                                        }
                                    }
                                }

                                is ResolvedValueLangType -> resolveWith(ErrorValueLangType.NotCausable)
                                is ErrorValueLangType -> resolveWithProxyError(signalType, tag.signal)
                            }

                            is NodeTag.NamedValue -> {
                                val (name, valueBreadcrumbs, typeDeclaration) = tag
                                val expectedValueType = resolvedTypes[ResolutionKey(EXPECTED, pendingKey.breadcrumbs)]
                                if (expectedValueType != null) {
                                    resolveWith(expectedValueType)
                                } else {
                                    when (val inferredType = getResolvedTypeOf(valueBreadcrumbs)) {
                                        is ValueLangType.Pending -> {}
                                        is ResolvedValueLangType -> resolveWith(inferredType)
                                        is ErrorValueLangType -> resolveWithProxyError(inferredType, valueBreadcrumbs)
                                    }
                                }
                            }

                            is NodeTag.IsPrimitiveValue -> resolveWith(tag.kind.toValueLangType())

                            is NodeTag.IsFunction -> {
                                val canReturn = pendingNodeTags.mapNotNull {
                                    if (it is NodeTag.FunctionCanReturnTypeOf) it
                                    else null
                                }

                                val returnType = when (canReturn.size) {
                                    0 -> throw AssertionError("Every function should be able to return something")
                                    1 -> getResolvedTypeOf(canReturn[0].returnExpression)
                                    else -> ErrorValueLangType.ImplementationTodo("Can't infer a function that can return from multiple locations")
                                }

                                resolveWith(
                                    FunctionValueLangType(
                                        name = tag.name, returnType = returnType,
                                        // TODO
                                        params = emptyList()
                                    )
                                )
                            }

                            is NodeTag.ReferencesFile -> {
                                val exportName = tag.exportName
                                if (exportName != null) {
                                    val file = allOtherFiles[tag.path]
                                    if (file != null) {
                                        val export = file.exports[exportName]
                                        if (export != null) {
                                            resolveWith(export)
                                        } else {
                                            resolveWith(ErrorValueLangType.ExportNotFound)
                                        }
                                    } else {
                                        resolveWith(ErrorValueLangType.FileNotFound)
                                    }
                                } else {
                                    // TODO: I guess eventually we'll probably want to import whole files,
                                    // but we don't really have a "file reference" type yet so meh
                                    resolveWith(ErrorValueLangType.ImplementationTodo("Can't import a whole file"))
                                }
                            }

                            is NodeTag.ReferenceNotInScope -> resolveWith(ErrorValueLangType.NotInScope)

                            is NodeTag.IsBranch -> run branch@{
                                val branchOptions = pendingNodeTags.mapNotNull { it as? NodeTag.HasBranchOption }

                                if (branchOptions.isEmpty()) {
                                    resolveWith(PrimitiveValueLangType(LangPrimitiveKind.ACTION))
                                    return@branch
                                }

                                val elseBranches =
                                    branchOptions.mapNotNull { if (it.type == NodeTag.BranchOptionType.ELSE) it else null }

                                if (elseBranches.size > 1) {
                                    resolveWith(ErrorValueLangType.TooManyElseBranches)
                                    return@branch
                                }

                                if (elseBranches.isEmpty()) {
                                    resolveWith(ErrorValueLangType.MissingElseBranch)
                                    return@branch
                                }

                                val branchValueTypes =
                                    branchOptions.map { Pair(it.branchOption, getResolvedTypeOf(it.branchOption)) }

                                if (branchValueTypes.all { !it.second.isPending() }) {
                                    val nonErrorValueTypes =
                                        branchValueTypes.filter { it.second.getError() == null }
                                    if (nonErrorValueTypes.isEmpty()) {
                                        resolveWith(branchValueTypes[0].second)
                                        return@branch
                                    }

                                    val firstNonErrorValueType = nonErrorValueTypes[0].second
                                    if (nonErrorValueTypes.all { it.second.getError() != null || it.second == firstNonErrorValueType }) {
                                        resolveWith(firstNonErrorValueType)
                                    } else {
                                        resolveWith(ErrorValueLangType.IncompatibleTypes(branchValueTypes.map {
                                            ErrorValueLangType.IncompatibleTypes.IncompatibleType(
                                                it.second, getSourcePosition(it.first)
                                            )
                                        }))
                                    }
                                }
                            }

                            else -> {}
                        }

                        EXPECTED -> when (tag) {
                            is NodeTag.TypeAnnotated -> {
                                when (val foundType = getResolvedTypeOf(tag.annotation)) {
                                    is ValueLangType.Pending -> {}
                                    is ResolvedValueLangType -> {
                                        foundType.getInstanceType().let { expectedType ->
                                            if (expectedType != null) resolveWith(expectedType)
                                            else resolveWith(ErrorValueLangType.NotATypeReference(foundType))
                                        }
                                    }

                                    is ErrorValueLangType -> resolveWithProxyError(foundType, tag.annotation)
                                }
                            }

                            is NodeTag.ParameterForCall -> {
                                val (call, index) = tag
                                val callTag = nodeTags[call]!!.asSequence().mapNotNull { it as? NodeTag.Calls }.first()
                                when (val callType = getResolvedTypeOf(callTag.callee)) {
                                    is ValueLangType.Pending -> {}
                                    is ResolvedValueLangType -> {
                                        val params = when (callType) {
                                            is FunctionValueLangType -> callType.params
                                            is TypeReferenceValueLangType -> when (val canonicalType =
                                                callType.canonicalType) {
                                                is CanonicalLangType.SignalCanonicalLangType -> canonicalType.params
                                            }

                                            else -> throw AssertionError("Call expression not callable (should have been caught by call resolution)")
                                        }

                                        if (index >= params.size) {
                                            resolveWith(ErrorValueLangType.ExcessParameter(expected = params.size))
                                        } else {
                                            val param = params[index]
                                            resolveWith(param.valueType)
                                        }
                                    }

                                    is ErrorValueLangType -> resolveWithProxyError(callType, callTag.callee)
                                }
                            }

                            else -> {}
                        }
                    }
                }
            }

            val resolved = iterationResolvedReferences.size
            for ((key, newType) in iterationResolvedReferences) {
                val oldResolvedType = resolvedTypes[key]?.let {
                    if (it.isPending()) null else it
                }

                if (oldResolvedType != null) {
                    throw AssertionError("Accidentally clobbered a resolved reference (${key} = ${oldResolvedType}) with $newType")
                }

                if (newType is CanonicalLangType) {
                    knownCanonicalTypes[newType.id] = newType
                    resolvedTypes[key] = TypeReferenceValueLangType(newType)
                } else {
                    resolvedTypes[key] = newType
                }
            }
            if (resolved == 0) {
                break
            }

        }
        run { // compare expected to inferred types
            val expectedTypes = resolvedTypes.filterKeys { it.type == EXPECTED }
            for ((expectedKey, expectedType) in expectedTypes) {
                val (_, breadcrumbsOfExpectedType) = expectedKey
                val expectedResolvedType = expectedType as? ResolvedValueLangType
                val actualType =
                    resolvedTypes[ResolutionKey(INFERRED, breadcrumbsOfExpectedType)] as? ResolvedValueLangType

                if (expectedResolvedType != null && actualType != null && expectedResolvedType != actualType) {
                    resolvedTypes[ResolutionKey(INFERRED, breadcrumbsOfExpectedType)] =
                        ErrorValueLangType.MismatchedType(
                            expected = expectedResolvedType, actual = actualType
                        )
                }
            }
        }

        val file = ResolvedFile(
            path, resolvedTypes, knownCanonicalTypes
        )
        val errors = resolvedTypes.mapNotNull { (key, resolvedType) ->
            val breadcrumbs = key.breadcrumbs
            val source by lazy {
                SourcePosition.Source(
                    path, breadcrumbs, fileNode.findNode(breadcrumbs).info.position
                )
            }

            when (resolvedType) {
                is ResolvedValueLangType -> null
                is ErrorValueLangType.ProxyError -> null
                is ErrorValueLangType -> ResolverError(
                    source,
                    error = resolvedType,
                )

                is ValueLangType.Pending -> ResolverError(
                    source,
                    error = ErrorValueLangType.NeverResolved,
                )
            }
        }

        return Pair(file, errors)
    }
}