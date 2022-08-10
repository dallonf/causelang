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
        assertEquals(result.expectReturnValue(), RuntimeValue.String("yup"))
    }
}