package com.dallonf.ktcause

import com.dallonf.ktcause.types.ConstraintValueLangType

object CoreExports {
    const val BUILTINS_FILE = "core/builtin.cau"
    const val STRING_FILE = "core/string.cau"
    const val MATH_FILE = "core/math.cau"

    fun getBinaryAnswer(boolean: Boolean): RuntimeValue {
        return if (boolean) {
            getCoreExport(BUILTINS_FILE, "True")
        } else {
            getCoreExport(BUILTINS_FILE, "False")
        }
    }

    fun getCoreExport(fileName: String, exportName: String): RuntimeValue {
        if (fileName == BUILTINS_FILE) {
            if (setOf(
                    "String",
                    "Number",
                    "Count",
                    "BinaryAnswer",
                    "Action",
                    "Debug",
                    "TypeError",
                    "AssumptionBroken",
                    "Anything",
                    "AnySignal",
                    "True",
                    "False"
                ).contains(exportName)
            ) {
                val exportedType = CoreDescriptors.coreBuiltinFile.second.exports[exportName] as ConstraintValueLangType

                return RuntimeValue.RuntimeTypeConstraint(exportedType.valueType)
            } else {
                throw LangVm.InternalVmError("There is no builtin named $exportName.")
            }
        } else if (fileName == STRING_FILE) {
            when (exportName) {
                "append" -> {
                    return RuntimeValue.NativeFunction("appendString") { params ->
                        val val1 = params[0] as? RuntimeValue.String
                            ?: throw LangVm.VmError("I was expecting the inputs to append to be strings.")
                        val val2 = params[1] as? RuntimeValue.String
                            ?: throw LangVm.VmError("I was expecting the inputs to append to be strings.")

                        RuntimeValue.String("${val1.value}${val2.value}")
                    }
                }

                "equals" -> {
                    return RuntimeValue.NativeFunction("stringEquals") { params ->
                        val val1 = params[0] as? RuntimeValue.String
                            ?: throw LangVm.VmError("I was expecting the inputs to append to be strings.")
                        val val2 = params[1] as? RuntimeValue.String
                            ?: throw LangVm.VmError("I was expecting the inputs to append to be strings.")

                        getBinaryAnswer(val1 == val2)
                    }
                }

                "count_to_string" -> {
                    return RuntimeValue.NativeFunction("integer_to_string") { params ->
                        val value = params[0] as? RuntimeValue.Count
                            ?: throw LangVm.VmError("I was expecting the input to be a Count.")

                        RuntimeValue.String(value.value.toString())
                    }
                }

                "number_to_string" -> {
                    return RuntimeValue.NativeFunction("number_to_string") { params ->
                        val value = params[0] as? RuntimeValue.Number
                            ?: throw LangVm.VmError("I was expecting the input to be a Number.")

                        RuntimeValue.String(value.value.toString())
                    }
                }

                else -> {
                    throw LangVm.InternalVmError("There is no export named $exportName in $fileName")
                }
            }
        } else if (fileName == MATH_FILE) {
            when (exportName) {
                "add" -> {
                    return RuntimeValue.NativeFunction("add") { params ->
                        val val1 = params[0] as RuntimeValue.Number
                        val val2 = params[1] as RuntimeValue.Number

                        RuntimeValue.Number(val1.value + val2.value)
                    }
                }

                "add_count" -> {
                    return RuntimeValue.NativeFunction("add_count") { params ->
                        val val1 = params[0] as RuntimeValue.Count
                        val val2 = params[1] as RuntimeValue.Count

                        RuntimeValue.Count(val1.value + val2.value)
                    }
                }

                else -> {
                    throw LangVm.InternalVmError("There is no export named $exportName in $fileName")
                }
            }
        } else {
            throw LangVm.InternalVmError("There is no core file called $fileName.")
        }
    }
}