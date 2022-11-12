import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CommentsTest {
    @Test
    fun helloWithComments() {
        val vm = LangVm {
            addFile(
                "project/hello.cau", """
                    // top comment
                    
                    /*
                     * Big multiline docs-y comment
                     * probably not how inline docs would actually work
                     * but it should work syntactically
                     */
                    function main() {
                        // more comments
                        cause Debug(/* inline comment */ "Hello world!")
                        // even more
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val result = vm.executeFunction("project/hello.cau", "main", listOf()).expectCausedSignal()
        assertEquals(
            """
                {
                    "#type": "core/builtin.cau:Debug",
                    "value": "Hello world!"
                }
            """.trimIndent(), result.debug()
        )
    }
}