import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.Resolver.debug
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.Ignore
import kotlin.test.assertEquals

class OptionsAndUniqueObjects {
    @Test
    fun defineUniqueObjectTypes() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    object One
                    object Two
                    object Three
                    
                    function main() {
                        let one = One
                        let two = Two
                        let three = Three
                        
                        two
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(
            """
            {
                "#type": "RuntimeTypeConstraint",
                "id": "project/test.cau:Two"
            }
            """.trimIndent(), result.debug()
        )
    }

    @Test
    fun doesntChokeOnInstantiatingUniqueType() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                object Test
                
                function main() {
                    Test()
                }
            """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(
            """
            {
                "#type": "RuntimeTypeConstraint",
                "id": "project/test.cau:Test"
            }
            """.trimIndent(), result.debug()
        )
    }

    @Test
    fun withParensIsStillUnique() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                object Test()
                
                function main() {
                    Test
                }
            """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(
            """
            {
                "#type": "RuntimeTypeConstraint",
                "id": "project/test.cau:Test"
            }
            """.trimIndent(), result.debug()
        )
    }

    @Test
    fun supportsTypeAnnotations() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                object Test1
                object Test2
                
                function main() {
                    let test: Test1 = Test1
                    let error: Test2 = Test1
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
                        "breadcrumbs": "declarations.3.body.statements.1.declaration",
                        "position": "6:4-6:28"
                    },
                    "error": {
                        "#type": "MismatchedType",
                        "expected": {
                            "valueType": {
                                "#type": "Instance",
                                "canonicalType": {
                                    "#type": "Object",
                                    "id": "project/test.cau:Test2",
                                    "name": "Test2",
                                    "fields": [
                                    ]
                                }
                            }
                        },
                        "actual": {
                            "#type": "Constraint",
                            "valueType": {
                                "#type": "Instance",
                                "canonicalType": {
                                    "#type": "Object",
                                    "id": "project/test.cau:Test1",
                                    "name": "Test1",
                                    "fields": [
                                    ]
                                }
                            }
                        }
                    }
                }
            ]
            """.trimIndent(), vm.codeBundle.compileErrors.debug()
        )


        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(RuntimeValue.Action, result)
    }

    @Test
    fun defineOptionTypes() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                object Hearts
                object Diamonds
                object Clubs
                object Spades
                
                option Suit(
                    Hearts,
                    Diamonds,
                    Clubs,
                    Spades,
                )
                
                function main() {
                    let card_suit: Suit = Diamonds
                    card_suit
                }
            """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(
            """
            {
                "#type": "RuntimeTypeConstraint",
                "id": "project/test.cau:Diamonds"
            }
            """.trimIndent(), result.debug()
        )
    }

    @Test
    fun optionTypesTypeCheck() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    object Hearts
                    object Diamonds
                    
                    option Suit(
                        Hearts,
                        Diamonds,
                    )
                    
                    function main() {
                        let card_suit: Suit = 5
                        card_suit
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
                        "breadcrumbs": "declarations.4.body.statements.0.declaration",
                        "position": "10:4-10:27"
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
                                            "#type": "Instance",
                                            "canonicalType": {
                                                "#type": "Object",
                                                "id": "project/test.cau:Hearts",
                                                "name": "Hearts",
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
                                                "id": "project/test.cau:Diamonds",
                                                "name": "Diamonds",
                                                "fields": [
                                                ]
                                            }
                                        }
                                    }
                                ]
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
        assertEquals(
            """
            {
                "#type": "BadValue",
                "position": {
                    "#type": "SourcePosition",
                    "path": "project/test.cau",
                    "breadcrumbs": "declarations.4.body.statements.0.declaration",
                    "position": "10:4-10:27"
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
                                        "#type": "Instance",
                                        "canonicalType": {
                                            "#type": "Object",
                                            "id": "project/test.cau:Hearts",
                                            "name": "Hearts",
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
                                            "id": "project/test.cau:Diamonds",
                                            "name": "Diamonds",
                                            "fields": [
                                            ]
                                        }
                                    }
                                }
                            ]
                        }
                    },
                    "actual": {
                        "#type": "Primitive",
                        "kind": "Number"
                    }
                }
            }
            """.trimIndent(), vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue().debug()
        )
    }

    @Test
    @Ignore
    fun optionTypesWithShorthand() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                option MaybeNumber(
                    object None,
                    object Some(value: Number),
                )
                
                function main(): MaybeNumber {
                    MaybeNumber.Some(4)
                }
            """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        assertEquals(
            """
            """.trimIndent(), vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue().debug()
        )
    }

    @Test
    fun uniqueSignals() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    signal UniqueSignal: Action
                    
                    function main() {
                        effect for UniqueSignal as s {
                            cause Debug("unique signal intercepted")
                            cause s
                        }
                        
                        cause UniqueSignal
                        cause Debug("done")
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        vm.executeFunction("project/test.cau", "main", listOf())
            .let { TestUtils.expectValidCaused(it, vm.codeBundle.getBuiltinTypeId("Debug")) }
            .let { assertEquals(RuntimeValue.Text("unique signal intercepted"), it.values[0]) }

        vm.resumeExecution(RuntimeValue.Action)
            .let { TestUtils.expectValidCaused(it, vm.codeBundle.getTypeId("project/test.cau", "UniqueSignal")) }
            .let {
                assertEquals(
                    """
                    {
                        "#type": "project/test.cau:UniqueSignal"
                    }
                    """.trimIndent(),
                    it.debug()
                )
            }

        vm.resumeExecution(RuntimeValue.Action)
            .let { TestUtils.expectValidCaused(it, vm.codeBundle.getBuiltinTypeId("Debug")) }
            .let { assertEquals(RuntimeValue.Text("done"), it.values[0]) }

        vm.resumeExecution(RuntimeValue.Action).expectReturnValue()
            .let { assertEquals(RuntimeValue.Action, it) }
    }
}