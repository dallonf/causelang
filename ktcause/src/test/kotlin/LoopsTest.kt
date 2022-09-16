import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.Resolver.debug
import com.dallonf.ktcause.RunResult
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals

class LoopsTest {
    @Test
    fun basicLoop() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/math (add, at_least)
                    
                    function main() {
                        let variable i = 0
                        loop {
                            cause Debug(i)
                            set i = add(i, 1)
                            branch {
                                if at_least(i, 5) => break
                                else => {}
                            }
                        }
                        cause Debug("Done!")
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        TestUtils.runMainExpectingDebugValues(
            vm, "project/test.cau", listOf(
                RuntimeValue.Number(0),
                RuntimeValue.Number(1),
                RuntimeValue.Number(2),
                RuntimeValue.Number(3),
                RuntimeValue.Number(4),
                RuntimeValue.Text("Done!"),
            )
        )
    }

    @Test
    fun cannotBreakInsideFunction() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/math (add, at_least)
                    
                    function main() {
                        let variable i = 0
                        loop {
                            set i = add(i, 1)
                            
                            function break_on_5(i: Number) {
                                branch {
                                    if at_least(i, 5) => break
                                    else => {}
                                }
                            }
                            break_on_5(i)
                        }
                        cause Debug("Done!")
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
                        "breadcrumbs": "declarations.2.body.statements.1.expression.body.statements.1.declaration.body.statements.0.expression.branches.0.body.statement.expression",
                        "position": "10:37-10:42"
                    },
                    "error": {
                        "#type": "CannotBreakHere"
                    }
                }
            ]
            """.trimIndent(), vm.codeBundle.compileErrors.debug()
        )

        TestUtils.expectTypeError(vm.executeFunction("project/test.cau", "main", listOf()), vm)
    }

    @Test
    fun canUseEffectToBreak() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/math (add, at_least)
                    
                    signal BreakMainLoop: NeverContinues
                        
                    function main() {
                        let variable i = 0
                        loop {
                            cause Debug(i)
                            
                            set i = add(i, 1)
                            
                            effect for BreakMainLoop {
                                break
                            }
                            
                            function break_on_3(i: Number) {
                                branch {
                                    if at_least(i, 3) => cause BreakMainLoop
                                    else => {}
                                }
                            }
                            break_on_3(i)
                        }
                        cause Debug("Done!")
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        TestUtils.runMainExpectingDebugValues(
            vm, "project/test.cau", listOf(
                RuntimeValue.Number(0),
                RuntimeValue.Number(1),
                RuntimeValue.Number(2),
                RuntimeValue.Text("Done!"),
            )
        )
    }

    @Test
    fun abortsInfiniteLoops() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                function main() {
                    loop { }
                    cause Debug("Done!")
                }
            """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        vm.executeFunction("project/test.cau", "main", listOf()).expectCausedSignal().let {
            assertEquals(vm.codeBundle.getBuiltinTypeId("RunawayLoop"), it.typeDescriptor.id)
        }
    }

    @Test
    fun allowsLongLoopsByResettingCounters() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/math (add, at_least)
                    
                    signal Progress(i: Number): Action
                    
                    function main(stop_at: Number) {
                        let variable i = 0
                        loop {
                            cause Progress(i)
                            set i = add(i, 1)
                            branch {
                                if at_least(i, stop_at) => break
                                else => {}
                            }
                        }
                        cause Debug("Done!")
                    }
            """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val stopAt = vm.options.runawayLoopThreshold!! * 2

        var current = vm.executeFunction("project/test.cau", "main", listOf(RuntimeValue.Number(stopAt.toBigDecimal())))
        var count: BigDecimal = 0.toBigDecimal()
        val progress = vm.codeBundle.getTypeId("project/test.cau", "Progress")
        while (current is RunResult.Caused && current.signal.typeDescriptor.id == progress) {
            count = (current.signal.values[0] as RuntimeValue.Number).value
            vm.reportTick()
            current = vm.resumeExecution(RuntimeValue.Action)
        }
        current.expectCausedSignal().let {
            assertEquals(vm.codeBundle.getBuiltinTypeId("Debug"), it.typeDescriptor.id)
            assertEquals(RuntimeValue.Text("Done!"), it.values[0])
        }
        assertEquals((stopAt - 1).toBigDecimal(), count)
        vm.resumeExecution(RuntimeValue.Action).expectReturnValue().let {
            assertEquals(RuntimeValue.Action, it)
        }
    }

    @Test
    fun canBreakWithValue() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/math ( add )
                    
                    object Wrapper(item: WrapperItem)
                    option WrapperItem(Number, Wrapper)
                    
                    function main() {
                        let wrapper = Wrapper(42)
                        let wrapper = Wrapper(Wrapper(Wrapper(Wrapper(wrapper))))
                        
                        let variable count = 0
                        let variable currentWrapper = wrapper 
                        let inner = loop {
                            set count = add(count, 1)
                            branch with currentWrapper.item {
                                is Number as number => break with number
                                is Wrapper as wrapper => set currentWrapper = wrapper
                            }
                        }
                        cause Debug(inner)
                        cause Debug(count)
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        TestUtils.runMainExpectingDebugValues(
            vm, "project/test.cau", listOf(RuntimeValue.Number(42), RuntimeValue.Number(5))
        )
    }

}