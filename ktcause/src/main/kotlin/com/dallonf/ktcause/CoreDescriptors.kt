package com.dallonf.ktcause

import com.dallonf.ktcause.types.*

object CoreDescriptors {
    val coreBuiltinFile by lazy {
        val filename = "core/builtin.cau"

        val types = mapOf(
            CanonicalLangType.SignalCanonicalLangType(
                CanonicalLangTypeId(filename, name = "Debug", number = 0u), name = "Debug", fields = listOf(
                    CanonicalLangType.ObjectField("value", AnythingValueLangType.toConstraint().asConstraintReference())
                ), result = LangPrimitiveKind.ACTION.toConstraintLangType().asConstraintReference()
            ).toPair(), CanonicalLangType.SignalCanonicalLangType(
                CanonicalLangTypeId(filename, name = "TypeError", number = 0u),
                name = "TypeError",
                fields = listOf(
                    CanonicalLangType.ObjectField(
                        "badValue",
                        BadValueLangType.toConstraint().asConstraintReference()
                    )
                ),
                result = NeverContinuesValueLangType.toConstraint().asConstraintReference()
            ).toPair(), CanonicalLangType.SignalCanonicalLangType(
                CanonicalLangTypeId(filename, name = "AssumptionBroken", number = 0.toUByte()),
                name = "AssumptionBroken",
                fields = listOf(
                    CanonicalLangType.ObjectField(
                        "message", LangPrimitiveKind.STRING.toConstraintLangType().asConstraintReference()
                    )
                ),
                result = NeverContinuesValueLangType.toConstraint().asConstraintReference()
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
            put("Anything", AnythingValueLangType.toConstraint())
            put("AnySignal", AnySignalValueLangType.toConstraint())

            for ((id, type) in types.filter { it.key.name != null && it.key.parentName == null }) {
                put(id.name!!, InstanceValueLangType(type).toConstraint())
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
                            name = "append",
                            params = listOf(
                                LangParameter(
                                    "this",
                                    LangPrimitiveKind.STRING.toConstraintLangType().asConstraintReference()
                                ),
                                LangParameter(
                                    "other",
                                    LangPrimitiveKind.STRING.toConstraintLangType().asConstraintReference()
                                ),
                            ),
                            returnConstraint = LangPrimitiveKind.STRING.toConstraintLangType().asConstraintReference()
                        ), "equals" to FunctionValueLangType(
                            name = "stringEquals",
                            params = listOf(
                                LangParameter(
                                    "this",
                                    LangPrimitiveKind.STRING.toConstraintLangType().asConstraintReference()
                                ),
                                LangParameter(
                                    "other",
                                    LangPrimitiveKind.STRING.toConstraintLangType().asConstraintReference()
                                ),
                            ),
                            returnConstraint = LangPrimitiveKind.BOOLEAN.toConstraintLangType().asConstraintReference()
                        ),
                        "integer_to_string" to FunctionValueLangType(
                            name = "integer_to_string",
                            params = listOf(
                                LangParameter(
                                    "this",
                                    LangPrimitiveKind.INTEGER.toConstraintLangType().asConstraintReference()
                                ),
                            ),
                            returnConstraint = LangPrimitiveKind.STRING.toConstraintLangType().asConstraintReference()
                        )
                    ), types = mapOf()
                )

            )
        }

        run {
            val filename = "core/math.cau"
            result.add(
                filename to Resolver.ExternalFileDescriptor(
                    exports = mapOf(
                        "add" to FunctionValueLangType(
                            name = "add", params = listOf(
                                LangParameter(
                                    "this",
                                    LangPrimitiveKind.INTEGER.toConstraintLangType().asConstraintReference()
                                ),
                                LangParameter(
                                    "other",
                                    LangPrimitiveKind.INTEGER.toConstraintLangType().asConstraintReference()
                                ),
                            ),
                            returnConstraint = LangPrimitiveKind.INTEGER.toConstraintLangType().asConstraintReference()
                        )
                    ),
                    types = mapOf()
                )
            )
        }

        result.toList()
    }
}