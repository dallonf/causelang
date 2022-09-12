import TestUtils.expectTypeError
import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.Resolver.debug
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class BranchesTest {
    @Test
    fun branchExpressionReturnsValue() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """                
                    function main() {
                        branch {
                            if equals("red", "blue") => "nope"
                            if equals("red", "red") => "yup"
                            else => "wut"
                        }
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val result = vm.executeFunction("project/test.cau", "main", listOf())
        assertEquals(RuntimeValue.String("yup"), result.expectReturnValue())
    }

    @Test
    fun branchWithOptionType() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    object Hearts
                    object Diamonds
                    object Spades
                    object Clubs
                    option Suit(Hearts, Diamonds, Spades, Clubs)
                    
                    function main() {
                        process_suit(Hearts)
                        process_suit(Diamonds)
                        process_suit(Clubs)
                    }                
                    
                    function process_suit(this: Suit) {
                        branch with this {
                            is Hearts => cause Debug("Hearts")
                            is Diamonds => cause Debug("Diamonds")
                            else => cause Debug("something else")
                        }
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        TestUtils.runMainExpectingDebugs(
            vm, "project/test.cau", listOf(
                "Hearts", "Diamonds", "something else"
            )
        )
    }

    @Test
    fun exhaustiveBranchDoesNotNeedElse() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
                object Hearts
                object Diamonds
                object Spades
                object Clubs
                option Suit(Hearts, Diamonds, Spades, Clubs)
                
                function main() {
                    process_suit(Hearts)
                    process_suit(Diamonds)
                    process_suit(Spades)
                    process_suit(Clubs)
                }                
                
                function process_suit(this: Suit) {
                    branch with this {
                        is Hearts => cause Debug("Hearts")
                        is Diamonds => cause Debug("Diamonds")
                        is Spades => cause Debug("Spades")
                        is Clubs => cause Debug("Clubs")
                    }
                }
            """.trimIndent()
        )

        TestUtils.runMainExpectingDebugs(
            vm, "project/test.cau", listOf(
                "Hearts", "Diamonds", "Spades", "Clubs"
            )
        )
    }

    @Test
    fun nonExhaustiveBranchErrors() {
        val vm = LangVm()
        vm.addFile(
            "project/test.cau", """
                object Hearts
                object Diamonds
                object Spades
                object Clubs
                option Suit(Hearts, Diamonds, Spades, Clubs)
                
                function main() {
                    process_suit(Spades)
                }                
                
                function process_suit(this: Suit) {
                    branch with this {
                        is Hearts => cause Debug("Hearts")
                        is Diamonds => cause Debug("Diamonds")
                    }
                }
            """.trimIndent()
        )

        assertEquals(
            """
            [
                {
                    "position": {
                        "path": "project/test.cau",
                        "breadcrumbs": "declarations.7.body.statements.0.expression",
                        "position": "12:4-15:5"
                    },
                    "error": {
                        "#type": "MissingElseBranch",
                        "options": {
                            "options": [
                                {
                                    "#type": "Resolved",
                                    "valueType": {
                                        "#type": "Instance",
                                        "canonicalType": {
                                            "#type": "Object",
                                            "id": "project/test.cau:Spades",
                                            "name": "Spades",
                                            "fields": [
                                            ]
                                        }
                                    }
                                },
                                {
                                    "#type": "Resolved",
                                    "valueType": {
                                        "#type": "Instance",
                                        "canonicalType": {
                                            "#type": "Object",
                                            "id": "project/test.cau:Clubs",
                                            "name": "Clubs",
                                            "fields": [
                                            ]
                                        }
                                    }
                                }
                            ]
                        }
                    }
                }
            ]
            """.trimIndent(), vm.compileErrors.debug()
        )

        expectTypeError(vm.executeFunction("project/test.cau", "main", listOf()), vm)
    }

    @Test
    fun continueOnErrorIfBranchReturnsAValue() {
        val vm = LangVm()
        vm.addFile(
            "project/test.cau", """
                object Hearts
                object Diamonds
                object Spades
                object Clubs
                option Suit(Hearts, Diamonds, Spades, Clubs)
                
                function main() {
                    let result = suit_to_string(Spades)
                    cause Debug(result)
                }                
                
                function suit_to_string(this: Suit) {
                    branch with this {
                        is Hearts => "Hearts"
                        is Diamonds => "Diamonds"
                    }
                }
            """.trimIndent()
        )

        vm.executeFunction("project/test.cau", "main", listOf()).expectCausedSignal().let {
            assertEquals(
                """
                {
                    "#type": "core/builtin.cau:Debug",
                    "value": {
                        "#type": "BadValue",
                        "position": {
                            "#type": "SourcePosition",
                            "path": "project/test.cau",
                            "breadcrumbs": "declarations.6.body.statements.1.expression.signal.parameters.0",
                            "position": "9:16-9:22"
                        },
                        "error": {
                            "#type": "ProxyError",
                            "actualError": {
                                "#type": "MissingElseBranch",
                                "options": {
                                    "options": [
                                        {
                                            "#type": "Resolved",
                                            "valueType": {
                                                "#type": "Instance",
                                                "canonicalType": {
                                                    "#type": "Object",
                                                    "id": "project/test.cau:Spades",
                                                    "name": "Spades",
                                                    "fields": [
                                                    ]
                                                }
                                            }
                                        },
                                        {
                                            "#type": "Resolved",
                                            "valueType": {
                                                "#type": "Instance",
                                                "canonicalType": {
                                                    "#type": "Object",
                                                    "id": "project/test.cau:Clubs",
                                                    "name": "Clubs",
                                                    "fields": [
                                                    ]
                                                }
                                            }
                                        }
                                    ]
                                }
                            },
                            "proxyChain": [
                                {
                                    "#type": "SourcePosition",
                                    "path": "project/test.cau",
                                    "breadcrumbs": "declarations.6.body.statements.1.expression.signal.parameters.0.value",
                                    "position": "9:16-9:22"
                                },
                                {
                                    "#type": "SourcePosition",
                                    "path": "project/test.cau",
                                    "breadcrumbs": "declarations.6.body.statements.0.declaration",
                                    "position": "8:4-8:39"
                                },
                                {
                                    "#type": "SourcePosition",
                                    "path": "project/test.cau",
                                    "breadcrumbs": "declarations.6.body.statements.0.declaration.value",
                                    "position": "8:17-8:39"
                                },
                                {
                                    "#type": "SourcePosition",
                                    "path": "project/test.cau",
                                    "breadcrumbs": "declarations.6.body.statements.0.declaration.value.callee",
                                    "position": "8:17-8:31"
                                },
                                {
                                    "#type": "SourcePosition",
                                    "path": "project/test.cau",
                                    "breadcrumbs": "declarations.7.body",
                                    "position": "12:36-17:1"
                                },
                                {
                                    "#type": "SourcePosition",
                                    "path": "project/test.cau",
                                    "breadcrumbs": "declarations.7.body.statements.0",
                                    "position": "13:4-16:5"
                                },
                                {
                                    "#type": "SourcePosition",
                                    "path": "project/test.cau",
                                    "breadcrumbs": "declarations.7.body.statements.0.expression",
                                    "position": "13:4-16:5"
                                }
                            ]
                        }
                    }
                }
                """.trimIndent(), it.debug()
            )
        }
    }

    @Test
    fun errorIfExhaustiveButSomeBranchesReturnValueAndOthersReturnAction() {
        val vm = LangVm()
        vm.addFile(
            "project/test.cau", """
                object Hearts
                object Diamonds
                object Spades
                object Clubs
                option Suit(Hearts, Diamonds, Spades, Clubs)
                
                function main() {
                    let result = suit_to_string(Clubs)
                    cause Debug(result)
                }                
                
                function suit_to_string(this: Suit) {
                    branch with this {
                        is Hearts => "Hearts"
                        is Diamonds => cause Debug("Diamonds")
                        is Spades => "Spades"
                        is Clubs => cause Debug("Clubs")
                    }
                }
            """.trimIndent()
        )

        assertEquals(
            """
            [
                {
                    "position": {
                        "path": "project/test.cau",
                        "breadcrumbs": "declarations.7.body.statements.0.expression",
                        "position": "13:4-18:5"
                    },
                    "error": {
                        "#type": "ActionIncompatibleWithValueTypes",
                        "actions": [
                            {
                                "#type": "SourcePosition",
                                "path": "project/test.cau",
                                "breadcrumbs": "declarations.7.body.statements.0.expression.branches.1",
                                "position": "15:8-15:46"
                            },
                            {
                                "#type": "SourcePosition",
                                "path": "project/test.cau",
                                "breadcrumbs": "declarations.7.body.statements.0.expression.branches.3",
                                "position": "17:8-17:40"
                            }
                        ],
                        "types": [
                            {
                                "type": {
                                    "#type": "Primitive",
                                    "kind": "String"
                                },
                                "position": {
                                    "#type": "SourcePosition",
                                    "path": "project/test.cau",
                                    "breadcrumbs": "declarations.7.body.statements.0.expression.branches.0",
                                    "position": "14:8-14:29"
                                }
                            },
                            {
                                "type": {
                                    "#type": "Primitive",
                                    "kind": "String"
                                },
                                "position": {
                                    "#type": "SourcePosition",
                                    "path": "project/test.cau",
                                    "breadcrumbs": "declarations.7.body.statements.0.expression.branches.2",
                                    "position": "16:8-16:29"
                                }
                            }
                        ]
                    }
                }
            ]
            """.trimIndent(), vm.compileErrors.debug()
        )

        vm.executeFunction("project/test.cau", "main", listOf()).let {
            TestUtils.expectValidCaused(it, vm.getBuiltinTypeId("Debug"))
        }.let {
            assertEquals(RuntimeValue.String("Clubs"), it.values[0])
        }

        vm.resumeExecution(RuntimeValue.Action).let { TestUtils.expectTypeError(it, vm) }
    }

    @Test
    fun capturesMatchingValues() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
                import core/string (number_to_string)
                
                object Nothing
                option MaybeNumber(Nothing, Number)
                
                function main() {
                    print_number(Nothing)
                    print_number(42.0)
                }                
                
                function print_number(this: MaybeNumber) {
                    branch with this {
                        is Number as i => cause Debug(number_to_string(i))
                        is Nothing => Action
                    }
                }
            """.trimIndent()
        )

        TestUtils.runMainExpectingDebugs(vm, "project/test.cau", listOf("42.0"))
    }
}