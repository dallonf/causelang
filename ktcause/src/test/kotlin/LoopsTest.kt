import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test

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

        TestUtils.printCompileErrors(vm)

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
}