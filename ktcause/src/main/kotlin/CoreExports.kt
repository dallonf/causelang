import com.dallonf.ktcause.CoreDescriptors
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue
import com.dallonf.ktcause.types.CanonicalLangType

object CoreExports {
    const val BUILTINS_FILE = "core/builtin.cau"
    const val STRING_FILE = "core/string.cau"

    fun getCoreExport(fileName: String, exportName: String): RuntimeValue {
        if (fileName == BUILTINS_FILE) {
            if (exportName == "Debug" || exportName == "TypeError" || exportName == "AssumptionBroken") {
                val signal = CoreDescriptors.coreBuiltinFile.second.exports[exportName]

                return RuntimeValue.RuntimeTypeReference(signal as CanonicalLangType)
            } else {
                throw LangVm.InternalVmError("There is no builtin named $exportName.")
            }
        } else if (fileName == STRING_FILE) {
            if (exportName == "append") {
                return RuntimeValue.NativeFunction("appendString") { params ->
                    val val1 = params[0] as? RuntimeValue.String
                        ?: throw LangVm.VmError("I was expecting the inputs to append to be strings.")
                    val val2 = params[1] as? RuntimeValue.String
                        ?: throw LangVm.VmError("I was expecting the inputs to append to be strings.")

                    RuntimeValue.String("${val1.value}${val2.value}")
                }
            } else {
                throw LangVm.InternalVmError("There is no export named $exportName in $fileName")
            }
        } else {
            throw LangVm.InternalVmError("There is no core file called $fileName.")
        }
    }
}