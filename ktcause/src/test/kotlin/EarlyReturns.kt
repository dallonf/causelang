import TestUtils.runMainExpectingDebugValues
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue
import com.dallonf.ktcause.types.InstanceValueLangType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EarlyReturns {
    @Test
    fun earlyReturnAction() {
        val vm = LangVm {
            addFile(
                "project/test.cau",
                """
                function main() {
                    cause Debug("Should print")
                    return
                    cause Debug("Should not print")
                }
            """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        TestUtils.runMainExpectingDebugs(vm, "project/test.cau", listOf("Should print"))
    }

    @Test
    fun earlyReturnAsCast() {
        val vm = LangVm {
            addFile(
                "project/test.cau",
                """
                    import core/math ( add )
                    
                    object Nothing
                    option MaybeNumber(Number, Nothing) 
                    
                    function main() {
                        cause Debug(maybe_add(1.0, 2.0))
                        cause Debug(maybe_add(1.0, Nothing))
                        cause Debug(maybe_add(Nothing, 2.0))
                        cause Debug(maybe_add(Nothing, Nothing))
                    }
                    
                    function maybe_add(this: MaybeNumber, other: MaybeNumber): MaybeNumber {
                        let this = branch with this {
                            is Nothing => return Nothing
                            is Number as i => i
                        }
                        let other = branch with other {
                            is Nothing => return Nothing
                            is Number as i => i
                        }
                        
                        add(this, other)
                    }
                """.trimIndent()
            )

        }
        TestUtils.expectNoCompileErrors(vm)
        val nothing = RuntimeValue.RuntimeTypeConstraint(
            InstanceValueLangType(vm.codeBundle.getType("project/test.cau", "Nothing").id)
        )
        runMainExpectingDebugValues(
            vm, "project/test.cau", listOf(
                RuntimeValue.Number(3),
                nothing,
                nothing,
                nothing
            )
        )
    }

    @Test
    fun earlyReturnFromEffect() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/text (append)
                    
                    function main() {
                        signal Return(result: Text): NeverContinues
                        effect for Return as it {
                            return it.result
                        }
                        
                        cause Return("hello")
                        "goodbye"
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        assertEquals(
            RuntimeValue.Text("hello"),
            vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        )
    }
}