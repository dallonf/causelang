import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.Resolver.debug
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ErrorHandlingBasicTest {
    @Test
    fun noArgumentsForSignal() {
        val vm = LangVm {
            addFile(
                "project/hello.cau", """
                    function main() {
                        cause Debug()
                    }
               """.trimIndent()
            )
        }

        assertEquals(
            """
                [
                    {
                        "position": {
                            "path": "project/hello.cau",
                            "breadcrumbs": "declarations.1.body.statements.0.expression.signal",
                            "position": "2:10-2:17"
                        },
                        "error": {
                            "#type": "MissingParameters",
                            "names": [
                                "value"
                            ]
                        }
                    }
                ]
            """.trimIndent(),
            vm.codeBundle.compileErrors.debug(),
        )

        val result = vm.executeFunction("project/hello.cau", "main", listOf())
        val badValue = TestUtils.expectTypeError(result, vm)
        assertEquals(
            """
            {
                "#type": "BadValue",
                "position": {
                    "#type": "SourcePosition",
                    "path": "project/hello.cau",
                    "breadcrumbs": "declarations.1.body.statements.0.expression",
                    "position": "2:4-2:17"
                },
                "error": {
                    "#type": "ProxyError",
                    "actualError": {
                        "#type": "MissingParameters",
                        "names": [
                            "value"
                        ]
                    },
                    "proxyChain": [
                        {
                            "#type": "SourcePosition",
                            "path": "project/hello.cau",
                            "breadcrumbs": "declarations.1.body.statements.0.expression.signal",
                            "position": "2:10-2:17"
                        }
                    ]
                }
            }
            """.trimIndent(),
            badValue.debug(),
        )
    }

    @Test
    fun mistypedConstructParameter() {
        val vm = LangVm {
            addFile(
                "project/hello.cau", """
                    signal ExpectText(message: Text): Action
                    
                    function main() {
                        cause ExpectText(1)
                    }
                """.trimIndent()
            )
        }

        assertEquals(
            """
            [
                {
                    "position": {
                        "path": "project/hello.cau",
                        "breadcrumbs": "declarations.2.body.statements.0.expression.signal.parameters.0",
                        "position": "4:21-4:22"
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
            """.trimIndent(),
            vm.codeBundle.compileErrors.debug(),
        )

        val result = vm.executeFunction("project/hello.cau", "main", listOf())
        assertEquals(
            """
            {
                "#type": "BadValue",
                "position": {
                    "#type": "SourcePosition",
                    "path": "project/hello.cau",
                    "breadcrumbs": "declarations.2.body.statements.0.expression.signal.parameters.0",
                    "position": "4:21-4:22"
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
            """.trimIndent(),
            TestUtils.expectInvalidSignal(result).debug(),
        )
    }

    @Test
    fun mistypedCallParameter() {
        val vm = LangVm {
            addFile(
                "project/hello.cau", """               
                    function main() {
                        expect_text(1)
                    }
                    
                    function expect_text(message: Text) {
                        cause Debug(message)
                    }
                """.trimIndent()
            )
        }

        assertEquals(
            """
            [
                {
                    "position": {
                        "path": "project/hello.cau",
                        "breadcrumbs": "declarations.1.body.statements.0.expression.parameters.0",
                        "position": "2:16-2:17"
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

        val result = vm.executeFunction("project/hello.cau", "main", listOf())
        assertEquals(
            """
            {
                "#type": "core/builtin.cau:Debug",
                "value": {
                    "#type": "BadValue",
                    "position": {
                        "#type": "SourcePosition",
                        "path": "project/hello.cau",
                        "breadcrumbs": "declarations.1.body.statements.0.expression.parameters.0",
                        "position": "2:16-2:17"
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
            }
            """.trimIndent(),
            result.expectCausedSignal().debug(),
        )
    }

    @Test
    fun causeNonSignal() {
        val vm = LangVm {
            addFile(
                "project/hello.cau", """
                    function main() {
                        cause "oops"
                    }
                """.trimIndent()
            )
        }

        assertEquals(
            vm.codeBundle.compileErrors.debug(), """
            [
                {
                    "position": {
                        "path": "project/hello.cau",
                        "breadcrumbs": "declarations.1.body.statements.0.expression",
                        "position": "2:4-2:16"
                    },
                    "error": {
                        "#type": "NotCausable"
                    }
                }
            ]
            """.trimIndent()
        )

        val result = vm.executeFunction("project/hello.cau", "main", listOf())
        assertEquals(
            TestUtils.expectTypeError(result, vm).debug(), """
            {
                "#type": "BadValue",
                "position": {
                    "#type": "SourcePosition",
                    "path": "project/hello.cau",
                    "breadcrumbs": "declarations.1.body.statements.0.expression",
                    "position": "2:4-2:16"
                },
                "error": {
                    "#type": "NotCausable"
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun nonExistentSignal() {
        val vm = LangVm {
            addFile(
                "project/hello.cau", """
                    function main() {
                      cause DoesntExist("oops")
                    }
                """.trimIndent()
            )
        }

        assertEquals(
            """
            [
                {
                    "position": {
                        "path": "project/hello.cau",
                        "breadcrumbs": "declarations.1.body.statements.0.expression.signal.callee",
                        "position": "2:8-2:19"
                    },
                    "error": {
                        "#type": "NotInScope"
                    }
                }
            ]
            """.trimIndent(), vm.codeBundle.compileErrors.debug()
        )

        val result = vm.executeFunction("project/hello.cau", "main", listOf())
        assertEquals(
            """
            {
                "#type": "BadValue",
                "position": {
                    "#type": "SourcePosition",
                    "path": "project/hello.cau",
                    "breadcrumbs": "declarations.1.body.statements.0.expression",
                    "position": "2:2-2:27"
                },
                "error": {
                    "#type": "ProxyError",
                    "actualError": {
                        "#type": "NotInScope"
                    },
                    "proxyChain": [
                        {
                            "#type": "SourcePosition",
                            "path": "project/hello.cau",
                            "breadcrumbs": "declarations.1.body.statements.0.expression.signal",
                            "position": "2:8-2:27"
                        },
                        {
                            "#type": "SourcePosition",
                            "path": "project/hello.cau",
                            "breadcrumbs": "declarations.1.body.statements.0.expression.signal.callee",
                            "position": "2:8-2:19"
                        }
                    ]
                }
            }
            """.trimIndent(),
            TestUtils.expectTypeError(result, vm).debug(),
        )
    }

    @Test
    fun mistypedNamedValue() {
        val vm = LangVm {
            addFile(
                "project/hello.cau", """
                    function main() {
                        let name: Text = 5
                    }
                """.trimIndent()
            )
        }
        assertEquals(
            """
            [
                {
                    "position": {
                        "path": "project/hello.cau",
                        "breadcrumbs": "declarations.1.body.statements.0.declaration",
                        "position": "2:4-2:22"
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
            """.trimIndent(),
            vm.codeBundle.compileErrors.debug(),
        )
        val result = vm.executeFunction("project/hello.cau", "main", listOf())

        // although there's a compile error, it doesn't fail at runtime; the bad value goes nowhere.
        assertEquals(result.expectReturnValue(), RuntimeValue.Action)
    }
}