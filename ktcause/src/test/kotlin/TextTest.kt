import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test

class TextTest {
    @Test
    fun countsCharacters() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/text ( count_characters )
                    
                    function main() {
                        cause Debug(count_characters("howdy"))
                        cause Debug(count_characters("😀"))
                        cause Debug(count_characters("👨‍👩‍👧‍👦"))
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)
        TestUtils.runMainExpectingDebugValues(
            vm, "project/test.cau", listOf(
                RuntimeValue.Number(5),
                RuntimeValue.Number(1),
                RuntimeValue.Number(1),
            )
        )
    }

    @Test
    fun slices() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/text ( slice_index, slice_nth )
                    
                    function main() {
                        cause Debug(slice_index("hello world", 0, 5))
                        cause Debug(slice_nth("hello world", 1, 5))
                        cause Debug(slice_index("earth", 1, 4))
                        cause Debug(slice_nth("earth", 2, 4))
                        cause Debug(slice_index("🤣😂😁", 1, 2))
                        cause Debug(slice_nth("🤣😂😁", 2, 2))
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)
        TestUtils.runMainExpectingDebugValues(
            vm, "project/test.cau", listOf(
                RuntimeValue.Text("hello"),
                RuntimeValue.Text("hello"),
                RuntimeValue.Text("art"),
                RuntimeValue.Text("art"),
                RuntimeValue.Text("\uD83D\uDE02"),
                RuntimeValue.Text("\uD83D\uDE02"),
            )
        )
    }
}