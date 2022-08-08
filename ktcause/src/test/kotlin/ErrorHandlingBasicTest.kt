import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.Resolver.debug
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
            vm.compileErrors.debug(), """
                [
                    {
                        "position": {
                            "path": "project/hello.cau",
                            "breadcrumbs": "declarations.1.body.statements.0.expression.signal",
                            "position": "2:15-2:16"
                        },
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

        val result = vm.executeFunction("project/hello.cau", "main", listOf())
        val badValue = TestUtils.expectTypeError(result, vm)
        assertEquals(
            badValue.debug(), """
            {
                "#type": "BadValue",
                "position": {
                    "type": "SourcePosition",
                    "path": "project/hello.cau",
                    "breadcrumbs": "declarations.1.body.statements.0.expression",
                    "position": "2:4-2:16"
                },
                "error": {
                    "type": "ProxyError",
                    "actualError": {
                        "type": "MissingParameters",
                        "names": [
                            "message"
                        ]
                    },
                    "proxyChain": [
                        {
                            "type": "SourcePosition",
                            "path": "project/hello.cau",
                            "breadcrumbs": "declarations.1.body.statements.0.expression.signal",
                            "position": "2:15-2:16"
                        }
                    ]
                }
            }
            """.trimIndent()
        )
    }
}