package com.dallonf.ktcause

import com.dallonf.ktcause.ast.Breadcrumbs
import com.dallonf.ktcause.types.CanonicalLangType
import com.dallonf.ktcause.types.ErrorValueLangType
import com.dallonf.ktcause.types.ValueLangType

sealed class RuntimeValue {
    object Action : RuntimeValue()
    data class BadValue(val filePath: kotlin.String, val breadcrumbs: Breadcrumbs, val error: ErrorValueLangType) :
        RuntimeValue()

    data class String(val value: kotlin.String) : RuntimeValue()
    data class Integer(val value: Long) : RuntimeValue()
    data class Float(val value: Double) : RuntimeValue()

    // TODO: probably want to make it harder to make an invalid RuntimeObject
    data class RuntimeObject(val typeDescriptor: RuntimeTypeReference, val values: List<RuntimeValue>) : RuntimeValue()

    data class RuntimeTypeReference(val type: CanonicalLangType) : RuntimeValue()

    // TODO: definitely don't want these to come from anywhere but the core modules
    data class NativeFunction(val name: kotlin.String, val function: (List<RuntimeValue>) -> RuntimeValue) : RuntimeValue()

    fun isAssignableTo(langType: ValueLangType): Boolean {
        // TODO: implement this!
        return true
    }

    fun validate(): RuntimeValue {
        return when (this) {
            is BadValue, is Action, is String, is Integer, is Float, is RuntimeTypeReference, is NativeFunction -> this
            is RuntimeObject -> TODO()
        }
    }

    fun isValid(): Boolean {
        return when (this) {
            is BadValue -> false
            is Action, is String, is Integer, is Float, is RuntimeTypeReference, is NativeFunction -> true
            is RuntimeObject -> this.values.all { it.isValid() }
        }
    }
}