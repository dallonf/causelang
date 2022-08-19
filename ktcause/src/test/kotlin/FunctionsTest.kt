import TestUtils.addFileAndPrintCompileErrors
import TestUtils.addFileExpectingNoCompileErrors
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FunctionsTest {
    @Test
    fun callsAnotherFunctionAndUsesItsValue() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
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
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
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

    @Test
    fun causesInFunctionCall() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
                function main() {
                    greet()
                }
                
                function greet() {
                    cause Debug("Hello World")
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
    fun functionTakesParameters() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
                import core/string (append)
                
                function main(): String {
                    formatGreeting("Hello", "World")
                }
                
                function formatGreeting(greeting: String, name: String): String {
                    append(greeting, append(", ", name))
                }
            """.trimIndent()
        )

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(RuntimeValue.String("Hello, World"), result)
    }

    @Test
    fun functionCanAccessOuterScope() {
        val vm = LangVm()
        vm.addFileAndPrintCompileErrors(
            "project/test.cau", """
                import core/math (add)                   
                                
                function main(): Integer {
                    let base = 1
                    function next() {
                        add(base, 2)
                    }
                    next()
                }
            """.trimIndent()
        )

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(RuntimeValue.Integer(3), result)
    }
}