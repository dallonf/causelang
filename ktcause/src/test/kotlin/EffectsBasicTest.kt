import com.dallonf.ktcause.CompiledFile
import com.dallonf.ktcause.LangVm
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
                        result = LangPrimitiveKind.ACTION.toValueLangType()
                    )
                ),
                chunks = listOf(),
                exports = mapOf(interceptThisTypeId.name!! to CompiledFile.CompiledExport.Type(interceptThisTypeId))
            )
        )
        TestUtils.addFileExpectingNoCompileErrors(
            vm, "project/test.cau", """
                import test/test (InterceptThis)
                
                function main() {
                    effect (_: InterceptThis) {
                        cause Debug("Intercepted an InterceptThis effect")
                    }
                    
                    cause InterceptThis()
                    cause Debug("This should not have been intercepted")
                    
                }
            """.trimIndent()
        )

        val debugTypeId = vm.getTypeId("core/builtin.cau", "Debug")
        val result1 = vm.executeFunction("project/test.cau", "main", listOf())
            .let { TestUtils.expectValidCaused(it, debugTypeId) }
        assertEquals(result1.values[0], RuntimeValue.String("Intercepted an InterceptThis effect"))

        val result2 = vm.resumeExecution(RuntimeValue.Action)
            .let { TestUtils.expectValidCaused(it, debugTypeId) }
        assertEquals(result2.values[0], RuntimeValue.String("This should not have been intercepted"))

        vm.resumeExecution(RuntimeValue.Action).expectReturnValue()
            .let { assertEquals(it, RuntimeValue.Action) }
    }
}