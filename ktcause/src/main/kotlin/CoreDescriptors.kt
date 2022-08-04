package com.dallonf.ktcause

import com.dallonf.ktcause.types.*

object CoreDescriptors {
    val coreBuiltinFile by lazy {
        val filename = "core/builtin.cau"
        val descriptor = Resolver.ExternalFileDescriptor(
            exports = mapOf(
                "String" to PrimitiveTypeLangValueType(PrimitiveLangValueType.STRING),
                "Integer" to PrimitiveTypeLangValueType(PrimitiveLangValueType.INTEGER),
                "Float" to PrimitiveTypeLangValueType(PrimitiveLangValueType.FLOAT),
                "Action" to PrimitiveTypeLangValueType(PrimitiveLangValueType.ACTION),
                "Debug" to CanonicalLangType.SignalCanonicalLangType(
                    CanonicalLangTypeId(filename, name = "Debug", number = 0),
                    name = "Debug",
                    params = listOf(LangParameter("message", PrimitiveLangValueType.STRING)),
                    result = PrimitiveLangValueType.ACTION
                ),
                "TypeError" to CanonicalLangType.SignalCanonicalLangType(
                    CanonicalLangTypeId(filename, name = "TypeError", number = 0),
                    name = "TypeError",
                    params = listOf(LangParameter("message", BadLangValueType)),
                    result = NeverContinuesLangValueType
                ),
                "AssumptionBroken" to CanonicalLangType.SignalCanonicalLangType(
                    CanonicalLangTypeId(filename, name = "AssumptionBroken", number = 0),
                    name = "AssumptionBroken",
                    params = listOf(LangParameter("message", BadLangValueType)),
                    result = NeverContinuesLangValueType
                ),
            )
        )

        filename to descriptor
    }

    val coreFiles by lazy {
        val result = mutableListOf<Pair<String, Resolver.ExternalFileDescriptor>>()

        run {
            val filename = "core/string.cau"
            result.add(
                filename to Resolver.ExternalFileDescriptor(
                    exports = mapOf(
                        "append" to FunctionLangValueType(
                            name = "append",
                            params = listOf(
                                LangParameter("this", PrimitiveLangValueType.STRING),
                                LangParameter("other", PrimitiveLangValueType.STRING),
                            ),
                            returnType = PrimitiveLangValueType.STRING
                        )
                    )
                )
            )
        }

        result.toList()
    }
}