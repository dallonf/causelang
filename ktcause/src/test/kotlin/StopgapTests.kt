import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StopgapTests {
    @Test
    fun dictionary() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                import core/stopgap/collections (Dictionary, get_item, with_item_at_key, without_key, list_entries)
                
                function main() {
                    let dictionary = Dictionary()
                    let dictionary = with_item_at_key(dictionary, "batman", "Bruce Wayne")
                    cause Debug(get_item(dictionary, "batman"))
                    cause Debug(get_item(dictionary, "superman"))
                    
                    let dictionary = with_item_at_key(dictionary, "spider-man", "Peter Parker")
                    cause Debug(get_item(dictionary, "spider-man"))
                    let dictionary = with_item_at_key(dictionary, "spider-man", "Miles Morales")
                    cause Debug(get_item(dictionary, "spider-man"))
                    
                    let dictionary = with_item_at_key(dictionary, "flash", "Barry Allen")
                    let dictionary = without_key(dictionary, "spider-man")
                    
                    cause Debug(list_entries(dictionary))
                }
                """.trimIndent()
            )
        }

        TestUtils.printCompileErrors(vm)

        val output = TestUtils.runMainAndGetDebugValues(vm, "project/test.cau")
        assertEquals(RuntimeValue.Text("Bruce Wayne"), output[0])
        assertEquals(
            RuntimeValue.RuntimeObject(vm.codeBundle.getType("core/stopgap/collections.cau", "Empty"), emptyList()),
            output[1]
        )
        assertEquals(RuntimeValue.Text("Peter Parker"), output[2])
        assertEquals(RuntimeValue.Text("Miles Morales"), output[3])

        assertEquals(
            """
            {
                "#type": "core/stopgap/collections.cau:Stack",
                "top": {
                    "#type": "core/stopgap/collections.cau:KeyValuePair",
                    "key": "batman",
                    "value": "Bruce Wayne"
                },
                "next": {
                    "#type": "core/stopgap/collections.cau:Stack",
                    "top": {
                        "#type": "core/stopgap/collections.cau:KeyValuePair",
                        "key": "flash",
                        "value": "Barry Allen"
                    },
                    "next": {
                        "#type": "core/stopgap/collections.cau:Empty"
                    }
                }
            }
            """.trimIndent(), output[4].debug()
        )
    }
}