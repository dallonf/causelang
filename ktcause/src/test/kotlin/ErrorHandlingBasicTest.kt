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
                    "breadcrumbs": "declarations.1.body.statements.0.expression.signal",
                    "position": "2:8-2:27"
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

    @Test
    fun mistypedFunctionReturn() {
        val vm = LangVm {
            addFile(
                "project/hello.cau", """
                    function main(): Number {
                        "oh no that's not a number"
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
                        "breadcrumbs": "declarations.1.body",
                        "position": "1:24-3:1"
                    },
                    "error": {
                        "#type": "MismatchedType",
                        "expected": {
                            "valueType": {
                                "#type": "Primitive",
                                "kind": "Number"
                            }
                        },
                        "actual": {
                            "#type": "Primitive",
                            "kind": "Text"
                        }
                    }
                }
            ]
            """.trimIndent(),
            vm.codeBundle.compileErrors.debug(),
        )

        val result = vm.executeFunction("project/hello.cau", "main", listOf())
        TestUtils.expectBadValue(
            result.expectReturnValue(),
            """
            {
                "#type": "BadValue",
                "position": {
                    "#type": "SourcePosition",
                    "path": "project/hello.cau",
                    "breadcrumbs": "declarations.1.body",
                    "position": "1:24-3:1"
                },
                "error": {
                    "#type": "MismatchedType",
                    "expected": {
                        "valueType": {
                            "#type": "Primitive",
                            "kind": "Number"
                        }
                    },
                    "actual": {
                        "#type": "Primitive",
                        "kind": "Text"
                    }
                }
            }
            """.trimIndent(),
        )
    }

    @Test
    fun mistypedEarlyReturn() {
        val vm = LangVm {
            addFile(
                "project/hello.cau", """                    
                    option MainReturn(Number, Text)
                    object NotThat
                    object OrThat
                    
                    function main(): MainReturn {
                        branch {
                            if equals(2, 2) => return NotThat
                            if equals(1, 2) => return OrThat
                            else => "maybe"
                        }
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
                        "breadcrumbs": "declarations.4.body.statements.0.expression.branches.0.body.statement.expression.value",
                        "position": "7:34-7:41"
                    },
                    "error": {
                        "#type": "MismatchedType",
                        "expected": {
                            "valueType": {
                                "#type": "Option",
                                "options": [
                                    {
                                        "#type": "Resolved",
                                        "valueType": {
                                            "#type": "Primitive",
                                            "kind": "Number"
                                        }
                                    },
                                    {
                                        "#type": "Resolved",
                                        "valueType": {
                                            "#type": "Primitive",
                                            "kind": "Text"
                                        }
                                    }
                                ]
                            }
                        },
                        "actual": {
                            "#type": "Constraint",
                            "valueType": {
                                "#type": "Instance",
                                "canonicalType": {
                                    "#type": "Object",
                                    "id": "project/hello.cau:NotThat",
                                    "name": "NotThat",
                                    "fields": [
                                    ]
                                }
                            }
                        }
                    }
                },
                {
                    "position": {
                        "path": "project/hello.cau",
                        "breadcrumbs": "declarations.4.body.statements.0.expression.branches.1.body.statement.expression.value",
                        "position": "8:34-8:40"
                    },
                    "error": {
                        "#type": "MismatchedType",
                        "expected": {
                            "valueType": {
                                "#type": "Option",
                                "options": [
                                    {
                                        "#type": "Resolved",
                                        "valueType": {
                                            "#type": "Primitive",
                                            "kind": "Number"
                                        }
                                    },
                                    {
                                        "#type": "Resolved",
                                        "valueType": {
                                            "#type": "Primitive",
                                            "kind": "Text"
                                        }
                                    }
                                ]
                            }
                        },
                        "actual": {
                            "#type": "Constraint",
                            "valueType": {
                                "#type": "Instance",
                                "canonicalType": {
                                    "#type": "Object",
                                    "id": "project/hello.cau:OrThat",
                                    "name": "OrThat",
                                    "fields": [
                                    ]
                                }
                            }
                        }
                    }
                }
            ]
            """.trimIndent(),
            vm.codeBundle.compileErrors.debug(),
        )
        val result = vm.executeFunction("project/hello.cau", "main", listOf())
        TestUtils.expectBadValue(
            result.expectReturnValue(), """
            {
                "#type": "BadValue",
                "position": {
                    "#type": "SourcePosition",
                    "path": "project/hello.cau",
                    "breadcrumbs": "declarations.4.body.statements.0.expression.branches.0.body.statement.expression.value",
                    "position": "7:34-7:41"
                },
                "error": {
                    "#type": "MismatchedType",
                    "expected": {
                        "valueType": {
                            "#type": "Option",
                            "options": [
                                {
                                    "#type": "Resolved",
                                    "valueType": {
                                        "#type": "Primitive",
                                        "kind": "Number"
                                    }
                                },
                                {
                                    "#type": "Resolved",
                                    "valueType": {
                                        "#type": "Primitive",
                                        "kind": "Text"
                                    }
                                }
                            ]
                        }
                    },
                    "actual": {
                        "#type": "Constraint",
                        "valueType": {
                            "#type": "Instance",
                            "canonicalType": {
                                "#type": "Object",
                                "id": "project/hello.cau:NotThat",
                                "name": "NotThat",
                                "fields": [
                                ]
                            }
                        }
                    }
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun mistypedInlineFunctionReturn() {
        val vm = LangVm {
            addFile(
                "project/hello.cau", """                    
                    function main() {
                        let test = fn(): Number "not a number"
                        test()
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
                        "breadcrumbs": "declarations.1.body.statements.0.declaration.value.body",
                        "position": "2:28-2:42"
                    },
                    "error": {
                        "#type": "MismatchedType",
                        "expected": {
                            "valueType": {
                                "#type": "Primitive",
                                "kind": "Number"
                            }
                        },
                        "actual": {
                            "#type": "Primitive",
                            "kind": "Text"
                        }
                    }
                }
            ]
            """.trimIndent(),
            vm.codeBundle.compileErrors.debug(),
        )
        val result = vm.executeFunction("project/hello.cau", "main", listOf())
        TestUtils.expectBadValue(
            result.expectReturnValue(), """
            {
                "#type": "BadValue",
                "position": {
                    "#type": "SourcePosition",
                    "path": "project/hello.cau",
                    "breadcrumbs": "declarations.1.body.statements.0.declaration.value.body",
                    "position": "2:28-2:42"
                },
                "error": {
                    "#type": "MismatchedType",
                    "expected": {
                        "valueType": {
                            "#type": "Primitive",
                            "kind": "Number"
                        }
                    },
                    "actual": {
                        "#type": "Primitive",
                        "kind": "Text"
                    }
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun mistypedInlineFunctionExplicitReturn() {
        val vm = LangVm {
            addFile(
                "project/hello.cau", """                    
                    function main() {
                        let test = fn(): Number {
                            return "not a number"
                        }
                        test()
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
                        "breadcrumbs": "declarations.1.body.statements.0.declaration.value.body.block.statements.0.expression.value",
                        "position": "3:15-3:29"
                    },
                    "error": {
                        "#type": "MismatchedType",
                        "expected": {
                            "valueType": {
                                "#type": "Primitive",
                                "kind": "Number"
                            }
                        },
                        "actual": {
                            "#type": "Primitive",
                            "kind": "Text"
                        }
                    }
                }
            ]
            """.trimIndent(),
            vm.codeBundle.compileErrors.debug(),
        )
        val result = vm.executeFunction("project/hello.cau", "main", listOf())
        TestUtils.expectBadValue(
            result.expectReturnValue(), """
            {
                "#type": "BadValue",
                "position": {
                    "#type": "SourcePosition",
                    "path": "project/hello.cau",
                    "breadcrumbs": "declarations.1.body.statements.0.declaration.value.body.block.statements.0.expression.value",
                    "position": "3:15-3:29"
                },
                "error": {
                    "#type": "MismatchedType",
                    "expected": {
                        "valueType": {
                            "#type": "Primitive",
                            "kind": "Number"
                        }
                    },
                    "actual": {
                        "#type": "Primitive",
                        "kind": "Text"
                    }
                }
            }
            """.trimIndent()
        )
    }
}