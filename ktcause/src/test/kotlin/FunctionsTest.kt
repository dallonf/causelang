import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FunctionsTest {
    @Test
    fun callsAnotherFunctionAndUsesItsValue() {
        val vm = LangVm()
        TestUtils.addFileExpectingNoCompileErrors(
            vm, "project/test.cau", """
                function main() {
                    cause Debug(getGreeting())
                }
                
                function getGreeting() {
                    "Hello World"
                }
            """.trimIndent()
        )

        val debug = TestUtils.expectValidCaused(
            vm.executeFunction("project/test.cau", "main", listOf()),
            vm.getTypeId("core/builtin.cau", "Debug")
        )
        assertEquals(debug.values[0], RuntimeValue.String("Hello World"))
        assertEquals(vm.resumeExecution(RuntimeValue.Action).expectReturnValue(), RuntimeValue.Action)
    }

    @Test
    fun jugglesScope() {
        val vm = LangVm()
        TestUtils.addFileExpectingNoCompileErrors(
            vm, "project/test.cau", """
                import core/string ( append )
                
                function main() {
                    let name = getName()
                    let prefix = getGreetingPrefix()
                    cause Debug(append(prefix, name))
                }
                
                function getName() {
                    let end = "ld"
                    let start = "Wor"
                    append(start, end)
                }
                
                function getGreetingPrefix() {
                    append("Hello", ", ")
                }
            """.trimIndent()
        )

        val debug = TestUtils.expectValidCaused(
            vm.executeFunction("project/test.cau", "main", listOf()),
            vm.getTypeId("core/builtin.cau", "Debug")
        )
        assertEquals(debug.values[0], RuntimeValue.String("Hello, World"))
        assertEquals(vm.resumeExecution(RuntimeValue.Action).expectReturnValue(), RuntimeValue.Action)
    }
}