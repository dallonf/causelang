package com.dallonf.ktcause

import com.dallonf.ktcause.types.*

object CoreDescriptors {
    val coreBuiltinFile by lazy {
        val filename = "core/builtin.cau"
        val descriptor = Resolver.ExternalFileDescriptor(
            exports = mapOf(
                "String" to PrimitiveTypeValueLangType(PrimitiveValueLangType.STRING),
                "Integer" to PrimitiveTypeValueLangType(PrimitiveValueLangType.INTEGER),
                "Float" to PrimitiveTypeValueLangType(PrimitiveValueLangType.FLOAT),
                "Action" to PrimitiveTypeValueLangType(PrimitiveValueLangType.ACTION),
                "Debug" to CanonicalLangType.SignalCanonicalLangType(
                    CanonicalLangTypeId(filename, name = "Debug", number = 0.toUByte()),
                    name = "Debug",
                    params = listOf(LangParameter("message", PrimitiveValueLangType.STRING)),
                    result = PrimitiveValueLangType.ACTION
                ),
                "TypeError" to CanonicalLangType.SignalCanonicalLangType(
                    CanonicalLangTypeId(filename, name = "TypeError", number = 0.toUByte()),
                    name = "TypeError",
                    params = listOf(LangParameter("message", BadValueLangType)),
                    result = NeverContinuesValueLangType
                ),
                "AssumptionBroken" to CanonicalLangType.SignalCanonicalLangType(
                    CanonicalLangTypeId(filename, name = "AssumptionBroken", number = 0.toUByte()),
                    name = "AssumptionBroken",
                    params = listOf(LangParameter("message", BadValueLangType)),
                    result = NeverContinuesValueLangType
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
                        "append" to FunctionValueLangType(
                            name = "append",
                            params = listOf(
                                LangParameter("this", PrimitiveValueLangType.STRING),
                                LangParameter("other", PrimitiveValueLangType.STRING),
                            ),
                            returnType = PrimitiveValueLangType.STRING
                        )
                    )
                )
            )
        }

        result.toList()
    }
}