package com.dallonf.ktcause.types

import com.dallonf.ktcause.Debug
import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.Debug.debugMini
import com.dallonf.ktcause.ast.SourcePosition
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure

@Serializable(with = CanonicalLangTypeIdSerializer::class)
data class CanonicalLangTypeId(
    val path: String,
    val parentName: String? = null,
    val name: String?,
    val number: UInt,
) {
    override fun toString(): String {
        val name = name ?: "<anonymous>"
        val numberIfApplicable = if (number == 0.toUInt()) "" else number

        return if (parentName != null) {
            "${path}:${parentName}_${name}${numberIfApplicable}"
        } else {
            "${path}:${name}${numberIfApplicable}"
        }
    }
}

class CanonicalLangTypeIdSerializer : KSerializer<CanonicalLangTypeId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("CanonicalLangTypeId", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: CanonicalLangTypeId) = encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): CanonicalLangTypeId {
        TODO("Not yet implemented")
    }
}


@Serializable
sealed interface CanonicalLangType {
    val id: CanonicalLangTypeId

    fun isPending(): Boolean
    fun getError(): ErrorLangType?

    fun isUnique(): Boolean

    @Serializable
    @SerialName("Signal")
    data class SignalCanonicalLangType(
        override val id: CanonicalLangTypeId,
        val name: String,
        var fields: List<ObjectField>,
        var result: ConstraintReference,
    ) : CanonicalLangType {
        override fun isUnique() = fields.isEmpty()

        @Transient
        private var recursivePending: Int = 0
        override fun isPending(): Boolean {
            if (recursivePending > 0) return false
            recursivePending += 1
            return (result.isPending() || fields.any { it.valueConstraint.isPending() }).also {
                recursivePending -= 1
            }
        }

        @Transient
        private var recursiveError: Int = 0

        override fun getError(): ErrorLangType? {
            if (recursiveError > 0) return null
            recursiveError += 1
            return (result.getError()
                ?: fields.firstNotNullOfOrNull { it.valueConstraint.getError() }).also { recursiveError -= 1 }
        }
    }

    @Serializable
    @SerialName("Object")
    data class ObjectCanonicalLangType(
        override val id: CanonicalLangTypeId, var name: String, var fields: List<ObjectField>
    ) : CanonicalLangType {

        override fun isUnique() = fields.isEmpty()

        @Transient
        private var recursivePending: Int = 0
        override fun isPending(): Boolean {
            if (recursivePending > 0) return false
            recursivePending += 1
            return fields.any { it.valueConstraint.isPending() }.also {
                recursivePending -= 1
            }
        }

        @Transient
        private var recursiveError: Int = 0

        override fun getError(): ErrorLangType? {
            if (recursiveError > 0) return null
            recursiveError += 1
            return fields.firstNotNullOfOrNull { it.valueConstraint.getError() }.also { recursiveError -= 1 }
        }
    }

    @Serializable
    data class ObjectField(val name: String, val valueConstraint: ConstraintReference) {
        fun asLangParameter() = LangParameter(name, valueConstraint)
    }

    fun asConstraintReference() = ConstraintReference.ResolvedConstraint(InstanceValueLangType(this))
}

@Serializable
data class LangParameter(
    val name: String,
    val valueConstraint: ConstraintReference,
)

sealed interface ValueLangType {
    @Serializable
    @SerialName("Pending")
    object Pending : ValueLangType {
        override fun isPending() = true
    }

    fun isPending(): Boolean

    fun getRuntimeError(): ErrorLangType? = when (this) {
        is ErrorLangType -> this
        Pending -> ErrorLangType.NeverResolved
        is ResolvedValueLangType -> null
    }

    fun getError(): ErrorLangType? = null

    fun expectConstraint(): ValueLangType {
        return when (this) {
            is ConstraintValueLangType, is Pending, is ErrorLangType -> this
            is ResolvedValueLangType -> ErrorLangType.ValueUsedAsConstraint(this)
        }
    }

