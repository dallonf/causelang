package com.dallonf.ktcause

import com.dallonf.ktcause.ResolutionType.*
import com.dallonf.ktcause.ast.*
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
        error("Couldn't find any type for $breadcrumbs" + (debug?.let { ": $it" } ?: ""))
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
            for (node in fileNode.allDescendants()) {
                run eachNode@{
                    val nodeBreadcrumbs = node.info.breadcrumbs
                    fun track(resolutionType: ResolutionType, nodeToTrack: AstNode = node) {
                        resolvedTypes[ResolutionKey(resolutionType, nodeToTrack.info.breadcrumbs)] = LangType.Pending
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
                                LangPrimitiveKind.BOOLEAN.toConstraintLangType()
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
            val iterationResolvedReferences = mutableListOf<Pair<ResolutionKey, LangType>>()
            fun getResolvedTypeOf(breadcrumbs: Breadcrumbs): LangType {
                val foundConstraint = resolvedTypes[ResolutionKey(CONSTRAINT, breadcrumbs)]

                if (foundConstraint is ErrorLangType) {
                    return ErrorLangType.ProxyError.from(foundConstraint, getSourcePosition(breadcrumbs))
                }

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

                if (foundValue is ErrorLangType) {
                    return ErrorLangType.ProxyError.from(foundValue, getSourcePosition(breadcrumbs))
                }

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

            fun getResolvedTypeOf(node: AstNode) = getResolvedTypeOf(node.info.breadcrumbs)

            val pendingReferences = resolvedTypes.asSequence().mapNotNull { if (it.value.isPending()) it.key else null }
            pendingReferences.forEach eachPendingNode@{ pendingKey ->
                fun resolveWith(langType: LangType) {
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

                            resolveWith(getResolvedTypeOf(comesFromTag.source).asConstraint())
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
                                val returnConstraint: ConstraintLangType,
                                val strictParams: Boolean
                            )
                            val (expectedParams, returnConstraint, strictParams) = when (val calleeType =
                                getResolvedTypeOf(node.callee)) {
                                is LangType.Pending, is ErrorLangType -> {
                                    resolveWith(calleeType)
                                    return@eachPendingNode
                                }

                                is FunctionValueLangType -> {
                                    Callee(calleeType.params, calleeType.returnConstraint, strictParams = false)
                                }

                                is TypeReferenceConstraintLangType -> {
                                    val fields = when (val canonicalType = calleeType.canonicalType) {
                                        is CanonicalLangType.SignalCanonicalLangType -> canonicalType.fields
                                        is CanonicalLangType.ObjectCanonicalLangType -> canonicalType.fields
                                    }

                                    Callee(fields.map { it.asLangParameter() }, calleeType, strictParams = true)
                                }

                                is UniqueObjectLangType -> Callee(emptyList(), calleeType, strictParams = true)

                                is ResolvedValueLangType -> {
                                    resolveWith(ErrorLangType.NotCallable)
                                    return@eachPendingNode
                                }

                                is ResolvedConstraintLangType -> {
                                    resolveWith(ErrorLangType.NotCallable)
                                    return@eachPendingNode
                                }
                            }

                            val params = arrayOfNulls<LangType>(expectedParams.size)
                            node.parameters.forEachIndexed { i, paramNode ->
                                val paramType = getResolvedTypeOf(paramNode)
                                if (strictParams && paramType is ErrorLangType) {
                                    resolveWith(paramType)
                                    return@eachPendingNode
                                }
                                params[i] = paramType
                            }

                            val foundParams = mutableListOf<LangType>()
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
                                        is LangType.Pending -> LangType.Pending
                                        is ErrorLangType -> ErrorLangType.ProxyError.from(
                                            returnConstraint,
                                            getSourcePosition(node.callee)
                                        )

                                        is ResolvedConstraintLangType -> returnConstraint.toInstanceType()
                                    }
                                )
                            }
                        }

                        is ExpressionNode.CallExpression.ParameterNode -> {
                            resolveWith(getResolvedTypeOf(node.value).asValue())
                        }

                        is ExpressionNode.CauseExpression -> {
                            val signalType = when (val signalType = getResolvedTypeOf(node.signal)) {
                                is LangType.Pending, is ErrorLangType -> {
                                    resolveWith(signalType)
                                    return@eachPendingNode
                                }

                                is InstanceValueLangType -> signalType.canonicalType

                                is ResolvedValueLangType -> {
                                    resolveWith(ErrorLangType.NotCausable)
                                    return@eachPendingNode
                                }

                                is ResolvedConstraintLangType -> {
                                    resolveWith(ErrorLangType.NotCausable)
                                    return@eachPendingNode
                                }
                            }

                            when (signalType) {
                                is CanonicalLangType.SignalCanonicalLangType -> {
                                    when (signalType.result) {
                                        is ResolvedConstraintLangType -> resolveWith(signalType.result.toInstanceType())
                                        else -> resolveWith(signalType.result)
                                    }
                                }

                                else -> resolveWith(ErrorLangType.NotCausable)
                            }
                        }

                        is ExpressionNode.BranchExpressionNode -> {
                            if (node.branches.isEmpty()) {
                                resolveWith(LangPrimitiveKind.ACTION.toValueLangType())
                            }

                            val elseBranches = node.branches.mapNotNull { it as? BranchOptionNode.ElseBranchOptionNode }
                            if (elseBranches.size > 1) {
                                resolveWith(ErrorLangType.TooManyElseBranches)
                                return@eachPendingNode
                            }
                            if (elseBranches.isEmpty()) {
                                resolveWith(ErrorLangType.MissingElseBranch)
                                return@eachPendingNode
                            }

                            val branchValueTypes = node.branches.map {
                                Pair(it, getResolvedTypeOf(it.body))
                            }

                            if (branchValueTypes.all { !it.second.isPending() }) {
                                val nonErrorValueTypes = branchValueTypes.filter { it.second.getError() == null }
                                if (nonErrorValueTypes.isEmpty()) {
                                    resolveWith(branchValueTypes[0].second)
                                    return@eachPendingNode
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

                        is ExpressionNode.MemberExpression -> {
                            val obj = getResolvedTypeOf(node.objectExpression)
                            when (obj) {
                                is LangType.Pending, is ErrorLangType -> resolveWith(obj)
                                is ResolvedConstraintLangType -> resolveWith(ErrorLangType.ImplementationTodo("Can't get members of a type"))

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
                                            is LangType.Pending -> {}
                                            is ErrorLangType -> resolveWithProxyError(
                                                constraint, node.objectExpression
                                            )

                                            is ResolvedConstraintLangType -> resolveWith(constraint.toInstanceType())
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
                            resolveWith(getResolvedTypeOf(node.expression).asValue())
                        }

                        is StatementNode.EffectStatement -> {
                            val resultType = run result@{
                                val conditionType = getResolvedTypeOf(node.pattern).asValue().let {
                                    if (it is InstanceValueLangType && it.canonicalType is CanonicalLangType.SignalCanonicalLangType) {
                                        it.canonicalType
                                    } else {
                                        when (it) {
                                            is LangType.Pending, is ErrorLangType -> {
                                                resolveWith(it)
                                                return@eachPendingNode
                                            }

                                            is AnySignalValueLangType -> {
                                                return@result AnythingConstraintLangType
                                            }

                                            is ResolvedValueLangType -> {
                                                resolveWith(ErrorLangType.NotCausable)
                                                return@eachPendingNode
                                            }
                                        }
                                    }
                                }
                                val resultType = when (val result = conditionType.result) {
                                    is LangType.Pending -> return@eachPendingNode
                                    is ErrorLangType -> {
                                        resolveWithProxyError(result, node.pattern.typeReference)
                                        return@eachPendingNode
                                    }

                                    is ResolvedConstraintLangType -> result
                                }
                                resultType
                            }

                            iterationResolvedReferences.add(
                                ResolutionKey(
                                    CONSTRAINT,
                                    node.body.info.breadcrumbs
                                ) to resultType
                            )

                            val bodyResult = getResolvedTypeOf(node.body).asValue().let {
                                when (it) {
                                    is LangType.Pending, is ErrorLangType -> {
                                        resolveWith(it)
                                        return@eachPendingNode
                                    }

                                    is ResolvedValueLangType -> it
                                }
                            }

                            iterationResolvedReferences.add(
                                ResolutionKey(
                                    INFERRED,
                                    node.body.info.breadcrumbs
                                ) to bodyResult
                            )

                            resolveWith(LangPrimitiveKind.ACTION.toValueLangType())
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

                            val expectedType = when (val variableValue = getResolvedTypeOf(variable).asValue()) {
                                is LangType.Pending, is ErrorLangType -> {
                                    resolveWith(variableValue)
                                    return@eachPendingNode
                                }

                                is ResolvedValueLangType -> variableValue.toConstraint()
                            }

                            val newValue = when (val newValue = getResolvedTypeOf(node.expression).asValue()) {
                                is LangType.Pending, is ErrorLangType -> {
                                    resolveWith(newValue)
                                    return@eachPendingNode
                                }

                                is ResolvedValueLangType -> newValue
                            }

                            if (newValue.isAssignableTo(expectedType)) {
                                resolveWith(LangPrimitiveKind.ACTION.toValueLangType())
                            } else {
                                resolveWith(ErrorLangType.MismatchedType(expectedType, newValue))
                            }
                        }

                        is BodyNode.BlockBodyNode -> {
                            val lastStatement = node.statements.lastOrNull()
                            if (lastStatement is StatementNode.ExpressionStatement) {
                                resolveWith(getResolvedTypeOf(lastStatement).asValue())
                            } else {
                                resolveWith(LangPrimitiveKind.ACTION.toValueLangType())
                            }
                        }

                        is BodyNode.SingleExpressionBodyNode -> resolveWith(getResolvedTypeOf(node.expression))

                        is DeclarationNode.NamedValue -> resolveWith(getResolvedTypeOf(node.value))

                        is DeclarationNode.ObjectType -> {
                            val id = CanonicalLangTypeId(path, null, node.name.text, 0u)
                            // TODO: increment number to resolve dupes
                            val fields = node.fields?.map { field ->
                                val fieldType = getResolvedTypeOf(field.typeConstraint).asConstraint()
                                CanonicalLangType.ObjectField(field.name.text, fieldType)
                            } ?: emptyList()
                            val objectType = CanonicalLangType.ObjectCanonicalLangType(id, node.name.text, fields)

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

                        is DeclarationNode.SignalType -> {
                            val id = CanonicalLangTypeId(path, null, node.name.text, 0u)
                            // TODO: increment number to resolve dupes
                            val fields = node.fields?.map { field ->
                                val fieldType = getResolvedTypeOf(field.typeConstraint).asConstraint()
                                CanonicalLangType.ObjectField(field.name.text, fieldType)
                            } ?: emptyList()
                            val result = getResolvedTypeOf(node.result).asConstraint()

                            val signalType =
                                CanonicalLangType.SignalCanonicalLangType(id, node.name.text, fields, result)

                            val existingKnownType = knownCanonicalTypes[id]
                            if (existingKnownType != null && !existingKnownType.isPending()) {
                                error("Accidentally clobbered canonical type: $existingKnownType with $signalType.")
                            }

                            knownCanonicalTypes[id] = signalType
                            resolveWith(TypeReferenceConstraintLangType(signalType))
                        }

                        is DeclarationNode.OptionType -> {
                            val options = node.options.map {
                                getResolvedTypeOf(it).asConstraint()
                            }
                            resolveWith(OptionConstraintLangType(options))
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
                                is LangType.Pending -> LangType.Pending
                                is ErrorLangType -> ErrorLangType.ProxyError.from(
                                    returnType, getSourcePosition(canReturn[0].returnExpression)
                                )

                                is ResolvedValueLangType -> returnType.toConstraint()
                                is ResolvedConstraintLangType -> ErrorLangType.ConstraintUsedAsValue(returnType)
                            }
                            val params = node.params.map { paramNode ->
                                val typeReference =
                                    paramNode.typeReference?.let { getResolvedTypeOf(it) } ?: LangType.Pending
                                LangParameter(paramNode.name.text, typeReference.asConstraint())
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

                            resolveWith(getResolvedTypeOf(annotation).asConstraint())
                        }

                        is DeclarationNode.Function.FunctionParameterNode -> {
                            node.typeReference?.let { resolveWith(getResolvedTypeOf(it).asConstraint()) }
                        }

                        is ExpressionNode.CallExpression.ParameterNode -> {
                            val paramTag = pendingNodeTags.firstNotNullOf { it as? NodeTag.ParameterForCall }
                            val (callBreadcrumbs, index) = paramTag
                            val call = fileNode.findNode(callBreadcrumbs) as ExpressionNode.CallExpression
                            when (val callType = getResolvedTypeOf(call.callee)) {
                                is LangType.Pending, is ErrorLangType -> resolveWith(callType)
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
                            }
                        }

                        is PatternNode -> {
                            resolveWith(getResolvedTypeOf(node.typeReference).asConstraint())
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