import TestUtils.runMainExpectingDebugs
import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.Resolver.debug
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class BlockResultSyntaxTest {
    @Test
    fun helloWorld() {
        val vm = LangVm {
            addFile(
                "project/hello.cau", """
                    function main() {
                        let greeting: Text = { 
                            ^ "Hello, world!"
                        }
                        cause Debug(greeting)
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)
        runMainExpectingDebugs(vm, "project/hello.cau", listOf("Hello, world!"))
    }

    @Test
    fun actionIfOmitted() {
        val vm = LangVm {
            addFile(
                "project/hello.cau", """
                    function main() returns Action {
                        greet()
                    }
                    
                    function greet() returns Action {
                        cause Debug("Hi")
                        "should not be returned"
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)
        runMainExpectingDebugs(vm, "project/hello.cau", listOf("Hello, world!"))
    }

    @Test
    fun errorIfOmittedWhereResultNeeded() {
        val vm = LangVm {
            addFile(
                "project/hello.cau", """
                    function main() {
                        let greeting: Text = {
                            "Hello, world!"
                        }
                        cause Debug(greeting)
                    }
               """.trimIndent()
            )
        }

        assertEquals(
            """
            """.trimIndent(),
            vm.codeBundle.compileErrors.debug(),
        )

        val result = vm.executeFunction("project/hello.cau", "main", listOf())
        val badValue = TestUtils.expectTypeError(result, vm)
        assertEquals(
            """
            """.trimIndent(),
            badValue.debug(),
        )
    }
}