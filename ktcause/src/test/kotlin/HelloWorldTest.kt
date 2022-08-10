import TestUtils.addFileExpectingNoCompileErrors
import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class HelloWorldTest {
    @Test
    fun helloWorld() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/hello.cau", """
                function main() {
                    cause Debug("Hello world!")
                }
            """.trimIndent()
        )

        val result1 = vm.executeFunction("project/hello.cau", "main", listOf()).expectCausedSignal()
        assertEquals(
            result1.debug(), """
                {
                    "#type": "core/builtin.cau:Debug",
                    "message": "Hello world!"
                }
            """.trimIndent()
        )

        val result2 = vm.resumeExecution(RuntimeValue.Action).expectReturnValue()
        assertEquals(
            result2.debug(), """
                {
                    "#type": "Action"
                }
            """.trimIndent()
        )
    }
}