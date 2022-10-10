import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MathTest {
    @Test
    fun fourFunction() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                import core/math (add, subtract, multiply, divide)
                            
                function main() {
                    divide(multiply(add(1, subtract(3, 0.5)), 4), 7)
                }
            """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue().let {
            assertEquals(RuntimeValue.Number(2), it)
        }
    }

    @Test
    fun rounding() {
        val vm = LangVm {
            addFile(
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
        }
        TestUtils.expectNoCompileErrors(vm)

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

    @Test
    fun fractions() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/math (divide, multiply)
                    
                    function main(): Number {
                        let value = divide(1, 3)
                        multiply(value, 6)
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue().let {
            assertEquals(RuntimeValue.Number(2), it)
        }
    }

    @Test
    fun naiveFizzBuzz() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                import core/text (number_to_text)
                import core/math (remainder)
                
                function fizz_buzz(input: Number): Text { 
                    branch {
                        if equals(remainder(input, 15), 0) => "FizzBuzz"
                        if equals(remainder(input, 3), 0) => "Fizz"
                        if equals(remainder(input, 5), 0) => "Buzz"
                        else => number_to_text(input)
                    }
                }
            """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val list = (1..20).map { i ->
            vm.executeFunction("project/test.cau", "fizz_buzz", listOf(RuntimeValue.Number(i.toLong())))
                .expectReturnValue().let { (it as RuntimeValue.Text).value }
        }
        val expected = listOf<String>(
            "1",
            "2",
            "Fizz",
            "4",
            "Buzz",
            "Fizz",
            "7",
            "8",
            "Fizz",
            "Buzz",
            "11",
            "Fizz",
            "13",
            "14",
            "FizzBuzz",
            "16",
            "17",
            "Fizz",
            "19",
            "Buzz"
        )
        assertEquals(expected, list)
    }
}