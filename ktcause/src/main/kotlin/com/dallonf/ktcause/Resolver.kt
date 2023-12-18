package com.dallonf.ktcause

import com.dallonf.ktcause.ResolutionType.*
import com.dallonf.ktcause.ast.*
import com.dallonf.ktcause.types.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

enum class ResolutionType {
    INFERRED, CONSTRAINT;
}

data class ResolutionKey(val type: ResolutionType, val breadcrumbs: Breadcrumbs)
data class ResolvedFile(
    val path: String,
    val resolvedTypes: Map<ResolutionKey, ValueLangType>,
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
        error("Couldn't find any type for $breadcrumbs" + (debug?.let { ": $it" } ?: ""))
    }

    fun getExpectedType(breadcrumbs: Breadcrumbs): ValueLangType {
        val foundConstraint = resolvedTypes[ResolutionKey(CONSTRAINT, breadcrumbs)]
        // TODO: if the inferred type is more specific than the constraint, we'll
        // want to return that
        return if (foundConstraint is ConstraintValueLangType) {
            foundConstraint.valueType
        } else if (foundConstraint != null) {
            foundConstraint
        } else {
            getInferredType(breadcrumbs)
        }
    }

    fun getInferredType(breadcrumbs: Breadcrumbs): ValueLangType {
        val foundType = resolvedTypes[ResolutionKey(
            INFERRED, breadcrumbs
        )]

        if (foundType != null) {
            return foundType
        } else {
            val debug = debugContext()?.getNodeContext(breadcrumbs)
            error("Couldn't find any type for $breadcrumbs" + (debug?.let { ": $it" } ?: ""))
        }
    }
}

object Resolver {
    @Serializable
    data class ResolverError(val position: SourcePosition.Source, val error: ErrorLangType) {
        fun debug(): String {
            return Debug.debugSerializer.encodeToString(this)
        }
    }

    internal fun List<ResolverError>.debug(): String {
        return Debug.debugSerializer.encodeToString(this)
    }


    data class ExternalFileDescriptor(
        val exports: Map<String, ValueLangType>, val types: Map<CanonicalLangTypeId, CanonicalLangType>
    )

