import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.Resolver.debug
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
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
                RuntimeValue.String("Done!"),
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
                        "breadcrumbs": "declarations.2.body.statements.1.expression.body.statements.1.declaration.body.statements.0.expression.branches.0.body.expression",
                        "position": "10:37-10:42"
                    },
                    "error": {
                        "#type": "CannotBreakHere"
                    }
                }
            ]
            """.trimIndent(),
            vm.codeBundle.compileErrors.debug()
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
                RuntimeValue.String("Done!"),
            )
        )
    }

    fun abortsInfiniteLoops() {}

    fun canBreakWithValue() {}

}