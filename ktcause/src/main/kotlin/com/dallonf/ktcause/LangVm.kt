package com.dallonf.ktcause

import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.Debug.debugMini
import com.dallonf.ktcause.types.*


class LangVm(val codeBundle: CodeBundle, val options: Options = Options()) {

    data class Options(val runawayLoopThreshold: Long? = 5_000, val debugInstructionLevelExecution: Boolean = false)

    constructor(options: Options = Options(), buildBundle: CodeBundleBuilder.() -> Unit) : this(
        CodeBundleBuilder().let { builder ->
            buildBundle(builder)
            builder.build()
        }, options
    )

    open class VmError(message: String) : Error(message)
    class InternalVmError(message: String) :
        VmError("$message This probably isn't your fault. This shouldn't happen if the compiler is working properly.")

    private var stackFrame: StackFrame? = null

    private data class RuntimeEffect(
        val file: CompiledFile,
        val chunk: CompiledFile.InstructionChunk,
        val existsInFrame: StackFrame,
        val nextEffect: RuntimeEffect?
    )

    private class OpenLoop(
        val stackFrame: StackFrame,
        val continueInstruction: Int,
        val breakInstruction: Int,
        val stackEnd: Int,
        val outerLoop: OpenLoop?,
    ) {
        var iterations: Long = 0
    }

    private sealed class StackFrame(
        val file: CompiledFile,
        val chunk: CompiledFile.InstructionChunk,
        val stack: ArrayDeque<RuntimeValue>,
        var firstEffect: RuntimeEffect?,
        var currentLoop: OpenLoop?
    ) {
        var instruction: Int = 0
        var pendingSignal: RuntimeValue.RuntimeObject? = null

        abstract val executionParent: StackFrame?

        abstract val stackStart: Int

        class Main(
            file: CompiledFile,
            chunk: CompiledFile.InstructionChunk,
            stack: ArrayDeque<RuntimeValue>,
            firstEffect: RuntimeEffect?,
            currentLoop: OpenLoop?
        ) : StackFrame(file, chunk, stack, firstEffect, currentLoop) {
            override val executionParent
                get() = null

            override val stackStart
                get() = 0
        }

        class Call(
            val parent: StackFrame,
            override var stackStart: Int,
            file: CompiledFile,
            chunk: CompiledFile.InstructionChunk,
            stack: ArrayDeque<RuntimeValue>,
            firstEffect: RuntimeEffect?,
            currentLoop: OpenLoop?,
        ) : StackFrame(file, chunk, stack, firstEffect, currentLoop) {
            override val executionParent
                get() = parent
        }

        class Cause(
            val causeParent: StackFrame,
            val existsInFrame: StackFrame,
            file: CompiledFile,
            chunk: CompiledFile.InstructionChunk,
            stack: ArrayDeque<RuntimeValue>,
            firstEffect: RuntimeEffect?,
            currentLoop: OpenLoop?,
        ) : StackFrame(file, chunk, stack, firstEffect, currentLoop) {
            override val executionParent
                get() = causeParent

            override val stackStart
                get() = 0
        }
    }

    fun executeFunction(filePath: String, functionName: String, parameters: List<RuntimeValue>): RunResult {
        val file = codeBundle.requireFile(filePath)
        val export =
            requireNotNull(file.exports[functionName]) { "The file at $filePath doesn't contain anything (at least that's not private) called $functionName." }

        val function =
            requireNotNull(export as? CompiledFile.CompiledExport.Function) { "$functionName isn't a function, so I can't execute it." }

        require(function.type is FunctionValueLangType)
        if (parameters.size != function.type.params.size) {
            throw IllegalArgumentException("This function needs ${function.type.params.size} parameters, but I only received ${parameters.size}")
        }

        val stack = ArrayDeque<RuntimeValue>()
        for (param in parameters) {
            stack.addLast(param)
        }

        stackFrame = StackFrame.Main(
            file,
            file.chunks[function.chunkIndex],
            stack = stack,
            firstEffect = null,
            currentLoop = null,
        )

        return execute()
    }

