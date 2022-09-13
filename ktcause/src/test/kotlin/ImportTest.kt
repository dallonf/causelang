import com.dallonf.ktcause.CodeBundleBuilder
import com.dallonf.ktcause.LangVm
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ImportTest {
    @Test
    fun importsFromAnotherFile() {
        val builder = CodeBundleBuilder()
        builder.addFile(
            "project/test.cau", """
                import project/support ( print_hello )
                
                function main() {
                    print_hello()
                }
            """.trimIndent()
        )
        assertEquals(listOf("project/support.cau"), builder.requiredFilePaths)
        builder.addFile(
            "project/support.cau", """
                function print_hello() {
                    cause Debug("Hello World")
                }
            """.trimIndent()
        )

        val vm = LangVm(builder.build())
        TestUtils.expectNoCompileErrors(vm)

        TestUtils.runMainExpectingDebugs(vm, "project/test.cau", listOf("Hello World"))
    }
}