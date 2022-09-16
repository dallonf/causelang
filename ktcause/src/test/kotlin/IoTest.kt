import com.dallonf.ktcause.CompiledFile
import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue
import com.dallonf.ktcause.types.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class IoTest {
    fun ioFile(): CompiledFile {
        val print = CanonicalLangType.SignalCanonicalLangType(
            CanonicalLangTypeId(
                "test/io.cau", name = "Print", number = 0u
            ), "Print", fields = listOf(
                CanonicalLangType.ObjectField(
                    "message", LangPrimitiveKind.TEXT.toConstraintLangType().asConstraintReference()
                )
            ), result = ActionValueLangType.toConstraint().asConstraintReference()
        )
        val prompt = CanonicalLangType.SignalCanonicalLangType(
            CanonicalLangTypeId(
                "test/io.cau", name = "Prompt", number = 0u
            ),
            "Prompt",
            fields = listOf(),
            result = LangPrimitiveKind.TEXT.toConstraintLangType().asConstraintReference()
        )

        return CompiledFile("test/io.cau", types = buildMap {
            put(print.id, print)
            put(prompt.id, prompt)
        }, chunks = emptyList(), exports = buildMap {
            put("Print", CompiledFile.CompiledExport.Constraint(print.asConstraintReference()))
            put("Prompt", CompiledFile.CompiledExport.Constraint(prompt.asConstraintReference()))
        })
    }

    @Test
    fun receiveValueFromInputEffect() {
        val vm = LangVm {
            addCompiledFile(ioFile())
            addFile(
                "project/test.cau", """
                    import core/text ( append )
                    import test/io ( Print, Prompt )
                
                    function main() {
                        cause Print("What is your name?")
                        cause Print(append("Hello, ", cause Prompt()))
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        val result1 = TestUtils.expectValidCaused(
            vm.executeFunction("project/test.cau", "main", listOf()), vm.codeBundle.getTypeId("test/io.cau", "Print")
        )
        assertEquals(
            result1.debug(), """
            {
                "#type": "test/io.cau:Print",
                "message": "What is your name?"
            }
            """.trimIndent()
        )

        val result2 = TestUtils.expectValidCaused(
            vm.resumeExecution(RuntimeValue.Action), vm.codeBundle.getTypeId("test/io.cau", "Prompt")
        )
        assertEquals(
            result2.debug(), """
            {
                "#type": "test/io.cau:Prompt"
            }
            """.trimIndent()
        )

        val result3 = TestUtils.expectValidCaused(
            vm.resumeExecution(RuntimeValue.Text("Bob")), vm.codeBundle.getTypeId("test/io.cau", "Print")
        )
        assertEquals(
            result3.debug(), """
            {
                "#type": "test/io.cau:Print",
                "message": "Hello, Bob"
            }
            """.trimIndent()
        )

        val finalResult = vm.resumeExecution(RuntimeValue.Action).expectReturnValue()
        assertEquals(finalResult, RuntimeValue.Action)
    }

    @Test
    fun assignReceivedValueToName() {
        val vm = LangVm {
            addCompiledFile(ioFile())
            addFile(
                "project/test.cau", """
                import core/text ( append )
                import test/io ( Print, Prompt )
                
                function main() {
                   cause Print("What is your name?")
                   let name = cause Prompt()
                   cause Print(append("Hello, ", name))
                }
            """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        TestUtils.expectValidCaused(
            vm.executeFunction("project/test.cau", "main", listOf()), vm.codeBundle.getTypeId("test/io.cau", "Print")
        )
        TestUtils.expectValidCaused(
            vm.resumeExecution(RuntimeValue.Action), vm.codeBundle.getTypeId("test/io.cau", "Prompt")
        )
        val finalPrint = TestUtils.expectValidCaused(
            vm.resumeExecution(RuntimeValue.Text("Bob")), vm.codeBundle.getTypeId("test/io.cau", "Print")
        )
        assertEquals(finalPrint.values[0], RuntimeValue.Text("Hello, Bob"))

        assertEquals(vm.resumeExecution(RuntimeValue.Action).expectReturnValue(), RuntimeValue.Action)
    }

    @Test
    fun inlineBlockExpression() {
        val vm = LangVm {
            addCompiledFile(ioFile())
            addFile(
                "project/test.cau", """
                    import core/text ( append )
                    import test/io ( Print, Prompt )
                    
                    function main() {
                      let greeting = {
                        let name = cause Prompt()
                        append("Hello, ", name)
                      }
                      cause Print(greeting)
                    }
                """.trimIndent()
            )
        }
        TestUtils.expectNoCompileErrors(vm)

        TestUtils.expectValidCaused(
            vm.executeFunction("project/test.cau", "main", listOf()), vm.codeBundle.getTypeId("test/io.cau", "Prompt")
        )
        val finalPrint = TestUtils.expectValidCaused(
            vm.resumeExecution(RuntimeValue.Text("Bob")), vm.codeBundle.getTypeId("test/io.cau", "Print")
        )
        assertEquals(finalPrint.values[0], RuntimeValue.Text("Hello, Bob"))
        assertEquals(vm.resumeExecution(RuntimeValue.Action).expectReturnValue(), RuntimeValue.Action)
    }
}