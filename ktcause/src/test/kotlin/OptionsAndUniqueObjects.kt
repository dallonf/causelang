import TestUtils.addFileAndPrintCompileErrors
import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class OptionsAndUniqueObjects {
    @Test
    fun defineUniqueObjectTypes() {
        val vm = LangVm()
        vm.addFileAndPrintCompileErrors(
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

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(
            """
            {
                "#type": "RuntimeUniqueObject",
                "id": "project/test.cau:Two"
            }
            """.trimIndent(),
            result.debug()
        )
    }

    @Test
    fun doesntChokeOnInstantiatingUniqueType() {
        val vm = LangVm()
        vm.addFileAndPrintCompileErrors(
            "project/test.cau", """
                object Test
                
                function main() {
                    Test()
                }
            """.trimIndent()
        )

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(
            """
            {
                "#type": "RuntimeUniqueObject",
                "id": "project/test.cau:Test"
            }
            """.trimIndent(),
            result.debug()
        )
    }
}