    fun asConstraintReference(): ConstraintReference {
        return when (this) {
            is Pending -> ConstraintReference.Pending
            is ErrorLangType -> ConstraintReference.Error(this)
            is ConstraintValueLangType -> ConstraintReference.ResolvedConstraint(this.valueType)
            is ResolvedValueLangType -> ConstraintReference.Error(ErrorLangType.ValueUsedAsConstraint(this))
        }
    }

    fun valueToConstraintReference(): ConstraintReference {
        return this.letIfResolved { it.toConstraint() }.asConstraintReference()
    }

    fun <T> letIfResolved(f: (ResolvedValueLangType) -> T, otherwise: T): T {
        return if (this is ResolvedValueLangType) {
            f(this)
        } else {
            otherwise
        }
    }

    fun letIfResolved(f: (ResolvedValueLangType) -> ValueLangType): ValueLangType {
        return letIfResolved(f, this)
    }

    fun letIfError(f: (ErrorLangType) -> ValueLangType): ValueLangType {
        return if (this is ErrorLangType) {
            f(this)
        } else {
            this
        }
    }
}


sealed interface ErrorLangType : ValueLangType {
    override fun isPending() = false

    override fun getError() = this

    fun friendlyMessage(ctx: Debug.DebugContext? = null): String

    @Serializable
    @SerialName("NeverResolved")
    object NeverResolved : ErrorLangType {
        override fun friendlyMessage(ctx: Debug.DebugContext?) =
            "I couldn't figure out what type this is.\n" + "There might be a circular loop somewhere that you can resolve by adding a type annotation."
    }

    @Serializable
    @SerialName("NotInScope")
    object NotInScope : ErrorLangType {
        override fun friendlyMessage(ctx: Debug.DebugContext?) = "I can't find anything with this name in scope."
    }

    @Serializable
    @SerialName("FileNotFound")
    object FileNotFound : ErrorLangType {
        override fun friendlyMessage(ctx: Debug.DebugContext?) = "I can't find this file."
    }

    @Serializable
    @SerialName("ImportPathInvalid")
    object ImportPathInvalid : ErrorLangType {
        override fun friendlyMessage(ctx: Debug.DebugContext?): String =
            "This isn't a file path I understand.\n" + "If it's a relative path that goes up with \"..\", it might be going up too far."
    }

    @Serializable
    @SerialName("ExportNotFound")
    object ExportNotFound : ErrorLangType {
        override fun friendlyMessage(ctx: Debug.DebugContext?) = "I can't find an export by that name in this file."
    }

    @Serializable
    @SerialName("ProxyError")
    data class ProxyError(val actualError: ErrorLangType, val proxyChain: List<SourcePosition>) : ErrorLangType {
        companion object {
            fun from(error: ErrorLangType, source: SourcePosition?): ProxyError {
                val sourceList = source?.let { listOf(it) } ?: emptyList()
                return if (error is ProxyError) {
                    ProxyError(
                        error.actualError, sourceList + error.proxyChain
                    )
                } else {
                    ProxyError(
                        error, sourceList
                    )
                }
            }
        }

        override fun friendlyMessage(ctx: Debug.DebugContext?): String {
            val summary = "I can't use this value because of an earlier error:"
            val chain = proxyChain.joinToString("\n") {
                val position = when (it) {
                    is SourcePosition.Export -> "${it.path} (${it.exportName})"
                    is SourcePosition.Source -> "${it.path} line ${it.position.start}"
                }
                "  at $position"
            }
            val previousError = actualError.friendlyMessage(ctx)
            return "$summary\n$chain\n$previousError"
        }
    }

    @Serializable
    @SerialName("NotCallable")
    object NotCallable : ErrorLangType {
        override fun friendlyMessage(ctx: Debug.DebugContext?) =
            "I was expecting a function or a type here; I can't call this as-is."
    }

    @Serializable
    @SerialName("NotCausable")
    object NotCausable : ErrorLangType {
        override fun friendlyMessage(ctx: Debug.DebugContext?) =
            "I was expecting a signal here; I can't cause this as-is."
    }

