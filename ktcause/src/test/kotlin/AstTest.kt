import com.dallonf.ktcause.*
import kotlin.test.Test
import com.dallonf.ktcause.parse.parse
import com.tylerthrailkill.helpers.prettyprint.pp

internal class AstTest {
    @Test
    fun testParse() {
        val source = """
            import core/string ( append )
            
            function main() {
                cause Debug(append("Hello, ", "Bob"))
            }
        """.trimIndent()
        val ast = parse(source)
        val analyzed = Analyzer.analyzeFile(ast)
        val resolved = Resolver.resolveForFile(
            path = "project/test.cau", fileNode = ast, analyzed = analyzed, otherFiles = mapOf()
        )
        resolved.getUniqueErrors().pp()
        val compiled = Compiler.compile(ast, analyzed, resolved)
        val vm = LangVm()
        vm.addFile("project/test.cau", source)
        val result1 = vm.executeFunction("project/test.cau", "main", emptyList())
        println(result1.expectCaused().debug())
        val result2 = vm.resumeExecution(RuntimeValue.Action)
        println(result2.expectReturned().debug())
    }
}