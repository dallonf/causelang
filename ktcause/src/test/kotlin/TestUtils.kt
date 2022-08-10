import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.Resolver.debug
import com.dallonf.ktcause.RunResult
import com.dallonf.ktcause.RuntimeValue
import com.dallonf.ktcause.types.CanonicalLangTypeId
import kotlin.math.sign
import kotlin.test.assertEquals
import kotlin.test.assertIs

object TestUtils {

    fun expectNoCompileErrors(vm: LangVm) {
        if (vm.compileErrors.isNotEmpty()) {
            throw AssertionError("Compile errors: ${vm.compileErrors.debug()}")
        }
    }

    fun LangVm.addFileExpectingNoCompileErrors(path: String, source: String) {
        addFile(path, source)
        expectNoCompileErrors(this)
    }

    // TODO: it's kinda weird that there's two ways to get almost the same runtime error, hm?
    fun expectTypeError(result: RunResult, vm: LangVm): RuntimeValue.BadValue {
        require(result is RunResult.Caused)
        assertEquals(vm.getTypeId("core/builtin.cau", "TypeError"), result.signal.typeDescriptor.type.id)
        return result.signal.values[0] as RuntimeValue.BadValue
    }

    fun expectInvalidSignal(result: RunResult): RuntimeValue.BadValue {
        require(result is RunResult.Caused)
        return result.signal.validate() as RuntimeValue.BadValue
    }

    fun expectValidCaused(result: RunResult, expectedType: CanonicalLangTypeId): RuntimeValue.RuntimeObject {
        val signal = result.expectCausedSignal().validate() as RuntimeValue.RuntimeObject
        assertEquals(expectedType, signal.typeDescriptor.type.id)
        return signal
    }
}