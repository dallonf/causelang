package com.dallonf.ktcause

import com.dallonf.ktcause.ast.Breadcrumbs
import com.dallonf.ktcause.ast.FileNode
import com.dallonf.ktcause.types.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.math.max

object Debug {
    val serializersModule = SerializersModule {
        fun PolymorphicModuleBuilder<ResolvedValueLangType>.registerResolvedValueLangTypeSubclasses() {
            subclass(ActionValueLangType::class)
            subclass(FunctionValueLangType::class)
            subclass(PrimitiveValueLangType::class)
            subclass(InstanceValueLangType::class)
            subclass(StopgapDictionaryLangType::class)
            subclass(OptionValueLangType::class)
            subclass(AnythingValueLangType::class)
            subclass(AnySignalValueLangType::class)
            subclass(ConstraintValueLangType::class)
            subclass(BadValueLangType::class)
            subclass(NeverContinuesValueLangType::class)
        }

        fun PolymorphicModuleBuilder<ErrorLangType>.registerErrorValueLangTypeSubclasses() {
            subclass(ErrorLangType.NeverResolved::class)
            subclass(ErrorLangType.NotInScope::class)
            subclass(ErrorLangType.FileNotFound::class)
            subclass(ErrorLangType.ExportNotFound::class)
            subclass(ErrorLangType.ImportPathInvalid::class)
            subclass(ErrorLangType.ProxyError::class)
            subclass(ErrorLangType.NotCallable::class)
            subclass(ErrorLangType.NotCausable::class)
            subclass(ErrorLangType.ImplementationTodo::class)
            subclass(ErrorLangType.MismatchedType::class)
            subclass(ErrorLangType.MissingParameters::class)
            subclass(ErrorLangType.ExcessParameter::class)
            subclass(ErrorLangType.UnknownParameter::class)
            subclass(ErrorLangType.MissingElseBranch::class)
            subclass(ErrorLangType.UnreachableBranch::class)
            subclass(ErrorLangType.ActionIncompatibleWithValueTypes::class)
            subclass(ErrorLangType.ConstraintUsedAsValue::class)
            subclass(ErrorLangType.ValueUsedAsConstraint::class)
            subclass(ErrorLangType.DoesNotHaveAnyMembers::class)
            subclass(ErrorLangType.DoesNotHaveMember::class)
            subclass(ErrorLangType.NotVariable::class)
            subclass(ErrorLangType.OuterVariable::class)
            subclass(ErrorLangType.CannotBreakHere::class)
        }

        // TODO: in 17.20 you'll be able to just pop @Serializable on a sealed interface
        // (which ValueLangType is)
        polymorphic(ErrorLangType::class) {
            registerErrorValueLangTypeSubclasses()
        }

        polymorphic(ResolvedValueLangType::class) {
            registerResolvedValueLangTypeSubclasses()
        }

        polymorphic(ValueLangType::class) {
            registerErrorValueLangTypeSubclasses()
            registerResolvedValueLangTypeSubclasses()
            subclass(ValueLangType.Pending::class)
        }

        polymorphic(CanonicalLangType::class) {
            subclass(CanonicalLangType.ObjectCanonicalLangType::class)
            subclass(CanonicalLangType.SignalCanonicalLangType::class)
        }
    }
    val debugSerializer by lazy {
        Json {
            prettyPrint = true
            serializersModule = Debug.serializersModule

            classDiscriminator = "#type"
        }
    }

    fun RuntimeValue.debug(): kotlin.String {
        return Debug.debugSerializer.encodeToString(this.toJson())
    }

    fun ValueLangType.debug(): kotlin.String {
        return Debug.debugSerializer.encodeToString(this)
    }

