import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FunctionsTest {
    @Test
    fun callsAnotherFunctionAndUsesItsValue() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    function main() {
                        cause Debug(getGreeting())
                    }
                    
                    function getGreeting() {
                        "Hello World"
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val debug = TestUtils.expectValidCaused(
            vm.executeFunction("project/test.cau", "main", listOf()), vm.codeBundle.getBuiltinTypeId("Debug")
        )
        assertEquals(debug.values[0], RuntimeValue.Text("Hello World"))
        assertEquals(vm.resumeExecution(RuntimeValue.Action).expectReturnValue(), RuntimeValue.Action)
    }

    @Test
    fun jugglesScope() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/text ( append )
                    
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
        }
        TestUtils.expectNoCompileErrors(vm)

        val debug = TestUtils.expectValidCaused(
            vm.executeFunction("project/test.cau", "main", listOf()), vm.codeBundle.getBuiltinTypeId("Debug")
        )
        assertEquals(debug.values[0], RuntimeValue.Text("Hello, World"))
        assertEquals(vm.resumeExecution(RuntimeValue.Action).expectReturnValue(), RuntimeValue.Action)
    }

    @Test
    fun causesInFunctionCall() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                function main() {
                    greet()
                }
                
                function greet() {
                    cause Debug("Hello World")
                }
            """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val debug = TestUtils.expectValidCaused(
            vm.executeFunction("project/test.cau", "main", listOf()), vm.codeBundle.getBuiltinTypeId("Debug")
        )
        assertEquals(debug.values[0], RuntimeValue.Text("Hello World"))
        assertEquals(vm.resumeExecution(RuntimeValue.Action).expectReturnValue(), RuntimeValue.Action)
    }

    @Test
    fun functionTakesParameters() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                import core/text (append)
                
                function main(): String {
                    formatGreeting("Hello", "World")
                }
                
                function formatGreeting(greeting: Text, name: Text): Text {
                    append(greeting, append(", ", name))
                }
            """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(RuntimeValue.Text("Hello, World"), result)
    }

    @Test
    fun functionCanAccessOuterScope() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/math (add)                   
                                    
                    function main(): Number {
                        let base = 1.0
                        function next() {
                            add(base, 2.0)
                        }
                        next()
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(RuntimeValue.Number(3.0), result)
    }
}