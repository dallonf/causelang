package com.dallonf.ktcause

import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.ast.Breadcrumbs
import com.dallonf.ktcause.ast.SourcePosition
import com.dallonf.ktcause.parse.parse
import com.dallonf.ktcause.types.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LangVm {
    open class VmError(message: String) : Error(message)
    class InternalVmError(message: String) : VmError(message)

    private val files = mutableMapOf<String, CompiledFile>()
    private val _compileErrors = mutableListOf<Resolver.ResolverError>()
    private var callFrame: CallFrame? = null
    private val stack: ArrayDeque<RuntimeValue> = ArrayDeque()

    val compileErrors: List<Resolver.ResolverError>
        get() = _compileErrors

    private class CallFrame(val chunk: CompiledFile.InstructionChunk, val parent: CallFrame? = null) {
        var instruction: Int = 0
        var stackStart: Int = 0
        var pendingSignal: RuntimeValue.RuntimeObject? = null
    }

    data class ErrorTrace(
        val position: SourcePosition,
        val error: ErrorValueLangType,
        val proxyChain: List<SourcePosition>
    )

    fun addCompiledFile(file: CompiledFile) {
        files[file.path] = file
    }

    fun addFile(filePath: String, source: String) {
        val astNode = parse(source)
        val analyzedFile = Analyzer.analyzeFile(astNode)
        // TODO: need a step between here and compilation to allow for loading other files

        val otherFiles = files.mapValues { (path, compiledFile) -> compiledFile.toFileDescriptor() }
        val (resolvedFile, resolverErrors) = Resolver.resolveForFile(
            filePath, astNode, analyzedFile, otherFiles
        )
        _compileErrors.addAll(resolverErrors)

        val compiledFile = Compiler.compile(astNode, analyzedFile, resolvedFile)
        addCompiledFile(compiledFile)
    }

    private fun getFileDescriptor(filePath: String): Resolver.ExternalFileDescriptor {
        return if (filePath == "core/builtin.cau") {
            CoreDescriptors.coreBuiltinFile.second
        } else if (filePath.startsWith("core/")) {
            CoreDescriptors.coreFiles.find { it.first == filePath }?.second
        } else {
            files[filePath]?.toFileDescriptor()
        }.let { requireNotNull(it) { "I couldn't find a file called $filePath" } }
    }

    fun getTypeId(filePath: String, name: String): CanonicalLangTypeId {
        val descriptor = getFileDescriptor(filePath)

        val found = requireNotNull(descriptor.exports[name]) { "$filePath doesn't have an export called $name." }

        return when (found) {
            is CanonicalLangType.SignalCanonicalLangType -> found.id
            is TypeReferenceValueLangType -> found.canonicalType.id
            else -> throw VmError("$name isn't a type.")
        }
    }

    fun executeFunction(filePath: String, functionName: String, parameters: List<RuntimeValue>): RunResult {
        val file = requireNotNull(files[filePath]) { "I don't know about any file at $filePath." }
        val export =
            requireNotNull(file.exports[functionName]) { "The file at $filePath doesn't contain anything (at least that's not private) called $functionName." }

        val function =
            requireNotNull(export as? CompiledFile.CompiledExport.Function) { "$functionName isn't a function, so I can't execute it." }

        require(function.type is FunctionValueLangType)
        if (parameters.isNotEmpty() || function.type.params.isNotEmpty()) {
            TODO("I don't support executing a function with arguments right now.")
        }

        stack.clear()
        callFrame = CallFrame(file.chunks[function.chunkIndex])

        return execute()
    }

    fun resumeExecution(value: RuntimeValue): RunResult {
        val STATE_ERROR = "I'm not currently waiting for a signal, so I can't resume execution."
        val callFrame = requireNotNull(callFrame) { STATE_ERROR }
        val pendingSignal = requireNotNull(callFrame.pendingSignal) { STATE_ERROR }
        val pendingSignalType = when (pendingSignal.typeDescriptor.type) {
            is CanonicalLangType.SignalCanonicalLangType -> pendingSignal.typeDescriptor.type
        }

        if (value.isAssignableTo(pendingSignalType.result)) {
            callFrame.pendingSignal = null
            stack.addLast(value)
            return execute()
        } else {
            throw VmError("I need to resolve a ${pendingSignalType.name} signal with a ${pendingSignalType.result}, but $value isn't a ${pendingSignalType.result}.")
        }
    }

    private val COMPILE_ERROR_ASSURANCE =
        "This probably isn't your fault. This shouldn't happen if the compiler is working properly.";

    private val stackJsonSerializer = Json

    private fun execute(): RunResult {
        while (true) {
            val callFrame = requireNotNull(callFrame) { "I'm not ready to execute anything!" }

            val instruction =
                requireNotNull(callFrame.chunk.instructions.getOrNull(callFrame.instruction)) { "I've gotten to instruction #${callFrame.instruction}, but there are no more instructions to read! $COMPILE_ERROR_ASSURANCE" }

            fun getConstant(id: Int): CompiledFile.CompiledConstant {
                return requireNotNull(callFrame.chunk.constantTable.getOrNull(id)) {
                    "I'm looking for a constant with the ID of $id, but I can't find it. $COMPILE_ERROR_ASSURANCE"
                }
            }

            // TODO: VM play-by-play enabled optionally
            val debugStack = stackJsonSerializer.encodeToString(stack.map { it.toJson() })
            println("stack: $debugStack")
            println("instruction: $instruction")

            when (instruction) {
                is Instruction.Pop -> stack.removeLast()
                is Instruction.PushAction -> TODO()
                is Instruction.Literal -> {
                    val newValue = when (val constant = getConstant(instruction.constant)) {
                        is CompiledFile.CompiledConstant.StringConst -> RuntimeValue.String(constant.value)
                        is CompiledFile.CompiledConstant.IntegerConst -> RuntimeValue.Integer(constant.value)
                        is CompiledFile.CompiledConstant.FloatConst -> RuntimeValue.Float(constant.value)
                        is CompiledFile.CompiledConstant.ErrorConst -> RuntimeValue.BadValue(
                            constant.sourcePosition,
                            constant.error
                        )
                    }
                    stack.addLast(newValue)
                }

                is Instruction.Import -> {
                    val filePath = run {
                        val constant = getConstant(instruction.filePathConstant)
                        if (constant is CompiledFile.CompiledConstant.StringConst) {
                            constant.value
                        } else {
                            throw VmError("I was expecting constant #${instruction.filePathConstant} to be a filepath string, but it was $constant. $COMPILE_ERROR_ASSURANCE")
                        }
                    }
                    val exportName = run {
                        val constant = getConstant(instruction.exportNameConstant)
                        if (constant is CompiledFile.CompiledConstant.StringConst) {
                            constant.value
                        } else {
                            throw VmError("I was expecting constant #${instruction.exportNameConstant} to be an identifier string, but it was $constant. $COMPILE_ERROR_ASSURANCE")
                        }
                    }

                    if (filePath.startsWith("core/")) {
                        val value = CoreExports.getCoreExport(filePath, exportName)
                        stack.addLast(value)
                    } else {
                        val file = requireNotNull(files[filePath]) { "I couldn't find the file: $filePath." }
                        val export =
                            requireNotNull(file.exports[exportName]) { "The file $filePath doesn't export anything (at least non-private) called $exportName." }

                        val value = when (export) {
                            is CompiledFile.CompiledExport.Type -> {
                                val canonicalType =
                                    requireNotNull(file.types[export.typeId]) { "The file $filePath exports a type of ${export.typeId} but doesn't define it" }
                                RuntimeValue.RuntimeTypeReference(canonicalType)
                            }

                            is CompiledFile.CompiledExport.Function -> TODO()
                            is CompiledFile.CompiledExport.Value -> TODO()
                        }

                        stack.addLast(value)
                    }
                }

                is Instruction.ReadLocal -> TODO()
                is Instruction.Construct -> {
                    val constructorType = stack.removeLast().let {
                        if (it is RuntimeValue.RuntimeTypeReference)
                            it
                        else throw VmError("Tried to construct a $it. $COMPILE_ERROR_ASSURANCE")
                    }

                    val params = mutableListOf<RuntimeValue>()
                    for (i in 0 until instruction.arity) {
                        params.add(stack.removeLast())
                    }
                    params.reverse()

                    val obj = RuntimeValue.RuntimeObject(constructorType, params)
                    stack.addLast(obj)
                }

                is Instruction.CallFunction -> {
                    val function = stack.removeLast()

                    val params = mutableListOf<RuntimeValue>()
                    for (i in 0 until instruction.arity) {
                        params.add(stack.removeLast())
                    }
                    params.reverse()

                    when (function) {
                        is RuntimeValue.NativeFunction -> {
                            // TODO: probably want to runtime typecheck native function
                            // params in development
                            val result = function.function(params)
                            stack.addLast(result)
                        }

                        is RuntimeValue.BadValue -> throw VmError("I tried to call a function, but it has an error: $function")
                        else -> throw InternalVmError("Can't call $function")
                    }
                }

                is Instruction.Cause -> {
                    val signal = stack.removeLast().let {
                        when (it) {
                            is RuntimeValue.RuntimeObject -> it
                            is RuntimeValue.BadValue -> throw VmError("I tried to cause a signal, but is has an error: $it")
                            else -> throw InternalVmError("Can't cause $it")
                        }
                    }

                    callFrame.pendingSignal = signal
                    callFrame.instruction += 1
                    return RunResult.Caused(signal)
                }

                is Instruction.Return -> {
                    val value = stack.removeLast()

                    // TODO: handle popping a call frame and returning to a calling function
                    return RunResult.Returned(value)
                }
            }

            callFrame.instruction += 1
        }
    }
}

sealed interface RunResult {
    data class Returned(val returnValue: RuntimeValue) : RunResult {
        fun debug() = returnValue.debug()
    }

    data class Caused(val signal: RuntimeValue.RuntimeObject) : RunResult {
        fun debug() = signal.debug()
    }

    fun expectReturned(): Returned = this as Returned
    fun expectCaused(): Caused = this as Caused
}