    @Serializable
    @SerialName("ImplementationTodo")
    data class ImplementationTodo(val description: String) : ErrorLangType {
        override fun friendlyMessage(ctx: Debug.DebugContext?) =
            "I don't support this feature yet. Details: $description"
    }

    @Serializable
    @SerialName("MismatchedType")
    data class MismatchedType(val expected: ConstraintValueLangType, val actual: ResolvedValueLangType) :
        ErrorLangType {
        override fun friendlyMessage(ctx: Debug.DebugContext?): String {
            val summary = "I was expecting this to be a ${expected.debugMini()}, but it was a ${actual.debugMini()}"

            return "$summary\n" + "Expected type details: ${expected.debug()}\n" + "Actual type details: ${actual.debug()}\n"
        }
    }

    @Serializable
    @SerialName("MissingParameters")

    data class MissingParameters(val names: List<String>) : ErrorLangType {
        override fun friendlyMessage(ctx: Debug.DebugContext?) =
            "I was expecting more parameters; I'm missing: ${names.joinToString(", ")}."
    }

    @Serializable
    @SerialName("ExcessParameters")
    data class ExcessParameters(val expected: Int) : ErrorLangType {
        override fun friendlyMessage(ctx: Debug.DebugContext?) =
            "There are too many parameters here, I was only expecting $expected."
    }

    @Serializable
    @SerialName("UnknownParameter")
    object UnknownParameter : ErrorLangType {
        override fun friendlyMessage(ctx: Debug.DebugContext?) =
            "I wasn't expecting this parameter. Does it have the right name?"
    }

    @Serializable
    @SerialName("MissingElseBranch")
    data class MissingElseBranch(val options: OptionValueLangType?) : ErrorLangType {
        override fun friendlyMessage(ctx: Debug.DebugContext?) =
            "Not all possibilities of this branch are covered. You might need to add an \"else\" condition."
    }

    @Serializable
    @SerialName("UnreachableBranch")
    data class UnreachableBranch(val options: OptionValueLangType?) : ErrorLangType {
        override fun friendlyMessage(ctx: Debug.DebugContext?) =
            "I can never reach this condition because of the conditions above it."
    }

    @Serializable
    @SerialName("ActionIncompatibleWithValueTypes")
    data class ActionIncompatibleWithValueTypes(
        val actions: List<SourcePosition.Source>, val types: List<ValueType>
    ) : ErrorLangType {
        @Serializable
        data class ValueType(val type: ValueLangType, val position: SourcePosition.Source)

        override fun friendlyMessage(ctx: Debug.DebugContext?): String {
            return "Some code paths return as an Action: ${
                actions.joinToString(", ") { "line ${it.position.start}" }
            }\n" + "but others return a value:\n" + types.joinToString("\n") {
                "  ${it.type.debugMini()} at line ${it.position.position.start}"
            }
        }
    }

    @Serializable
    @SerialName("ConstraintUsedAsValue")
    data class ConstraintUsedAsValue(val type: ConstraintValueLangType) : ErrorLangType {
        override fun friendlyMessage(ctx: Debug.DebugContext?): String =
            "${type.debugMini()} is a type constraint, but it's used here like a value."
    }

    @Serializable
    @SerialName("ValueUsedAsConstraint")
    data class ValueUsedAsConstraint(val type: ValueLangType) : ErrorLangType {
        override fun friendlyMessage(ctx: Debug.DebugContext?): String =
            "This ${type.debugMini()} is a value, but it's used like here like a type constraint."
    }

    @Serializable
    @SerialName("DoesNotHaveAnyMembers")
    object DoesNotHaveAnyMembers : ErrorLangType {
        override fun friendlyMessage(ctx: Debug.DebugContext?) = "I can't use \".\" to get members of this value."
    }

    @Serializable
    @SerialName("DoesNotHaveMember")
    object DoesNotHaveMember : ErrorLangType {
        override fun friendlyMessage(ctx: Debug.DebugContext?) = "I can't find a member by that name on this object."
    }

    @Serializable
    @SerialName("NotVariable")
    object NotVariable : ErrorLangType {
        override fun friendlyMessage(ctx: Debug.DebugContext?) =
            "I can't set this because it's not a variable. You can add \"variable\" to its definition, like \"let variable x = ...\"."
    }

    @Serializable
    @SerialName("OuterVariable")
    object OuterVariable : ErrorLangType {
        override fun friendlyMessage(ctx: Debug.DebugContext?) =
            "I can't use this value here because it's a variable in an outer function."
    }

    @Serializable
    @SerialName("CannotBreakHere")
    object CannotBreakHere : ErrorLangType {
        override fun friendlyMessage(ctx: Debug.DebugContext?) =
            "I don't understand what \"break\" means here, because it's not inside a loop."
    }
}