    fun resumeExecution(value: RuntimeValue): RunResult {
        val STATE_ERROR = "I'm not currently waiting for a signal, so I can't resume execution."
        val stackFrame = requireNotNull(stackFrame) { STATE_ERROR }
        val pendingSignal = requireNotNull(stackFrame.pendingSignal) { STATE_ERROR }
        val pendingSignalType = when (pendingSignal.typeDescriptor) {
            is CanonicalLangType.SignalCanonicalLangType -> pendingSignal.typeDescriptor
            is CanonicalLangType.ObjectCanonicalLangType -> error("somehow an object got in pendingSignal")
        }

        if (value.isAssignableTo(pendingSignalType.result)) {
            stackFrame.pendingSignal = null
            stackFrame.stack.addLast(value)
            return execute()
        } else {
            throw VmError("I need to resolve a ${pendingSignalType.name} signal with a ${pendingSignalType.result}, but $value isn't a ${pendingSignalType.result}.")
        }
    }

    /**
     * Reports to the VM that control has passed through the host for a "tick".
     * For example, you should call this once per frame in a game.
     *
     * This allows long-running functions and loops to execute without getting caught as "runaway",
     * as long as they're allowing I/O of the host application to continue and not hogging the thread.
     */
    fun reportTick() {
        stackFrame?.let { resetLoopsForStackFrame(it) }
    }

    private fun resetLoopsForStackFrame(stackFrame: StackFrame) {
        var currentLoop = stackFrame.currentLoop
        while (currentLoop != null) {
            currentLoop.iterations = 0
            currentLoop = currentLoop.outerLoop
        }
        when (stackFrame) {
            is StackFrame.Call -> resetLoopsForStackFrame(stackFrame.parent)
            is StackFrame.Cause -> {
                resetLoopsForStackFrame(stackFrame.causeParent)
                resetLoopsForStackFrame(stackFrame.existsInFrame)
            }

            is StackFrame.Main -> {}
        }
    }

