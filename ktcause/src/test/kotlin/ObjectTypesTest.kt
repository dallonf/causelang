import TestUtils.addFileAndPrintCompileErrors
import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ObjectTypesTest {
    @Test
    fun defineObjectAndInstantiateIt() {
        val vm = LangVm()
        vm.addFileAndPrintCompileErrors(
            "project/test.cau", """
                object Card(
                    suit: String,
                    rank: Integer,
                )
                
                function main(): Card {
                    Card("hearts", 3)
                }
            """.trimIndent()
        )

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(
            """
            
            """.trimIndent(),
            result.debug()
        )
    }
}