sealed interface ResolvedValueLangType : ValueLangType {
    override fun isPending() = false

    fun isAssignableTo(constraint: ConstraintValueLangType): Boolean {
        if (this is NeverContinuesValueLangType) return true

        return when (val constraintInstanceType = constraint.valueType) {
            is InstanceValueLangType -> {
                this is InstanceValueLangType && this.canonicalType.id == constraintInstanceType.canonicalType.id ||
                        // handle unique types
                        (this is ConstraintValueLangType && this.tryGetCanonicalType()
                            ?.let { it.isUnique() && it.id == constraintInstanceType.canonicalType.id } ?: false)
            }

            is FunctionValueLangType -> {
                this is FunctionValueLangType && run {
                    val paramsMatch =
                        this.params.size == constraintInstanceType.params.size && this.params.zip(constraintInstanceType.params)
                            .all { (thisParam, constraintParam) ->
                                // Names can be different, but types can't be
                                // at least until we work out variance
                                thisParam.valueConstraint == constraintParam.valueConstraint
                            }
                    val returnConstraintMatches = run {
                        val returnValue = this.returnConstraint.asValueType() as? ResolvedValueLangType
                        val constraintReturnConstraint =
                            constraintInstanceType.returnConstraint.asConstraintValue() as? ConstraintValueLangType

                        returnValue != null && constraintReturnConstraint != null && returnValue.isAssignableTo(
                            constraintReturnConstraint
                        )
                    }

                    paramsMatch && returnConstraintMatches
                }
            }

            is PrimitiveValueLangType -> this is PrimitiveValueLangType && this.kind == constraintInstanceType.kind
            is OptionValueLangType -> constraintInstanceType.isSupersetOf(this)

            StopgapDictionaryLangType -> this is StopgapDictionaryLangType
            StopgapListLangType -> this is StopgapListLangType

            AnySignalValueLangType -> this is InstanceValueLangType && this.canonicalType is CanonicalLangType.SignalCanonicalLangType
            AnythingValueLangType -> true

            ActionValueLangType -> this is ActionValueLangType
            BadValueLangType -> this is BadValueLangType
            NeverContinuesValueLangType -> this is NeverContinuesValueLangType

            is ConstraintValueLangType -> this is ConstraintValueLangType && this.valueType.isAssignableTo(
                constraintInstanceType
            )
        }
    }

    fun toConstraint() = ConstraintValueLangType(this)
}

@Serializable
@SerialName("Constraint")
data class ConstraintValueLangType(val valueType: ResolvedValueLangType) : ResolvedValueLangType {
    override fun isPending() = valueType.isPending()

    fun tryGetCanonicalType(): CanonicalLangType? {
        return if (valueType is InstanceValueLangType) {
            valueType.canonicalType
        } else {
            null
        }
    }
}

@Serializable
sealed class ConstraintReference {
    @Serializable
    @SerialName("Resolved")
    data class ResolvedConstraint(val valueType: ResolvedValueLangType) : ConstraintReference() {
        fun asResolvedConstraintValue() = valueType.toConstraint()
    }

    @Serializable
    @SerialName("Pending")
    object Pending : ConstraintReference()