    private fun execute(): RunResult {
        while (true) {
            run iteration@{
                val stackFrame = requireNotNull(stackFrame) { throw VmError("I'm not ready to execute anything!") }
                val stack = stackFrame.stack

                val instruction = requireNotNull(stackFrame.chunk.instructions.getOrNull(stackFrame.instruction)) {
                    throw InternalVmError(
                        "I've gotten to instruction #${stackFrame.instruction}, but there are no more instructions to read!"
                    )
                }
                // note: This means the instruction pointer will always be one ahead of the actual instruction!
                stackFrame.instruction += 1

                fun getConstant(id: Int): CompiledFile.CompiledConstant {
                    return requireNotNull(stackFrame.chunk.constantTable.getOrNull(id)) {
                        throw InternalVmError("I'm looking for a constant with the ID of $id, but I can't find it.")
                    }
                }

                if (options.debugInstructionLevelExecution) {
                    val debugStack = stack.joinToString(", ") { it.debugMini() }
                    println("stack: $debugStack")
                    println("instruction #${stackFrame.instruction - 1}: $instruction")
                }

                when (instruction) {
                    is Instruction.NoOp -> {}

                    is Instruction.Pop -> {
                        for (i in 0 until instruction.number) {
                            stack.removeLast()
                        }
                    }

                    is Instruction.PopScope -> {
                        val result = stack.removeLast()
                        for (i in 0 until instruction.values) {
                            stack.removeLast()
                        }
                        stack.addLast(result)
                    }

                    is Instruction.RegisterEffect -> {
                        val chunk = stackFrame.file.chunks[instruction.chunk]
                        stackFrame.firstEffect = RuntimeEffect(
                            stackFrame.file, chunk, existsInFrame = stackFrame, nextEffect = stackFrame.firstEffect
                        )
                    }

                    is Instruction.PopEffects -> {
                        for (i in 0 until instruction.number) {
                            stackFrame.firstEffect = stackFrame.firstEffect!!.nextEffect
                        }
                    }

                    is Instruction.PushAction -> stack.addLast(RuntimeValue.Action)

                    is Instruction.Literal -> {
                        val newValue = when (val constant = getConstant(instruction.constant)) {
                            is CompiledFile.CompiledConstant.StringConst -> RuntimeValue.Text(constant.value)
                            is CompiledFile.CompiledConstant.NumberConst -> RuntimeValue.Number(constant.value)
                            is CompiledFile.CompiledConstant.TypeConst -> RuntimeValue.RuntimeTypeConstraint(constant.type)
                            is CompiledFile.CompiledConstant.ErrorConst -> RuntimeValue.BadValue(
                                constant.sourcePosition, constant.error
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
                                throw InternalVmError("I was expecting constant #${instruction.filePathConstant} to be a filepath string, but it was $constant.")
                            }
                        }
                        val exportName = run {
                            val constant = getConstant(instruction.exportNameConstant)
                            if (constant is CompiledFile.CompiledConstant.StringConst) {
                                constant.value
                            } else {
                                throw InternalVmError("I was expecting constant #${instruction.exportNameConstant} to be an identifier string, but it was $constant.")
                            }
                        }


                        val file = codeBundle.requireFile(filePath)
                        val value = RuntimeValue.fromExport(file, exportName)
                        stack.addLast(value)
                    }

                    is Instruction.ImportSameFile -> {
                        val exportName =
                            requireNotNull(getConstant(instruction.exportNameConstant) as? CompiledFile.CompiledConstant.StringConst) {
                                throw InternalVmError("Can't get exportName")
                            }.value
                        val value = RuntimeValue.fromExport(stackFrame.file, exportName)
                        stack.addLast(value)
                    }

                    is Instruction.DefineFunction -> {
                        val capturedValues = run {
                            val list = mutableListOf<RuntimeValue>()
                            for (i in 0 until instruction.capturedValues) {
                                list.add(stack.removeLast())
                            }
                            list.reverse()
                            list.toList()
                        }

                        val functionType =
                            (getConstant(instruction.typeConstant) as CompiledFile.CompiledConstant.TypeConst).type as FunctionValueLangType

                        val functionValue = RuntimeValue.Function(
                            functionType.name,
                            file = stackFrame.file,
                            chunkIndex = instruction.chunkIndex,
                            type = functionType,
                            capturedValues = capturedValues
                        )
                        stack.addLast(functionValue)
                    }

                    is Instruction.ReadLocal -> {
                        val index = instruction.index
                        val value = stack[stackFrame.stackStart + index]
                        stack.addLast(value)
                    }

                    is Instruction.WriteLocal -> {
                        val index = stackFrame.stackStart + instruction.index
                        val value = stack.removeLast()
                        stack[index] = value
                    }

                    is Instruction.ReadLocalThroughEffectScope -> {
                        val index = instruction.index
                        var parentFrame = stackFrame
                        for (i in 0 until instruction.effectDepth) {
                            require(parentFrame is StackFrame.Cause)
                            parentFrame = parentFrame.existsInFrame
                        }
                        val value = parentFrame.stack[parentFrame.stackStart + index]
                        stack.addLast(value)
                    }

                    is Instruction.WriteLocalThroughEffectScope -> {
                        val index = stackFrame.stackStart + instruction.index
                        var parentFrame = stackFrame
                        for (i in 0 until instruction.effectDepth) {
                            require(parentFrame is StackFrame.Cause)
                            parentFrame = parentFrame.existsInFrame
                        }
                        val value = stack.removeLast()
                        parentFrame.stack[parentFrame.stackStart + index] = value
                    }

                    is Instruction.Construct -> {
                        val constructorTypeValue = stack.removeLast()
                        val constructorType = constructorTypeValue.let { stackValue ->
                            (stackValue as? RuntimeValue.RuntimeTypeConstraint)?.let {
                                it.valueType as? InstanceValueLangType
                            } ?: throw InternalVmError("Tried to construct a $stackValue.")
                        }

                        val params = mutableListOf<RuntimeValue>()
                        for (i in 0 until instruction.arity) {
                            params.add(stack.removeLast())
                        }
                        params.reverse()

                        if (constructorType.canonicalType.isUnique()) {
                            stack.addLast(constructorTypeValue)
                        } else {
                            val obj = RuntimeValue.RuntimeObject(constructorType.canonicalType, params)
                            stack.addLast(obj)
                        }

                    }

                    is Instruction.CallFunction -> {
                        when (val function = stack.removeLast()) {
                            is RuntimeValue.NativeFunction -> {
                                // TODO: probably want to runtime typecheck native function
                                // params in development
                                val params = mutableListOf<RuntimeValue>()
                                for (i in 0 until instruction.arity) {
                                    params.add(stack.removeLast())
                                }
                                params.reverse()
                                val result = function.function(params)
                                stack.addLast(result)
                            }

                            is RuntimeValue.Function -> {
                                val newStackFrame = StackFrame.Call(
                                    parent = stackFrame,
                                    stackStart = stack.size - instruction.arity,
                                    file = function.file,
                                    chunk = function.file.chunks[function.chunkIndex],
                                    stack = stack,
                                    firstEffect = stackFrame.firstEffect,
                                    currentLoop = null,
                                )
                                stack.addAll(function.capturedValues)
                                this.stackFrame = newStackFrame
                            }

                            is RuntimeValue.BadValue -> throw VmError("I tried to call a function, but it has an error: $function")
                            else -> throw InternalVmError("Can't call $function.")
                        }
                    }

                    is Instruction.GetMember -> {
                        when (val obj = stack.removeLast()) {
                            is RuntimeValue.RuntimeObject -> {
                                stack.addLast(obj.values[instruction.index])
                            }

                            is RuntimeValue.BadValue -> throw VmError("I tried to get a member from a bad value: ${obj.debug()}.")
                            else -> throw InternalError("Can't get a member from ${obj.debug()}.")
                        }
                    }

                    is Instruction.IsAssignableTo -> {
                        val typeValue = stack.removeLast()
                        val value = stack.removeLast()

                        if (typeValue is RuntimeValue.BadValue) {
                            throw VmError("I tried to check the type of ${value.debug()}, but the type reference has an error: ${typeValue.debug()}")
                        }

                        val type = (typeValue as RuntimeValue.RuntimeTypeConstraint).valueType

                        stack.addLast(CoreFiles.getBinaryAnswer(value.isAssignableTo(type.toConstraint())))
                    }

                    is Instruction.Jump -> {
                        stackFrame.instruction = instruction.instruction
                    }

                    is Instruction.JumpIfFalse -> {
                        val condition = stack.removeLast()
                        if (condition is RuntimeValue.BadValue) {
                            throw VmError(condition.debug())
                        }
                        if (condition == CoreFiles.getBinaryAnswer(false)) {
                            stackFrame.instruction = instruction.instruction
                        }
                    }

                    is Instruction.StartLoop -> {
                        val newLoop = OpenLoop(
                            stackFrame,
                            continueInstruction = stackFrame.instruction,
                            breakInstruction = instruction.endInstruction,
                            stackEnd = stackFrame.stack.lastIndex,
                            outerLoop = stackFrame.currentLoop
                        )
                        stackFrame.currentLoop = newLoop
                    }

                    is Instruction.ContinueLoop -> {
                        val loop =
                            requireNotNull(this.stackFrame?.currentLoop) { throw InternalVmError("I tried to continue a loop, but I'm not in a loop.") }

                        if (options.runawayLoopThreshold != null) {
                            loop.iterations += 1
                            if (loop.iterations > options.runawayLoopThreshold) {
                                val error = RuntimeValue.RuntimeObject(
                                    codeBundle.getType(CoreFiles.builtin.path, "RunawayLoop"), emptyList()
                                )
                                return RunResult.Caused(error)
                            }
                        }

                        val newCallFrame = loop.stackFrame
                        newCallFrame.instruction = loop.continueInstruction

                        val excessStackItems = newCallFrame.stack.lastIndex - loop.stackEnd
                        for (i in 0 until excessStackItems) {
                            newCallFrame.stack.removeLast()
                        }

                        this.stackFrame = newCallFrame

                    }

                    is Instruction.BreakLoop -> {
                        for (levelI in 0 until instruction.levels) {
                            val loop =
                                requireNotNull(this.stackFrame?.currentLoop) { throw InternalVmError("I tried to break a loop, but I'm not in a loop.") }

                            val result = stackFrame.stack.removeLast()

                            val newCallFrame = loop.stackFrame
                            newCallFrame.instruction = loop.breakInstruction

                            val excessStackItems = newCallFrame.stack.lastIndex - loop.stackEnd
                            for (stackItemI in 0 until excessStackItems) {
                                newCallFrame.stack.removeLast()
                            }
                            newCallFrame.stack.addLast(result)
                            newCallFrame.currentLoop = loop.outerLoop

                            this.stackFrame = newCallFrame
                        }
                    }

                    is Instruction.Cause -> {
                        val signal = stack.removeLast().let {
                            when (it) {
                                is RuntimeValue.RuntimeObject -> it
                                is RuntimeValue.RuntimeTypeConstraint -> {
                                    if (it.valueType is InstanceValueLangType) {
                                        RuntimeValue.RuntimeObject(it.valueType.canonicalType, emptyList())
                                    } else {
                                        throw InternalVmError("Can't cause $it.")
                                    }
                                }

                                is RuntimeValue.BadValue -> throw VmError("I tried to cause a signal, but it has an error: $it")
                                else -> throw InternalVmError("Can't cause $it.")
                            }
                        }

                        stackFrame.pendingSignal = signal
                        val effect = stackFrame.firstEffect
                        if (effect != null) {
                            this.stackFrame = StackFrame.Cause(
                                causeParent = stackFrame,
                                existsInFrame = effect.existsInFrame,
                                stack = ArrayDeque(listOf(signal)),
                                file = effect.file,
                                chunk = effect.chunk,
                                firstEffect = effect.nextEffect,
                                currentLoop = effect.existsInFrame.currentLoop,
                            )
                        } else {
                            return RunResult.Caused(signal)
                        }

                    }

                    is Instruction.RejectSignal -> {
                        require(stackFrame is StackFrame.Cause)
                        val nextEffect = stackFrame.firstEffect
                        val signal = stackFrame.causeParent.pendingSignal!!
                        if (nextEffect != null) {
                            this.stackFrame = StackFrame.Cause(
                                causeParent = stackFrame.causeParent,
                                existsInFrame = nextEffect.existsInFrame,
                                file = nextEffect.file,
                                chunk = nextEffect.chunk,
                                stack = ArrayDeque(listOf(signal)),
                                firstEffect = nextEffect.nextEffect,
                                currentLoop = nextEffect.existsInFrame.currentLoop,
                            )
                        } else {
                            this.stackFrame = stackFrame.causeParent
                            return RunResult.Caused(signal)
                        }
                    }

                    is Instruction.FinishEffect -> {
                        require(stackFrame is StackFrame.Cause)
                        val value = stack.removeLast()

                        val causeParent = stackFrame.causeParent

                        val pendingSignalType =
                            (causeParent.pendingSignal!!.typeDescriptor as CanonicalLangType.SignalCanonicalLangType)
                        if (!value.isAssignableTo(pendingSignalType.result)) {
                            throw VmError("I tried to resolve a ${pendingSignalType.name} signal with a value of ${value.debug()}, but it needs to be ${pendingSignalType.result}")
                        }

                        causeParent.stack.addLast(value)
                        causeParent.pendingSignal = null
                        this.stackFrame = causeParent
                    }

                    is Instruction.Return -> {
                        val value = stack.removeLast()

                        val returnFromFrame = run {
                            var current = stackFrame
                            while (current is StackFrame.Cause) {
                                current = current.existsInFrame
                            }
                            current
                        }

                        when (returnFromFrame) {
                            is StackFrame.Call -> {
                                val functionScopeLength = stack.size - returnFromFrame.stackStart
                                for (i in 0 until functionScopeLength) {
                                    stack.removeLast()
                                }

                                returnFromFrame.parent.stack.addLast(value)
                                this.stackFrame = returnFromFrame.parent
                            }

                            is StackFrame.Main -> return RunResult.Returned(value)
                            is StackFrame.Cause -> throw InternalVmError("Shouldn't have a cause frame here")
                        }
                    }
                }
            }
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

    fun expectReturnValue(): RuntimeValue = (this as Returned).returnValue
    fun expectCausedSignal(): RuntimeValue.RuntimeObject = (this as Caused).signal
}