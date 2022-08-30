import TestUtils.addFileAndPrintCompileErrors
import TestUtils.addFileExpectingNoCompileErrors
import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ObjectTypesTest {
    @Test
    fun defineObjectAndInstantiateIt() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
                object Card(
                    suit: String,
                    rank: Count,
                )
                
                function main(): Card {
                    Card("hearts", 3)
                }
            """.trimIndent()
        )

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(
            """
            {
                "#type": "project/test.cau:Card",
                "suit": "hearts",
                "rank": 3
            }
            """.trimIndent(),
            result.debug()
        )
    }

    @Test
    fun retrieveValuesFromACustomObject() {
        val vm = LangVm()
        vm.addFileAndPrintCompileErrors(
            "project/test.cau", """
                object Card(
                    suit: String,
                    rank: Count,
                )
                
                function main(): String {
                    let card = Card("spades", 7)
                    card.suit
                }
            """.trimIndent()
        )

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(
            RuntimeValue.String("spades"),
            result
        )
    }
}