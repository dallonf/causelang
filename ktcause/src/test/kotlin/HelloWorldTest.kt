import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class HelloWorldTest {
    @Test
    fun helloWorld() {
        val vm = LangVm()
        TestUtils.addFileExpectingNoCompileErrors(
            vm,
            "project/hello.cau", """
                function main() {
                    cause Debug("Hello world!")
                }
            """.trimIndent()
        )

        val result1 = vm.executeFunction("project/hello.cau", "main", listOf()).expectCaused()
        assertEquals(
            result1.debug(), """
                {
                    "#type": "core/builtin.cau:Debug",
                    "message": "Hello world!"
                }
            """.trimIndent()
        )

        val result2 = vm.resumeExecution(RuntimeValue.Action).expectReturned()
        assertEquals(
            result2.debug(), """
                {
                    "#type": "Action"
                }
            """.trimIndent()
        )
    }
}