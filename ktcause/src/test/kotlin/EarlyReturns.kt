import TestUtils.addFileExpectingNoCompileErrors
import TestUtils.runMainExpectingDebugValues
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue
import com.dallonf.ktcause.types.InstanceValueLangType
import org.junit.jupiter.api.Test

class EarlyReturns {
    @Test
    fun earlyReturnAction() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau",
            """
                function main() {
                    cause Debug("Should print")
                    return
                    cause Debug("Should not print")
                }
            """.trimIndent()
        )

        TestUtils.runMainExpectingDebugs(vm, "project/test.cau", listOf("Should print"))
    }

    @Test
    fun earlyReturnAsCast() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
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
                
                function maybe_add(this: MaybeNumber, other: MaybeNumber): MaybeInteger {
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

        val nothing = RuntimeValue.RuntimeTypeConstraint(
            InstanceValueLangType(vm.getType("project/test.cau", "Nothing"))
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
}