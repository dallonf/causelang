import TestUtils.addFileAndPrintCompileErrors
import TestUtils.addFileExpectingNoCompileErrors
import com.dallonf.ktcause.LangVm
import org.junit.jupiter.api.Test

class EarlyReturns {
    @Test
    fun earlyReturnAction() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau",
            """
                function main() {
                    cause Debug("Should print")
                    return
                    cause Debug("Should not print")
                }
            """.trimIndent()
        )

        TestUtils.runMainExpectingDebugs(vm, "project/test.cau", listOf("Should print"))
    }
}