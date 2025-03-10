import com.dallonf.ktcause.LangVm
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ExecutionTraceTest {
    @Test
    fun basicExecutionTrace() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    function x() {
                        y()
                    }
                    
                    function y() {
                        z()
                    }
                    
                    function z() {
                        cause AssumptionBroken("Kaboom!")
                    }
                    
                    function main() {
                        x()
                    }
                """.trimIndent()
            )
        }

        TestUtils.expectNoCompileErrors(vm)

        TestUtils.expectValidCaused(
            vm.executeFunction("project/test.cau", "main", listOf()),
            vm.codeBundle.getBuiltinTypeId("AssumptionBroken")
        )

        assertEquals(
            """
            Traceback (most recent call last):
            ${"\t"}function main() at project/test.cau at line 14
            ${"\t"}function x() at project/test.cau at line 2
            ${"\t"}function y() at project/test.cau at line 6
            ${"\t"}function z() at project/test.cau at line 10
            
            """.trimIndent(), vm.getExecutionTrace()
        )
    }
}