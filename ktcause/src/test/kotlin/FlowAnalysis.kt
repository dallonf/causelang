import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.Resolver.debug
import com.dallonf.ktcause.RunResult
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FlowAnalysisTest {

    @Test
    fun blockWithAnEarlyReturnIsNeverContinues() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """                    
                    function main() {
                        let test: NeverContinues = {
                            cause Debug("one")
                            return
                            cause Debug("two")
                        }
                        cause Debug(test)
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        TestUtils.runMainExpectingDebugValues(
            vm, "project/test.cau", listOf(
                RuntimeValue.Text("one"),
            )
        )
    }
}