import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.Resolver.debug
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class FunctionPiping {
    @Test
    fun canPipeTextAppend() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/text (append, number_to_text)
                    
                    function main(name: Text, favorite_number: Number) {
                        "hello">>append(", ")>>append(name)>>append("!")
                            >>append(" ")
                            >>append("My favorite number is")
                            >>append(" ")
                            >>append(favorite_number>>number_to_text())
                    }
                """.trimIndent()
            )
        }

        TestUtils.expectNoCompileErrors(vm)
        vm.executeFunction(
            "project/test.cau", "main", listOf(
                RuntimeValue.Text("Douglas"), RuntimeValue.Number(42)
            )
        ).expectReturnValue().let {
            assertEquals(RuntimeValue.Text("hello, Douglas! My favorite number is 42"), it)
        }
    }

    @Test
    fun handlesPipesWithDifferentParameterTypes() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/text (append, number_to_text)
                    
                    function append_number(it: Text, number: Number) {
                        it>>append(number>>number_to_text())
                    }
                    
                    function main() {
                        "hello, ">>append_number(42)                        
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)
        vm.executeFunction(
            "project/test.cau", "main", listOf()
        ).expectReturnValue().let {
            assertEquals(RuntimeValue.Text("hello, 42"), it)
        }
    }

    @Test
    fun handlesPipesWithOneParameter() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/text (append, number_to_text)
                    
                    function exclaim(it: Text) {
                        it>>append("!")
                    }
                    
                    function main() {
                        "hello">>exclaim()                        
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)
        vm.executeFunction(
            "project/test.cau", "main", listOf()
        ).expectReturnValue().let {
            assertEquals(RuntimeValue.Text("hello!"), it)
        }
    }

    @Test
    fun reportsErrorsIfNotFound() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """                 
                    function main() {
                        "hello">>greet("world")
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
                        "breadcrumbs": "declarations.1.body.statements.0.expression.callee",
                        "position": "2:13-2:18"
                    },
                    "error": {
                        "#type": "NotInScope"
                    }
                }
            ]
            """.trimIndent(), vm.codeBundle.compileErrors.debug()
        )

        TestUtils.expectTypeError(
            vm.executeFunction(
                "project/test.cau", "main", listOf()
            ), vm
        )
    }

    @Test
    fun reportsErrorIfSubjectMistyped() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/text (append)
                    
                    function greet(it: Text) {
                        "Hello, ">>append(it)
                    }
                    
                    function main() {
                        42>>greet()
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
                        "breadcrumbs": "declarations.3.body.statements.0.expression.subject",
                        "position": "8:4-8:6"
                    },
                    "error": {
                        "#type": "MismatchedType",
                        "expected": {
                            "valueType": {
                                "#type": "Primitive",
                                "kind": "Text"
                            }
                        },
                        "actual": {
                            "#type": "Primitive",
                            "kind": "Number"
                        }
                    }
                }
            ]
            """.trimIndent(), vm.codeBundle.compileErrors.debug()
        )

        val error = assertThrows<LangVm.VmError> {
            vm.executeFunction(
                "project/test.cau", "main", listOf()
            )
        }
        assertEquals(
            """
            I couldn't call a builtin function: {
                "#type": "ProxyError",
                "actualError": {
                    "#type": "MismatchedType",
                    "expected": {
                        "valueType": {
                            "#type": "Primitive",
                            "kind": "Text"
                        }
                    },
                    "actual": {
                        "#type": "Primitive",
                        "kind": "Number"
                    }
                },
                "proxyChain": [
                    {
                        "#type": "SourcePosition",
                        "path": "project/test.cau",
                        "breadcrumbs": "declarations.2.body.statements.0.expression",
                        "position": "4:13-4:25"
                    }
                ]
            }
            """.trimIndent(), error.message
        )
    }

    @Test
    fun attemptToPipeConstraint() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    object SomeType(value: Text)
                    
                    function print_some_type(some_type: SomeType) {
                        cause Debug(some_type.value)
                    }
                    
                    function main() {
                        SomeType>>print_some_type()
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
                        "breadcrumbs": "declarations.3.body.statements.0.expression.subject",
                        "position": "8:4-8:12"
                    },
                    "error": {
                        "#type": "MismatchedType",
                        "expected": {
                            "valueType": {
                                "#type": "Instance",
                                "canonicalType": "project/test.cau:SomeType"
                            }
                        },
                        "actual": {
                            "#type": "Constraint",
                            "valueType": {
                                "#type": "Instance",
                                "canonicalType": "project/test.cau:SomeType"
                            }
                        }
                    }
                }
            ]
            """.trimIndent(), vm.codeBundle.compileErrors.debug()
        )
        val error = assertThrows<LangVm.VmError> {
            vm.executeFunction(
                "project/test.cau", "main", listOf()
            )
        }
        assertEquals(
            """
            I tried to get a member from a bad value: {
                "#type": "BadValue",
                "position": {
                    "#type": "SourcePosition",
                    "path": "project/test.cau",
                    "breadcrumbs": "declarations.3.body.statements.0.expression.subject",
                    "position": "8:4-8:12"
                },
                "error": {
                    "#type": "MismatchedType",
                    "expected": {
                        "valueType": {
                            "#type": "Instance",
                            "canonicalType": "project/test.cau:SomeType"
                        }
                    },
                    "actual": {
                        "#type": "Constraint",
                        "valueType": {
                            "#type": "Instance",
                            "canonicalType": "project/test.cau:SomeType"
                        }
                    }
                }
            }.
            """.trimIndent(), error.message
        )
    }
}