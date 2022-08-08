import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RunResult
import com.dallonf.ktcause.RuntimeValue
import kotlin.test.assertEquals
import kotlin.test.assertIs

object TestUtils {
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
}