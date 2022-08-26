import TestUtils.addFileAndPrintCompileErrors
import TestUtils.addFileExpectingNoCompileErrors
import com.dallonf.ktcause.CompiledFile
import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.Resolver.debug
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
        val vm = LangVm()
        val interceptThisTypeId = CanonicalLangTypeId("test/test.cau", name = "InterceptThis", number = 0u)
        vm.addCompiledFile(
            CompiledFile(
                "test/test.cau",
                types = mapOf(
                    interceptThisTypeId to CanonicalLangType.SignalCanonicalLangType(
                        interceptThisTypeId,
                        interceptThisTypeId.name!!,
                        listOf(),
                        result = ActionValueLangType.toConstraint().asConstraintReference()
                    )
                ),
                chunks = listOf(),
                exports = mapOf(interceptThisTypeId.name!! to CompiledFile.CompiledExport.Type(interceptThisTypeId))
            )
        )
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
                import test/test (InterceptThis)
                
                function main() {
                    effect (InterceptThis) {
                        cause Debug("Intercepted an InterceptThis signal")
                    }
                    
                    cause InterceptThis()
                    cause Debug("This should not have been intercepted")
                }
            """.trimIndent()
        )

        val debugTypeId = vm.getTypeId("core/builtin.cau", "Debug")
        val result1 = vm.executeFunction("project/test.cau", "main", listOf())
            .let { TestUtils.expectValidCaused(it, debugTypeId) }
        assertEquals(result1.values[0], RuntimeValue.String("Intercepted an InterceptThis signal"))

        val result2 = vm.resumeExecution(RuntimeValue.Action).let { TestUtils.expectValidCaused(it, debugTypeId) }
        assertEquals(result2.values[0], RuntimeValue.String("This should not have been intercepted"))

        vm.resumeExecution(RuntimeValue.Action).expectReturnValue().let { assertEquals(it, RuntimeValue.Action) }
    }

    @Test
    fun getValuesFromCapturedSignal() {
        val vm = LangVm()
        val greetTypeId = CanonicalLangTypeId("test/test.cau", name = "Greet", number = 0u)
        vm.addCompiledFile(
            CompiledFile(
                "test/test.cau",
                types = mapOf(
                    greetTypeId to CanonicalLangType.SignalCanonicalLangType(
                        greetTypeId, greetTypeId.name!!, listOf(
                            CanonicalLangType.ObjectField(
                                "name", LangPrimitiveKind.STRING.toConstraintLangType().asConstraintReference()
                            )
                        ), result = ActionValueLangType.toConstraint().asConstraintReference()
                    )
                ),
                chunks = listOf(),
                exports = mapOf(greetTypeId.name!! to CompiledFile.CompiledExport.Type(greetTypeId))
            )
        )
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
                import core/string (append)
                import test/test (Greet)
                
                function main() {
                    effect (let s: Greet) {
                        cause Debug(append("Howdy, ", s.name))
                    }
                    
                    cause Debug("Don't handle this one")
                    cause Greet("partner")
                }
            """.trimIndent()
        )

        val debugTypeId = vm.getTypeId("core/builtin.cau", "Debug")
        val result1 = vm.executeFunction("project/test.cau", "main", listOf())
            .let { TestUtils.expectValidCaused(it, debugTypeId) }
        assertEquals(RuntimeValue.String("Don't handle this one"), result1.values[0])
        val result2 = vm.resumeExecution(RuntimeValue.Action).let { TestUtils.expectValidCaused(it, debugTypeId) }
        assertEquals(RuntimeValue.String("Howdy, partner"), result2.values[0])

        vm.resumeExecution(RuntimeValue.Action).expectReturnValue().let { assertEquals(RuntimeValue.Action, it) }
    }

    @Test
    fun defineOwnSignals() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
                import core/string (append)
                
                signal Greet(name: String): String
                
                function main() {
                    effect (let e: Greet) {
                        append("Howdy, ", e.name)
                    }
                    
                    let greeting = cause Greet("partner")
                    cause Debug(greeting)
                }
            """.trimIndent()
        )

        TestUtils.expectValidCaused(
            vm.executeFunction("project/test.cau", "main", listOf()), vm.getTypeId("core/builtin.cau", "Debug")
        ).let {
            assertEquals(RuntimeValue.String("Howdy, partner"), it.values[0])
        }

        vm.resumeExecution(RuntimeValue.Action).expectReturnValue().let {
            assertEquals(RuntimeValue.Action, it)
        }
    }

    @Test
    fun correctlyFiltersCustomSignals() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
                import core/string (append, integer_to_string)
                
                signal TestSignal1(value: String): Action
                signal TestSignal2(value: Integer): Action
                
                function main() {
                    effect (let e: TestSignal1) {
                        cause Debug(append("One ", e.value))
                    }
                    effect (let e: TestSignal2) {
                        cause Debug(append("Two ", integer_to_string(e.value)))
                    }
                    
                    cause TestSignal1("hello")
                    cause TestSignal2(42)
                }
            """.trimIndent()
        )

        TestUtils.runMainExpectingDebugs(
            vm, "project/test.cau", listOf(
                "One hello", "Two 42"
            )
        )
    }

    @Test
    fun canCaptureAnySignal() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
                signal Extract(value: Anything): Action
                
                function main() {
                    effect (let s: AnySignal) {
                        cause Extract(s)
                    }
                    
                    cause Debug("this should be extracted and not printed")
                }
            """.trimIndent()
        )

        vm.executeFunction("project/test.cau", "main", listOf())
            .let { TestUtils.expectValidCaused(it, vm.getTypeId("project/test.cau", "Extract")) }.let {
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
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
                signal SetX(value: Integer): Action
                
                function main() {
                    let variable x = 1
                    
                    effect (let s: SetX) {
                        set x = s.value
                    }
                    
                    function update() {
                        cause SetX(2)
                    }
                    
                    update()
                    
                    x
                }
            """.trimIndent()
        )

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(RuntimeValue.Integer(2), result)
    }
}