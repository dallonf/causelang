import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.Resolver.debug
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
                
                function main(): Text {
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

    @Test
    fun nestedFunctionCanAccessOuterScope() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/math (add)                   
                                    
                    function main(): Number {
                        let base = 1.0
                        function x() {
                            function y() {
                                add(base, 2.0)
                            }
                            y()
                        }
                        x()
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(RuntimeValue.Number(3.0), result)
    }

    @Test
    fun nestedInlineFunctionCanAccessOuterScope() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/math (add)                   
                                    
                    function main(): Number {
                        let base = 1.0
                        (fn() (fn() add(base, 2.0))())()
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(RuntimeValue.Number(3.0), result)
    }

    @Test
    fun functionWithParametersCanAccessOuterScope() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/math (add)                   
                                    
                    function main(): Number {
                        let base = 1.0
                        function next(other: Number) {
                            add(base, other)
                        }
                        next(2.0)
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(RuntimeValue.Number(3.0), result)
    }

    @Test
    fun inlineFunctionCanAccessOuterScope() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/math (add)                   
                                    
                    function main(): Number {
                        let base = 1.0
                        let next = fn(other: Number) add(base, other)
                        next(2.0)
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(RuntimeValue.Number(3.0), result)
    }

    @Test
    fun higherOrderFunctions() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/math (add, multiply)
                    
                    function single_map(this: Number, callback: Function(it: Number): Number) {
                        cause Debug(callback(this))
                    }
                                    
                    function main() {
                        single_map(1, fn(it: Number) add(it, 2))
                        single_map(2, fn(it: Number) multiply(it, 2))
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        TestUtils.runMainExpectingDebugValues(
            vm, "project/test.cau", listOf(RuntimeValue.Number(3), RuntimeValue.Number(4))
        )
    }

    @Test
    fun returnTypeVariance() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/math (add)
                                    
                    function main() {
                        let func: Function(it: Number): Anything = fn(it: Number) add(it, 1) 
                        func(2)
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        assertEquals(
            RuntimeValue.Number(3), vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        )
    }

    @Test
    fun handlesErroredInlineFunction() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    function main() {
                        non_existent(3, fn() {
                            cause Debug("oh no")
                        })
                    }
                """.trimIndent()
            )
        }
        assertEquals(
            """       
            [
                {
                    "position": {
                        "path": "project/test.cau",
                        "breadcrumbs": "declarations.1.body.statements.0.expression.callee",
                        "position": "2:4-2:16"
                    },
                    "error": {
                        "#type": "NotInScope"
                    }
                }
            ]
            """.trimIndent(), vm.codeBundle.compileErrors.debug()
        )

        TestUtils.expectTypeError(vm.executeFunction("project/test.cau", "main", listOf()), vm)
    }

    @Test
    fun recursiveFunction() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/math (subtract)
                    
                    function count_down(number: Number): Number {
                        cause Debug(number)
                        branch {
                            if equals(number, 0) => number
                            else => count_down(subtract(number, 1))
                        }
                    }
                    
                    function main() {
                        let result = count_down(3)
                        cause Debug(result)
                    }
                """.trimIndent()
            )
        }
        TestUtils.printCompileErrors(vm)

        TestUtils.runMainExpectingDebugValues(
            vm, "project/test.cau", listOf(
                RuntimeValue.Number(3),
                RuntimeValue.Number(2),
                RuntimeValue.Number(1),
                RuntimeValue.Number(0),
                RuntimeValue.Number(0),
            )
        )
    }

    @Test
    fun innerRecursiveFunction() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/math (subtract)
                    
                    function count_down_from_3(): Number {
                        function count_down(number: Number): Number {
                            cause Debug(number)
                            branch {
                                if equals(number, 0) => number
                                else => count_down(subtract(number, 1))
                            }
                        }
                        count_down(3)
                    }
                    
                    function main() {
                        let result = count_down_from_3()
                        cause Debug(result)
                    }
                """.trimIndent()
            )
        }
        TestUtils.printCompileErrors(vm)

        TestUtils.runMainExpectingDebugValues(
            vm, "project/test.cau", listOf(
                RuntimeValue.Number(3),
                RuntimeValue.Number(2),
                RuntimeValue.Number(1),
                RuntimeValue.Number(0),
                RuntimeValue.Number(0),
            )
        )
    }

    @Test
    fun nestedRecursion() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    function main() {
                        function x(done: BinaryAnswer): Action {
                            cause Debug("x")
                            function y(done: BinaryAnswer): Action {
                                cause Debug("y")
                                function z(done: BinaryAnswer): Action {
                                    cause Debug("z")
                                    branch {
                                        if done => {}
                                        else => x(True)
                                    }
                                }
                                z(done)
                            }
                            y(done)
                        }
                        x(False)
                    }
                """.trimIndent()
            )
        }

        TestUtils.runMainExpectingDebugs(
            vm, "project/test.cau", listOf(
                "x", "y", "z", "x", "y", "z"
            )
        )
    }
}