    fun resolveForFile(
        path: String,
        fileNode: FileNode,
        analyzed: AnalyzedNode,
        otherFiles: Map<String, ExternalFileDescriptor>,
        debugContext: Debug.DebugContext? = null,
    ): Pair<ResolvedFile, List<ResolverError>> {
        val allOtherFiles = otherFiles.toMutableMap().also {
            for (file in CoreFiles.all) {
                it[file.path] = file.toFileDescriptor()
            }
        }.toMap()

        val nodeTags = analyzed.nodeTags

        val resolvedTypes = mutableMapOf<ResolutionKey, ValueLangType>()
        val knownCanonicalTypes = mutableMapOf<CanonicalLangTypeId, CanonicalLangType>()
        for (file in allOtherFiles.values) {
            for ((id, type) in file.types) {
                knownCanonicalTypes[id] = type
            }
        }

        fun registerObjectType(
            id: CanonicalLangTypeId,
            name: String,
            fields: List<CanonicalLangType.ObjectField>
        ): CanonicalLangType.ObjectCanonicalLangType {
            val objType = CanonicalLangType.ObjectCanonicalLangType(
                id, name, fields
            )
            knownCanonicalTypes[id] = objType
            return objType
        }

        fun registerSignalType(
            id: CanonicalLangTypeId,
            name: String,
            fields: List<CanonicalLangType.ObjectField>,
            result: ConstraintReference,
        ): CanonicalLangType.SignalCanonicalLangType {
            val signalType = CanonicalLangType.SignalCanonicalLangType(
                id, name, fields, result
            )
            knownCanonicalTypes[id] = signalType
            return signalType
        }

        val builtins = CoreFiles.builtin.toFileDescriptor().exports

        // Seed the crawler with everything the compiler will want to know about
        run {
            for (node in fileNode.allDescendants()) {
                run eachNode@{
                    val nodeBreadcrumbs = node.info.breadcrumbs
                    fun track(resolutionType: ResolutionType, nodeToTrack: AstNode = node) {
                        resolvedTypes[ResolutionKey(resolutionType, nodeToTrack.info.breadcrumbs)] =
                            ValueLangType.Pending
                    }

                    nodeTags[nodeBreadcrumbs]?.firstNotNullOfOrNull { it as? NodeTag.TopLevelDeclaration }?.let {
                        track(INFERRED)
                    }

                    when (node) {
                        is NamedValueNode -> {
                            track(INFERRED)
                            if (node.typeAnnotation != null) {
                                track(CONSTRAINT)
                            }
                        }

                        is PatternNode -> track(CONSTRAINT)

                        is ImportMappingNode -> track(INFERRED)

                        is FunctionSignatureParameterNode -> track(CONSTRAINT)

                        is IfBranchOptionNode -> {
                            resolvedTypes[ResolutionKey(CONSTRAINT, node.condition.info.breadcrumbs)] =
                                builtins["TrueOrFalse"]!!
                        }

                        is LoopExpressionNode -> {
                            resolvedTypes[ResolutionKey(CONSTRAINT, node.body.info.breadcrumbs)] =
                                ActionValueLangType.toConstraint()
                        }

                        is CallExpressionNode -> {
                            track(INFERRED)
                            for (param in node.parameters) {
                                track(INFERRED, param)
                            }
                        }

                        is PipeCallExpression -> {
                            track(INFERRED)
                            track(INFERRED, node.callee)
                            for (param in node.parameters) {
                                track(INFERRED, param)
                            }
                        }

                        is ExpressionNode -> track(INFERRED)

                        is EffectStatementNode -> {
                            track(INFERRED)
                            track(CONSTRAINT, node.body)
                        }

                        is SetStatementNode -> track(INFERRED)
                        is ExpressionStatementNode -> track(INFERRED)

                        is BodyNode -> track(INFERRED)

                        else -> {}
                    }
                }
            }
        }

        fun getSourcePosition(breadcrumbs: Breadcrumbs) = SourcePosition.Source(
            path, breadcrumbs, fileNode.findNode(breadcrumbs).info.position
        )

        fun getSourcePosition(node: AstNode) = getSourcePosition(node.info.breadcrumbs)

        while (true) {
            val iterationResolvedReferences = mutableListOf<Pair<ResolutionKey, ValueLangType>>()
            fun getResolvedTypeOf(breadcrumbs: Breadcrumbs): ValueLangType {
                val foundConstraint = resolvedTypes[ResolutionKey(CONSTRAINT, breadcrumbs)]

                if (foundConstraint != null) {
                    return when (foundConstraint) {
                        is ValueLangType.Pending -> foundConstraint
                        is ErrorLangType -> ErrorLangType.ProxyError.from(
                            foundConstraint, getSourcePosition(breadcrumbs)
                        )

                        is ConstraintValueLangType -> foundConstraint.valueType
                        is ResolvedValueLangType -> ErrorLangType.ValueUsedAsConstraint(foundConstraint)
                    }
                }

                val foundValue = resolvedTypes[ResolutionKey(
                    INFERRED, breadcrumbs
                )]

                if (foundValue is OptionValueLangType) {
                    return foundValue.proxyAllErrors(getSourcePosition(breadcrumbs))
                }

                if (foundValue is ErrorLangType) {
                    return ErrorLangType.ProxyError.from(foundValue, getSourcePosition(breadcrumbs))
                }

                if (foundValue != null) {
                    return foundValue
                }

                iterationResolvedReferences.add(
                    ResolutionKey(
                        INFERRED, breadcrumbs
                    ) to ValueLangType.Pending
                )
                return ValueLangType.Pending
            }

            fun getResolvedTypeOf(node: AstNode) = getResolvedTypeOf(node.info.breadcrumbs)

            val pendingReferences =
                resolvedTypes.mapNotNull { if (it.value.isPending(knownCanonicalTypes)) it.key else null }
            pendingReferences.forEach eachPendingNode@{ pendingKey ->
                fun resolveWith(langType: ValueLangType) {
                    iterationResolvedReferences.add(pendingKey to langType)
                }

                fun resolveWithProxyError(error: ErrorLangType, breadcrumbs: Breadcrumbs) {
                    val sourcePosition = getSourcePosition(breadcrumbs)
                    resolveWith(
                        ErrorLangType.ProxyError.from(error, sourcePosition)
                    )
                }

                fun resolveWithProxyError(error: ErrorLangType, sourceNode: AstNode) {
                    resolveWithProxyError(error, sourceNode.info.breadcrumbs)
                }

                val pendingNodeBreadcrumbs = pendingKey.breadcrumbs
                val node = fileNode.findNode(pendingNodeBreadcrumbs)
                val pendingNodeTags = nodeTags[pendingKey.breadcrumbs] ?: emptyList()

                fun resolveFunction(
                    nameNode: IdentifierNode?,
                    paramNodes: List<FunctionSignatureParameterNode>,
                    returnTypeNode: TypeReferenceNode?
                ) {
                    val returnConstraint = run returnConstraint@{
                        val explicitReturnType = returnTypeNode?.let {
                            getResolvedTypeOf(returnTypeNode).expectConstraint().let { resolvedReturnType ->
                                if (resolvedReturnType is ConstraintValueLangType) {
                                    resolvedReturnType.asConstraintReference()
                                } else {
                                    return@returnConstraint resolvedReturnType.valueToConstraintReference()
                                }
                            }
                        }

                        val canReturn = pendingNodeTags.mapNotNull { tag ->
                            when (tag) {
                                is NodeTag.FunctionCanReturnTypeOf -> tag.returnExpressionValue to getResolvedTypeOf(tag.returnExpressionValue)
                                is NodeTag.FunctionCanReturnAction -> tag.returnExpression to ActionValueLangType
                                else -> null
                            }
                        }

                        explicitReturnType?.let {
                            for (possibleReturn in canReturn) {
                                iterationResolvedReferences.add(
                                    Pair(
                                        ResolutionKey(CONSTRAINT, possibleReturn.first),
                                        explicitReturnType.asConstraintValue()
                                    )
                                )
                            }
                            return@returnConstraint explicitReturnType
                        }

                        if (canReturn.any { it.second.isPending(knownCanonicalTypes) }) {
                            return@returnConstraint ConstraintReference.Pending
                        }

                        val actionReturns = canReturn.filter { it.second is ActionValueLangType }
                        val valueReturns = canReturn.filter { it.second !is ActionValueLangType }
                        if (actionReturns.size > 1 && valueReturns.size > 1) {
                            return@returnConstraint ErrorLangType.ActionIncompatibleWithValueTypes(actionReturns.map {
                                getSourcePosition(
                                    it.first
                                )
                            }, valueReturns.map {
                                ErrorLangType.ActionIncompatibleWithValueTypes.ValueType(
                                    type = it.second, position = getSourcePosition(it.first)
                                )
                            }).asConstraintReference()
                        }

                        val returnType = OptionValueLangType(canReturn.map { (_, returnType) ->
                            returnType.valueToConstraintReference()
                        }).simplifyToValue()

                        returnType.getError()?.let { error ->
                            ConstraintReference.Error(error)
                        } ?: returnType.valueToConstraintReference()
                    }

                    val params = paramNodes.map { paramNode ->
                        val typeReference =
                            paramNode.typeReference?.let { getResolvedTypeOf(it) } ?: ValueLangType.Pending
                        LangParameter(paramNode.name.text, typeReference.asConstraintReference())
                    }

                    resolveWith(FunctionValueLangType(nameNode?.text, returnConstraint, params))
                }

                fun resolveCall(callee: Breadcrumbs, params: List<Breadcrumbs>) {
                    data class Callee(
                        val expectedParams: List<LangParameter>,
                        val returnConstraint: ConstraintReference,
                        val strictParams: Boolean
                    )
                    val (expectedParams, returnConstraint, strictParams) = when (val calleeType =
                        getResolvedTypeOf(callee)) {
                        is ValueLangType.Pending, is ErrorLangType -> {
                            resolveWith(calleeType)
                            return
                        }

                        is FunctionValueLangType -> {
                            Callee(calleeType.params, calleeType.returnConstraint, strictParams = false)
                        }

                        is ConstraintValueLangType -> {
                            when (val valueType = calleeType.valueType) {
                                is InstanceValueLangType -> {
                                    val canonicalType =
                                        knownCanonicalTypes[valueType.canonicalTypeId] ?: return resolveWith(
                                            ValueLangType.Pending
                                        )
                                    val fields = when (canonicalType) {
                                        is CanonicalLangType.SignalCanonicalLangType -> canonicalType.fields
                                        is CanonicalLangType.ObjectCanonicalLangType -> canonicalType.fields
                                    }

                                    val resultType = if (canonicalType.isUnique()) {
                                        calleeType.toConstraint().asConstraintReference()
                                    } else {
                                        calleeType.asConstraintReference()
                                    }

                                    Callee(
                                        fields.map { it.asLangParameter() }, resultType, strictParams = true
                                    )
                                }

                                is StopgapDictionaryLangType -> {
                                    Callee(
                                        expectedParams = emptyList(),
                                        returnConstraint = valueType.valueToConstraintReference(),
                                        strictParams = true
                                    )
                                }

                                is StopgapListLangType -> {
                                    Callee(
                                        expectedParams = emptyList(),
                                        returnConstraint = valueType.valueToConstraintReference(),
                                        strictParams = true
                                    )
                                }

                                else -> {
                                    resolveWith(ErrorLangType.NotCallable)
                                    return
                                }
                            }
                        }

                        is ResolvedValueLangType -> {
                            resolveWith(ErrorLangType.NotCallable)
                            return
                        }
                    }

                    params.forEachIndexed { i, paramBreadcrumbs ->
                        expectedParams.getOrNull(i)?.let { expected ->
                            if (expected.valueConstraint.isPending(knownCanonicalTypes)) {
                                // Make sure parameters have resolved their constraints
                                // before checking for other errors
                                return
                            }

                            iterationResolvedReferences.add(
                                ResolutionKey(
                                    CONSTRAINT, paramBreadcrumbs
                                ) to expected.valueConstraint.asConstraintValue()
                            )
                        }
                    }

                    if (params.size > expectedParams.size) {
                        resolveWith(ErrorLangType.ExcessParameters(expectedParams.size))
                        return
                    }

                    val paramTypes = arrayOfNulls<ValueLangType>(expectedParams.size)
                    params.forEachIndexed { i, paramBreadcrumbs ->
                        val paramType = getResolvedTypeOf(paramBreadcrumbs)
                        if (strictParams && paramType is ErrorLangType) {
                            resolveWith(paramType)
                            return
                        }
                        paramTypes[i] = paramType
                    }

                    val foundParams = mutableListOf<ValueLangType>()
                    val missingParams = mutableListOf<LangParameter>()
                    for ((i, param) in paramTypes.withIndex()) {
                        if (param != null) {
                            foundParams.add(param)
                        } else {
                            missingParams.add(expectedParams[i])
                        }
                    }

                    if (missingParams.isNotEmpty()) {
                        resolveWith(ErrorLangType.MissingParameters(missingParams.map { it.name }))
                    } else if (foundParams.all { !it.isPending(knownCanonicalTypes) }) {
                        resolveWith(
                            when (returnConstraint) {
                                is ConstraintReference.Pending -> ValueLangType.Pending
                                is ConstraintReference.Error -> ErrorLangType.ProxyError.from(
                                    returnConstraint.errorType, getSourcePosition(callee)
                                )

                                is ConstraintReference.ResolvedConstraint -> returnConstraint.valueType
                            }
                        )
                    }
                }

                when (pendingKey.type) {
                    INFERRED -> when (node) {
                        is IdentifierTypeReferenceNode -> {
                            val comesFromTag = pendingNodeTags.firstNotNullOfOrNull { it as? NodeTag.ValueComesFrom }
                            if (comesFromTag == null) {
                                resolveWith(ErrorLangType.NotInScope)
                                return@eachPendingNode
                            }

                            resolveWith(getResolvedTypeOf(comesFromTag.source).expectConstraint())
                        }

                        is FunctionTypeReferenceNode -> {
                            val returnConstraint =
                                getResolvedTypeOf(node.returnType).expectConstraint().asConstraintReference()
                            val params: List<LangParameter> = node.params.map { param ->
                                val paramTypeConstraint = param.typeReference?.let {
                                    getResolvedTypeOf(it).expectConstraint().asConstraintReference()
                                }
                                LangParameter(param.name.text, paramTypeConstraint ?: ConstraintReference.Pending)
                            }

                            resolveWith(FunctionValueLangType(name = null, returnConstraint, params).toConstraint())
                        }

                        is StringLiteralExpressionNode -> resolveWith(LangPrimitiveKind.TEXT.toValueLangType())
                        is NumberLiteralExpressionNode -> resolveWith(LangPrimitiveKind.NUMBER.toValueLangType())

                        is GroupExpressionNode -> resolveWith(getResolvedTypeOf(node.expression))

                        is BlockExpressionNode -> resolveWith(getResolvedTypeOf(node.block))

                        is FunctionExpressionNode -> {
                            resolveFunction(null, node.params, node.returnType)
                        }

                        is IdentifierExpressionNode -> {
                            val comesFromTag = pendingNodeTags.firstNotNullOfOrNull { it as? NodeTag.ValueComesFrom }
                            if (comesFromTag == null) {
                                resolveWith(ErrorLangType.NotInScope)
                                return@eachPendingNode
                            }

                            val usesCapturedValue =
                                pendingNodeTags.firstNotNullOfOrNull { it as? NodeTag.UsesCapturedValue }
                            if (usesCapturedValue != null) {
                                val sourceNode = fileNode.findNode(comesFromTag.source)
                                if (sourceNode is NamedValueNode && sourceNode.isVariable) {
                                    resolveWith(ErrorLangType.OuterVariable)
                                    return@eachPendingNode
                                }
                            }

                            val resolvedType = getResolvedTypeOf(comesFromTag.source)

                            // special case: using `Action` as a keyword creates an Action value,
                            // not a reference to the type
                            if (resolvedType is ConstraintValueLangType && resolvedType.valueType == ActionValueLangType) {
                                resolveWith(ActionValueLangType)
                                return@eachPendingNode
                            }

                            resolveWith(resolvedType)
                        }

                        is CallExpressionNode -> {
                            resolveCall(node.callee.info.breadcrumbs, node.parameters.map { it.info.breadcrumbs })
                        }

                        is PipeCallExpression -> {
                            // TODO: it could be a warning to use this syntax where the first parameter isn't named "this" or "it"
                            resolveCall(node.callee.info.breadcrumbs,
                                listOf(node.subject.info.breadcrumbs) + node.parameters.map { it.info.breadcrumbs })
                        }

                        is FunctionCallParameterNode -> {
                            resolveWith(getResolvedTypeOf(node.value))
                        }

                        is CauseExpressionNode -> {
                            val signalType = when (val signalType = getResolvedTypeOf(node.signal)) {
                                is ValueLangType.Pending, is ErrorLangType -> {
                                    resolveWith(signalType)
                                    return@eachPendingNode
                                }

                                is InstanceValueLangType -> knownCanonicalTypes[signalType.canonicalTypeId]

                                is ConstraintValueLangType -> signalType.tryGetCanonicalTypeId()?.let {
                                    if (it.isUnique) knownCanonicalTypes[it] else null
                                } ?: run {
                                    resolveWith(ErrorLangType.NotCausable)
                                    return@eachPendingNode
                                }

                                is ResolvedValueLangType -> {
                                    resolveWith(ErrorLangType.NotCausable)
                                    return@eachPendingNode
                                }
                            }

                            when (signalType) {
                                is CanonicalLangType.SignalCanonicalLangType -> {
                                    when (val result = signalType.result) {
                                        is ConstraintReference.Pending -> {}
                                        is ConstraintReference.Error -> resolveWithProxyError(
                                            result.errorType, node.signal
                                        )

                                        is ConstraintReference.ResolvedConstraint -> resolveWith(result.valueType)
                                    }
                                }

                                else -> resolveWith(ErrorLangType.NotCausable)
                            }
                        }

                        is BranchExpressionNode -> {
                            if (node.branches.isEmpty()) {
                                resolveWith(ActionValueLangType)
                                return@eachPendingNode
                            }

                            var withValue = OptionValueLangType.from(node.withValue?.let { getResolvedTypeOf(it) }
                                ?: AnythingValueLangType)
                            if (withValue.isPending(knownCanonicalTypes)) {
                                return@eachPendingNode
                            }

                            data class PossibleReturnValue(val value: ValueLangType, val source: SourcePosition.Source?)

                            val possibleReturnValues = mutableListOf<PossibleReturnValue>()

                            val branchesBeforeElse =
                                node.branches.takeWhile { it !is ElseBranchOptionNode }
                            for (branch in branchesBeforeElse) {
                                var resolvedType = getResolvedTypeOf(branch.body)
                                when (branch) {
                                    is IfBranchOptionNode -> {
                                        if (withValue.isEmpty()) {
                                            resolvedType = ErrorLangType.UnreachableBranch(withValue)
                                        }
                                    }

                                    is IsBranchOptionNode -> {
                                        val patternType = getResolvedTypeOf(branch.pattern)
                                        if (patternType is ResolvedValueLangType) {
                                            if (withValue.isSupersetOf(patternType)) {
                                                withValue = withValue.narrow(patternType)
                                            } else {
                                                resolvedType = ErrorLangType.UnreachableBranch(withValue)
                                            }
                                        }
                                    }

                                    is ElseBranchOptionNode -> error("else branch in branches before else")
                                }

                                val sourcePosition = getSourcePosition(branch)
                                iterationResolvedReferences.add(
                                    ResolutionKey(
                                        INFERRED, sourcePosition.breadcrumbs
                                    ) to resolvedType
                                )
                                possibleReturnValues.add(PossibleReturnValue(resolvedType.letIfError {
                                    ErrorLangType.ProxyError.from(
                                        it, sourcePosition
                                    )
                                }, sourcePosition))
                            }

                            val elseBranch =
                                node.branches.firstNotNullOfOrNull { it as? ElseBranchOptionNode }
                            if (elseBranch != null) {
                                withValue = OptionValueLangType(emptyList())
                                val resolvedType = getResolvedTypeOf(elseBranch.body)
                                iterationResolvedReferences.add(
                                    ResolutionKey(
                                        INFERRED, elseBranch.info.breadcrumbs
                                    ) to resolvedType
                                )
                                possibleReturnValues.add(PossibleReturnValue(resolvedType.letIfError {
                                    ErrorLangType.ProxyError.from(
                                        it, getSourcePosition(elseBranch)
                                    )
                                }, source = null))
                            }

                            val branchesAfterElse = node.branches.drop(branchesBeforeElse.size + 1)
                            for (branch in branchesAfterElse) {
                                val unreachableError = ErrorLangType.UnreachableBranch(null)
                                val sourcePosition = getSourcePosition(branch)
                                iterationResolvedReferences.add(
                                    ResolutionKey(
                                        INFERRED, sourcePosition.breadcrumbs
                                    ) to unreachableError
                                )
                                possibleReturnValues.add(
                                    PossibleReturnValue(
                                        ErrorLangType.ProxyError.from(
                                            unreachableError, sourcePosition
                                        ), sourcePosition
                                    )
                                )
                            }

                            if (withValue.options.isNotEmpty()) {
                                possibleReturnValues.add(
                                    PossibleReturnValue(
                                        ErrorLangType.MissingElseBranch(withValue), source = null
                                    )
                                )
                            }

                            val actionReturns = possibleReturnValues.filter { it.value is ActionValueLangType }
                            if (actionReturns.isNotEmpty()) {
                                val nonActionReturns = possibleReturnValues.filter {
                                    it.value is ResolvedValueLangType && it.value !is ActionValueLangType && it.value !is NeverContinuesValueLangType
                                }
                                if (nonActionReturns.isNotEmpty()) {
                                    resolveWith(
                                        ErrorLangType.ActionIncompatibleWithValueTypes(actions = actionReturns.map { it.source!! },
                                            types = nonActionReturns.map {
                                                ErrorLangType.ActionIncompatibleWithValueTypes.ValueType(
                                                    it.value, it.source!!
                                                )
                                            })
                                    )
                                    return@eachPendingNode
                                }
                            }

                            resolveWith(OptionValueLangType(possibleReturnValues.map { (value, _) ->
                                value.letIfResolved { it.toConstraint() }.asConstraintReference()
                            }).simplifyToValue())
                        }

                        is LoopExpressionNode -> {
                            val breaks = pendingNodeTags.mapNotNull { it as? NodeTag.LoopBreaksAt }

                            if (breaks.isEmpty()) {
                                resolveWith(NeverContinuesValueLangType)
                            } else {
                                val breakTypes = breaks.map {
                                    val breakExpression =
                                        fileNode.findNode(it.breakExpression) as BreakExpression
                                    breakExpression to if (breakExpression.withValue != null) {
                                        getResolvedTypeOf(breakExpression.withValue)
                                    } else {
                                        ActionValueLangType
                                    }
                                }

                                if (breakTypes.any { it.second.isPending(knownCanonicalTypes) }) {
                                    return@eachPendingNode
                                }

                                val actionReturns = breakTypes.filter { it.second is ActionValueLangType }
                                val valueReturns = breakTypes.filter { it.second !is ActionValueLangType }
                                if (actionReturns.size > 1 && valueReturns.size > 1) {
                                    resolveWith(
                                        ErrorLangType.ActionIncompatibleWithValueTypes(actionReturns.map {
                                            getSourcePosition(
                                                it.first
                                            )
                                        }, valueReturns.map {
                                            ErrorLangType.ActionIncompatibleWithValueTypes.ValueType(
                                                type = it.second, position = getSourcePosition(it.first)
                                            )
                                        })
                                    )
                                    return@eachPendingNode
                                }

                                val loopResultType = OptionValueLangType(breakTypes.map { (_, returnType) ->
                                    returnType.valueToConstraintReference()
                                }).simplifyToValue()
                                resolveWith(loopResultType)
                            }
                        }

                        is ReturnExpression -> {
                            resolveWith(NeverContinuesValueLangType)
                        }

                        is BreakExpression -> {
                            val breakTag = pendingNodeTags.firstNotNullOfOrNull { it as? NodeTag.BreaksLoop }
                            if (breakTag != null) {
                                resolveWith(NeverContinuesValueLangType)
                            } else {
                                resolveWith(ErrorLangType.CannotBreakHere)
                            }
                        }

                        is MemberExpression -> {
                            val obj = getResolvedTypeOf(node.objectExpression)

                            if (obj.isPending(knownCanonicalTypes)) {
                                return@eachPendingNode
                            }

                            when (obj) {
                                is ValueLangType.Pending, is ErrorLangType -> resolveWith(obj)
                                is ConstraintValueLangType -> resolveWith(ErrorLangType.ImplementationTodo("Can't get members of a type"))

                                // TODO: it's kinda weird that the resolution of
                                // which field is being referenced isn't passed to the compiler
                                is InstanceValueLangType -> {
                                    val fields = when (val type = knownCanonicalTypes[obj.canonicalTypeId]!!) {
                                        is CanonicalLangType.ObjectCanonicalLangType -> type.fields
                                        is CanonicalLangType.SignalCanonicalLangType -> type.fields
                                    }
                                    val referencedField = fields.firstOrNull { it.name == node.memberIdentifier.text }

                                    if (referencedField != null) {
                                        when (val constraint = referencedField.valueConstraint) {
                                            is ConstraintReference.Pending -> {}
                                            is ConstraintReference.Error -> resolveWithProxyError(
                                                constraint.errorType, node.objectExpression
                                            )

                                            is ConstraintReference.ResolvedConstraint -> resolveWith(constraint.valueType)
                                        }
                                    } else {
                                        resolveWith(ErrorLangType.DoesNotHaveMember)
                                    }
                                }

                                is ResolvedValueLangType -> {
                                    resolveWith(ErrorLangType.DoesNotHaveAnyMembers)
                                }
                            }
                        }

                        is ExpressionStatementNode -> {
                            resolveWith(getResolvedTypeOf(node.expression))
                        }

                        is DeclarationStatementNode -> {
                            if (node.declaration is NamedValueNode) {
                                if (getResolvedTypeOf(node.declaration.value.info.breadcrumbs) == NeverContinuesValueLangType) {
                                    resolveWith(NeverContinuesValueLangType)
                                    return@eachPendingNode
                                }
                            }

                            resolveWith(ActionValueLangType)
                        }

                        is EffectStatementNode -> {
                            val resultType = run result@{
                                val conditionType = getResolvedTypeOf(node.pattern).let {
                                    if (it is InstanceValueLangType && it.canonicalTypeId.category == CanonicalLangTypeId.CanonicalLangTypeIdCategory.SIGNAL) {
                                        knownCanonicalTypes[it.canonicalTypeId] as CanonicalLangType.SignalCanonicalLangType
                                    } else {
                                        when (it) {
                                            is ValueLangType.Pending, is ErrorLangType -> {
                                                resolveWith(it)
                                                return@eachPendingNode
                                            }

                                            is AnySignalValueLangType -> {
                                                return@result ConstraintValueLangType(AnythingValueLangType)
                                            }

                                            is ResolvedValueLangType -> {
                                                resolveWith(ErrorLangType.NotCausable)
                                                return@eachPendingNode
                                            }
                                        }
                                    }
                                }
                                val resultType = when (val result = conditionType.result) {
                                    is ConstraintReference.Pending -> return@eachPendingNode
                                    is ConstraintReference.Error -> {
                                        resolveWithProxyError(result.errorType, node.pattern.typeReference)
                                        return@eachPendingNode
                                    }

                                    is ConstraintReference.ResolvedConstraint -> result.asConstraintValue()
                                }
                                resultType
                            }

                            iterationResolvedReferences.add(
                                ResolutionKey(
                                    CONSTRAINT, node.body.info.breadcrumbs
                                ) to resultType
                            )

                            resolveWith(ActionValueLangType)
                        }

                        is SetStatementNode -> {
                            val tag = pendingNodeTags.firstNotNullOfOrNull { it as? NodeTag.SetsVariable } ?: run {
                                resolveWith(ErrorLangType.NotInScope)
                                return@eachPendingNode
                            }
                            val variableBreadcrumbs = tag.variable

                            val variable = fileNode.findNode(variableBreadcrumbs)
                            if (variable !is NamedValueNode || !variable.isVariable) {
                                resolveWith(ErrorLangType.NotVariable)
                                return@eachPendingNode
                            }

                            if (pendingNodeTags.any { it is NodeTag.UsesCapturedValue }) {
                                resolveWith(ErrorLangType.OuterVariable)
                                return@eachPendingNode
                            }

                            val expectedType = when (val variableValue = getResolvedTypeOf(variable)) {
                                is ValueLangType.Pending, is ErrorLangType -> {
                                    resolveWith(variableValue)
                                    return@eachPendingNode
                                }

                                is ResolvedValueLangType -> ConstraintValueLangType(variableValue)
                            }

                            val newValue = when (val newValue = getResolvedTypeOf(node.expression)) {
                                is ValueLangType.Pending, is ErrorLangType -> {
                                    resolveWith(newValue)
                                    return@eachPendingNode
                                }

                                is ResolvedValueLangType -> newValue
                            }

                            if (newValue.isAssignableTo(expectedType)) {
                                resolveWith(ActionValueLangType)
                            } else {
                                resolveWith(ErrorLangType.MismatchedType(expectedType, newValue))
                            }
                        }

                        is BlockBodyNode -> {
                            var lastType: ValueLangType = ActionValueLangType
                            for (statement in node.statements) {
                                if (lastType == NeverContinuesValueLangType) {
//                                    TODO: unreachable code warnings?
                                    resolveWith(lastType)
                                    return@eachPendingNode
                                }
                                lastType = getResolvedTypeOf(statement)
                                if (lastType == ValueLangType.Pending) {
                                    return@eachPendingNode
                                }
                            }

                            resolveWith(lastType)
                        }

                        is SingleStatementBodyNode -> resolveWith(getResolvedTypeOf(node.statement))

                        is NamedValueNode -> resolveWith(getResolvedTypeOf(node.value))

                        is ObjectType -> {
                            val canonicalIdTag = pendingNodeTags.firstNotNullOf { it as? NodeTag.CanonicalIdInfo }
                            val fields = node.fields?.map { field ->
                                val fieldType = getResolvedTypeOf(field.typeConstraint).asConstraintReference()
                                CanonicalLangType.ObjectField(field.name.text, fieldType)
                            } ?: emptyList()
                            val id = CanonicalLangTypeId(
                                path,
                                canonicalIdTag.parentName,
                                node.name.text,
                                canonicalIdTag.index,
                                category = CanonicalLangTypeId.CanonicalLangTypeIdCategory.OBJECT,
                                isUnique = fields.isEmpty()
                            )
                            registerObjectType(id, node.name.text, fields)

                            resolveWith(ConstraintValueLangType(InstanceValueLangType(id)))
                        }

                        is SignalType -> {
                            val canonicalIdTag = pendingNodeTags.firstNotNullOf { it as? NodeTag.CanonicalIdInfo }
                            val fields = node.fields?.map { field ->
                                val fieldType = getResolvedTypeOf(field.typeConstraint).asConstraintReference()
                                CanonicalLangType.ObjectField(field.name.text, fieldType)
                            } ?: emptyList()
                            val id = CanonicalLangTypeId(
                                path, canonicalIdTag.parentName, node.name.text, canonicalIdTag.index,
                                category = CanonicalLangTypeId.CanonicalLangTypeIdCategory.SIGNAL,
                                isUnique = fields.isEmpty()
                            )
                            registerSignalType(
                                id,
                                node.name.text,
                                fields,
                                node.result?.let { getResolvedTypeOf(it).asConstraintReference() }
                                    ?: ActionValueLangType.valueToConstraintReference())

                            resolveWith(ConstraintValueLangType(InstanceValueLangType(id)))
                        }

                        is OptionType -> {
                            val options = node.options.map {
                                getResolvedTypeOf(it).asConstraintReference()
                            }
                            resolveWith(ConstraintValueLangType(OptionValueLangType(options)))
                        }

                        is FunctionNode -> {
                            resolveFunction(node.name, node.params, node.returnType)
                        }

                        is ImportMappingNode -> {
                            val referenceFileTag =
                                pendingNodeTags.firstNotNullOfOrNull { it as? NodeTag.ReferencesFile }
                            if (referenceFileTag != null) {
                                val exportName = node.sourceName.text
                                val file = allOtherFiles[referenceFileTag.path] ?: run {
                                    resolveWith(ErrorLangType.FileNotFound)
                                    return@eachPendingNode
                                }
                                val export = file.exports[exportName] ?: run {
                                    resolveWith(ErrorLangType.ExportNotFound)
                                    return@eachPendingNode
                                }

                                resolveWith(export)
                            }

                            val comesFromTag = pendingNodeTags.firstNotNullOfOrNull { it as? NodeTag.ValueComesFrom }
                            val badFileTag =
                                comesFromTag?.source.let { source -> nodeTags[source]?.firstNotNullOfOrNull { it as? NodeTag.BadFileReference } }

                            if (badFileTag != null) {
                                resolveWith(ErrorLangType.ImportPathInvalid)
                                return@eachPendingNode
                            }
                        }

                        else -> {}
                    }

                    CONSTRAINT -> when (node) {
                        is NamedValueNode -> {
                            val annotation = requireNotNull(node.typeAnnotation)
                            val annotationType = getResolvedTypeOf(annotation)

                            resolveWith(annotationType.expectConstraint())
                        }

                        is FunctionSignatureParameterNode -> {
                            node.typeReference?.let { resolveWith(getResolvedTypeOf(it).expectConstraint()) }
                        }

                        is PatternNode -> {
                            resolveWith(getResolvedTypeOf(node.typeReference).expectConstraint())
                        }

                        else -> {}
                    }
                }

            }


            val changedResolutions = iterationResolvedReferences.filter { (key, value) -> resolvedTypes[key] != value }
            val resolved = changedResolutions.size
            for ((key, newType) in changedResolutions) {
                val oldResolvedType = resolvedTypes[key]?.let {
                    if (it.isPending(knownCanonicalTypes)) null else it
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
                val constraintType = (constraint as? ResolvedValueLangType)
                if (constraintType != null && constraintType !is ConstraintValueLangType) {
                    resolvedTypes[ResolutionKey(CONSTRAINT, breadcrumbsOfConstraint)] =
                        ErrorLangType.ValueUsedAsConstraint(constraintType)
                    continue
                }
                require(constraintType is ConstraintValueLangType?)

                val actualType =
                    resolvedTypes[ResolutionKey(INFERRED, breadcrumbsOfConstraint)] as? ResolvedValueLangType

                if (constraintType != null && actualType != null && !actualType.isAssignableTo(constraintType)) {
                    resolvedTypes[ResolutionKey(INFERRED, breadcrumbsOfConstraint)] = ErrorLangType.MismatchedType(
                        expected = constraintType, actual = actualType
                    )
                }
            }
        }

        // TODO: knownCanonicalTypes should probably be filtered down to the ones actually used
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
                is ErrorLangType.ProxyError -> null
                is ErrorLangType -> ResolverError(
                    source,
                    error = foundError,
                )

                is ValueLangType.Pending -> ResolverError(
                    source,
                    error = ErrorLangType.NeverResolved,
                )
            }
        }

        return Pair(file, errors)
    }
}