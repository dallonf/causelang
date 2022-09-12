import TestUtils.addFileExpectingNoCompileErrors
import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class HelloWorldTest {
    @Test
    fun helloWorld() {
        val vm = LangVm {
            addFile(
                "project/hello.cau", """
                    function main() {
                        cause Debug("Hello world!")
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val result1 = vm.executeFunction("project/hello.cau", "main", listOf()).expectCausedSignal()
        assertEquals(
            """
                {
                    "#type": "core/builtin.cau:Debug",
                    "value": "Hello world!"
                }
            """.trimIndent(), result1.debug()
        )

        val result2 = vm.resumeExecution(RuntimeValue.Action).expectReturnValue()
        assertEquals(
            """
                {
                    "#type": "Action"
                }
            """.trimIndent(),
            result2.debug(),
        )
    }
}