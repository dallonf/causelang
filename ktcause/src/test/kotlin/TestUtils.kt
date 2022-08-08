import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RunResult
import com.dallonf.ktcause.RuntimeValue
import com.dallonf.ktcause.types.CanonicalLangTypeId
import kotlin.math.sign
import kotlin.test.assertEquals
import kotlin.test.assertIs

object TestUtils {
    fun expectNoCompileErrors(vm: LangVm) {
        assertEquals(vm.compileErrors, listOf())
    }

    fun addFileExpectingNoCompileErrors(vm: LangVm, path: String, source: String) {
        vm.addFile(path, source)
        expectNoCompileErrors(vm)
    }

    // TODO: it's kinda weird that there's two ways to get almost the same runtime error, hm?
    fun expectTypeError(result: RunResult, vm: LangVm): RuntimeValue.BadValue {
        require(result is RunResult.Caused)
        assertEquals(result.signal.typeDescriptor.type.id, vm.getTypeId("core/builtin.cau", "TypeError"))
        return result.signal.values[0] as RuntimeValue.BadValue
    }

    fun expectInvalidSignal(result: RunResult): RuntimeValue.BadValue {
        require(result is RunResult.Caused)
        return result.signal.validate() as RuntimeValue.BadValue
    }

    fun expectValidCaused(result: RunResult, expectedType: CanonicalLangTypeId): RuntimeValue.RuntimeObject {
        val signal = result.expectCaused().signal.validate() as RuntimeValue.RuntimeObject
        assertEquals(signal.typeDescriptor.type.id, expectedType)
        return signal
    }
}