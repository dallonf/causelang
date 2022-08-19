import TestUtils.addFileAndPrintCompileErrors
import TestUtils.addFileExpectingNoCompileErrors
import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.Resolver.debug
import com.dallonf.ktcause.RuntimeValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class OptionsAndUniqueObjects {
    @Test
    fun defineUniqueObjectTypes() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
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
            """.trimIndent(), result.debug()
        )
    }

    @Test
    fun doesntChokeOnInstantiatingUniqueType() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
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
            """.trimIndent(), result.debug()
        )
    }

    @Test
    fun withParensIsStillUnique() {
        val vm = LangVm()
        vm.addFileExpectingNoCompileErrors(
            "project/test.cau", """
                object Test()
                
                function main() {
                    Test
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
            """.trimIndent(), result.debug()
        )
    }

    @Test
    fun supportsTypeAnnotations() {
        val vm = LangVm()
        vm.addFile(
            "project/test.cau", """
                object Test1
                object Test2
                
                function main() {
                    let test: Test1 = Test1
                    let error: Test2 = Test1
                }
            """.trimIndent()
        )

        assertEquals(
            """
            [
                {
                    "position": {
                        "path": "project/test.cau",
                        "breadcrumbs": "declarations.3.body.statements.1.declaration",
                        "position": "6:4-6:28"
                    },
                    "error": {
                        "#type": "MismatchedType",
                        "expected": {
                            "#type": "UniqueObject",
                            "canonicalType": {
                                "#type": "Object",
                                "id": "project/test.cau:Test2",
                                "name": "Test2",
                                "fields": [
                                ]
                            }
                        },
                        "actual": {
                            "#type": "UniqueObject",
                            "canonicalType": {
                                "#type": "Object",
                                "id": "project/test.cau:Test1",
                                "name": "Test1",
                                "fields": [
                                ]
                            }
                        }
                    }
                }
            ]
            """.trimIndent(), vm.compileErrors.debug()
        )


        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(RuntimeValue.Action, result)
    }

    @Test
    fun defineOptionTypes() {
        val vm = LangVm()
        vm.addFileAndPrintCompileErrors(
            "project/test.cau", """
                object Hearts
                object Diamonds
                object Clubs
                object Spades
                
                option Suit(
                    Hearts,
                    Diamonds,
                    Clubs,
                    Spades,
                )
                
                function main() {
                    let card_suit: Suit = Diamonds
                    card_suit
                }
            """.trimIndent()
        )

        val result = vm.executeFunction("project/test.cau", "main", listOf()).expectReturnValue()
        assertEquals(
            """
            {
                "#type": "RuntimeUniqueObject",
                "id": "project/test.cau:Diamonds"
            }
            """.trimIndent(), result.debug()
        )
    }
}