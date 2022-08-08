package com.dallonf.ktcause

import com.dallonf.ktcause.types.*

object CoreDescriptors {
    val coreBuiltinFile by lazy {
        val filename = "core/builtin.cau"
        val descriptor = Resolver.ExternalFileDescriptor(
            exports = mapOf(
                "String" to LangPrimitiveKind.STRING.toTypeValueLangType(),
                "Integer" to LangPrimitiveKind.INTEGER.toTypeValueLangType(),
                "Float" to LangPrimitiveKind.FLOAT.toTypeValueLangType(),
                "Action" to LangPrimitiveKind.ACTION.toTypeValueLangType(),
                "Debug" to CanonicalLangType.SignalCanonicalLangType(
                    CanonicalLangTypeId(filename, name = "Debug", number = 0.toUByte()),
                    name = "Debug",
                    params = listOf(
                        LangParameter("message", LangPrimitiveKind.STRING.toValueLangType()),
                    ),
                    result = LangPrimitiveKind.ACTION.toValueLangType()
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
                                LangParameter("this", LangPrimitiveKind.STRING.toValueLangType()),
                                LangParameter("other", LangPrimitiveKind.STRING.toValueLangType()),
                            ),
                            returnType = LangPrimitiveKind.STRING.toValueLangType()
                        )
                    )
                )
            )
        }

        result.toList()
    }
}