import com.dallonf.ktcause.Analyzer
import kotlin.test.Test
import com.dallonf.ktcause.parse.parse
import com.tylerthrailkill.helpers.prettyprint.pp

internal class AstTest {
    @Test
    fun testParse() {
        val ast = parse(
            """
                import core/string ( append )
                import test/io ( Print, Prompt )
                
                function main() {
                    cause Print("What is your name?")
                    let name = cause Prompt()
                    cause Print(append("Hello, ", name))
                }
            """.trimIndent()
        )
        val analyzed = Analyzer.analyzeFile(ast)
        analyzed.pp()
    }
}