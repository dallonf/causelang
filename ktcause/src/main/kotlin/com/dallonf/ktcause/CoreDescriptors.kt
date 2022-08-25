package com.dallonf.ktcause

import com.dallonf.ktcause.types.*

object CoreDescriptors {
    val coreBuiltinFile by lazy {
        val filename = "core/builtin.cau"

        val types = buildMap {
            add(
                CanonicalLangType.ObjectCanonicalLangType(
                    CanonicalLangTypeId(filename, name = "True", number = 0u),
                    name = "True",
                    fields = emptyList(),
                )
            )
            add(
                CanonicalLangType.ObjectCanonicalLangType(
                    CanonicalLangTypeId(filename, name = "False", number = 0u),
                    name = "False",
                    fields = emptyList(),
                )
            )

            add(
                CanonicalLangType.SignalCanonicalLangType(
                    CanonicalLangTypeId(filename, name = "Debug", number = 0u), name = "Debug", fields = listOf(
                        CanonicalLangType.ObjectField(
                            "value", AnythingValueLangType.toConstraint().asConstraintReference()
                        )
                    ), result = ActionValueLangType.toConstraint().asConstraintReference()
                )
            )
            add(
                CanonicalLangType.SignalCanonicalLangType(
                    CanonicalLangTypeId(filename, name = "TypeError", number = 0u), name = "TypeError", fields = listOf(
                        CanonicalLangType.ObjectField(
                            "badValue", BadValueLangType.toConstraint().asConstraintReference()
                        )
                    ), result = NeverContinuesValueLangType.toConstraint().asConstraintReference()
                )
            )
            add(
                CanonicalLangType.SignalCanonicalLangType(
                    CanonicalLangTypeId(filename, name = "AssumptionBroken", number = 0.toUByte()),
                    name = "AssumptionBroken",
                    fields = listOf(
                        CanonicalLangType.ObjectField(
                            "message", LangPrimitiveKind.STRING.toConstraintLangType().asConstraintReference()
                        )
                    ),
                    result = NeverContinuesValueLangType.toConstraint().asConstraintReference()
                )
            )
        }

        val trueType = types[CanonicalLangTypeId(filename, name = "True", number = 0u)]!!
        val falseType = types[CanonicalLangTypeId(filename, name = "False", number = 0u)]!!

        val descriptor = Resolver.ExternalFileDescriptor(exports = buildMap {
            put("String", LangPrimitiveKind.STRING.toConstraintLangType())
            put("Integer", LangPrimitiveKind.INTEGER.toConstraintLangType())
            put("Float", LangPrimitiveKind.FLOAT.toConstraintLangType())
            put(
                "BinaryAnswer", OptionValueLangType(
                    listOf(trueType.asConstraintReference(), falseType.asConstraintReference())
                ).toConstraint()
            )
            put("Action", ActionValueLangType.toConstraint())
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

        val builtins = coreBuiltinFile.second.exports

        run {
            val filename = "core/string.cau"
            result.add(
                filename to Resolver.ExternalFileDescriptor(
                    exports = mapOf(
                        "append" to FunctionValueLangType(
                            name = "append",
                            params = listOf(
                                LangParameter(
                                    "this", LangPrimitiveKind.STRING.toConstraintLangType().asConstraintReference()
                                ),
                                LangParameter(
                                    "other", LangPrimitiveKind.STRING.toConstraintLangType().asConstraintReference()
                                ),
                            ),
                            returnConstraint = LangPrimitiveKind.STRING.toConstraintLangType().asConstraintReference()
                        ), "equals" to FunctionValueLangType(
                            name = "stringEquals",
                            params = listOf(
                                LangParameter(
                                    "this", LangPrimitiveKind.STRING.toConstraintLangType().asConstraintReference()
                                ),
                                LangParameter(
                                    "other", LangPrimitiveKind.STRING.toConstraintLangType().asConstraintReference()
                                ),
                            ),
                            returnConstraint = builtins["BinaryAnswer"]!!.asConstraintReference()
                        ), "integer_to_string" to FunctionValueLangType(
                            name = "integer_to_string",
                            params = listOf(
                                LangParameter(
                                    "this", LangPrimitiveKind.INTEGER.toConstraintLangType().asConstraintReference()
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
                            name = "add",
                            params = listOf(
                                LangParameter(
                                    "this", LangPrimitiveKind.INTEGER.toConstraintLangType().asConstraintReference()
                                ),
                                LangParameter(
                                    "other", LangPrimitiveKind.INTEGER.toConstraintLangType().asConstraintReference()
                                ),
                            ),
                            returnConstraint = LangPrimitiveKind.INTEGER.toConstraintLangType().asConstraintReference()
                        )
                    ), types = mapOf()
                )
            )
        }

        result.toList()
    }
}

private fun MutableMap<CanonicalLangTypeId, CanonicalLangType>.add(type: CanonicalLangType): CanonicalLangType {
    put(type.id, type)
    return type
}