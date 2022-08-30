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
    fun fourFunctionCount() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
                import core/math (add_count, subtract_count, multiply_count, divide_count)
                            
                function main() {
                    divide_count(multiply_count(subtract_count(add_count(1, 2), 5), 6), 3)
                }
            """.trimIndent()
        )

        vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue().let {
            assertEquals(RuntimeValue.Count(-4), it)
        }
    }

    @Test
    fun roundingAndConversion() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
                import core/math (round_to_count, floor_to_count, ceiling_to_count, to_number)
                            
                function main() {
                    cause Debug(to_number(3))
                    cause Debug(round_to_count(1.2))
                    cause Debug(round_to_count(2.5))
                    cause Debug(round_to_count(3.7))
                    cause Debug(round_to_count(4.0))
                    cause Debug(floor_to_count(1.2))
                    cause Debug(floor_to_count(2.5))
                    cause Debug(floor_to_count(3.7))
                    cause Debug(floor_to_count(4.0))
                    cause Debug(ceiling_to_count(1.2))
                    cause Debug(ceiling_to_count(2.5))
                    cause Debug(ceiling_to_count(3.7))
                    cause Debug(ceiling_to_count(4.0))
                }
            """.trimIndent()
        )

        TestUtils.runMainExpectingDebugValues(vm, "project/test.cau", listOf(
            RuntimeValue.Number(3),
            // rounding
            RuntimeValue.Count(1),
            RuntimeValue.Count(3),
            RuntimeValue.Count(4),
            RuntimeValue.Count(4),
            // flooring
            RuntimeValue.Count(1),
            RuntimeValue.Count(2),
            RuntimeValue.Count(3),
            RuntimeValue.Count(4),
            // ceiling...ing
            RuntimeValue.Count(2),
            RuntimeValue.Count(3),
            RuntimeValue.Count(4),
            RuntimeValue.Count(4),
        ))
    }
}