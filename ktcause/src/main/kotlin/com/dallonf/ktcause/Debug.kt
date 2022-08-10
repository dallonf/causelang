package com.dallonf.ktcause

import com.dallonf.ktcause.types.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal object Debug {
    val serializersModule = SerializersModule {
        fun PolymorphicModuleBuilder<ResolvedValueLangType>.registerResolvedValueLangTypeSubclasses() {
            subclass(FunctionValueLangType::class)
            subclass(PrimitiveValueLangType::class)
            subclass(InstanceValueLangType::class)
            subclass(BadValueLangType::class)
            subclass(NeverContinuesValueLangType::class)
        }

        fun PolymorphicModuleBuilder<ResolvedConstraintLangType>.registerResolvedConstraintLangTypeSubclasses() {
            subclass(FunctionConstraintLangType::class)
            subclass(PrimitiveConstraintLangType::class)
            subclass(TypeReferenceConstraintLangType::class)
            subclass(BadValueConstraintLangType::class)
            subclass(NeverContinuesConstraintLangType::class)
        }

        fun PolymorphicModuleBuilder<ErrorLangType>.registerErrorValueLangTypeSubclasses() {
            subclass(ErrorLangType.NeverResolved::class)
            subclass(ErrorLangType.NotInScope::class)
            subclass(ErrorLangType.FileNotFound::class)
            subclass(ErrorLangType.ExportNotFound::class)
            subclass(ErrorLangType.ProxyError::class)
            subclass(ErrorLangType.NotCallable::class)
            subclass(ErrorLangType.NotCausable::class)
            subclass(ErrorLangType.ImplementationTodo::class)
            subclass(ErrorLangType.MismatchedType::class)
            subclass(ErrorLangType.MissingParameters::class)
            subclass(ErrorLangType.ExcessParameter::class)
            subclass(ErrorLangType.UnknownParameter::class)
            subclass(ErrorLangType.ConstraintUsedAsValue::class)
            subclass(ErrorLangType.ValueUsedAsConstraint::class)
        }

        // TODO: in 17.20 you'll be able to just pop @Serializable on a sealed interface
        // (which ValueLangType is)
        polymorphic(ErrorLangType::class) {
            registerErrorValueLangTypeSubclasses()
        }

        polymorphic(ResolvedValueLangType::class) {
            registerResolvedValueLangTypeSubclasses()
        }

        polymorphic(ResolvedConstraintLangType::class) {
            registerResolvedConstraintLangTypeSubclasses()
        }

        polymorphic(LangType::class) {
            registerErrorValueLangTypeSubclasses()
            registerResolvedValueLangTypeSubclasses()
            registerResolvedConstraintLangTypeSubclasses()
            subclass(LangType.Pending::class)
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
}