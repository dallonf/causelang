package com.dallonf.ktcause

import com.dallonf.ktcause.CompiledFile.CompiledExport
import com.dallonf.ktcause.types.*
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object CoreFiles {
    fun getBinaryAnswer(boolean: Boolean): RuntimeValue {
        return if (boolean) {
            RuntimeValue.fromExport(builtin, "True")
        } else {
            RuntimeValue.fromExport(builtin, "False")
        }
    }

    val others by lazy { listOf(math, string) }

    val all by lazy { listOf(builtin) + others }

    val builtin by lazy {
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

        val exports = buildMap<String, CompiledExport> {
            put(
                "String", CompiledExport.Constraint(
                    LangPrimitiveKind.STRING.toConstraintLangType().asConstraintReference()
                )
            )
            put(
                "Number", CompiledExport.Constraint(
                    LangPrimitiveKind.NUMBER.toConstraintLangType().asConstraintReference()
                )
            )
            put(
                "WholeNumber", CompiledExport.Constraint(
                    LangPrimitiveKind.WHOLE_NUMBER.toConstraintLangType().asConstraintReference()
                )
            )
            put("Action", CompiledExport.Constraint(ActionValueLangType.valueToConstraintReference()))
            put("Anything", CompiledExport.Constraint(AnythingValueLangType.valueToConstraintReference()))
            put(
                "AnySignal", CompiledExport.Constraint(AnySignalValueLangType.valueToConstraintReference())
            )

            for ((_, type) in types) {
                put(type.id.name!!, CompiledExport.Constraint(type.asConstraintReference()))
            }

            val binaryAnswer = OptionValueLangType(
                listOf(
                    trueType.asConstraintReference(),
                    falseType.asConstraintReference(),
                )
            )
            put("BinaryAnswer", CompiledExport.Constraint(binaryAnswer.valueToConstraintReference()))

            put("equals", CompiledExport.NativeFunction(
                FunctionValueLangType(
                    "equals", params = listOf(
                        LangParameter("this", AnythingValueLangType.valueToConstraintReference()),
                        LangParameter("other", AnythingValueLangType.valueToConstraintReference()),
                    ), returnConstraint = binaryAnswer.valueToConstraintReference()
                )
            ) { (thisVal, other) ->
                getBinaryAnswer(thisVal == other)
            })
        }

        CompiledFile(
            filename, types, chunks = emptyList(), exports
        )
    }

    val math by lazy {
        val filename = "core/math.cau"

        val exports = buildMap<String, CompiledExport> {

            listOf<Pair<String, (BigDecimal, BigDecimal) -> BigDecimal>>(
                "add" to { x, y -> x + y },
                "subtract" to { x, y -> x - y },
                "multiply" to { x, y -> x * y },
                "divide" to { x, y -> x / y },
            ).forEach { (name, fn) ->
                put(name, CompiledExport.NativeFunction(
                    FunctionValueLangType(
                        name = name,
                        params = listOf(
                            LangParameter(
                                "this", LangPrimitiveKind.NUMBER.toConstraintLangType().asConstraintReference()
                            ),
                            LangParameter(
                                "other", LangPrimitiveKind.NUMBER.toConstraintLangType().asConstraintReference()
                            ),
                        ),
                        returnConstraint = LangPrimitiveKind.NUMBER.toConstraintLangType().asConstraintReference()
                    )
                ) { (val1, val2) ->
                    require(val1 is RuntimeValue.Number)
                    require(val2 is RuntimeValue.Number)

                    RuntimeValue.Number(fn(val1.value, val2.value))
                })
            }

            listOf<Pair<String, (Long, Long) -> Long>>(
                "add_whole" to { x, y -> x + y },
                "subtract_whole" to { x, y -> x - y },
                "multiply_whole" to { x, y -> x * y },
                "divide_whole" to { x, y -> x / y },
            ).forEach { (name, fn) ->
                put(name, CompiledExport.NativeFunction(
                    FunctionValueLangType(
                        name = name, params = listOf(
                            LangParameter(
                                "this", LangPrimitiveKind.WHOLE_NUMBER.toConstraintLangType().asConstraintReference()
                            ),
                            LangParameter(
                                "other", LangPrimitiveKind.WHOLE_NUMBER.toConstraintLangType().asConstraintReference()
                            ),
                        ), returnConstraint = LangPrimitiveKind.WHOLE_NUMBER.toConstraintLangType().asConstraintReference()
                    )
                ) { (val1, val2) ->
                    require(val1 is RuntimeValue.WholeNumber)
                    require(val2 is RuntimeValue.WholeNumber)

                    RuntimeValue.WholeNumber(fn(val1.value, val2.value))
                })
            }

            put("to_number", CompiledExport.NativeFunction(
                FunctionValueLangType(
                    "to_number", params = listOf(
                        LangParameter(
                            "this", LangPrimitiveKind.WHOLE_NUMBER.toConstraintLangType().asConstraintReference()
                        )
                    ), returnConstraint = LangPrimitiveKind.NUMBER.toConstraintLangType().asConstraintReference()
                )
            ) { (thisVal) ->
                require(thisVal is RuntimeValue.WholeNumber)
                RuntimeValue.Number(thisVal.value)
            })

            listOf<Pair<String, (BigDecimal) -> Long>>(
                "round" to {
                    it.round(
                        MathContext(
                            1, RoundingMode.HALF_UP
                        )
                    ).toLong()
                },
                "floor" to {
                    it.round(
                        MathContext(
                            1, RoundingMode.FLOOR
                        )
                    ).toLong()
                },
                "ceiling" to {
                    it.round(
                        MathContext(
                            1, RoundingMode.CEILING
                        )
                    ).toLong()
                },
            ).forEach { (name, fn) ->
                put(name, CompiledExport.NativeFunction(
                    FunctionValueLangType(
                        name = name, params = listOf(
                            LangParameter(
                                "this", LangPrimitiveKind.NUMBER.toConstraintLangType().asConstraintReference()
                            ),
                        ), returnConstraint = LangPrimitiveKind.WHOLE_NUMBER.toConstraintLangType().asConstraintReference()
                    )
                ) { (thisVal) ->
                    require(thisVal is RuntimeValue.Number)
                    RuntimeValue.WholeNumber(fn(thisVal.value))
                })
            }
        }

        CompiledFile(filename, types = emptyMap(), chunks = emptyList(), exports)
    }

    val string by lazy {
        val filename = "core/string.cau"

        val exports = buildMap<String, CompiledExport> {
            put("append", CompiledExport.NativeFunction(
                FunctionValueLangType(
                    name = "append", params = listOf(
                        LangParameter(
                            "this", LangPrimitiveKind.STRING.toConstraintLangType().asConstraintReference()
                        ),
                        LangParameter(
                            "other", LangPrimitiveKind.STRING.toConstraintLangType().asConstraintReference()
                        ),
                    ), returnConstraint = LangPrimitiveKind.STRING.toConstraintLangType().asConstraintReference()
                )
            ) { (thisVal, other) ->
                require(thisVal is RuntimeValue.String)
                require(other is RuntimeValue.String)

                RuntimeValue.String(thisVal.value + other.value)
            })

            put("number_to_string", CompiledExport.NativeFunction(
                FunctionValueLangType(
                    name = "number_to_string", params = listOf(
                        LangParameter(
                            "this", LangPrimitiveKind.NUMBER.toConstraintLangType().asConstraintReference()
                        )
                    ), returnConstraint = LangPrimitiveKind.STRING.toConstraintLangType().asConstraintReference()
                )
            ) { (thisVal) ->
                require(thisVal is RuntimeValue.Number)
                RuntimeValue.String(thisVal.value.toString())
            })

            put("whole_number_to_string", CompiledExport.NativeFunction(
                FunctionValueLangType(
                    name = "whole_number_to_string", params = listOf(
                        LangParameter(
                            "this", LangPrimitiveKind.WHOLE_NUMBER.toConstraintLangType().asConstraintReference()
                        )
                    ), returnConstraint = LangPrimitiveKind.STRING.toConstraintLangType().asConstraintReference()
                )
            ) { (thisVal) ->
                require(thisVal is RuntimeValue.WholeNumber)
                RuntimeValue.String(thisVal.value.toString())
            })
        }

        CompiledFile(filename, types = emptyMap(), chunks = emptyList(), exports)
    }
}

private fun MutableMap<CanonicalLangTypeId, CanonicalLangType>.add(type: CanonicalLangType): CanonicalLangType {
    put(type.id, type)
    return type
}