    @Serializable
    @SerialName("Error")
    data class Error(val errorType: ErrorLangType) : ConstraintReference()

    fun isPending() = when (this) {
        is Pending -> true
        is ResolvedConstraint -> this.valueType.isPending()
        else -> false
    }


    fun getError() = when (this) {
        is Error -> this.errorType
        is ResolvedConstraint -> this.valueType.getError()
        else -> null
    }

    fun asConstraintValue() = when (this) {
        is ResolvedConstraint -> this.asResolvedConstraintValue()
        is Pending -> ValueLangType.Pending
        is Error -> this.errorType
    }

    fun asValueType() = when (this) {
        is ResolvedConstraint -> this.valueType
        is Pending -> ValueLangType.Pending
        is Error -> this.errorType
    }
}

@Serializable
@SerialName("Function")
data class FunctionValueLangType(
    val name: String?, val returnConstraint: ConstraintReference, val params: List<LangParameter>
) : ResolvedValueLangType {
    override fun isPending(): Boolean = returnConstraint.isPending() || params.any() { it.valueConstraint.isPending() }

    override fun getError(): ErrorLangType? =
        returnConstraint.getError() ?: params.firstNotNullOfOrNull { it.valueConstraint.getError() }
}

@Serializable
enum class LangPrimitiveKind() {
    @SerialName("Text")
    TEXT,

    @SerialName("Number")
    NUMBER;

    fun toValueLangType() = PrimitiveValueLangType(this)
    fun toConstraintLangType() = ConstraintValueLangType(toValueLangType())

    fun toConstraintReference() = toConstraintLangType().asConstraintReference()
}

@Serializable
@SerialName("Primitive")
data class PrimitiveValueLangType(val kind: LangPrimitiveKind) : ResolvedValueLangType

@Serializable
@SerialName("StopgapDictionary")
object StopgapDictionaryLangType : ResolvedValueLangType

@Serializable
@SerialName("StopgapList")
object StopgapListLangType : ResolvedValueLangType

@Serializable(with = InstanceValueLangType.InstanceValueLangTypeSerializer::class)
data class InstanceValueLangType(val canonicalType: CanonicalLangType) : ResolvedValueLangType {

    override fun isPending() = canonicalType.isPending()
    override fun getError() = canonicalType.getError()

    object InstanceValueLangTypeSerializer : KSerializer<InstanceValueLangType> {
        override val descriptor: SerialDescriptor
            get() = buildClassSerialDescriptor("Instance") {
                element("canonicalType", CanonicalLangTypeId.serializer().descriptor)
            }

        override fun serialize(encoder: Encoder, value: InstanceValueLangType) {
            encoder.encodeStructure(
                descriptor
            ) {
                encodeSerializableElement(
                    descriptor, 0, CanonicalLangTypeId.serializer(), value.canonicalType.id
                )
            }
        }

        override fun deserialize(decoder: Decoder): InstanceValueLangType {
            TODO("Not yet implemented")
        }
    }
}

@Serializable
@SerialName("Option")
data class OptionValueLangType(val options: List<ConstraintReference>) : ResolvedValueLangType {

    companion object {
        fun from(type: ValueLangType): OptionValueLangType {
            return if (type is OptionValueLangType) {
                type
            } else {
                OptionValueLangType(listOf(type.valueToConstraintReference()))
            }
        }

        private fun isMergeable(lessSpecific: ConstraintReference, moreSpecific: ConstraintReference): Boolean {
            if (lessSpecific == moreSpecific) return true

            val type = moreSpecific.asConstraintValue() as? ConstraintValueLangType
            val value = lessSpecific.asValueType() as? ResolvedValueLangType

            return type != null && value != null && value.isAssignableTo(type)
        }
    }

    override fun isPending() = options.any { it.isPending() }
    override fun getError() = options.firstNotNullOfOrNull { it.getError() }

