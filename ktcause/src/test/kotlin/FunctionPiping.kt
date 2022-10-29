import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FunctionPiping {
    @Test
    fun canPipeTextAppend() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/text (append, number_to_text)
                    
                    function main(name: Text, favorite_number: Number) {
                        "hello">>append(", ")>>append(name)>>append("!")
                            >>append(" ")
                            >>append("My favorite number is")
                            >>append(" ")
                            >>append(favorite_number>>number_to_text())
                    }
                """.trimIndent()
            )
        }

        TestUtils.printCompileErrors(vm)
        vm.executeFunction(
            "project/test.cau", "main", listOf(
                RuntimeValue.Text("Douglas"),
                RuntimeValue.Number(42)
            )
        ).expectReturnValue().let {
            assertEquals(RuntimeValue.Text("hello, Douglas! My favorite number is 42"), it)
        }
    }
}