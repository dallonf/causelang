import com.dallonf.ktcause.CompiledFile
import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RunResult
import com.dallonf.ktcause.RuntimeValue
import com.dallonf.ktcause.types.ActionValueLangType
import com.dallonf.ktcause.types.CanonicalLangType
import com.dallonf.ktcause.types.CanonicalLangTypeId
import com.dallonf.ktcause.types.LangPrimitiveKind
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EffectsBasicTest {
    @Test
    fun interceptSignal() {
        val interceptThisTypeId = CanonicalLangTypeId("test/test.cau", name = "InterceptThis", number = 0u)
        val interceptThisType = CanonicalLangType.SignalCanonicalLangType(
            interceptThisTypeId,
            interceptThisTypeId.name!!,
            listOf(),
            result = ActionValueLangType.toConstraint().asConstraintReference()
        )
        val vm = LangVm {
            addCompiledFile(
                CompiledFile(
                    "test/test.cau", types = mapOf(
                        interceptThisTypeId to interceptThisType
                    ), procedures = listOf(), exports = mapOf(
                        interceptThisTypeId.name!! to CompiledFile.CompiledExport.Constraint(
                            interceptThisType.asConstraintReference()
                        )
                    )
                )
            )
            addFile(
                "project/test.cau", """
                    import test/test (InterceptThis)
                    
                    function main() {
                        effect for InterceptThis {
                            cause Debug("Intercepted an InterceptThis signal")
                        }
                        
                        cause InterceptThis()
                        cause Debug("This should not have been intercepted")
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val debugTypeId = vm.codeBundle.getBuiltinTypeId("Debug")
        val result1 = vm.executeFunction("project/test.cau", "main", listOf())
            .let { TestUtils.expectValidCaused(it, debugTypeId) }
        assertEquals(result1.values[0], RuntimeValue.Text("Intercepted an InterceptThis signal"))

        val result2 = vm.resumeExecution(RuntimeValue.Action).let { TestUtils.expectValidCaused(it, debugTypeId) }
        assertEquals(result2.values[0], RuntimeValue.Text("This should not have been intercepted"))

        vm.resumeExecution(RuntimeValue.Action).expectReturnValue().let { assertEquals(it, RuntimeValue.Action) }
    }

    @Test
    fun getValuesFromCapturedSignal() {
        val greetTypeId = CanonicalLangTypeId("test/test.cau", name = "Greet", number = 0u)
        val greetType = CanonicalLangType.SignalCanonicalLangType(
            greetTypeId, greetTypeId.name!!, listOf(
                CanonicalLangType.ObjectField(
                    "name", LangPrimitiveKind.TEXT.toConstraintLangType().asConstraintReference()
                )
            ), result = ActionValueLangType.toConstraint().asConstraintReference()
        )
        val vm = LangVm {
            addCompiledFile(
                CompiledFile(
                    "test/test.cau",
                    types = mapOf(
                        greetTypeId to greetType
                    ),
                    procedures = listOf(),
                    exports = mapOf(greetTypeId.name!! to CompiledFile.CompiledExport.Constraint(greetType.asConstraintReference()))
                )
            )
            addFile(
                "project/test.cau", """
                import core/text (append)
                import test/test (Greet)
                
                function main() {
                    effect for Greet as s {
                        cause Debug(append("Howdy, ", s.name))
                    }
                    
                    cause Debug("Don't handle this one")
                    cause Greet("partner")
                }
            """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val debugTypeId = vm.codeBundle.getBuiltinTypeId("Debug")
        val result1 = vm.executeFunction("project/test.cau", "main", listOf())
            .let { TestUtils.expectValidCaused(it, debugTypeId) }
        assertEquals(RuntimeValue.Text("Don't handle this one"), result1.values[0])
        val result2 = vm.resumeExecution(RuntimeValue.Action).let { TestUtils.expectValidCaused(it, debugTypeId) }
        assertEquals(RuntimeValue.Text("Howdy, partner"), result2.values[0])

        vm.resumeExecution(RuntimeValue.Action).expectReturnValue().let { assertEquals(RuntimeValue.Action, it) }
    }

    @Test
    fun defineOwnSignals() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/text (append)
                    
                    signal Greet(name: Text): Text
                    
                    function main() {
                        effect for Greet as s {
                            append("Howdy, ", s.name)
                        }
                        
                        let greeting = cause Greet("partner")
                        cause Debug(greeting)
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        TestUtils.expectValidCaused(
            vm.executeFunction("project/test.cau", "main", listOf()), vm.codeBundle.getBuiltinTypeId("Debug")
        ).let {
            assertEquals(RuntimeValue.Text("Howdy, partner"), it.values[0])
        }

        vm.resumeExecution(RuntimeValue.Action).expectReturnValue().let {
            assertEquals(RuntimeValue.Action, it)
        }
    }

    @Test
    fun signalResultDefaultsToAction() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/text (append)
                    
                    signal Greet(name: Text)
                    
                    function main(): Action {
                        effect for Greet as s {
                            cause Debug(append("Howdy, ", s.name))
                        }
                        
                        cause Greet("partner")
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        TestUtils.expectValidCaused(
            vm.executeFunction("project/test.cau", "main", listOf()), vm.codeBundle.getBuiltinTypeId("Debug")
        ).let {
            assertEquals(RuntimeValue.Text("Howdy, partner"), it.values[0])
        }

        vm.resumeExecution(RuntimeValue.Action).expectReturnValue().let {
            assertEquals(RuntimeValue.Action, it)
        }
    }

    @Test
    fun defineInlineSignal() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/text (append)
                    
                    function main() {
                        signal Print(value: Text): Action
                        effect for Print as it {
                            cause Debug(it.value)
                        }
                        
                        cause Print("hello")
                        cause Debug("goodbye")
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        TestUtils.runMainExpectingDebugs(
            vm, "project/test.cau", listOf(
                "hello", "goodbye"
            )
        )
    }

    @Test
    fun correctlyFiltersCustomSignals() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/text (append, number_to_text)
                    
                    signal TestSignal1(value: Text): Action
                    signal TestSignal2(value: Number): Action
                    
                    function main() {
                        effect for TestSignal1 as s {
                            cause Debug(append("One ", s.value))
                        }
                        effect for TestSignal2 as s {
                            cause Debug(append("Two ", number_to_text(s.value)))
                        }
                        
                        cause TestSignal1("hello")
                        cause TestSignal2(42.0)
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        TestUtils.runMainExpectingDebugs(
            vm, "project/test.cau", listOf(
                "One hello", "Two 42"
            )
        )
    }

    @Test
    fun canCaptureAnySignal() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                signal Extract(value: Anything): Action
                
                function main() {
                    effect for AnySignal as s {
                        cause Extract(s)
                    }
                    
                    cause Debug("this should be extracted and not printed")
                }
            """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        vm.executeFunction("project/test.cau", "main", listOf())
            .let { TestUtils.expectValidCaused(it, vm.codeBundle.getTypeId("project/test.cau", "Extract")) }.let {
                assertEquals(
                    """
                    {
                        "#type": "project/test.cau:Extract",
                        "value": {
                            "#type": "core/builtin.cau:Debug",
                            "value": "this should be extracted and not printed"
                        }
                    }
                    """.trimIndent(), it.debug()
                )
            }
        assertEquals(
            RuntimeValue.Action,
            vm.resumeExecution(RuntimeValue.Action).expectReturnValue(),
        )
    }

    @Test
    fun setFromInnerFunctionWithCause() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                signal SetX(value: Number): Action
                
                function main() {
                    let variable x = 1.0
                    
                    effect for SetX as s {
                        set x = s.value
                    }
                    
                    function update() {
                        cause SetX(2.0)
                    }
                    
                    update()
                    
                    x
                }
            """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(RuntimeValue.Number(2), result)
    }

    @Test
    fun canDefineMultipleSignalsWithTheSameNameInScope() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                signal Test(value: Number): Action
                
                function main() {
                    cause Test(42)
                    
                    signal Test(value: Text): Action
                    cause Test("hello world")
                }
            """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val signals = mutableListOf<RuntimeValue>()
        var result = vm.executeFunction("project/test.cau", "main", listOf())
        while (result is RunResult.Caused) {
            signals.add(result.signal)
            result = vm.resumeExecution(RuntimeValue.Action)
        }
        assertEquals("""
            {
                "#type": "project/test.cau:Test",
                "value": 42
            }
            {
                "#type": "project/test.cau:main_Test",
                "value": "hello world"
            }
            """.trimIndent(), signals.joinToString("\n") { it.debug() })
        assertEquals(RuntimeValue.Action, result.expectReturnValue())
    }
}