import com.dallonf.ktcause.LangVm
import org.junit.jupiter.api.Test

class RealWorldExamples {
    @Test
    fun nestedCount() {
        val vm = LangVm {
            addFile(
                "project/test.cau", """
                    import core/math (add, greater_than)
                    import core/text (append, number_to_text)
    
                    function count_inclusive(from: Number, to: Number, callback: Function(i: Number): Action): Action {
                      let variable current = from
                      loop {
                        branch {
                          if greater_than(current, to) => return
                          else => {
                            callback(current)
                            set current = add(current, 1)
                          }
                        }
                      }
                    }
                    
                    function main() {
                        let MAX = 2
                        count_inclusive(0, MAX, fn(i: Number) {
                            let row = i
                            count_inclusive(0, MAX, fn(i: Number) {
                                let column = i
                                cause Debug(append(append(number_to_text(row), ", "), number_to_text(column)))
                            })
                            cause Debug("end of row")
                        })
                        cause Debug("done")
                    }
                """.trimIndent()
            )
        }

        TestUtils.expectNoCompileErrors(vm)

        TestUtils.runMainExpectingDebugs(
            vm, "project/test.cau", listOf(
                "0, 0",
                "0, 1",
                "0, 2",
                "end of row",
                "1, 0",
                "1, 1",
                "1, 2",
                "end of row",
                "2, 0",
                "2, 1",
                "2, 2",
                "end of row",
                "done",
            )
        )

    }
}