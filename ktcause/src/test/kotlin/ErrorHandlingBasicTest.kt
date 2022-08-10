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
                            "position": "2:15-2:17"
                        },
                        "error": {
                            "#type": "MissingParameters",
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
                            "message"
                        ]
                    },
                    "proxyChain": [
                        {
                            "#type": "SourcePosition",
                            "path": "project/hello.cau",
                            "breadcrumbs": "declarations.1.body.statements.0.expression.signal",
                            "position": "2:15-2:17"
                        }
                    ]
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun mistypedArgument() {
        val vm = LangVm()
        vm.addFile(
            "project/hello.cau", """
            function main() {
                cause Debug(1)
            }
            """.trimIndent()
        )

        assertEquals(
            vm.compileErrors.debug(), """
            [
                {
                    "position": {
                        "path": "project/hello.cau",
                        "breadcrumbs": "declarations.1.body.statements.0.expression.signal.parameters.0",
                        "position": "2:16-2:17"
                    },
                    "error": {
                        "#type": "MismatchedType",
                        "expected": {
                            "#type": "PrimitiveConstraint",
                            "kind": "String"
                        },
                        "actual": {
                            "#type": "Primitive",
                            "kind": "Integer"
                        }
                    }
                }
            ]
            """.trimIndent()
        )

        val result = vm.executeFunction("project/hello.cau", "main", listOf())
        assertEquals(
            TestUtils.expectInvalidSignal(result).debug(), """
            {
                "#type": "BadValue",
                "position": {
                    "#type": "SourcePosition",
                    "path": "project/hello.cau",
                    "breadcrumbs": "declarations.1.body.statements.0.expression.signal.parameters.0",
                    "position": "2:16-2:17"
                },
                "error": {
                    "#type": "MismatchedType",
                    "expected": {
                        "#type": "PrimitiveConstraint",
                        "kind": "String"
                    },
                    "actual": {
                        "#type": "Primitive",
                        "kind": "Integer"
                    }
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun causeNonSignal() {
        val vm = LangVm()
        vm.addFile(
            "project/hello.cau", """
                function main() {
                    cause "oops"
                }
            """.trimIndent()
        )

        assertEquals(
            vm.compileErrors.debug(), """
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
        val vm = LangVm()
        vm.addFile(
            "project/hello.cau", """
                function main() {
                  cause DoesntExist("oops")
                }
            """.trimIndent()
        )

        assertEquals(
            vm.compileErrors.debug(), """
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
                            "position": "2:19-2:27"
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
            """.trimIndent()
        )
    }

    @Test
    fun mistypedNamedValue() {
        val vm = LangVm()
        vm.addFile(
            "project/hello.cau", """
                function main() {
                    let name: String = 5
                }
            """.trimIndent()
        )
        assertEquals(
            vm.compileErrors.debug(), """
            """.trimIndent()
        )
        val result = vm.executeFunction("project/hello.cau", "main", listOf())
        assertEquals(
            TestUtils.expectTypeError(result, vm).debug(), """
            """.trimIndent()
        )
    }
}