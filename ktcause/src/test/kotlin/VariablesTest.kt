import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.Resolver.debug
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VariablesTest {
    @Test
    fun changesValueOfVariable() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    function main() {
                        let variable x = 1
                        set x = 2
                        cause Debug(x)
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val result = vm.executeFunction("project/test.cau", "main", listOf()).let {
            TestUtils.expectValidCaused(it, vm.codeBundle.getBuiltinTypeId("Debug"))
        }
        assertEquals(RuntimeValue.Number(2), result.values[0])
    }

    @Test
    fun cantSetANonVariable() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                function main() {
                    let x = 1
                    set x = 2
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
                        "breadcrumbs": "declarations.1.body.statements.1",
                        "position": "3:4-3:13"
                    },
                    "error": {
                        "#type": "NotVariable"
                    }
                }
            ]
            """.trimIndent(), vm.codeBundle.compileErrors.debug()
        )

        val error = TestUtils.expectTypeError(vm.executeFunction("project/test.cau", "main", listOf()), vm)
        assertEquals(
            """
            {
                "#type": "BadValue",
                "position": {
                    "#type": "SourcePosition",
                    "path": "project/test.cau",
                    "breadcrumbs": "declarations.1.body.statements.1",
                    "position": "3:4-3:13"
                },
                "error": {
                    "#type": "NotVariable"
                }
            }
            """.trimIndent(), error.debug()
        )
    }

    @Test
    fun cantReadVariableInInnerFunction() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                function main() {
                    let variable x = 1
                    function read() {
                        cause Debug(x)
                    }
                    set x = 2
                    read()
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
                        "breadcrumbs": "declarations.1.body.statements.1.declaration.body.statements.0.expression.signal.parameters.0.value",
                        "position": "4:20-4:21"
                    },
                    "error": {
                        "#type": "OuterVariable"
                    }
                }
            ]
            """.trimIndent(), vm.codeBundle.compileErrors.debug()
        )

        val signal = vm.executeFunction("project/test.cau", "main", listOf()).expectCausedSignal()
        assertEquals(
            """
            {
                "#type": "core/builtin.cau:TypeError",
                "badValue": {
                    "#type": "BadValue",
                    "position": {
                        "#type": "SourcePosition",
                        "path": "project/test.cau",
                        "breadcrumbs": "declarations.1.body.statements.1.declaration.body.statements.0.expression.signal",
                        "position": "4:14-4:22"
                    },
                    "error": {
                        "#type": "ProxyError",
                        "actualError": {
                            "#type": "OuterVariable"
                        },
                        "proxyChain": [
                            {
                                "#type": "SourcePosition",
                                "path": "project/test.cau",
                                "breadcrumbs": "declarations.1.body.statements.1.declaration.body.statements.0.expression.signal.parameters.0",
                                "position": "4:20-4:21"
                            },
                            {
                                "#type": "SourcePosition",
                                "path": "project/test.cau",
                                "breadcrumbs": "declarations.1.body.statements.1.declaration.body.statements.0.expression.signal.parameters.0.value",
                                "position": "4:20-4:21"
                            }
                        ]
                    }
                }
            }
            """.trimIndent(), signal.debug()
        )
    }

    @Test
    fun cantSetVariableInInnerFunction() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                function main() {
                    let variable x = 1
                    function update() {
                        set x = 2
                    }
                    update()
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
                        "breadcrumbs": "declarations.1.body.statements.1.declaration.body.statements.0",
                        "position": "4:8-4:17"
                    },
                    "error": {
                        "#type": "OuterVariable"
                    }
                }
            ]
            """.trimIndent(), vm.codeBundle.compileErrors.debug()
        )

        val error = TestUtils.expectTypeError(vm.executeFunction("project/test.cau", "main", listOf()), vm)
        assertEquals(
            """
            {
                "#type": "BadValue",
                "position": {
                    "#type": "SourcePosition",
                    "path": "project/test.cau",
                    "breadcrumbs": "declarations.1.body.statements.1.declaration.body.statements.0",
                    "position": "4:8-4:17"
                },
                "error": {
                    "#type": "OuterVariable"
                }
            }
            """.trimIndent(), error.debug()
        )
    }
}