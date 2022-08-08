package com.dallonf.ktcause

import com.dallonf.ktcause.types.ErrorValueLangType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal object Debug {
    val serializersModule = SerializersModule {
        // TODO: in 17.20 you'll be able to just pop @Serializable on a sealed interface
        // (which ValueLangType is)
        polymorphic(ErrorValueLangType::class) {
            subclass(ErrorValueLangType.NeverResolved::class)
            subclass(ErrorValueLangType.NotInScope::class)
            subclass(ErrorValueLangType.FileNotFound::class)
            subclass(ErrorValueLangType.ExportNotFound::class)
            subclass(ErrorValueLangType.ProxyError::class)
            subclass(ErrorValueLangType.NotCallable::class)
            subclass(ErrorValueLangType.NotCausable::class)
            subclass(ErrorValueLangType.ImplementationTodo::class)
            subclass(ErrorValueLangType.NotATypeReference::class)
            subclass(ErrorValueLangType.MismatchedType::class)
            subclass(ErrorValueLangType.MissingParameters::class)
            subclass(ErrorValueLangType.ExcessParameter::class)
            subclass(ErrorValueLangType.UnknownParameter::class)
        }
    }
    val debugSerializer by lazy {
        Json {
            prettyPrint = true
            serializersModule = Debug.serializersModule
        }
    }

    fun RuntimeValue.debug(): kotlin.String {
        return Debug.debugSerializer.encodeToString(this.toJson())
    }
}