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

    fun getInferredType(breadcrumbs: Breadcrumbs): ValueLangType = resolvedTypes[ResolutionKey(
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
            val core = CoreDescriptors.coreBuiltinFile
            it[core.first] = core.second
            it.putAll(CoreDescriptors.coreFiles)
        }.toMap()

        val nodeTags = analyzed.nodeTags

        val resolvedTypes = mutableMapOf<ResolutionKey, ValueLangType>()
        val knownCanonicalTypes = mutableMapOf<CanonicalLangTypeId, CanonicalLangType>()

        val builtins = CoreDescriptors.coreBuiltinFile.second.exports

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
                        is DeclarationNode.NamedValue -> {
                            track(INFERRED)
                            if (node.typeAnnotation != null) {
                                track(CONSTRAINT)
                            }
                        }

                        is PatternNode -> track(CONSTRAINT)

                        is DeclarationNode.Import.MappingNode -> track(INFERRED)

                        is DeclarationNode.Function.FunctionParameterNode -> track(CONSTRAINT)

                        is BranchOptionNode.IfBranchOptionNode -> {
                            resolvedTypes[ResolutionKey(CONSTRAINT, node.condition.info.breadcrumbs)] =
                                builtins["BinaryAnswer"]!!
                        }

                        is ExpressionNode.CallExpression -> {
                            track(INFERRED)
                            for (param in node.parameters) {
                                track(INFERRED, param)
                                track(CONSTRAINT, param)
                            }
                        }

                        is ExpressionNode -> track(INFERRED)

                        is StatementNode.EffectStatement -> {
                            track(INFERRED)
                            track(CONSTRAINT, node.body)
                        }

                        is StatementNode.SetStatement -> track(INFERRED)

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

            val pendingReferences = resolvedTypes.mapNotNull { if (it.value.isPending()) it.key else null }
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
                when (pendingKey.type) {
                    INFERRED -> when (node) {
                        is TypeReferenceNode.IdentifierTypeReferenceNode -> {
                            val comesFromTag = pendingNodeTags.firstNotNullOfOrNull { it as? NodeTag.ValueComesFrom }
                            if (comesFromTag == null) {
                                resolveWith(ErrorLangType.NotInScope)
                                return@eachPendingNode
                            }

                            resolveWith(getResolvedTypeOf(comesFromTag.source).expectConstraint())
                        }

                        is ExpressionNode.StringLiteralExpression -> resolveWith(LangPrimitiveKind.STRING.toValueLangType())
                        is ExpressionNode.IntegerLiteralExpression -> resolveWith(LangPrimitiveKind.INTEGER.toValueLangType())

                        is ExpressionNode.BlockExpressionNode -> resolveWith(getResolvedTypeOf(node.block))

                        is ExpressionNode.IdentifierExpression -> {
                            val comesFromTag = pendingNodeTags.firstNotNullOfOrNull { it as? NodeTag.ValueComesFrom }
                            if (comesFromTag == null) {
                                resolveWith(ErrorLangType.NotInScope)
                                return@eachPendingNode
                            }

                            val usesCapturedValue =
                                pendingNodeTags.firstNotNullOfOrNull { it as? NodeTag.UsesCapturedValue }
                            if (usesCapturedValue != null) {
                                val sourceNode = fileNode.findNode(comesFromTag.source)
                                if (sourceNode is DeclarationNode.NamedValue && sourceNode.isVariable) {
                                    resolveWith(ErrorLangType.OuterVariable)
                                    return@eachPendingNode
                                }
                            }

                            resolveWith(getResolvedTypeOf(comesFromTag.source))
                        }

                        is ExpressionNode.CallExpression -> {
                            data class Callee(
                                val expectedParams: List<LangParameter>,
                                val returnConstraint: ConstraintReference,
                                val strictParams: Boolean
                            )
                            val (expectedParams, returnConstraint, strictParams) = when (val calleeType =
                                getResolvedTypeOf(node.callee)) {
                                is ValueLangType.Pending, is ErrorLangType -> {
                                    resolveWith(calleeType)
                                    return@eachPendingNode
                                }

                                is FunctionValueLangType -> {
                                    Callee(calleeType.params, calleeType.returnConstraint, strictParams = false)
                                }

                                is ConstraintValueLangType -> {
                                    val canonicalType = calleeType.tryGetCanonicalType()
                                    if (canonicalType != null) {

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
                                    } else {
                                        resolveWith(ErrorLangType.NotCallable)
                                        return@eachPendingNode
                                    }
                                }

                                is ResolvedValueLangType -> {
                                    resolveWith(ErrorLangType.NotCallable)
                                    return@eachPendingNode
                                }
                            }

                            val params = arrayOfNulls<ValueLangType>(expectedParams.size)
                            node.parameters.forEachIndexed { i, paramNode ->
                                val paramType = getResolvedTypeOf(paramNode)
                                if (strictParams && paramType is ErrorLangType) {
                                    resolveWith(paramType)
                                    return@eachPendingNode
                                }
                                params[i] = paramType
                            }

                            val foundParams = mutableListOf<ValueLangType>()
                            val missingParams = mutableListOf<LangParameter>()
                            for ((i, param) in params.withIndex()) {
                                if (param != null) {
                                    foundParams.add(param)
                                } else {
                                    missingParams.add(expectedParams[i])
                                }
                            }

                            if (missingParams.isNotEmpty()) {
                                resolveWith(ErrorLangType.MissingParameters(missingParams.map { it.name }))
                            } else if (foundParams.all { !it.isPending() }) {
                                resolveWith(
                                    when (returnConstraint) {
                                        is ConstraintReference.Pending -> ValueLangType.Pending
                                        is ConstraintReference.Error -> ErrorLangType.ProxyError.from(
                                            returnConstraint.errorType, getSourcePosition(node.callee)
                                        )

                                        is ConstraintReference.ResolvedConstraint -> returnConstraint.valueType
                                    }
                                )
                            }
                        }

                        is ExpressionNode.CallExpression.ParameterNode -> {
                            resolveWith(getResolvedTypeOf(node.value))
                        }

                        is ExpressionNode.CauseExpression -> {
                            val signalType = when (val signalType = getResolvedTypeOf(node.signal)) {
                                is ValueLangType.Pending, is ErrorLangType -> {
                                    resolveWith(signalType)
                                    return@eachPendingNode
                                }

                                is InstanceValueLangType -> signalType.canonicalType

                                is ConstraintValueLangType -> signalType.tryGetCanonicalType()?.let {
                                    if (it.isUnique()) it else null
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
                                    when (signalType.result) {
                                        is ConstraintReference.Pending -> {}
                                        is ConstraintReference.Error -> resolveWithProxyError(
                                            signalType.result.errorType, node.signal
                                        )

                                        is ConstraintReference.ResolvedConstraint -> resolveWith(signalType.result.valueType)
                                    }
                                }

                                else -> resolveWith(ErrorLangType.NotCausable)
                            }
                        }

                        is ExpressionNode.BranchExpressionNode -> {
                            if (node.branches.isEmpty()) {
                                resolveWith(ActionValueLangType)
                                return@eachPendingNode
                            }

                            var withValue = OptionValueLangType.from(node.withValue?.let { getResolvedTypeOf(it) }
                                ?: AnythingValueLangType)
                            if (withValue.isPending()) {
                                return@eachPendingNode
                            }

                            val possibleReturnValues = mutableListOf<ValueLangType>()

                            val branchesBeforeElse =
                                node.branches.takeWhile { it !is BranchOptionNode.ElseBranchOptionNode }
                            for (branch in branchesBeforeElse) {
                                var resolvedType = getResolvedTypeOf(branch.body)
                                when (branch) {
                                    is BranchOptionNode.IfBranchOptionNode -> {
                                        if (withValue.isEmpty()) {
                                            resolvedType = ErrorLangType.UnreachableBranch(withValue)
                                        }
                                    }

                                    is BranchOptionNode.IsBranchOptionNode -> {
                                        val patternType = getResolvedTypeOf(branch.pattern)
                                        if (patternType is ResolvedValueLangType) {
                                            if (withValue.isSupersetOf(patternType)) {
                                                withValue = withValue.subtract(patternType)
                                            } else {
                                                resolvedType = ErrorLangType.UnreachableBranch(withValue)
                                            }
                                        }
                                    }

                                    is BranchOptionNode.ElseBranchOptionNode -> error("else branch in branches before else")
                                }

                                iterationResolvedReferences.add(
                                    ResolutionKey(
                                        INFERRED,
                                        branch.info.breadcrumbs
                                    ) to resolvedType
                                )
                                possibleReturnValues.add(resolvedType.letIfError {
                                    ErrorLangType.ProxyError.from(
                                        it,
                                        getSourcePosition(branch)
                                    )
                                })
                            }

                            val elseBranch =
                                node.branches.firstNotNullOfOrNull { it as? BranchOptionNode.ElseBranchOptionNode }
                            if (elseBranch != null) {
                                withValue = OptionValueLangType(emptyList())
                                val resolvedType = getResolvedTypeOf(elseBranch.body)
                                iterationResolvedReferences.add(
                                    ResolutionKey(
                                        INFERRED,
                                        elseBranch.info.breadcrumbs
                                    ) to resolvedType
                                )
                                possibleReturnValues.add(resolvedType.letIfError {
                                    ErrorLangType.ProxyError.from(
                                        it,
                                        getSourcePosition(elseBranch)
                                    )
                                })
                            }

                            val branchesAfterElse = node.branches.drop(branchesBeforeElse.size + 1)
                            for (branch in branchesAfterElse) {
                                val unreachableError = ErrorLangType.UnreachableBranch(null)
                                iterationResolvedReferences.add(
                                    ResolutionKey(
                                        INFERRED,
                                        branch.info.breadcrumbs
                                    ) to unreachableError
                                )
                                possibleReturnValues.add(
                                    ErrorLangType.ProxyError.from(
                                        unreachableError,
                                        getSourcePosition(branch)
                                    )
                                )
                            }

                            if (withValue.options.isNotEmpty()) {
                                possibleReturnValues.add(ErrorLangType.MissingElseBranch(withValue))
                            }

                            resolveWith(OptionValueLangType(possibleReturnValues.map { returnValue ->
                                returnValue.letIfResolved { it.toConstraint() }.asConstraintReference()
                            }).normalize())
                        }

                        is ExpressionNode.MemberExpression -> {
                            val obj = getResolvedTypeOf(node.objectExpression)
                            when (obj) {
                                is ValueLangType.Pending, is ErrorLangType -> resolveWith(obj)
                                is ConstraintValueLangType -> resolveWith(ErrorLangType.ImplementationTodo("Can't get members of a type"))

                                // TODO: it's kinda weird that the resolution of
                                // which field is being referenced isn't passed to the compiler
                                is InstanceValueLangType -> {
                                    val fields = when (val type = obj.canonicalType) {
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

                        is StatementNode.ExpressionStatement -> {
                            resolveWith(getResolvedTypeOf(node.expression))
                        }

                        is StatementNode.EffectStatement -> {
                            val resultType = run result@{
                                val conditionType = getResolvedTypeOf(node.pattern).let {
                                    if (it is InstanceValueLangType && it.canonicalType is CanonicalLangType.SignalCanonicalLangType) {
                                        it.canonicalType
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

                            val bodyResult = getResolvedTypeOf(node.body).let {
                                when (it) {
                                    is ValueLangType.Pending, is ErrorLangType -> {
                                        resolveWith(it)
                                        return@eachPendingNode
                                    }

                                    is ResolvedValueLangType -> it
                                }
                            }

                            iterationResolvedReferences.add(
                                ResolutionKey(
                                    INFERRED, node.body.info.breadcrumbs
                                ) to bodyResult
                            )

                            resolveWith(ActionValueLangType)
                        }

                        is StatementNode.SetStatement -> {
                            val tag = pendingNodeTags.firstNotNullOfOrNull { it as? NodeTag.SetsVariable } ?: run {
                                resolveWith(ErrorLangType.NotInScope)
                                return@eachPendingNode
                            }
                            val variableBreadcrumbs = tag.variable

                            val variable = fileNode.findNode(variableBreadcrumbs)
                            if (variable !is DeclarationNode.NamedValue || !variable.isVariable) {
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

                        is BodyNode.BlockBodyNode -> {
                            val lastStatement = node.statements.lastOrNull()
                            if (lastStatement is StatementNode.ExpressionStatement) {
                                resolveWith(getResolvedTypeOf(lastStatement))
                            } else {
                                resolveWith(ActionValueLangType)
                            }
                        }

                        is BodyNode.SingleExpressionBodyNode -> resolveWith(getResolvedTypeOf(node.expression))

                        is DeclarationNode.NamedValue -> resolveWith(getResolvedTypeOf(node.value))

                        is DeclarationNode.ObjectType -> {
                            // TODO: increment number to resolve dupes
                            val id = CanonicalLangTypeId(path, null, node.name.text, 0u)
                            val fields = node.fields?.map { field ->
                                val fieldType = getResolvedTypeOf(field.typeConstraint).asConstraintReference()
                                CanonicalLangType.ObjectField(field.name.text, fieldType)
                            } ?: emptyList()
                            val objectType = CanonicalLangType.ObjectCanonicalLangType(id, node.name.text, fields)

                            val existingKnownType = knownCanonicalTypes[id]
                            if (existingKnownType != null && !existingKnownType.isPending()) {
                                error("Accidentally clobbered canonical type: $existingKnownType with $objectType.")
                            }

                            knownCanonicalTypes[id] = objectType
                            resolveWith(ConstraintValueLangType(InstanceValueLangType(objectType)))
                        }

                        is DeclarationNode.SignalType -> {
                            val id = CanonicalLangTypeId(path, null, node.name.text, 0u)
                            // TODO: increment number to resolve dupes
                            val fields = node.fields?.map { field ->
                                val fieldType = getResolvedTypeOf(field.typeConstraint).asConstraintReference()
                                CanonicalLangType.ObjectField(field.name.text, fieldType)
                            } ?: emptyList()
                            val result = getResolvedTypeOf(node.result).asConstraintReference()

                            val signalType =
                                CanonicalLangType.SignalCanonicalLangType(id, node.name.text, fields, result)

                            val existingKnownType = knownCanonicalTypes[id]
                            if (existingKnownType != null && !existingKnownType.isPending()) {
                                error("Accidentally clobbered canonical type: $existingKnownType with $signalType.")
                            }

                            knownCanonicalTypes[id] = signalType
                            resolveWith(ConstraintValueLangType(InstanceValueLangType(signalType)))
                        }

                        is DeclarationNode.OptionType -> {
                            val options = node.options.map {
                                getResolvedTypeOf(it).asConstraintReference()
                            }
                            resolveWith(ConstraintValueLangType(OptionValueLangType(options)))
                        }

                        is DeclarationNode.Function -> {
                            val canReturn = pendingNodeTags.mapNotNull {
                                it as? NodeTag.FunctionCanReturnTypeOf
                            }
                            val returnType = when (canReturn.size) {
                                0 -> throw AssertionError("Every function should be able to return something")
                                1 -> getResolvedTypeOf(canReturn[0].returnExpression)
                                else -> ErrorLangType.ImplementationTodo("Can't infer a function that can return from multiple locations")
                            }
                            val returnConstraint = when (returnType) {
                                is ValueLangType.Pending -> ConstraintReference.Pending
                                is ErrorLangType -> ConstraintReference.Error(returnType)
                                is ResolvedValueLangType -> ConstraintReference.ResolvedConstraint(returnType)
                            }
                            val params = node.params.map { paramNode ->
                                val typeReference =
                                    paramNode.typeReference?.let { getResolvedTypeOf(it) } ?: ValueLangType.Pending
                                LangParameter(paramNode.name.text, typeReference.asConstraintReference())
                            }

                            resolveWith(FunctionValueLangType(node.name.text, returnConstraint, params))
                        }

                        is DeclarationNode.Import.MappingNode -> {
                            val referenceFileTag = pendingNodeTags.firstNotNullOf { it as? NodeTag.ReferencesFile }
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

                        else -> {}
                    }

                    CONSTRAINT -> when (node) {
                        is DeclarationNode.NamedValue -> {
                            val annotation = requireNotNull(node.typeAnnotation)
                            val annotationType = getResolvedTypeOf(annotation)

                            resolveWith(annotationType.expectConstraint())
                        }

                        is DeclarationNode.Function.FunctionParameterNode -> {
                            node.typeReference?.let { resolveWith(getResolvedTypeOf(it).expectConstraint()) }
                        }

                        is ExpressionNode.CallExpression.ParameterNode -> {
                            val paramTag = pendingNodeTags.firstNotNullOf { it as? NodeTag.ParameterForCall }
                            val (callBreadcrumbs, index) = paramTag
                            val call = fileNode.findNode(callBreadcrumbs) as ExpressionNode.CallExpression
                            when (val callType = getResolvedTypeOf(call.callee)) {
                                is ValueLangType.Pending, is ErrorLangType -> resolveWith(callType)
                                is ResolvedValueLangType -> {
                                    val params = when (callType) {
                                        is FunctionValueLangType -> callType.params
                                        is ConstraintValueLangType -> if (callType.valueType is InstanceValueLangType) {
                                            when (val canonicalType = callType.valueType.canonicalType) {
                                                is CanonicalLangType.SignalCanonicalLangType -> canonicalType.fields
                                                is CanonicalLangType.ObjectCanonicalLangType -> canonicalType.fields
                                            }.map { it.asLangParameter() }
                                        } else {
                                            throw AssertionError("Call expression not callable (should have been caught by call resolution)")
                                        }

                                        else -> throw AssertionError("Call expression not callable (should have been caught by call resolution)")
                                    }

                                    if (index >= params.size) {
                                        resolveWith(ErrorLangType.ExcessParameter(expected = params.size))
                                    } else {
                                        val param = params[index]
                                        resolveWith(
                                            when (param.valueConstraint) {
                                                is ConstraintReference.Pending -> ValueLangType.Pending
                                                is ConstraintReference.Error -> ErrorLangType.ProxyError.from(
                                                    param.valueConstraint.errorType, getSourcePosition(call)
                                                )

                                                is ConstraintReference.ResolvedConstraint -> param.valueConstraint.asConstraintValue()
                                            }
                                        )
                                    }
                                }
                            }
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