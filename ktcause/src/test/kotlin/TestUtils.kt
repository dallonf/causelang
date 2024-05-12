import com.dallonf.ktcause.*
import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.Resolver.debug
import com.dallonf.ktcause.gen.AstRustSerialization
import com.dallonf.ktcause.serialization.RustSerialization
import com.dallonf.ktcause.types.CanonicalLangTypeId
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.assertEquals

object TestUtils {

    fun assertSerializationEqual(vm: LangVm) {
        fun normalizeMapOrdering(jsonElement: JsonElement): JsonElement {
            if (jsonElement is JsonObject) {
                val sortedEntries = jsonElement.entries.sortedBy { (key, _) -> key }
                return buildJsonObject {
                    sortedEntries.forEach { (key, value) ->
                        put(key, normalizeMapOrdering(value))
                    }
                }
            } else {
                return jsonElement
            }
        }

        vm.codeBundle.inputFilesDebugContext?.forEach { (path, file) ->
            val ast = file.ast
            if (ast != null) {
                val rsAstJson = RustCompiler.rsSerializeAst(ast)
                val normalizedRsAstJson = RustSerialization.encoder.parseToJsonElement(rsAstJson).let {
                    RustSerialization.encoder.encodeToString(it)
                }
                val ktAstJson =
                    AstRustSerialization.serializeFile(ast).let { RustSerialization.encoder.encodeToString(it) }
                assertEquals(
                    normalizedRsAstJson, ktAstJson, "Kotlin-generated AST JSON for $path does not match Rust-generated"
                )
            }

            val tags = file.analyzed?.nodeTags
            if (tags != null) {
                val filteredTags = RustCompiler.getFilteredTags(tags)
                val rsTagsJson = RustCompiler.rsSerializeTags(filteredTags)
                val normalizedRsTagsJson =
                    RustSerialization.encoder.parseToJsonElement(rsTagsJson).let { rsTagsJsonParsed ->
                        require(rsTagsJsonParsed is JsonObject)
                        val sorted = rsTagsJsonParsed.entries.sortedBy { (key, _) -> key }
                        RustSerialization.encoder.encodeToString(sorted)
                    }
                val ktTagsJson = RustSerialization.serializeNodeTagMap(filteredTags).let { ktTagsSerialized ->
                    require(ktTagsSerialized is JsonObject)
                    val sorted = ktTagsSerialized.entries.sortedBy { (key, _) -> key }
                    RustSerialization.encoder.encodeToString(sorted)
                }
                assertEquals(
                    normalizedRsTagsJson,
                    ktTagsJson,
                    "Kotlin-generated node tags for $path does not match Rust-generated"
                )
            }
        }

        val externalFiles = vm.codeBundle.files.mapValues { (_, file) -> file.toFileDescriptor() }.filter { (path, _) ->
            if (path.startsWith("core/")) {
                RustCompiler.supportedCoreImports.contains(path)
            } else {
                true
            }
        }.let { RustCompiler.getFilteredExternalFiles(it) }
        val rsExternalFilesJson = RustCompiler.rsSerializeExternalFiles(externalFiles).let {
            RustSerialization.encoder.parseToJsonElement(it)
        }.let { normalizeMapOrdering(it) }.let { RustSerialization.encoder.encodeToString(it) }
        val ktExternalFilesJson =
            RustSerialization.serializeExternalFileDescriptorMap(externalFiles).let { normalizeMapOrdering(it) }
                .let { RustSerialization.encoder.encodeToString(it) }
        assertEquals(
            rsExternalFilesJson, ktExternalFilesJson, "Kotlin-generated file descriptors do not match Rust-generated"
        )
    }

    fun expectNoCompileErrors(vm: LangVm) {
        val (_, compileErrors) = vm.codeBundle
        if (compileErrors.isNotEmpty()) {
            Debug.printCompileErrors(vm)
            throw AssertionError("Compile errors: ${compileErrors.debug()}")
        }
    }

    fun printCompileErrors(vm: LangVm) {
        Debug.printCompileErrors(vm)
    }

    // TODO: it's kinda weird that there's two ways to get almost the same runtime error, hm?
    fun expectTypeError(result: RunResult, vm: LangVm): RuntimeValue.BadValue {
        require(result is RunResult.Caused)
        assertEquals(vm.codeBundle.getTypeId("core/builtin.cau", "TypeError"), result.signal.typeDescriptor.id)
        return result.signal.values[0] as RuntimeValue.BadValue
    }

    fun expectBadValue(value: RuntimeValue, expectedError: String) {
        require(value is RuntimeValue.BadValue) { "Expected BadValue, was ${value.debug()}" }
        assertEquals(expectedError, value.debug())
    }

    fun expectInvalidSignal(result: RunResult): RuntimeValue.BadValue {
        require(result is RunResult.Caused)
        return result.signal.validate() as RuntimeValue.BadValue
    }

    fun expectValidCaused(result: RunResult, expectedType: CanonicalLangTypeId): RuntimeValue.RuntimeObject {
        val signal = result.expectCausedSignal().validate() as RuntimeValue.RuntimeObject
        assertEquals(expectedType, signal.typeDescriptor.id)
        return signal
    }

    fun runMainExpectingDebugs(vm: LangVm, path: String, expected: List<String>) {
        runMainExpectingDebugValues(vm, path, expected.map { RuntimeValue.Text(it) })
    }

    fun runMainExpectingDebugValues(vm: LangVm, path: String, expected: List<RuntimeValue>) {
        assertEquals(expected, runMainAndGetDebugValues(vm, path))
    }

    fun runMainAndGetDebugValues(vm: LangVm, path: String): List<RuntimeValue> {
        var result = vm.executeFunction(path, "main", listOf())
        val debugType = vm.codeBundle.getTypeId("core/builtin.cau", "Debug")

        val debugs = mutableListOf<RuntimeValue>()
        while (result is RunResult.Caused) {
            assertEquals(debugType, result.signal.typeDescriptor.id)
            debugs.add(result.signal.values[0])

            result = vm.resumeExecution(RuntimeValue.Action)
        }

        assertEquals(RuntimeValue.Action, result.expectReturnValue())
        return debugs
    }
}