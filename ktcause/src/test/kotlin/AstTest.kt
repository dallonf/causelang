import kotlin.test.Test
import com.dallonf.ktcause.parse.parse
import com.tylerthrailkill.helpers.prettyprint.pp

internal class AstTest {
    @Test
    fun testParseImport() {
        val ast = parse(
            """
                import core/string ( append )
                import test/io ( Print, Prompt ) 
            """.trimIndent()
        )
        ast.pp()
    }
}