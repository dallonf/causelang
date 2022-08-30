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
                    divide(multiply(add(1.0, subtract(3.0, 0.5)), 4.0), 7.0)
                }
            """.trimIndent()
        )

        vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue().let {
            assertEquals(RuntimeValue.Number(2), it)
        }
    }

    @Test
    fun fourFunctionWhole() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
                import core/math (add_whole, subtract_whole, multiply_whole, divide_whole)
                            
                function main() {
                    divide_whole(multiply_whole(subtract_whole(add_whole(1, 2), 5), 6), 3)
                }
            """.trimIndent()
        )

        vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue().let {
            assertEquals(RuntimeValue.WholeNumber(-4), it)
        }
    }

    @Test
    fun roundingAndConversion() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
                import core/math (round, floor, ceiling, to_number)
                            
                function main() {
                    cause Debug(to_number(3))
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
                RuntimeValue.Number(3),
                // rounding
                RuntimeValue.WholeNumber(1),
                RuntimeValue.WholeNumber(3),
                RuntimeValue.WholeNumber(4),
                RuntimeValue.WholeNumber(4),
                // flooring
                RuntimeValue.WholeNumber(1),
                RuntimeValue.WholeNumber(2),
                RuntimeValue.WholeNumber(3),
                RuntimeValue.WholeNumber(4),
                // ceiling...ing
                RuntimeValue.WholeNumber(2),
                RuntimeValue.WholeNumber(3),
                RuntimeValue.WholeNumber(4),
                RuntimeValue.WholeNumber(4),
            )
        )
    }
}