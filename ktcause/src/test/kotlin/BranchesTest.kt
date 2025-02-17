import TestUtils.expectTypeError
import com.dallonf.ktcause.Debug
import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.Resolver.debug
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.math.sign
import kotlin.test.assertEquals

class BranchesTest {
    @Test
    fun branchExpressionReturnsValue() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """                
                    #function main() {
                        #branch {
                            #if equals("red", "blue") => "nope"
                            #if equals("red", "red") => "yup"
                            #else => "wut"
                        }
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val result = vm.executeFunction("project/test.cau", "main", listOf())
        assertEquals(RuntimeValue.Text("yup"), result.expectReturnValue())
    }

    @Test
    fun branchWithOptionType() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    #object Hearts
                    #object Diamonds
                    #object Spades
                    #object Clubs
                    #option Suit(Hearts, Diamonds, Spades, Clubs)
                    
                    #function main() {
                        process_suit(Hearts)
                        process_suit(Diamonds)
                        process_suit(Clubs)
                    }                
                    
                    #function process_suit(this: Suit) {
                        #branch #with this {
                            #is Hearts => #cause Debug("Hearts")
                            #is Diamonds => #cause Debug("Diamonds")
                            #else => #cause Debug("something else")
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
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    #object Hearts
                    #object Diamonds
                    #object Spades
                    #object Clubs
                    #option Suit(Hearts, Diamonds, Spades, Clubs)
                    
                    #function main() {
                        process_suit(Hearts)
                        process_suit(Diamonds)
                        process_suit(Spades)
                        process_suit(Clubs)
                    }                
                    
                    #function process_suit(this: Suit) {
                        #branch #with this {
                            #is Hearts => #cause Debug("Hearts")
                            #is Diamonds => #cause Debug("Diamonds")
                            #is Spades => #cause Debug("Spades")
                            #is Clubs => #cause Debug("Clubs")
                        }
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        TestUtils.runMainExpectingDebugs(
            vm, "project/test.cau", listOf(
                "Hearts", "Diamonds", "Spades", "Clubs"
            )
        )
    }

    @Test
    fun nonExhaustiveBranchErrors() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    #object Hearts
                    #object Diamonds
                    #object Spades
                    #object Clubs
                    #option Suit(Hearts, Diamonds, Spades, Clubs)
                    
                    #function main() {
                        process_suit(Spades)
                    }                
                    
                    #function process_suit(this: Suit) {
                        #branch #with this {
                            #is Hearts => #cause Debug("Hearts")
                            #is Diamonds => #cause Debug("Diamonds")
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
                                        "canonicalType": "project/test.cau:Spades"
                                    }
                                },
                                {
                                    "#type": "Resolved",
                                    "valueType": {
                                        "#type": "Instance",
                                        "canonicalType": "project/test.cau:Clubs"
                                    }
                                }
                            ]
                        }
                    }
                }
            ]
            """.trimIndent(), vm.codeBundle.compileErrors.debug()
        )

        expectTypeError(vm.executeFunction("project/test.cau", "main", listOf()), vm)
    }

    @Test
    fun continueOnErrorIfBranchReturnsAValue() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    #object Hearts
                    #object Diamonds
                    #object Spades
                    #object Clubs
                    #option Suit(Hearts, Diamonds, Spades, Clubs)
                    
                    #function main() {
                        #let result = suit_to_string(Spades)
                        #cause Debug(result)
                    }                
                    
                    #function suit_to_string(this: Suit) {
                        #branch #with this {
                            #is Hearts => "Hearts"
                            #is Diamonds => "Diamonds"
                        }
                    }
                """.trimIndent()
            )
        }

        vm.executeFunction("project/test.cau", "main", listOf()).expectCausedSignal().let { signal ->
            // Rust and Kotlin implementations differ on how they report this error,
            // but both are fine (Rust is even slightly preferable)
            assertEquals("core/builtin.cau:Debug", signal.typeDescriptor.id.toString())
            val value = signal.getValue("value") as RuntimeValue.BadValue
            val error = value.error.getActualErrorIfProxied()
            assertEquals(
                """
                {
                    "#type": "MissingElseBranch",
                    "options": {
                        "options": [
                            {
                                "#type": "Resolved",
                                "valueType": {
                                    "#type": "Instance",
                                    "canonicalType": "project/test.cau:Spades"
                                }
                            },
                            {
                                "#type": "Resolved",
                                "valueType": {
                                    "#type": "Instance",
                                    "canonicalType": "project/test.cau:Clubs"
                                }
                            }
                        ]
                    }
                }
                """.trimIndent(), error.debug()
            )
        }
    }

    @Test
    fun errorIfExhaustiveButSomeBranchesReturnValueAndOthersReturnAction() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    #object Hearts
                    #object Diamonds
                    #object Spades
                    #object Clubs
                    #option Suit(Hearts, Diamonds, Spades, Clubs)
                    
                    #function main() {
                        #let result = suit_to_string(Clubs)
                        #cause Debug(result)
                    }                
                    
                    #function suit_to_string(this: Suit) {
                        #branch #with this {
                            #is Hearts => "Hearts"
                            #is Diamonds => #cause Debug("Diamonds")
                            #is Spades => "Spades"
                            #is Clubs => #cause Debug("Clubs")
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
                        "path": "project/test.cau",
                        "breadcrumbs": "declarations.7.body.statements.0.expression",
                        "position": "13:4-18:5"
                    },
                    "error": {
                        "#type": "ActionIncompatibleWithValueTypes",
                        "actions": [
                            {
                                "path": "project/test.cau",
                                "breadcrumbs": "declarations.7.body.statements.0.expression.branches.1",
                                "position": "15:8-15:46"
                            },
                            {
                                "path": "project/test.cau",
                                "breadcrumbs": "declarations.7.body.statements.0.expression.branches.3",
                                "position": "17:8-17:40"
                            }
                        ],
                        "types": [
                            {
                                "type": {
                                    "#type": "Primitive",
                                    "kind": "Text"
                                },
                                "position": {
                                    "path": "project/test.cau",
                                    "breadcrumbs": "declarations.7.body.statements.0.expression.branches.0",
                                    "position": "14:8-14:29"
                                }
                            },
                            {
                                "type": {
                                    "#type": "Primitive",
                                    "kind": "Text"
                                },
                                "position": {
                                    "path": "project/test.cau",
                                    "breadcrumbs": "declarations.7.body.statements.0.expression.branches.2",
                                    "position": "16:8-16:29"
                                }
                            }
                        ]
                    }
                }
            ]
            """.trimIndent(), vm.codeBundle.compileErrors.debug()
        )

        vm.executeFunction("project/test.cau", "main", listOf()).let {
            TestUtils.expectValidCaused(it, vm.codeBundle.getBuiltinTypeId("Debug"))
        }.let {
            assertEquals(RuntimeValue.Text("Clubs"), it.values[0])
        }

        vm.resumeExecution(RuntimeValue.Action).let {
            val signal = it.expectCausedSignal()
            when (signal.typeDescriptor.id) {
                vm.codeBundle.getTypeId("core/builtin.cau", "TypeError") -> {
                    // all good (this is how the Kotlin implementation behaves)
                }

                vm.codeBundle.getTypeId("core/builtin.cau", "Debug") -> {
                    // Debugs with a BadValue
                    assert(signal.getValue("value") is RuntimeValue.BadValue)
                }

                else -> {
                    throw AssertionError("Unexpected result: ${signal.debug()}")
                }
            }
        }
    }

    @Test
    fun capturesMatchingValues() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    #import core/text (number_to_text)
                    
                    #object Nothing
                    #option MaybeNumber(Nothing, Number)
                    
                    #function main() {
                        print_number(Nothing)
                        print_number(42.0)
                    }                
                    
                    #function print_number(this: MaybeNumber) {
                        #branch #with this {
                            #is Number #as i => #cause Debug(number_to_text(i))
                            #is Nothing => Action
                        }
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        TestUtils.runMainExpectingDebugs(vm, "project/test.cau", listOf("42"))
    }

    @Test
    fun matchesAnything() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """                    
                    #function main() {
                        #let x: Anything = "Something"
                        #branch #with x {
                            #is Text #as x => #cause Debug(x)
                            #else => #cause AssumptionBroken("it should have been text")
                        }
                    }                
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        TestUtils.runMainExpectingDebugs(vm, "project/test.cau", listOf("Something"))
    }

    @Test
    fun consolidatesTypes() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """                    
                    #function main() {
                        #let number = #branch {
                            #if True => 1
                            #else => 2
                        }
                        require_number(number)
                    }
                    
                    #function require_number(input: Number) {}
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)
    }

    @Test
    fun canGetObjectTypeOutOfAnything() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    #object Something(
                        a: Text,
                        b: Text,
                    )
                    
                    #function main() {
                        #let value: Anything = Something("ay", "bee")
                        #let cast = #branch #with value {
                            #is Something #as value => value
                            #else => #cause AssumptionBroken("Not Something")
                        }
                        cast.a
                    }
                """.trimIndent()
            )
        }

        TestUtils.expectNoCompileErrors(vm)

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(RuntimeValue.Text("ay"), result)
    }
}