import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.debug
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ErrorHandlingBasicTest {
    @Test
    fun noArgumentsForSignal() {
        val vm = LangVm()
        vm.addFile(
            "project/hello.cau", """
                function main() {
                    cause Debug()
                }
            """.trimIndent()
        )

        assertEquals(
            vm.compileErrors.debug(),
            """
                [
                    {
                        "filePath": "project/hello.cau",
                        "location": "declarations.1.body.statements.0.signal",
                        "error": {
                            "type": "MissingParameters",
                            "names": [
                                "message"
                            ]
                        }
                    }
                ]
            """.trimIndent()
        )
    }
}