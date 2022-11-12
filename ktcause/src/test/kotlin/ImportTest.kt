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
        assertEquals(emptyList(), builder.requiredFilePaths)

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
                    
                    object Card(name: Text)
                    
                    function announce_card() {
                        let card = Card("Ace of Spades")
                        cause Debug(get_name(card))
                    }
                """.trimIndent()
            )

            addFile(
                "project/b.cau", """
                    import project/a ( Card )
                    
                    function get_name(card: Card): Text {
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

    @Test
    fun relativeImports() {
        val builder = CodeBundleBuilder()
        builder.addFile(
            "project/test.cau", """
                import ./support ( print_hello )
                
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
    fun relativeImportsAcrossDirectories() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import ./card/suit (hearts)
                    
                    function main() {
                        hearts()
                    }
                """.trimIndent()
            )

            addFile(
                "project/util.cau", """
                    function print(message: Text) {
                        cause Debug(message)
                    }
                """.trimIndent()
            )

            addFile(
                "project/card/suit.cau", """
                    import ../util (print)
                    
                    function hearts() {
                        print("hearts")
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        TestUtils.runMainExpectingDebugs(vm, "project/test.cau", listOf("hearts"))
    }

    @Test
    fun cantGoUpIndefinitely() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                import ../core/math (subtract)
                import ./inner/a (four)
                
                function main() {
                    subtract(four(), 1)
                }
                """.trimIndent()
            )

            addFile(
                "project/inner/a.cau", """
                import ../../core/math (add)
                import ../../../../../what (what)
                
                function four() {
                    add(2, 2)
                }
                """.trimIndent()
            )
        }

        assertEquals(
            """
            [
                {
                    "position": {
                        "path": "project/inner/a.cau",
                        "breadcrumbs": "declarations.1.mappings.0",
                        "position": "1:24-1:27"
                    },
                    "error": {
                        "#type": "ImportPathInvalid"
                    }
                },
                {
                    "position": {
                        "path": "project/inner/a.cau",
                        "breadcrumbs": "declarations.2.mappings.0",
                        "position": "2:28-2:32"
                    },
                    "error": {
                        "#type": "ImportPathInvalid"
                    }
                },
                {
                    "position": {
                        "path": "project/test.cau",
                        "breadcrumbs": "declarations.1.mappings.0",
                        "position": "1:21-1:29"
                    },
                    "error": {
                        "#type": "ImportPathInvalid"
                    }
                }
            ]
            """.trimIndent(), vm.codeBundle.compileErrors.debug()
        )
    }

    @Test
    fun renamesAnImportToAvoidAConflict() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/text (append as append_text)
                    
                    function append() {
                        cause Debug("append function")
                    }
                    
                    function main() {
                        append()
                        cause Debug("hello, ">>append_text("world!"))
                    }
                """.trimIndent()
            )
        }

        TestUtils.expectNoCompileErrors(vm)

        TestUtils.runMainExpectingDebugs(vm, "project/test.cau", listOf(
            "append function",
            "hello, world!",
        ))
    }
}