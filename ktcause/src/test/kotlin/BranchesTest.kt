import TestUtils.addFileAndPrintCompileErrors
import TestUtils.addFileExpectingNoCompileErrors
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class BranchesTest {
    @Test
    fun branchExpressionReturnsValue() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
                import core/string (equals)
                
                function main() {
                    branch {
                        if equals("red", "blue") => "nope"
                        if equals("red", "red") => "yup"
                        else => "wut"
                    }
                }
            """.trimIndent()
        )

        val result = vm.executeFunction("project/test.cau", "main", listOf())
        assertEquals(RuntimeValue.String("yup"), result.expectReturnValue())
    }

    @Test
    fun branchWithOptionType() {
        val vm = LangVm()
        vm.addFileAndPrintCompileErrors(
            "project/test.cau", """
                object Hearts
                object Diamonds
                object Spades
                object Clubs
                option Suit(Hearts, Diamonds, Spades, Clubs)
                
                function main() {
                    process_suit(Hearts)
                    process_suit(Diamonds)
                    process_suit(Clubs)
                }                
                
                function process_suit(this: Suit) {
                    branch with this {
                        is Hearts => cause Debug("Hearts")
                        is Diamonds => cause Debug("Diamonds")
                        else => cause Debug("something else")
                    }
                }
            """.trimIndent()
        )

        TestUtils.runMainExpectingDebugs(
            vm, "project/test.cau", listOf(
                "Hearts",
                "Diamonds",
                "something else"
            )
        )
    }
}