    fun isSupersetOf(otherType: ResolvedValueLangType): Boolean {
        val possibleValues = when (otherType) {
            is OptionValueLangType -> otherType.simplify().options.map { option -> option.asValueType() }
            else -> listOf(otherType)
        }

        var pendingTrue = false
        val result = possibleValues.all { possibleValue ->
            when (possibleValue) {
                is ValueLangType.Pending -> {
                    pendingTrue = true
                    true
                }

                is ErrorLangType -> false
                is ResolvedValueLangType -> options.any { option ->
                    if (option is ConstraintReference.ResolvedConstraint) possibleValue.isAssignableTo(option.asResolvedConstraintValue()) else false
                }
            }
        }
        if (result && pendingTrue) {
            error("Value is pending; can't tell if it was truly assignable")
        }
        return result
    }


    fun simplify(): OptionValueLangType {
        var allPossibleConstraints = options.flatMap {
            when (val value = it.asValueType()) {
                is OptionValueLangType -> value.simplify().options.asSequence()
                else -> sequenceOf(it)
            }
        }

        allPossibleConstraints = if (allPossibleConstraints.size > 1) {
            val notDuplicated = mutableListOf<ConstraintReference>()
            for (possibleConstraint in allPossibleConstraints) {
                if (possibleConstraint !is ConstraintReference.ResolvedConstraint) {
                    notDuplicated.add(possibleConstraint)
                    continue
                }

                val isDuplicate = notDuplicated.any { existingConstraint ->
                    isMergeable(possibleConstraint, existingConstraint)
                }
                if (!isDuplicate) {
                    val redundantExistingTypes = notDuplicated.filter { existingConstraint ->
                        isMergeable(existingConstraint, possibleConstraint)
                    }

                    notDuplicated.add(possibleConstraint)
                    for (redundant in redundantExistingTypes) {
                        notDuplicated.remove(redundant)
                    }
                }
            }
            notDuplicated
        } else {
            allPossibleConstraints
        }

        return OptionValueLangType(allPossibleConstraints)
    }

    fun simplifyToValue(): ValueLangType {
        val simplified = simplify()
        return if (simplified.options.size == 1) {
            simplified.options[0].asValueType()
        } else {
            simplified
        }
    }

    fun narrow(otherType: ValueLangType): OptionValueLangType {
        val possibleValues = when (otherType) {
            is OptionValueLangType -> otherType.simplify().options.map { option -> option.asValueType() }
            else -> listOf(otherType)
        }

        val remainingOptions = options.filter { option ->
            val optionValue = option.asValueType()
            if (optionValue is ResolvedValueLangType) {
                possibleValues.none { possibleValue ->
                    if (possibleValue is ResolvedValueLangType) {
                        optionValue.isAssignableTo(possibleValue.toConstraint())
                    } else {
                        // don't count error or pending
                        false
                    }
                }
            } else {
                // keep error and pending options around
                true
            }
        }

        return OptionValueLangType(remainingOptions)
    }

    fun expand(otherType: ValueLangType): OptionValueLangType {
        val newValues = options + listOf(otherType.letIfResolved { it.toConstraint() }.asConstraintReference())
        return OptionValueLangType(newValues).simplify()
    }

    fun isEmpty(): Boolean {
        return options.isEmpty()
    }

    fun proxyAllErrors(sourcePosition: SourcePosition.Source): ValueLangType {
        return if (this.getError() != null) {
            OptionValueLangType(this.options.map { option ->
                option.asConstraintValue().letIfError { ErrorLangType.ProxyError.from(it, sourcePosition) }
                    .asConstraintReference()
            })
        } else {
            this
        }
    }

}

@Serializable
@SerialName("Action")
object ActionValueLangType : ResolvedValueLangType

@Serializable
@SerialName("Anything")
object AnythingValueLangType : ResolvedValueLangType

@Serializable
@SerialName("AnySignal")
object AnySignalValueLangType : ResolvedValueLangType


@Serializable
@SerialName("BadValue")
object BadValueLangType : ResolvedValueLangType

@Serializable
@SerialName("NeverContinues")
object NeverContinuesValueLangType : ResolvedValueLangType

