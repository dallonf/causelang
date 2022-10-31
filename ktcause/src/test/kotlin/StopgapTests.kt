import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RunResult
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

        TestUtils.expectNoCompileErrors(vm)

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

    @Test
    fun list() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/stopgap/collections (List, count, at_index, nth, nth_last, append, with_item_at_index, with_nth_item, insert_at_index, insert_nth_item)
                    
                    signal ExpectEqual(expected: Anything, actual: Anything): Action
                    
                    function main() {
                        let list = List()
                        let list = list>>append("hello")>>append("world")
                        cause ExpectEqual(2, list>>count())
                        cause ExpectEqual("hello", list>>nth(1))
                        cause ExpectEqual("world", list>>at_index(1))
                        
                        let list = list>>insert_at_index(1, "there")
                        cause ExpectEqual(3, list>>count()) // hello there world
                        cause ExpectEqual("there", list>>nth_last(2))
                        cause ExpectEqual("world", list>>nth_last(1))
                        
                        let list = list>>insert_nth_item(1, "oh") // oh hello there world
                        cause ExpectEqual(4, list>>count()) // hello there world
                        cause ExpectEqual("oh", list>>nth(1))
                        cause ExpectEqual("hello", list>>nth(2))
                        
                        let list = list>>with_nth_item(2, "hi")>>with_item_at_index(3, "universe")
                        cause ExpectEqual(
                            List()>>append("oh")>>append("hi")>>append("there")>>append("universe"),
                            list,
                        )
                    }
                """.trimIndent()
            )
        }
        TestUtils.printCompileErrors(vm)

        var result = vm.executeFunction("project/test.cau", "main", listOf())
        while (result is RunResult.Caused) {
            assertEquals(
                vm.codeBundle.getTypeId("project/test.cau", "ExpectEqual"), result.signal.typeDescriptor.id,
                "${vm.getExecutionTrace()}\nerror: ${result.signal.debug()}"
            )
            val expected = result.signal.values[0]
            val actual = result.signal.values[1]
            assertEquals(
                expected,
                actual,
                "${vm.getExecutionTrace()}\nExpected ${expected.debug()}, got ${actual.debug()}."
            )
            result = vm.resumeExecution(RuntimeValue.Action)
        }

        assertEquals(RuntimeValue.Action, result.expectReturnValue())
    }
}