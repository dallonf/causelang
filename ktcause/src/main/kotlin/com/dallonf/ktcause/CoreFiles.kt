package com.dallonf.ktcause

import com.dallonf.ktcause.CompiledFile.CompiledExport
import com.dallonf.ktcause.types.*
import com.github.hiking93.graphemesplitterlite.GraphemeSplitter
import org.apache.commons.numbers.fraction.BigFraction
import java.math.RoundingMode

object CoreFiles {
    fun getBinaryAnswer(boolean: Boolean): RuntimeValue {
        return if (boolean) {
            RuntimeValue.fromExport(builtin, "True")
        } else {
            RuntimeValue.fromExport(builtin, "False")
        }
    }

    val others by lazy { listOf(math, text, stopgapCollections) }

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
                            "message", LangPrimitiveKind.TEXT.toConstraintLangType().asConstraintReference()
                        )
                    ),
                    result = NeverContinuesValueLangType.toConstraint().asConstraintReference()
                )
            )
            add(
                CanonicalLangType.SignalCanonicalLangType(
                    CanonicalLangTypeId(filename, name = "RunawayLoop", number = 0.toUByte()),
                    name = "RunawayLoop",
                    fields = listOf(),
                    result = NeverContinuesValueLangType.toConstraint().asConstraintReference()
                )
            )
        }

        val trueType = types[CanonicalLangTypeId(filename, name = "True", number = 0u)]!!
        val falseType = types[CanonicalLangTypeId(filename, name = "False", number = 0u)]!!

        val exports = buildMap<String, CompiledExport> {
            put(
                "Text", CompiledExport.Constraint(
                    LangPrimitiveKind.TEXT.toConstraintLangType().asConstraintReference()
                )
            )
            put(
                "Number", CompiledExport.Constraint(
                    LangPrimitiveKind.NUMBER.toConstraintLangType().asConstraintReference()
                )
            )
            put("Action", CompiledExport.Constraint(ActionValueLangType.valueToConstraintReference()))
            put("NeverContinues", CompiledExport.Constraint(NeverContinuesValueLangType.valueToConstraintReference()))
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
            filename, types, procedures = emptyList(), exports
        )
    }

    val math by lazy {
        val filename = "core/math.cau"

        val exports = buildMap<String, CompiledExport> {

            listOf<Pair<String, (BigFraction, BigFraction) -> BigFraction>>(
                "add" to { x, y -> x.add(y) },
                "subtract" to { x, y -> x.subtract(y) },
                "multiply" to { x, y -> x.multiply(y) },
                "divide" to { x, y -> x.divide(y) },
                "remainder" to { x, y ->
                    val divided = x.divide(y)
                    val whole = divided.toLong()
                    divided.subtract(whole)
                },
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
                ) { (x, y) ->
                    require(x is RuntimeValue.Number)
                    require(y is RuntimeValue.Number)

                    RuntimeValue.Number(fn(x.value, y.value))
                })
            }

            listOf<Pair<String, (BigFraction) -> Long>>(
                "round" to {
                    it.bigDecimalValue(RoundingMode.HALF_UP).toLong()
                },
                "floor" to {
                    it.toLong()
                },
                "ceiling" to {
                    it.bigDecimalValue(
                        RoundingMode.CEILING
                    ).toLong()
                },
            ).forEach { (name, fn) ->
                put(name, CompiledExport.NativeFunction(
                    FunctionValueLangType(
                        name = name,
                        params = listOf(
                            LangParameter(
                                "this", LangPrimitiveKind.NUMBER.toConstraintLangType().asConstraintReference()
                            ),
                        ),
                        returnConstraint = LangPrimitiveKind.NUMBER.toConstraintLangType().asConstraintReference()
                    )
                ) { (thisVal) ->
                    require(thisVal is RuntimeValue.Number)
                    RuntimeValue.Number(fn(thisVal.value))
                })
            }

            listOf<Pair<String, (BigFraction, BigFraction) -> Boolean>>(
                "greater_than" to { x, y -> x > y },
                "less_than" to { x, y -> x < y },
                "at_least" to { x, y -> x >= y },
                "at_most" to { x, y -> x <= y },
            ).forEach { (name, fn) ->
                put(name, CompiledExport.NativeFunction(
                    FunctionValueLangType(
                        name = name,
                        params = listOf(
                            LangParameter(
                                "this", LangPrimitiveKind.NUMBER.toConstraintLangType().asConstraintReference()
                            ), LangParameter(
                                "other", LangPrimitiveKind.NUMBER.toConstraintLangType().asConstraintReference()
                            )
                        ),
                        returnConstraint = (builtin.exports["BinaryAnswer"] as CompiledExport.Constraint).constraint
                    )
                ) { (thisVal, otherVal) ->
                    require(thisVal is RuntimeValue.Number)
                    require(otherVal is RuntimeValue.Number)
                    getBinaryAnswer(fn(thisVal.value, otherVal.value))
                })
            }
        }

        CompiledFile(filename, types = emptyMap(), procedures = emptyList(), exports)
    }

    private val graphemeSplitter by lazy { GraphemeSplitter() }

    val text by lazy {
        val filename = "core/text.cau"

        val exports = buildMap<String, CompiledExport> {
            put("append", CompiledExport.NativeFunction(
                FunctionValueLangType(
                    name = "append", params = listOf(
                        LangParameter(
                            "this", LangPrimitiveKind.TEXT.toConstraintLangType().asConstraintReference()
                        ),
                        LangParameter(
                            "other", LangPrimitiveKind.TEXT.toConstraintLangType().asConstraintReference()
                        ),
                    ), returnConstraint = LangPrimitiveKind.TEXT.toConstraintLangType().asConstraintReference()
                )
            ) { (thisVal, other) ->
                require(thisVal is RuntimeValue.Text)
                require(other is RuntimeValue.Text)

                RuntimeValue.Text(thisVal.value + other.value)
            })

            put("number_to_text", CompiledExport.NativeFunction(
                FunctionValueLangType(
                    name = "number_to_text", params = listOf(
                        LangParameter(
                            "this", LangPrimitiveKind.NUMBER.toConstraintLangType().asConstraintReference()
                        )
                    ), returnConstraint = LangPrimitiveKind.TEXT.toConstraintLangType().asConstraintReference()
                )
            ) { (thisVal) ->
                require(thisVal is RuntimeValue.Number)
                RuntimeValue.Text(thisVal.value.toString())
            })

            put("count_characters", CompiledExport.NativeFunction(
                FunctionValueLangType(
                    name = "count_characters", params = listOf(
                        LangParameter(
                            "this", LangPrimitiveKind.TEXT.toConstraintLangType().asConstraintReference()
                        )
                    ), returnConstraint = LangPrimitiveKind.NUMBER.toConstraintLangType().asConstraintReference()
                )
            ) { (thisVal) ->
                require(thisVal is RuntimeValue.Text)
                val count = graphemeSplitter.split(thisVal.value).size
                RuntimeValue.Number(count.toLong())
            })

            put("slice_index", CompiledExport.NativeFunction(
                FunctionValueLangType(
                    name = "slice_index", params = listOf(
                        LangParameter(
                            "this", LangPrimitiveKind.TEXT.toConstraintLangType().asConstraintReference()
                        ), LangParameter(
                            "start_index", LangPrimitiveKind.NUMBER.toConstraintLangType().asConstraintReference()
                        ), LangParameter(
                            "until_index", LangPrimitiveKind.NUMBER.toConstraintLangType().asConstraintReference()
                        )
                    ), returnConstraint = LangPrimitiveKind.TEXT.toConstraintLangType().asConstraintReference()
                )
            ) { (thisVal, startIndex, untilIndex) ->
                require(thisVal is RuntimeValue.Text)
                require(startIndex is RuntimeValue.Number)
                require(untilIndex is RuntimeValue.Number)
                val graphemes = graphemeSplitter.split(thisVal.value)
                val slice = graphemes.subList(startIndex.value.toInt(), untilIndex.value.toInt())
                RuntimeValue.Text(slice.joinToString(""))
            })

            put("slice_nth", CompiledExport.NativeFunction(
                FunctionValueLangType(
                    name = "slice_nth", params = listOf(
                        LangParameter(
                            "this", LangPrimitiveKind.TEXT.toConstraintLangType().asConstraintReference()
                        ), LangParameter(
                            "first_character",
                            LangPrimitiveKind.NUMBER.toConstraintLangType().asConstraintReference()
                        ), LangParameter(
                            "last_character",
                            LangPrimitiveKind.NUMBER.toConstraintLangType().asConstraintReference()
                        )
                    ), returnConstraint = LangPrimitiveKind.TEXT.toConstraintLangType().asConstraintReference()
                )
            ) { (thisVal, startIndex, untilIndex) ->
                require(thisVal is RuntimeValue.Text)
                require(startIndex is RuntimeValue.Number)
                require(untilIndex is RuntimeValue.Number)
                val graphemes = graphemeSplitter.split(thisVal.value)
                val slice = graphemes.subList(startIndex.value.toInt() - 1, untilIndex.value.toInt())
                RuntimeValue.Text(slice.joinToString(""))
            })
        }

        CompiledFile(filename, types = emptyMap(), procedures = emptyList(), exports)
    }

    val stopgapCollections by lazy {
        val filename = "core/stopgap/collections.cau"

        val dictionaryType = ConstraintReference.ResolvedConstraint(StopgapDictionaryLangType)

        var maybeStack: OptionValueLangType
        val emptyId = CanonicalLangTypeId(filename, name = "Empty", number = 0u)
        val stackId = CanonicalLangTypeId(filename, name = "Stack", number = 0u)
        val keyValuePairId = CanonicalLangTypeId(filename, name = "KeyValuePair", number = 0u)
        val types = buildMap {
            val empty = add(
                CanonicalLangType.ObjectCanonicalLangType(
                    emptyId,
                    name = "Empty",
                    fields = emptyList(),
                )
            )

            val stack = add(
                CanonicalLangType.ObjectCanonicalLangType(
                    stackId, name = "Stack", fields = emptyList()
                ),
            )

            add(
                CanonicalLangType.ObjectCanonicalLangType(
                    keyValuePairId, name = "KeyValuePair", fields = listOf(
                        CanonicalLangType.ObjectField("key", LangPrimitiveKind.TEXT.toConstraintReference()),
                        CanonicalLangType.ObjectField("value", AnythingValueLangType.valueToConstraintReference()),
                    )
                )
            )

            maybeStack = OptionValueLangType(
                listOf(
                    empty.asConstraintReference(),
                    stack.asConstraintReference(),
                )
            )

            stack.fields = listOf(
                CanonicalLangType.ObjectField("top", AnythingValueLangType.valueToConstraintReference()),
                CanonicalLangType.ObjectField("next", maybeStack.valueToConstraintReference()),
            )
        }

        val exports = buildMap {
            for ((_, type) in types) {
                put(type.id.name!!, CompiledExport.Constraint(type.asConstraintReference()))
            }
            put("MaybeStack", CompiledExport.Constraint(maybeStack.valueToConstraintReference()))

            put("Dictionary", CompiledExport.Constraint(dictionaryType))

            put("get_item", CompiledExport.NativeFunction(
                FunctionValueLangType(
                    "get_item", AnythingValueLangType.valueToConstraintReference(), listOf(
                        LangParameter("this", dictionaryType),
                        LangParameter("key", LangPrimitiveKind.TEXT.toConstraintReference())
                    )
                )
            ) { params ->
                val thisVal = (params[0] as RuntimeValue.StopgapDictionary).map
                val key = (params[1] as RuntimeValue.Text).value
                thisVal[key] ?: RuntimeValue.RuntimeObject(types[emptyId]!!, emptyList())
            })

            put("with_item_at_key", CompiledExport.NativeFunction(
                FunctionValueLangType(
                    "with_item_at_key", dictionaryType, listOf(
                        LangParameter("this", dictionaryType),
                        LangParameter("key", LangPrimitiveKind.TEXT.toConstraintReference()),
                        LangParameter("value", AnythingValueLangType.valueToConstraintReference())
                    )
                )
            ) { params ->
                val thisVal = (params[0] as RuntimeValue.StopgapDictionary).map
                val key = (params[1] as RuntimeValue.Text).value
                val value = params[2]

                val result = thisVal.plus(key to value)

                RuntimeValue.StopgapDictionary(result)
            })

            put("without_key", CompiledExport.NativeFunction(
                FunctionValueLangType(
                    "without_key", dictionaryType, listOf(
                        LangParameter("this", dictionaryType),
                        LangParameter("key", LangPrimitiveKind.TEXT.toConstraintReference())
                    )
                )
            ) { params ->
                val thisVal = (params[0] as RuntimeValue.StopgapDictionary).map
                val key = (params[1] as RuntimeValue.Text).value

                val result = thisVal.minus(key)

                RuntimeValue.StopgapDictionary(result)
            })

            put("list_entries", CompiledExport.NativeFunction(
                FunctionValueLangType(
                    "list_entries", maybeStack.valueToConstraintReference(), listOf(
                        LangParameter("this", dictionaryType)
                    )
                )
            ) { params ->
                val thisValue = (params[0] as RuntimeValue.StopgapDictionary).map

                thisValue.entries.toList().foldRight(
                    RuntimeValue.RuntimeObject(types[emptyId]!!, emptyList())
                ) { next, prev ->
                    val pair = RuntimeValue.RuntimeObject(
                        types[keyValuePairId]!!, listOf(
                            RuntimeValue.Text(next.key),
                            next.value
                        )
                    )
                    RuntimeValue.RuntimeObject(
                        types[stackId]!!, listOf(
                            pair,
                            prev
                        )
                    )
                }
            })
        }

        CompiledFile(filename, types, procedures = emptyList(), exports)
    }
}

private fun <T : CanonicalLangType> MutableMap<CanonicalLangTypeId, CanonicalLangType>.add(type: T): T {
    put(type.id, type)
    return type
}