    fun RuntimeValue.debugMini(): kotlin.String {
        return when (this) {
            is RuntimeValue.Action -> "[Action]"
            is RuntimeValue.BadValue -> "[BadValue: ${this.error::class.simpleName}]"
            is RuntimeValue.Function -> if (this.name != null) {
                "[Function: ${this.name}]"
            } else {
                "[Function]"
            }

            is RuntimeValue.NativeFunction -> "[NativeFunction: ${this.name}]"
            is RuntimeValue.Number -> this.value.toString()
            is RuntimeValue.RuntimeObject -> "[${this.typeDescriptor.id.name ?: "object"}]"

            is RuntimeValue.RuntimeTypeConstraint -> {
                val valueType = when (this.valueType) {
                    ActionValueLangType -> "Action"
                    AnySignalValueLangType -> "AnySignal"
                    AnythingValueLangType -> "Anything"
                    BadValueLangType -> "BadValue"
                    is ConstraintValueLangType -> "TypeConstraint"
                    is FunctionValueLangType -> "Function"
                    is InstanceValueLangType -> this.valueType.canonicalType.id.name ?: "object"
                    NeverContinuesValueLangType -> "NeverContinues"
                    is OptionValueLangType -> "Option"
                    is PrimitiveValueLangType -> when (this.valueType.kind) {
                        LangPrimitiveKind.TEXT -> "Text"
                        LangPrimitiveKind.NUMBER -> "Number"
                    }

                    StopgapDictionaryLangType -> "StopgapDictionary"
                }
                "[TypeConstraint: $valueType]"
            }

            is RuntimeValue.Text -> debugSerializer.encodeToString(this.toJson())

            is RuntimeValue.StopgapDictionary -> "[StopgapDictionary (${this.map.size})]"
        }
    }

    fun printCompileErrors(vm: LangVm) {
        val (files, compileErrors) = vm.codeBundle
        if (compileErrors.isNotEmpty()) {
            println("Compile errors:\n")
            for (error in compileErrors) {
                println(error.debug())
                val file = files[error.position.path]!!
                if (file.debugCtx != null) {
                    println(file.debugCtx.getNodeContext(error.position.breadcrumbs))
                } else {
                    println("Can't show error for ${file.path}")
                }
                println("------------------------------");
            }
        }
    }

    data class DebugContext(
        val source: String? = null,
        val ast: FileNode? = null,
        val analyzed: AnalyzedNode? = null,
        val resolved: ResolvedFile? = null
    ) {
        fun getSourceContext(breadcrumbs: Breadcrumbs): String? {
            val contextLines = 2
            val builder = StringBuilder()

            val node = ast?.findNode(breadcrumbs)
            val position = node?.info?.position

            if (position == null || source == null) {
                return null
            }

            val startLine = position.start.line
            val contextStartLine = max(startLine - contextLines, 0)
            for (line in source.lineSequence().drop(contextStartLine).take(startLine - contextStartLine)) {
                builder.appendLine(line)
            }
            val col = position.start.column
            val prefixLength = max(col - 1, 0)
            val prefix = (0 until prefixLength).asSequence().map { '-' }.joinToString("")
            builder.appendLine("$prefix^")
            for (line in source.lineSequence().drop(position.end.line).take(contextLines)) {
                builder.appendLine(line)
            }

            return builder.toString()
        }

        fun getNodeContext(breadcrumbs: Breadcrumbs): String {


            val builder = StringBuilder()

            val node = ast?.findNode(breadcrumbs)
            val position = node?.info?.position

            if (node != null && position != null) {
                builder.appendLine("${node::class.simpleName} at $position${resolved?.let { " in ${resolved.path}" } ?: ""}")
            }

            if (position != null && source != null) {
                builder.appendLine("```")
                builder.append(getSourceContext(breadcrumbs))
                builder.appendLine("```")
            }

            if (resolved != null) {
                builder.appendLine("Inferred type: ${
                    resolved.resolvedTypes[ResolutionKey(
                        ResolutionType.INFERRED, breadcrumbs
                    )]?.let { debugSerializer.encodeToString(it) }
                }")
                val constraint = resolved.resolvedTypes[ResolutionKey(
                    ResolutionType.CONSTRAINT, breadcrumbs
                )]
                if (constraint != null) {
                    builder.appendLine(
                        "Constraint type: ${
                            debugSerializer.encodeToString(constraint)
                        }"
                    )
                }
            }

            if (analyzed != null) {
                val tags = analyzed.nodeTags[breadcrumbs]
                if (!tags.isNullOrEmpty()) {
                    builder.appendLine("Tags for this node:")
                    for (tag in tags) {
                        builder.appendLine("- $tag")
                    }
                } else {
                    builder.appendLine("There aren't any tags for this!")
                }
            }

            return builder.toString()
        }

        fun getNodeContext(breadcrumbsString: String): String {
            return getNodeContext(Breadcrumbs.parse(breadcrumbsString))
        }
    }
}