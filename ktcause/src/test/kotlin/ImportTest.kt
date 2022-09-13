import com.dallonf.ktcause.CodeBundleBuilder
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.Resolver.debug
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

    @Test
    fun gracefullyHandlesCircularReference() {
        // primarily making sure it doesn't get hung up in an infinite loop
        // TODO: better error handling
        // or just support it
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import project/a (announce_card)
                    
                    function main() {
                        announce_card()
                    }
                """.trimIndent()
            )

            addFile(
                "project/a.cau", """
                    import project/b ( get_name )
                    
                    object Card(name: String)
                    
                    function announce_card() {
                        let card = Card("Ace of Spades")
                        cause Debug(get_name(card))
                    }
                """.trimIndent()
            )

            addFile(
                "project/b.cau", """
                    import project/a ( Card )
                    
                    function get_name(card: Card): String {
                        card.name
                    }
                """.trimIndent()
            )
        }

        assertEquals(
            """
            [
                {
                    "position": {
                        "path": "project/test.cau",
                        "breadcrumbs": "declarations.1.mappings.0",
                        "position": "1:18-1:31"
                    },
                    "error": {
                        "#type": "FileNotFound"
                    }
                },
                {
                    "position": {
                        "path": "project/a.cau",
                        "breadcrumbs": "declarations.1.mappings.0",
                        "position": "1:19-1:27"
                    },
                    "error": {
                        "#type": "FileNotFound"
                    }
                }
            ]
            """.trimIndent(), vm.codeBundle.compileErrors.debug()
        )
    }
}