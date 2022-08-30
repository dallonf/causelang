import TestUtils.addFileExpectingNoCompileErrors
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MathTest {
    @Test
    fun fourFunction() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
                import core/math (add, subtract, multiply, divide)
                            
                function main() {
                    divide(multiply(add(1, subtract(3, 0.5)), 4), 7)
                }
            """.trimIndent()
        )

        vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue().let {
            assertEquals(RuntimeValue.Number(2), it)
        }
    }

    @Test
    fun roundingAndConversion() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
                import core/math (round, floor, ceiling)
                            
                function main() {
                    cause Debug(round(1.2))
                    cause Debug(round(2.5))
                    cause Debug(round(3.7))
                    cause Debug(round(4.0))
                    cause Debug(floor(1.2))
                    cause Debug(floor(2.5))
                    cause Debug(floor(3.7))
                    cause Debug(floor(4.0))
                    cause Debug(ceiling(1.2))
                    cause Debug(ceiling(2.5))
                    cause Debug(ceiling(3.7))
                    cause Debug(ceiling(4.0))
                }
            """.trimIndent()
        )

        TestUtils.runMainExpectingDebugValues(
            vm, "project/test.cau", listOf(
                // rounding
                RuntimeValue.Number(1),
                RuntimeValue.Number(3),
                RuntimeValue.Number(4),
                RuntimeValue.Number(4),
                // flooring
                RuntimeValue.Number(1),
                RuntimeValue.Number(2),
                RuntimeValue.Number(3),
                RuntimeValue.Number(4),
                // ceiling...ing
                RuntimeValue.Number(2),
                RuntimeValue.Number(3),
                RuntimeValue.Number(4),
                RuntimeValue.Number(4),
            )
        )
    }
}