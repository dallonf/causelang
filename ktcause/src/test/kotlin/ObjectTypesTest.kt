import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ObjectTypesTest {
    @Test
    fun defineObjectAndInstantiateIt() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                object Card(
                    suit: Text,
                    rank: Number,
                )
                
                function main(): Card {
                    Card("hearts", 3)
                }
            """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(
            """
            {
                "#type": "project/test.cau:Card",
                "suit": "hearts",
                "rank": 3
            }
            """.trimIndent(), result.debug()
        )
    }

    @Test
    fun retrieveValuesFromACustomObject() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    object Card(
                        suit: Text,
                        rank: Number,
                    )
                    
                    function main(): Text {
                        let card = Card("spades", 7)
                        card.suit
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(
            RuntimeValue.Text("spades"), result
        )
    }
}