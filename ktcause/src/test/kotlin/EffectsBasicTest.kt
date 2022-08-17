import TestUtils.addFileAndPrintCompileErrors
import TestUtils.addFileExpectingNoCompileErrors
import com.dallonf.ktcause.CompiledFile
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.Resolver.debug
import com.dallonf.ktcause.RuntimeValue
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
                        result = LangPrimitiveKind.ACTION.toConstraintLangType()
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
                    effect (_: InterceptThis) {
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
                        greetTypeId,
                        greetTypeId.name!!,
                        listOf(
                            CanonicalLangType.ObjectField(
                                "name",
                                LangPrimitiveKind.STRING.toConstraintLangType()
                            )
                        ),
                        result = LangPrimitiveKind.ACTION.toConstraintLangType()
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
        val result2 = vm.resumeExecution(RuntimeValue.Action)
            .let { TestUtils.expectValidCaused(it, debugTypeId) }
        assertEquals(RuntimeValue.String("Howdy, partner"), result2.values[0])

        vm.resumeExecution(RuntimeValue.Action).expectReturnValue()
            .let { assertEquals(RuntimeValue.Action, it) }
    }

    @Test
    fun defineOwnSignals() {
        val vm = LangVm()
        vm.addFileAndPrintCompileErrors(
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
            vm.executeFunction("project/test.cau", "main", listOf()),
            vm.getTypeId("core/builtin", "Debug")
        ).let {
            assertEquals(RuntimeValue.String("Howdy, partner"), it.values[0])
        }

        vm.resumeExecution(RuntimeValue.Action).expectReturnValue().let {
            assertEquals(RuntimeValue.Action, it)
        }
    }
}