import TestUtils.addFileAndPrintCompileErrors
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VariablesTest {
    @Test
    fun changesValueOfVariable() {
        val vm = LangVm()
        vm.addFileAndPrintCompileErrors(
            "project/test.cau", """
            function main() {
                let variable x = 1
                set x = 2
                cause Debug(x)
            }
        """.trimIndent()
        )

        val result = vm.executeFunction("project/test.cau", "main", listOf()).let {
            TestUtils.expectValidCaused(it, vm.getBuiltinTypeId("Debug"))
        }
        assertEquals(RuntimeValue.Integer(2), result.values[0])
    }
}