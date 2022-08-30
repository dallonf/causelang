import TestUtils.addFileExpectingNoCompileErrors
import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.Resolver.debug
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VariablesTest {
    @Test
    fun changesValueOfVariable() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
            function main() {
                let variable x = 1
                set x = 2
                cause Debug(x)
            }
        """.trimIndent()
        )

        val result = vm.executeFunction("project/test.cau", "main", listOf()).let {
            TestUtils.expectValidCaused(it, vm.getBuiltinTypeId("Debug"))
        }
        assertEquals(RuntimeValue.WholeNumber(2), result.values[0])
    }

    @Test
    fun cantSetANonVariable() {
        val vm = LangVm()
        vm.addFile(
            "project/test.cau", """
                function main() {
                    let x = 1
                    set x = 2
                }
            """.trimIndent()
        )

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
            """.trimIndent(), vm.compileErrors.debug()
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
        val vm = LangVm()
        vm.addFile(
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
            """.trimIndent(), vm.compileErrors.debug()
        )

        val debug = vm.executeFunction("project/test.cau", "main", listOf()).expectCausedSignal().let {
            assertEquals(vm.getBuiltinTypeId("Debug"), it.typeDescriptor.id)
            it.values[0]
        }
        assertEquals(
            """
            {
                "#type": "BadValue",
                "position": {
                    "#type": "SourcePosition",
                    "path": "project/test.cau",
                    "breadcrumbs": "declarations.1.body.statements.1.declaration.body.statements.0.expression.signal.parameters.0",
                    "position": "4:20-4:21"
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
                            "breadcrumbs": "declarations.1.body.statements.1.declaration.body.statements.0.expression.signal.parameters.0.value",
                            "position": "4:20-4:21"
                        }
                    ]
                }
            }
            """.trimIndent(), debug.debug()
        )
    }

    @Test
    fun cantSetVariableInInnerFunction() {
        val vm = LangVm()
        vm.addFile(
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
            """.trimIndent(), vm.compileErrors.debug()
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