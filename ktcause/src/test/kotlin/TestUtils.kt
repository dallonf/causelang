import com.dallonf.ktcause.Debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.Resolver.debug
import com.dallonf.ktcause.RunResult
import com.dallonf.ktcause.RuntimeValue
import com.dallonf.ktcause.types.CanonicalLangTypeId
import kotlin.test.assertEquals

object TestUtils {

    fun expectNoCompileErrors(vm: LangVm) {
        val (_, compileErrors) = vm.codeBundle
        if (compileErrors.isNotEmpty()) {
            Debug.printCompileErrors(vm)
            throw AssertionError("Compile errors: ${compileErrors.debug()}")
        }
    }
    
    fun printCompileErrors(vm: LangVm) {
        Debug.printCompileErrors(vm)
    }

    // TODO: it's kinda weird that there's two ways to get almost the same runtime error, hm?
    fun expectTypeError(result: RunResult, vm: LangVm): RuntimeValue.BadValue {
        require(result is RunResult.Caused)
        assertEquals(vm.codeBundle.getTypeId("core/builtin.cau", "TypeError"), result.signal.typeDescriptor.id)
        return result.signal.values[0] as RuntimeValue.BadValue
    }

    fun expectInvalidSignal(result: RunResult): RuntimeValue.BadValue {
        require(result is RunResult.Caused)
        return result.signal.validate() as RuntimeValue.BadValue
    }

    fun expectValidCaused(result: RunResult, expectedType: CanonicalLangTypeId): RuntimeValue.RuntimeObject {
        val signal = result.expectCausedSignal().validate() as RuntimeValue.RuntimeObject
        assertEquals(expectedType, signal.typeDescriptor.id)
        return signal
    }

    fun runMainExpectingDebugs(vm: LangVm, path: String, expected: List<String>) {
        runMainExpectingDebugValues(vm, path, expected.map { RuntimeValue.Text(it) })
    }

    fun runMainExpectingDebugValues(vm: LangVm, path: String, expected: List<RuntimeValue>) {
        var result = vm.executeFunction(path, "main", listOf())
        val debugType = vm.codeBundle.getTypeId("core/builtin.cau", "Debug")

        val debugs = mutableListOf<RuntimeValue>()
        while (result is RunResult.Caused) {
            assertEquals(debugType, result.signal.typeDescriptor.id)
            debugs.add(result.signal.values[0])

            result = vm.resumeExecution(RuntimeValue.Action)
        }

        assertEquals(RuntimeValue.Action, result.expectReturnValue())
        assertEquals(expected, debugs)
    }
}