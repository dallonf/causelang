import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RunResult
import com.dallonf.ktcause.RuntimeValue
import kotlin.test.assertEquals
import kotlin.test.assertIs

object TestUtils {
    fun expectTypeError(result: RunResult, vm: LangVm): RuntimeValue.BadValue {
        require(result is RunResult.Caused)
        assertEquals(result.signal.typeDescriptor.type.id, vm.getTypeId("core/builtin.cau", "TypeError"))
        return result.signal.values[0] as RuntimeValue.BadValue
    }
}