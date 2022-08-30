import com.dallonf.ktcause.Debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.Resolver.debug
import com.dallonf.ktcause.RunResult
import com.dallonf.ktcause.RuntimeValue
import com.dallonf.ktcause.types.CanonicalLangTypeId
import kotlin.test.assertEquals

object TestUtils {

    fun expectNoCompileErrors(vm: LangVm) {
        if (vm.compileErrors.isNotEmpty()) {
            throw AssertionError("Compile errors: ${vm.compileErrors.debug()}")
        }
    }

    fun printCompileErrors(vm: LangVm) {
        if (vm.compileErrors.isNotEmpty()) {
            println("Compile errors: ${vm.compileErrors.debug()}")
        }
    }

    fun LangVm.addFileAndPrintCompileErrors(path: String, source: String): Debug.DebugContext {
        val debugCtx = addFile(path, source)
        val errorsForFile = compileErrors.filter { it.position.path == path }
        if (errorsForFile.isNotEmpty()) {
            println("Compile errors in file:\n")
            for (error in errorsForFile) {
                println(error.debug())
                println(debugCtx.getNodeContext(error.position.breadcrumbs))
                println("------------------------------");
            }
        }
        return debugCtx
    }

    fun LangVm.addFileExpectingNoCompileErrors(path: String, source: String) {
        addFileAndPrintCompileErrors(path, source)
        expectNoCompileErrors(this)
    }

    // TODO: it's kinda weird that there's two ways to get almost the same runtime error, hm?
    fun expectTypeError(result: RunResult, vm: LangVm): RuntimeValue.BadValue {
        require(result is RunResult.Caused)
        assertEquals(vm.getTypeId("core/builtin.cau", "TypeError"), result.signal.typeDescriptor.id)
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
        runMainExpectingDebugValues(vm, path, expected.map { RuntimeValue.String(it) })
    }

    fun runMainExpectingDebugValues(vm: LangVm, path: String, expected: List<RuntimeValue>) {
        var result = vm.executeFunction(path, "main", listOf())
        val debugType = vm.getTypeId("core/builtin.cau", "Debug")

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