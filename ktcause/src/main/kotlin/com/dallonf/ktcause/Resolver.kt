package com.dallonf.ktcause

import com.dallonf.ktcause.ast.Breadcrumbs
import com.dallonf.ktcause.ast.FileNode

import com.dallonf.ktcause.ResolutionType.*
import com.dallonf.ktcause.ast.SourcePosition
import com.dallonf.ktcause.types.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

// TODO: It might not make sense to keep these in the same map, since
// they're also different types
enum class ResolutionType {
    INFERRED, CONSTRAINT;
}

data class ResolutionKey(val type: ResolutionType, val breadcrumbs: Breadcrumbs)
data class ResolvedFile(
    val path: String,
    val resolvedTypes: Map<ResolutionKey, LangType>,
    val canonicalTypes: Map<CanonicalLangTypeId, CanonicalLangType>,
    private val _debugContext: Debug.DebugContext? = null
) {

    fun debugContext() = _debugContext?.copy(resolved = this)

    fun checkForRuntimeErrors(breadcrumbs: Breadcrumbs): ErrorLangType? {
        val expected = resolvedTypes[ResolutionKey(CONSTRAINT, breadcrumbs)]?.getRuntimeError()
        if (expected != null) return expected

        val inferred = resolvedTypes[ResolutionKey(INFERRED, breadcrumbs)]

        if (inferred != null) return inferred.getRuntimeError()

        // Something's gone wrong, print debugging information

        val debug = debugContext()?.getNodeContext(breadcrumbs)
        error("Couldn't find any type for $breadcrumbs" + (debug ?: ""))
    }

    fun getExpectedType(breadcrumbs: Breadcrumbs): LangType {
        val foundConstraint = resolvedTypes[ResolutionKey(CONSTRAINT, breadcrumbs)]
        // TODO: if the inferred type is more specific than the constraint, we'll
        // want to return that
        return if (foundConstraint is ResolvedConstraintLangType) {
            foundConstraint.toInstanceType()
        } else if (foundConstraint != null) {
            foundConstraint
        } else {
            getInferredType(breadcrumbs)
        }
    }

    fun getInferredType(breadcrumbs: Breadcrumbs): LangType = resolvedTypes[ResolutionKey(
        INFERRED, breadcrumbs
    )]!!
}

object Resolver {
    @Serializable
    data class ResolverError(val position: SourcePosition.Source, val error: ErrorLangType) {
        internal fun debug(): String {
            return Debug.debugSerializer.encodeToString(this)
        }
    }

    internal fun List<ResolverError>.debug(): String {
        return Debug.debugSerializer.encodeToString(this)
    }


    data class ExternalFileDescriptor(
        val exports: Map<String, LangType>, val types: Map<CanonicalLangTypeId, CanonicalLangType>
    )

