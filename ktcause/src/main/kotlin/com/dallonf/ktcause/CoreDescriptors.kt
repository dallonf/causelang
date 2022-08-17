package com.dallonf.ktcause

import com.dallonf.ktcause.types.*

object CoreDescriptors {
    val coreBuiltinFile by lazy {
        val filename = "core/builtin.cau"

        val types = mapOf(
            CanonicalLangType.SignalCanonicalLangType(
                CanonicalLangTypeId(filename, name = "Debug", number = 0u), name = "Debug", fields = listOf(
                    CanonicalLangType.ObjectField("message", LangPrimitiveKind.STRING.toConstraintLangType())
                ), result = LangPrimitiveKind.ACTION.toConstraintLangType()
            ).toPair(), CanonicalLangType.SignalCanonicalLangType(
                CanonicalLangTypeId(filename, name = "TypeError", number = 0u),
                name = "TypeError",
                fields = listOf(CanonicalLangType.ObjectField("badValue", BadValueConstraintLangType)),
                result = NeverContinuesConstraintLangType
            ).toPair(), CanonicalLangType.SignalCanonicalLangType(
                CanonicalLangTypeId(filename, name = "AssumptionBroken", number = 0.toUByte()),
                name = "AssumptionBroken",
                fields = listOf(
                    CanonicalLangType.ObjectField(
                        "message", LangPrimitiveKind.STRING.toConstraintLangType()
                    )
                ),
                result = NeverContinuesConstraintLangType
            ).toPair()
        )

        val descriptor = Resolver.ExternalFileDescriptor(exports = buildMap {
            put("String", LangPrimitiveKind.STRING.toConstraintLangType())
            put("Integer", LangPrimitiveKind.INTEGER.toConstraintLangType())
            put("Float", LangPrimitiveKind.FLOAT.toConstraintLangType())
            put("Boolean", LangPrimitiveKind.BOOLEAN.toConstraintLangType())
            put("True", LangPrimitiveKind.BOOLEAN.toValueLangType())
            put("False", LangPrimitiveKind.BOOLEAN.toValueLangType())
            put("Action", LangPrimitiveKind.ACTION.toConstraintLangType())

            for ((id, type) in types.filter { it.key.name != null && it.key.parentName == null }) {
                put(id.name!!, TypeReferenceConstraintLangType(type))
            }
        }, types = types)


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
                            name = "append", params = listOf(
                                LangParameter("this", LangPrimitiveKind.STRING.toConstraintLangType()),
                                LangParameter("other", LangPrimitiveKind.STRING.toConstraintLangType()),
                            ), returnConstraint = LangPrimitiveKind.STRING.toConstraintLangType()
                        ), "equals" to FunctionValueLangType(
                            name = "stringEquals", params = listOf(
                                LangParameter("this", LangPrimitiveKind.STRING.toConstraintLangType()),
                                LangParameter("other", LangPrimitiveKind.STRING.toConstraintLangType()),
                            ), returnConstraint = LangPrimitiveKind.BOOLEAN.toConstraintLangType()
                        ),
                        "integer_to_string" to FunctionValueLangType(
                            name = "integer_to_string", params = listOf(
                                LangParameter("this", LangPrimitiveKind.INTEGER.toConstraintLangType()),
                            ), returnConstraint = LangPrimitiveKind.STRING.toConstraintLangType()
                        )
                    ), types = mapOf()
                )

            )
        }

        result.toList()
    }
}