    fun resolveForFile(
        path: String,
        fileNode: FileNode,
        analyzed: AnalyzedNode,
        otherFiles: Map<String, ExternalFileDescriptor>,
        debugContext: Debug.DebugContext? = null,
    ): Pair<ResolvedFile, List<ResolverError>> {
        val allOtherFiles = otherFiles.toMutableMap().also {
            val core = CoreDescriptors.coreBuiltinFile
            it[core.first] = core.second
            it.putAll(CoreDescriptors.coreFiles)
        }.toMap()

        val nodeTags = analyzed.nodeTags

        val resolvedTypes = mutableMapOf<ResolutionKey, LangType>()
        val knownCanonicalTypes = mutableMapOf<CanonicalLangTypeId, CanonicalLangType>()

        // Seed the crawler with everything the compiler will want to know about
        run {
            for ((nodeBreadcrumbs, tags) in nodeTags) {
                fun track(resolutionType: ResolutionType) {
                    resolvedTypes[ResolutionKey(resolutionType, nodeBreadcrumbs)] = LangType.Pending
                }
                for (tag in tags) {
                    when (tag) {
                        is NodeTag.TopLevelDeclaration -> track(INFERRED)
                        is NodeTag.IsExpression -> track(INFERRED)
                        is NodeTag.IsNamedValue -> track(INFERRED)

                        is NodeTag.ConditionFor -> track(CONSTRAINT)

                        is NodeTag.TypeAnnotated -> {
                            track(INFERRED)
                            track(CONSTRAINT)
                        }

                        is NodeTag.ParameterForCall -> {
                            track(INFERRED)
                            track(CONSTRAINT)
                        }

                        // expect Action results for non-value statements
                        // this keeps things compiling even if individual statements have errors
                        is NodeTag.IsEffectStatement -> {
                            track(INFERRED)
                            resolvedTypes[ResolutionKey(CONSTRAINT, nodeBreadcrumbs)] =
                                LangPrimitiveKind.ACTION.toConstraintLangType()
                        }

                        is NodeTag.IsSetStatement -> {
                            track(INFERRED)
                            resolvedTypes[ResolutionKey(CONSTRAINT, nodeBreadcrumbs)] =
                                LangPrimitiveKind.ACTION.toConstraintLangType()
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
            val iterationResolvedReferences = mutableListOf<Pair<ResolutionKey, LangType>>()
            fun getResolvedTypeOf(breadcrumbs: Breadcrumbs): LangType {
                val foundConstraint = resolvedTypes[ResolutionKey(CONSTRAINT, breadcrumbs)]

                if (foundConstraint != null) {
                    return if (foundConstraint is ResolvedConstraintLangType) {
                        foundConstraint.toInstanceType()
                    } else {
                        foundConstraint
                    }
                }

                val foundValue = resolvedTypes[ResolutionKey(
                    INFERRED, breadcrumbs
                )]

                if (foundValue != null) {
                    return foundValue
                }

                iterationResolvedReferences.add(
                    ResolutionKey(
                        INFERRED, breadcrumbs
                    ) to LangType.Pending
                )
                return LangType.Pending
            }

            val pendingReferences = resolvedTypes.asSequence().mapNotNull { if (it.value.isPending()) it.key else null }
            for (pendingKey in pendingReferences) {
                fun resolveWith(langType: LangType) {
                    iterationResolvedReferences.add(pendingKey to langType)
                }

                fun resolveWithProxyError(error: ErrorLangType, breadcrumbs: Breadcrumbs) {
                    val sourcePosition = getSourcePosition(breadcrumbs)
                    resolveWith(
                        ErrorLangType.ProxyError.from(error, sourcePosition)
                    )
                }

                val pendingNodeTags = nodeTags[pendingKey.breadcrumbs]
                pendingNodeTags?.forEach eachTag@{ tag ->
                    when (pendingKey.type) {
                        INFERRED -> when (tag) {
                            is NodeTag.ValueComesFrom -> {
                                val usesCapturedValue =
                                    pendingNodeTags.firstNotNullOfOrNull { it as? NodeTag.UsesCapturedValue }
                                if (usesCapturedValue != null) {
                                    val sourceTags = nodeTags[tag.source] ?: emptyList()
                                    if (sourceTags.any { it is NodeTag.IsVariable }) {
                                        resolveWith(ErrorLangType.OuterVariable)
                                        return@eachTag
                                    }
                                }

                                when (val sourceType = getResolvedTypeOf(tag.source)) {
                                    is LangType.Pending -> {}
                                    is ResolvedValueLangType -> resolveWith(sourceType)
                                    is ResolvedConstraintLangType -> resolveWith((sourceType))
                                    is ErrorLangType -> resolveWithProxyError(sourceType, tag.source)
                                }
                            }

                            is NodeTag.Calls -> {
                                when (val calleeType = getResolvedTypeOf(tag.callee)) {
                                    is LangType.Pending -> {}
                                    is FunctionValueLangType -> {
                                        when (calleeType.returnConstraint) {
                                            is LangType.Pending -> {}
                                            is ErrorLangType -> resolveWithProxyError(
                                                calleeType.returnConstraint, tag.callee
                                            )

                                            is ResolvedConstraintLangType -> resolveWith(calleeType.returnConstraint.toInstanceType())
                                        }
                                    }

                                    is TypeReferenceConstraintLangType -> {
                                        val expectedFields = when (calleeType.canonicalType) {
                                            is CanonicalLangType.SignalCanonicalLangType -> calleeType.canonicalType.fields
                                            is CanonicalLangType.ObjectCanonicalLangType -> calleeType.canonicalType.fields
                                        }

                                        val params = arrayOfNulls<LangType>(expectedFields.size)
                                        val parameterTags = nodeTags[pendingKey.breadcrumbs]?.mapNotNull {
                                            (it as? NodeTag.CallsWithParameter)
                                        }
                                        parameterTags?.forEach { (parameterBreadcrumbs, paramIndex) ->
                                            val argumentType = getResolvedTypeOf(parameterBreadcrumbs)
                                            if (argumentType is ErrorLangType) {
                                                resolveWithProxyError(argumentType, parameterBreadcrumbs)
                                                return@eachTag
                                            }
                                            params[paramIndex] = argumentType
                                        }

                                        val foundParams = mutableListOf<LangType>()
                                        val missingParams = mutableListOf<LangParameter>()
                                        for ((i, param) in params.withIndex()) {
                                            if (param != null) {
                                                foundParams.add(param)
                                            } else {
                                                missingParams.add(expectedFields[i].asLangParameter())
                                            }
                                        }

                                        if (missingParams.isNotEmpty()) {
                                            resolveWith(ErrorLangType.MissingParameters(missingParams.map { it.name }))
                                        } else if (foundParams.all { !it.isPending() }) {
                                            resolveWith(
                                                InstanceValueLangType(calleeType.canonicalType)
                                            )
                                        }
                                    }

                                    is UniqueObjectLangType -> resolveWith(calleeType)

                                    is ResolvedValueLangType -> resolveWith(ErrorLangType.NotCallable)
                                    is ResolvedConstraintLangType -> resolveWith(ErrorLangType.NotCallable)
                                    is ErrorLangType -> resolveWithProxyError(calleeType, tag.callee)
                                }
                            }

                            is NodeTag.Causes -> {
                                val signalType = when (val signalType = getResolvedTypeOf(tag.signal)) {
                                    is LangType.Pending -> return@eachTag
                                    is InstanceValueLangType -> signalType.canonicalType

                                    is ResolvedValueLangType -> {
                                        resolveWith(ErrorLangType.NotCausable)
                                        return@eachTag
                                    }

                                    is ResolvedConstraintLangType -> {
                                        resolveWith(ErrorLangType.NotCausable)
                                        return@eachTag
                                    }

                                    is ErrorLangType -> {
                                        resolveWithProxyError(signalType, tag.signal)
                                        return@eachTag
                                    }
                                }

                                when (signalType) {
                                    is CanonicalLangType.SignalCanonicalLangType -> {
                                        when (signalType.result) {
                                            is LangType.Pending -> {}
                                            is ErrorLangType -> resolveWithProxyError(
                                                signalType.result, tag.signal
                                            )

                                            is ResolvedConstraintLangType -> resolveWith(signalType.result.toInstanceType())
                                        }
                                    }

                                    else -> resolveWith(ErrorLangType.NotCausable)
                                }
                            }

                            is NodeTag.IsDeclarationStatement -> {
                                if (!getResolvedTypeOf(tag.declaration).isPending()) {
                                    resolveWith(LangPrimitiveKind.ACTION.toValueLangType())
                                }
                            }

                            is NodeTag.IsObjectType -> {
                                val fieldTags = pendingNodeTags.mapNotNull { it as? NodeTag.TypeHasField }
                                val id = CanonicalLangTypeId(path, null, tag.name, 0u)
                                val fields = fieldTags.map { field ->
                                    val fieldType = when (val resolvedFieldType =
                                        getResolvedTypeOf(field.typeReference).asConstraint()) {
                                        is LangType.Pending -> LangType.Pending
                                        is ErrorLangType -> ErrorLangType.ProxyError.from(
                                            resolvedFieldType, getSourcePosition(field.typeReference)
                                        )

                                        is ResolvedConstraintLangType -> resolvedFieldType
                                    }
                                    CanonicalLangType.ObjectField(field.name, fieldType)
                                }
                                val objectType = CanonicalLangType.ObjectCanonicalLangType(id, tag.name, fields)

                                val existingKnownType = knownCanonicalTypes[id]
                                if (existingKnownType != null && !existingKnownType.isPending()) {
                                    error("Accidentally clobbered canonical type: $existingKnownType with $objectType.")
                                }

                                knownCanonicalTypes[id] = objectType
                                if (objectType.fields.isEmpty()) {
                                    resolveWith(UniqueObjectLangType(objectType))
                                } else {
                                    resolveWith(TypeReferenceConstraintLangType(objectType))
                                }
                            }

                            is NodeTag.IsSignalType -> {
                                val fieldTags = pendingNodeTags.mapNotNull { it as? NodeTag.TypeHasField }
                                val id = CanonicalLangTypeId(path, null, tag.name, 0u)
                                val fields = fieldTags.map { field ->
                                    val fieldType = when (val resolvedFieldType =
                                        getResolvedTypeOf(field.typeReference).asConstraint()) {
                                        is LangType.Pending -> LangType.Pending
                                        is ErrorLangType -> ErrorLangType.ProxyError.from(
                                            resolvedFieldType, getSourcePosition(field.typeReference)
                                        )

                                        is ResolvedConstraintLangType -> resolvedFieldType
                                    }
                                    CanonicalLangType.ObjectField(field.name, fieldType)
                                }
                                val result = when (val resolvedResultType =
                                    getResolvedTypeOf(tag.resultTypeReference).asConstraint()) {
                                    is LangType.Pending -> LangType.Pending
                                    is ErrorLangType -> ErrorLangType.ProxyError.from(
                                        resolvedResultType, getSourcePosition(tag.resultTypeReference)
                                    )

                                    is ResolvedConstraintLangType -> resolvedResultType
                                }
                                val signalType = CanonicalLangType.SignalCanonicalLangType(id, tag.name, fields, result)

                                val existingKnownType = knownCanonicalTypes[id]
                                if (existingKnownType != null && !existingKnownType.isPending()) {
                                    error("Accidentally clobbered canonical type: $existingKnownType with $signalType.")
                                }

                                knownCanonicalTypes[id] = signalType
                                resolveWith(TypeReferenceConstraintLangType(signalType))
                            }

                            is NodeTag.IsOptionType -> {
                                val options: List<ConstraintLangType> =
                                    pendingNodeTags.asSequence().mapNotNull { it as? NodeTag.OptionTypeHasOption }
                                        .map(fun(it): ConstraintLangType = when (val resolvedType =
                                            getResolvedTypeOf(it.typeReference).asConstraint()) {
                                            is LangType.Pending -> LangType.Pending
                                            is ErrorLangType -> ErrorLangType.ProxyError.from(
                                                resolvedType, getSourcePosition(it.typeReference)
                                            )

                                            is ResolvedConstraintLangType -> resolvedType
                                        }).toList()

                                resolveWith(OptionConstraintLangType(options))
                            }

                            is NodeTag.IsPrimitiveValue -> resolveWith(tag.kind.toValueLangType())

                            is NodeTag.IsFunction -> {
                                val canReturn = pendingNodeTags.mapNotNull {
                                    it as? NodeTag.FunctionCanReturnTypeOf
                                }

                                val returnType = when (canReturn.size) {
                                    0 -> throw AssertionError("Every function should be able to return something")
                                    1 -> getResolvedTypeOf(canReturn[0].returnExpression)
                                    else -> ErrorLangType.ImplementationTodo("Can't infer a function that can return from multiple locations")
                                }

                                val returnConstraint = when (returnType) {
                                    is LangType.Pending -> LangType.Pending
                                    is ErrorLangType -> ErrorLangType.ProxyError.from(
                                        returnType, getSourcePosition(canReturn[0].returnExpression)
                                    )

                                    is ResolvedValueLangType -> returnType.toConstraint()
                                    is ResolvedConstraintLangType -> ErrorLangType.ConstraintUsedAsValue(returnType)
                                }

                                val paramTags = pendingNodeTags.mapNotNull { it as? NodeTag.FunctionHasParam }
                                val params = paramTags.map { paramTag ->
                                    val typeReference =
                                        paramTag.typeReference?.let { getResolvedTypeOf(it) } ?: LangType.Pending
                                    LangParameter(paramTag.name, typeReference.asConstraint())
                                }

                                resolveWith(
                                    FunctionValueLangType(
                                        tag.name, returnConstraint, params
                                    )
                                )
                            }

                            is NodeTag.ParamForFunction -> {
                                if (tag.typeReference != null) {
                                    when (val typeConstraint = getResolvedTypeOf(tag.typeReference).asConstraint()) {
                                        is LangType.Pending -> resolveWith(LangType.Pending)
                                        is ErrorLangType -> resolveWithProxyError(typeConstraint, tag.typeReference)
                                        is ResolvedConstraintLangType -> resolveWith(typeConstraint.toInstanceType())
                                    }
                                }
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
                                            resolveWith(ErrorLangType.ExportNotFound)
                                        }
                                    } else {
                                        resolveWith(ErrorLangType.FileNotFound)
                                    }
                                } else {
                                    // TODO: I guess eventually we'll probably want to import whole files,
                                    // but we don't really have a "file reference" type yet so meh
                                    resolveWith(ErrorLangType.ImplementationTodo("Can't import a whole file"))
                                }
                            }

                            is NodeTag.ReferenceNotInScope -> resolveWith(ErrorLangType.NotInScope)

                            is NodeTag.IsBranch -> run branch@{
                                val branchOptions = pendingNodeTags.mapNotNull { it as? NodeTag.HasBranchOption }

                                if (branchOptions.isEmpty()) {
                                    resolveWith(PrimitiveValueLangType(LangPrimitiveKind.ACTION))
                                    return@branch
                                }

                                val elseBranches =
                                    branchOptions.mapNotNull { if (it.type == NodeTag.BranchOptionType.ELSE) it else null }

                                if (elseBranches.size > 1) {
                                    resolveWith(ErrorLangType.TooManyElseBranches)
                                    return@branch
                                }

                                if (elseBranches.isEmpty()) {
                                    resolveWith(ErrorLangType.MissingElseBranch)
                                    return@branch
                                }

                                val branchValueTypes =
                                    branchOptions.map { Pair(it.branchOption, getResolvedTypeOf(it.branchOption)) }

                                if (branchValueTypes.all { !it.second.isPending() }) {
                                    val nonErrorValueTypes = branchValueTypes.filter { it.second.getError() == null }
                                    if (nonErrorValueTypes.isEmpty()) {
                                        resolveWith(branchValueTypes[0].second)
                                        return@branch
                                    }

                                    val firstNonErrorValueType = nonErrorValueTypes[0].second
                                    if (nonErrorValueTypes.all { it.second.getError() != null || it.second == firstNonErrorValueType }) {
                                        resolveWith(firstNonErrorValueType)
                                    } else {
                                        resolveWith(ErrorLangType.IncompatibleTypes(branchValueTypes.map {
                                            ErrorLangType.IncompatibleTypes.IncompatibleType(
                                                it.second, getSourcePosition(it.first)
                                            )
                                        }))
                                    }
                                }
                            }

                            is NodeTag.GetsMember -> {
                                val obj = getResolvedTypeOf(tag.objectExpression)
                                when (obj) {
                                    is LangType.Pending -> {}
                                    is ResolvedConstraintLangType -> resolveWith(ErrorLangType.ImplementationTodo("Can't get members of a type"))

                                    // TODO: it's kinda weird that the resolution of
                                    // which field is being referenced isn't passed to the compiler
                                    is InstanceValueLangType -> {
                                        val fields = when (val type = obj.canonicalType) {
                                            is CanonicalLangType.ObjectCanonicalLangType -> type.fields
                                            is CanonicalLangType.SignalCanonicalLangType -> type.fields
                                        }
                                        val referencedField = fields.firstOrNull { it.name == tag.memberName }

                                        if (referencedField != null) {
                                            when (val constraint = referencedField.valueConstraint) {
                                                is LangType.Pending -> {}
                                                is ResolvedConstraintLangType -> resolveWith(constraint.toInstanceType())
                                                is ErrorLangType -> resolveWithProxyError(
                                                    constraint, tag.objectExpression
                                                )
                                            }
                                        } else {
                                            resolveWith(ErrorLangType.DoesNotHaveMember)
                                        }
                                    }

                                    is ResolvedValueLangType -> {
                                        resolveWith(ErrorLangType.DoesNotHaveAnyMembers)
                                    }


                                    is ErrorLangType -> resolveWithProxyError(obj, tag.objectExpression)
                                }
                            }

                            is NodeTag.IsEffectStatement -> {

                                val resultType = run result@{
                                    val conditionType = getResolvedTypeOf(tag.condition).asValue().let {
                                        if (it is InstanceValueLangType && it.canonicalType is CanonicalLangType.SignalCanonicalLangType) {
                                            it.canonicalType
                                        } else {
                                            when (it) {
                                                is AnySignalValueLangType -> {
                                                    return@result AnythingConstraintLangType
                                                }

                                                is LangType.Pending -> return@eachTag
                                                is ErrorLangType -> {
                                                    resolveWithProxyError(it, tag.condition)
                                                    return@eachTag
                                                }

                                                is ResolvedValueLangType -> {
                                                    resolveWith(ErrorLangType.NotCausable)
                                                    return@eachTag
                                                }
                                            }
                                        }
                                    }

                                    val resultType = when (val result = conditionType.result) {
                                        is LangType.Pending -> return@eachTag
                                        is ErrorLangType -> {
                                            resolveWithProxyError(result, tag.condition)
                                            return@eachTag
                                        }

                                        is ResolvedConstraintLangType -> result
                                    }
                                    resultType
                                }

                                val bodyResult = getResolvedTypeOf(tag.effectBody).asValue().let {
                                    when (it) {
                                        is LangType.Pending -> return@eachTag
                                        is ErrorLangType -> {
                                            resolveWithProxyError(it, tag.condition)
                                            return@eachTag
                                        }

                                        is ResolvedValueLangType -> it
                                    }
                                }

                                if (bodyResult.isAssignableTo(resultType)) {
                                    resolveWith(LangPrimitiveKind.ACTION.toValueLangType())
                                } else {
                                    resolveWith(
                                        ErrorLangType.MismatchedType(
                                            expected = resultType, actual = bodyResult
                                        )
                                    )
                                }
                            }

                            is NodeTag.IsPattern -> {
                                getResolvedTypeOf(tag.typeReference).asConstraint().let { type ->
                                    when (type) {
                                        is LangType.Pending -> return@eachTag
                                        is ErrorLangType -> {
                                            resolveWithProxyError(type, tag.typeReference)
                                            return@eachTag
                                        }

                                        is ResolvedConstraintLangType -> {
                                            resolveWith(type.toInstanceType())
                                        }
                                    }
                                }
                            }

                            is NodeTag.IsSetStatement -> {
                                val variable = tag.sets

                                val variableTag =
                                    nodeTags[variable]?.firstNotNullOfOrNull { it -> it as? NodeTag.IsVariable }
                                if (variableTag == null) {
                                    resolveWith(ErrorLangType.NotVariable)
                                    return@eachTag
                                }

                                if (pendingNodeTags.any { it is NodeTag.UsesCapturedValue }) {
                                    resolveWith(ErrorLangType.OuterVariable)
                                    return@eachTag
                                }

                                val expectedType = when (val variableValue = getResolvedTypeOf(variable).asValue()) {
                                    is LangType.Pending -> return@eachTag
                                    is ErrorLangType -> {
                                        resolveWithProxyError(variableValue, variable)
                                        return@eachTag
                                    }

                                    is ResolvedValueLangType -> variableValue.toConstraint()
                                }

                                val newValue = when (val newValue = getResolvedTypeOf(tag.setTo).asValue()) {
                                    is LangType.Pending -> return@eachTag
                                    is ErrorLangType -> {
                                        resolveWithProxyError(newValue, tag.setTo)
                                        return@eachTag
                                    }

                                    is ResolvedValueLangType -> newValue
                                }

                                if (newValue.isAssignableTo(expectedType)) {
                                    resolveWith(LangPrimitiveKind.ACTION.toValueLangType())
                                } else {
                                    resolveWith(ErrorLangType.MismatchedType(expectedType, newValue))
                                }
                            }

                            else -> {}
                        }

                        CONSTRAINT -> when (tag) {
                            is NodeTag.TypeAnnotated -> {
                                when (val foundType = getResolvedTypeOf(tag.annotation)) {
                                    is LangType.Pending -> {}
                                    is ResolvedConstraintLangType -> resolveWith(foundType)


                                    is ResolvedValueLangType -> resolveWith(
                                        ErrorLangType.ValueUsedAsConstraint(
                                            foundType
                                        )
                                    )

                                    is ErrorLangType -> resolveWithProxyError(foundType, tag.annotation)
                                }
                            }

                            is NodeTag.ParameterForCall -> {
                                val (call, index) = tag
                                val callTag = nodeTags[call]!!.asSequence().mapNotNull { it as? NodeTag.Calls }.first()
                                when (val callType = getResolvedTypeOf(callTag.callee)) {
                                    is LangType.Pending -> {}
                                    is ResolvedValueLangType, is ResolvedConstraintLangType -> {
                                        val params = when (callType) {
                                            is FunctionValueLangType -> callType.params
                                            is TypeReferenceConstraintLangType -> when (val canonicalType =
                                                callType.canonicalType) {
                                                is CanonicalLangType.SignalCanonicalLangType -> canonicalType.fields
                                                is CanonicalLangType.ObjectCanonicalLangType -> canonicalType.fields
                                            }.map { it.asLangParameter() }

                                            else -> throw AssertionError("Call expression not callable (should have been caught by call resolution)")
                                        }

                                        if (index >= params.size) {
                                            resolveWith(ErrorLangType.ExcessParameter(expected = params.size))
                                        } else {
                                            val param = params[index]
                                            resolveWith(param.valueConstraint)
                                        }
                                    }

                                    is ErrorLangType -> resolveWithProxyError(callType, callTag.callee)
                                }
                            }

                            is NodeTag.ConditionFor -> resolveWith(LangPrimitiveKind.BOOLEAN.toConstraintLangType())

                            else -> {}
                        }
                    }
                }
            }

            val changedResolutions = iterationResolvedReferences.filter { (key, value) -> resolvedTypes[key] != value }
            val resolved = changedResolutions.size
            for ((key, newType) in changedResolutions) {
                val oldResolvedType = resolvedTypes[key]?.let {
                    if (it.isPending()) null else it
                }

                if (oldResolvedType != null) {
                    throw AssertionError("Accidentally clobbered a resolved reference (${key} = ${oldResolvedType}) with $newType")
                }

                resolvedTypes[key] = newType
            }
            if (resolved == 0) {
                break
            }

        }
        run { // compare constraints to inferred types
            val constraints = resolvedTypes.filterKeys { it.type == CONSTRAINT }
            for ((constrainedKey, constraint) in constraints) {
                val (_, breadcrumbsOfConstraint) = constrainedKey
                val constraintType = constraint as? ResolvedConstraintLangType
                val actualType =
                    resolvedTypes[ResolutionKey(INFERRED, breadcrumbsOfConstraint)] as? ResolvedValueLangType

                if (constraintType != null && actualType != null && !actualType.isAssignableTo(constraintType)) {
                    resolvedTypes[ResolutionKey(INFERRED, breadcrumbsOfConstraint)] = ErrorLangType.MismatchedType(
                        expected = constraintType, actual = actualType
                    )
                }
            }
        }

        val file = ResolvedFile(
            path, resolvedTypes, knownCanonicalTypes, (debugContext ?: Debug.DebugContext()).copy(
                ast = fileNode,
                analyzed = analyzed,
            )
        )
        val errors = resolvedTypes.mapNotNull { (key, resolvedType) ->
            val breadcrumbs = key.breadcrumbs
            val source by lazy {
                SourcePosition.Source(
                    path, breadcrumbs, fileNode.findNode(breadcrumbs).info.position
                )
            }

            when (val foundError = resolvedType.getError() ?: resolvedType) {
                is ResolvedValueLangType -> null
                is ResolvedConstraintLangType -> null
                is ErrorLangType.ProxyError -> null
                is ErrorLangType -> ResolverError(
                    source,
                    error = foundError,
                )

                is LangType.Pending -> ResolverError(
                    source,
                    error = ErrorLangType.NeverResolved,
                )
            }
        }

        return Pair(file, errors)
    }
}