package com.dallonf.ktcause

import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.Debug.debugMini
import com.dallonf.ktcause.ast.SourcePosition
import com.dallonf.ktcause.types.*


class LangVm(val codeBundle: CodeBundle, val options: Options = Options()) {

    val hasTypeErrors by lazy { codeBundle.compileErrors.isNotEmpty() }

    data class Options(
        val runawayLoopThreshold: Long? = 5_000,
        val debugInstructionLevelExecution: Boolean = false,
        val trackValueNames: Boolean = true,
    )

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
        set(value) {
            field = value
            if (options.debugInstructionLevelExecution && value != null) {
                val procedureInfo = value.info()
                println("=> proc: $procedureInfo:${value.procedure.identity.declaration.position.start}")
            }
        }


    private data class RuntimeEffect(
        val file: CompiledFile,
        val procedure: CompiledFile.Procedure,
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

    private class ValueStack(withNames: Boolean) {
        data class ValueName(val name: String, val original: Boolean, val variable: Boolean) {
            fun copied(): ValueName = ValueName(name, original = false, variable = false)
        }


        val values: ArrayDeque<RuntimeValue> = ArrayDeque()
        val names: ArrayDeque<ValueName?>? = if (withNames) ArrayDeque() else null

        val size: Int
            get() = values.size

        val lastIndex: Int
            get() = values.lastIndex

        fun push(value: RuntimeValue, name: ValueName? = null) {
            values.addLast(value)
            names?.addLast(name)
        }

        fun pop(): RuntimeValue {
            names?.removeLast()
            return values.removeLast()
        }

        fun popWithName(): Pair<RuntimeValue, ValueName?> = Pair(values.removeLast(), names?.removeLast())

        fun peek(): RuntimeValue {
            return values.last()
        }

        fun peekWithName(): Pair<RuntimeValue, ValueName?> = Pair(peek(), peekName())

        fun atIndex(index: Int): RuntimeValue = values[index]

        fun atIndexWithName(index: Int): Pair<RuntimeValue, ValueName?> = Pair(atIndex(index), nameAtIndex(index))

        fun setAtIndex(index: Int, value: RuntimeValue) {
            values[index] = value
        }

        fun nameAtIndex(index: Int): ValueName? = names?.get(index)

        fun peekName(): ValueName? {
            return names?.last()
        }

        fun setName(name: ValueName) {
            names?.set(names.lastIndex, name)
        }

        fun setNameAt(index: Int, name: ValueName) {
            names?.set(index, name)
        }

        fun all(): List<Pair<RuntimeValue, ValueName?>> {
            return if (names != null) {
                values.zip(names).map { (value, name) -> Pair(value, name) }
            } else {
                values.map { Pair(it, null) }
            }
        }
    }

    private sealed class StackFrame(
        val file: CompiledFile,
        val procedure: CompiledFile.Procedure,
        val stack: ValueStack,
        var firstEffect: RuntimeEffect?,
        var currentLoop: OpenLoop?
    ) {
        fun info(): String {
            val filePath = file.path
            val procedureInfo = when (val identity = procedure.identity) {
                is CompiledFile.Procedure.ProcedureIdentity.Function -> {
                    val functionName = (identity.name?.let { "function $it" } ?: "fn") + "()"
                    "$functionName at $filePath"
                }

                is CompiledFile.Procedure.ProcedureIdentity.Effect -> {
                    val signalName = run {
                        val type = identity.matchesType as? ConstraintReference.ResolvedConstraint
                        val canonicalType = type?.let { it.valueType as? InstanceValueLangType }
                        val signalId = canonicalType?.canonicalType?.id
                        signalId?.name
                    }
                    val patternDescription = signalName?.let { " for $it" }
                    "effect${patternDescription ?: ""} at $filePath"
                }
            }
            return procedureInfo
        }

        var instruction: Int = 0
        var pendingSignal: RuntimeValue.RuntimeObject? = null

        abstract val executionParent: StackFrame?

        abstract val stackStart: Int

        class Main(
            file: CompiledFile,
            procedure: CompiledFile.Procedure,
            stack: ValueStack,
            firstEffect: RuntimeEffect?,
            currentLoop: OpenLoop?
        ) : StackFrame(file, procedure, stack, firstEffect, currentLoop) {
            override val executionParent
                get() = null

            override val stackStart
                get() = 0
        }

        class Call(
            val parent: StackFrame,
            override var stackStart: Int,
            file: CompiledFile,
            procedure: CompiledFile.Procedure,
            stack: ValueStack,
            firstEffect: RuntimeEffect?,
            currentLoop: OpenLoop?,
        ) : StackFrame(file, procedure, stack, firstEffect, currentLoop) {
            override val executionParent
                get() = parent
        }

        class Cause(
            val causeParent: StackFrame,
            val existsInFrame: StackFrame,
            file: CompiledFile,
            procedure: CompiledFile.Procedure,
            stack: ValueStack,

            firstEffect: RuntimeEffect?,
            currentLoop: OpenLoop?,
        ) : StackFrame(file, procedure, stack, firstEffect, currentLoop) {
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

        val stack = ValueStack(withNames = options.trackValueNames)
        stack.push(
            RuntimeValue.Function(
                functionName, file, function.procedureIndex, function.type, capturedValues = emptyList()
            )
        )
        stack.setName(ValueStack.ValueName(functionName, original = false, variable = false))
        for (param in parameters) {
            stack.push(param)
        }

        stackFrame = StackFrame.Main(
            file,
            file.procedures[function.procedureIndex],
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
            stackFrame.stack.push(value)
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

                val instruction = requireNotNull(stackFrame.procedure.instructions.getOrNull(stackFrame.instruction)) {
                    throw InternalVmError(
                        "I've gotten to instruction #${stackFrame.instruction}, but there are no more instructions to read!"
                    )
                }
                // note: This means the instruction pointer will always be one ahead of the actual instruction!
                stackFrame.instruction += 1

                fun getConstant(id: Int): CompiledFile.CompiledConstant {
                    return requireNotNull(stackFrame.procedure.constantTable.getOrNull(id)) {
                        throw InternalVmError("I'm looking for a constant with the ID of $id, but I can't find it.")
                    }
                }

                if (options.debugInstructionLevelExecution) {
                    val instructionNumber = stackFrame.instruction - 1
                    if (instruction !is Instruction.NameValue) {
                        // don't debug stack if we're about to name a value, that just makes things confusing
                        val stackValues = stack.all()
                        val currentStack = stackValues.drop(stackFrame.stackStart)

                        val debugStack = currentStack.reversed().joinToString(", ") { (value, name) ->
                            debugStackValue(value, name)
                        }
                        println("stack (${stackFrame.stackStart}, rtl): $debugStack")
                    }
                    val sourceMapping = stackFrame.procedure.sourceMap?.let { it[instructionNumber] }?.position
                    val sourceMappingStr = sourceMapping?.let { ", l#${it}" } ?: ""
                    println("#${instructionNumber}${sourceMappingStr}: $instruction")
                }

                when (instruction) {
                    is Instruction.NoOp -> {}

                    is Instruction.Pop -> {
                        for (i in 0 until instruction.number) {
                            stack.pop()
                        }
                    }

                    is Instruction.Swap -> {
                        val a = stack.popWithName()
                        val b = stack.popWithName()
                        stack.push(a.first, a.second)
                        stack.push(b.first, b.second)
                    }

                    is Instruction.PopScope -> {
                        val name = stack.peekName()
                        val result = stack.pop()
                        for (i in 0 until instruction.values) {
                            stack.pop()
                        }
                        stack.push(result)
                        name?.let { stack.setName(it.copied()) }
                    }

                    is Instruction.RegisterEffect -> {
                        val procedure = stackFrame.file.procedures[instruction.procedureIndex]
                        stackFrame.firstEffect = RuntimeEffect(
                            stackFrame.file, procedure, existsInFrame = stackFrame, nextEffect = stackFrame.firstEffect
                        )
                    }

                    is Instruction.PopEffects -> {
                        for (i in 0 until instruction.number) {
                            stackFrame.firstEffect = stackFrame.firstEffect!!.nextEffect
                        }
                    }

                    is Instruction.PushAction -> stack.push(RuntimeValue.Action)

                    is Instruction.Literal -> {
                        val newValue = when (val constant = getConstant(instruction.constant)) {
                            is CompiledFile.CompiledConstant.StringConst -> RuntimeValue.Text(constant.value)
                            is CompiledFile.CompiledConstant.NumberConst -> RuntimeValue.Number(constant.value)
                            is CompiledFile.CompiledConstant.TypeConst -> RuntimeValue.RuntimeTypeConstraint(constant.type)
                            is CompiledFile.CompiledConstant.ErrorConst -> RuntimeValue.BadValue(
                                constant.sourcePosition, constant.error
                            )
                        }
                        stack.push(newValue)
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
                        stack.push(value)
                        stack.setName(ValueStack.ValueName(exportName, original = false, variable = false))
                    }

                    is Instruction.ImportSameFile -> {
                        val exportName =
                            requireNotNull(getConstant(instruction.exportNameConstant) as? CompiledFile.CompiledConstant.StringConst) {
                                throw InternalVmError("Can't get exportName")
                            }.value
                        val value = RuntimeValue.fromExport(stackFrame.file, exportName)
                        stack.push(value)
                        stack.setName(ValueStack.ValueName(exportName, original = false, variable = false))
                    }

                    is Instruction.DefineFunction -> {
                        val capturedValues = run {
                            val list = mutableListOf<Pair<RuntimeValue, String?>>()
                            for (i in 0 until instruction.capturedValues) {
                                val (value, name) = stack.popWithName()
                                list.add(Pair(value, name?.name))
                            }
                            list.reverse()
                            list.toList()
                        }

                        val functionType =
                            (getConstant(instruction.typeConstant) as CompiledFile.CompiledConstant.TypeConst).type as FunctionValueLangType

                        val functionValue = RuntimeValue.Function(
                            functionType.name,
                            file = stackFrame.file,
                            procedureIndex = instruction.procedureIndex,
                            type = functionType,
                            capturedValues = capturedValues
                        )
                        stack.push(functionValue, functionType.name?.let {
                            ValueStack.ValueName(
                                it, original = false, variable = false
                            )
                        })
                    }

                    is Instruction.ReadLocal -> {
                        val index = stackFrame.stackStart + instruction.index

                        val name = stack.nameAtIndex(index)
                        val value = stack.atIndex(index)
                        stack.push(value, name?.copied())
                    }

                    is Instruction.WriteLocal -> {
                        val index = stackFrame.stackStart + instruction.index
                        val value = stack.pop()
                        stack.setAtIndex(index, value)
                    }

                    is Instruction.ReadLocalThroughEffectScope -> {
                        var parentFrame = stackFrame
                        for (i in 0 until instruction.effectDepth) {
                            require(parentFrame is StackFrame.Cause)
                            parentFrame = parentFrame.existsInFrame
                        }
                        val index = parentFrame.stackStart + instruction.index
                        val name = parentFrame.stack.nameAtIndex(index)
                        val value = parentFrame.stack.atIndex(index)
                        stack.push(value, name?.copied())
                    }

                    is Instruction.WriteLocalThroughEffectScope -> {
                        var parentFrame = stackFrame
                        for (i in 0 until instruction.effectDepth) {
                            require(parentFrame is StackFrame.Cause)
                            parentFrame = parentFrame.existsInFrame
                        }
                        val index = parentFrame.stackStart + instruction.index
                        val value = stack.pop()
                        parentFrame.stack.setAtIndex(index, value)
                    }

                    is Instruction.Construct -> {
                        val params = mutableListOf<RuntimeValue>()
                        for (i in 0 until instruction.arity) {
                            params.add(stack.pop())
                        }
                        params.reverse()

                        val constructorConstraint = stack.pop()
                        val constructorType = constructorConstraint.let { stackValue ->
                            (stackValue as? RuntimeValue.RuntimeTypeConstraint)
                                ?: throw InternalVmError("Tried to construct a value (instead of a constraint): $stackValue.")
                        }.valueType

                        when (constructorType) {
                            is InstanceValueLangType -> {
                                if (constructorType.canonicalType.isUnique()) {
                                    stack.push(constructorConstraint)
                                } else {
                                    val obj = RuntimeValue.RuntimeObject(constructorType.canonicalType, params)
                                    stack.push(obj)
                                }
                            }

                            is StopgapDictionaryLangType -> {
                                stack.push(RuntimeValue.StopgapDictionary())
                            }

                            is StopgapListLangType -> {
                                stack.push(RuntimeValue.StopgapList())
                            }

                            else -> {
                                throw InternalVmError("Tried to construct a $constructorType.")
                            }
                        }
                    }

                    is Instruction.CallFunction -> {
                        when (val function = stack.atIndex(stack.lastIndex - instruction.arity)) {
                            is RuntimeValue.NativeFunction -> {
                                val params = mutableListOf<RuntimeValue>()
                                for (i in 0 until instruction.arity) {
                                    params.add(stack.pop())
                                }
                                params.reverse()
                                stack.pop() // pop the function off the stack

                                if (hasTypeErrors) {
                                    val functionParams = function.type.params
                                    val sourcePosition = stackFrame.procedure.sourceMap?.get(
                                        stackFrame.instruction - 1
                                    )?.let {
                                        SourcePosition.Source(
                                            stackFrame.file.path,
                                            it.nodeInfo.breadcrumbs,
                                            it.nodeInfo.position,
                                        )
                                    }
                                    val error = if (params.size > functionParams.size) {
                                        ErrorLangType.ExcessParameter(functionParams.size)
                                    } else if (params.size < functionParams.size) {
                                        ErrorLangType.MissingParameters(functionParams.takeLast(functionParams.size - params.size)
                                            .map { it.name })
                                    } else {
                                        functionParams.zip(params).firstNotNullOfOrNull { (paramType, paramValue) ->
                                            if (paramValue.isAssignableTo(paramType.valueConstraint)) {
                                                null
                                            } else {
                                                when (val type = paramValue.typeOf()) {
                                                    is ValueLangType.Pending -> ErrorLangType.NeverResolved
                                                    is ErrorLangType -> ErrorLangType.ProxyError.from(
                                                        type, sourcePosition
                                                    )

                                                    is ResolvedValueLangType -> ErrorLangType.MismatchedType(
                                                        expected = paramType.valueConstraint.asConstraintValue() as ConstraintValueLangType,
                                                        actual = type
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (error != null) {
                                        throw VmError("I couldn't call a builtin function: " + error.debug())
                                    }
                                }

                                val result = function.function(params)
                                stack.push(result)
                            }

                            is RuntimeValue.Function -> {
                                val newStackFrame = StackFrame.Call(
                                    parent = stackFrame,
                                    stackStart = stack.size - instruction.arity - 1,
                                    file = function.file,
                                    procedure = function.file.procedures[function.procedureIndex],
                                    stack = stack,
                                    firstEffect = stackFrame.firstEffect,
                                    currentLoop = null,
                                )
                                for (captured in function.capturedValues) {
                                    stack.push(captured.first, captured.second?.let {
                                        ValueStack.ValueName(
                                            it, original = true, variable = false
                                        )
                                    })
                                }
                                this.stackFrame = newStackFrame
                            }

                            is RuntimeValue.BadValue -> throw VmError("I tried to call a function, but it has an error: $function")
                            else -> throw InternalVmError("Can't call $function.")
                        }
                    }

                    is Instruction.GetMember -> {
                        val (obj, objName) = stack.popWithName()
                        when (obj) {
                            is RuntimeValue.RuntimeObject -> {
                                val fields = when (obj.typeDescriptor) {
                                    is CanonicalLangType.ObjectCanonicalLangType -> obj.typeDescriptor.fields
                                    is CanonicalLangType.SignalCanonicalLangType -> obj.typeDescriptor.fields
                                }
                                val fieldName = fields[instruction.index].name
                                val name = if (objName != null) {
                                    "${objName.name}.${fieldName}"
                                } else {
                                    fieldName
                                }
                                stack.push(
                                    obj.values[instruction.index],
                                    ValueStack.ValueName(name, original = false, variable = false)
                                )
                            }

                            is RuntimeValue.BadValue -> throw VmError("I tried to get a member from a bad value: ${obj.debug()}.")
                            else -> throw InternalError("Can't get a member from ${obj.debug()}.")
                        }
                    }

                    is Instruction.NameValue -> {
                        if (!options.trackValueNames) return@iteration
                        val nameString =
                            getConstant(instruction.nameConstant) as CompiledFile.CompiledConstant.StringConst
                        val name = ValueStack.ValueName(
                            nameString.value, original = true, variable = instruction.variable
                        )
                        if (instruction.localIndex != null) {
                            stack.setNameAt(instruction.localIndex + stackFrame.stackStart, name)
                        } else {
                            stack.setName(name)
                        }
                    }

                    is Instruction.IsAssignableTo -> {
                        val typeValue = stack.pop()
                        val value = stack.pop()

                        if (typeValue is RuntimeValue.BadValue) {
                            throw VmError("I tried to check the type of ${value.debug()}, but the type reference has an error: ${typeValue.debug()}")
                        }

                        val type = (typeValue as RuntimeValue.RuntimeTypeConstraint).valueType

                        stack.push(CoreFiles.getBinaryAnswer(value.isAssignableTo(type.toConstraint())))
                    }

                    is Instruction.Jump -> {
                        stackFrame.instruction = instruction.instruction
                    }

                    is Instruction.JumpIfFalse -> {
                        val condition = stack.pop()
                        if (condition is RuntimeValue.BadValue) {
                            throw VmError(condition.debug())
                        }
                        if (condition == CoreFiles.getBinaryAnswer(false)) {
                            stackFrame.instruction = instruction.instruction
                        }
                        Unit
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
                            newCallFrame.stack.pop()
                        }

                        this.stackFrame = newCallFrame

                    }

                    is Instruction.BreakLoop -> {
                        for (levelI in 0 until instruction.levels) {
                            val loop =
                                requireNotNull(this.stackFrame?.currentLoop) { throw InternalVmError("I tried to break a loop, but I'm not in a loop.") }

                            val (result, name) = stackFrame.stack.popWithName()

                            val newCallFrame = loop.stackFrame
                            newCallFrame.instruction = loop.breakInstruction

                            val excessStackItems = newCallFrame.stack.lastIndex - loop.stackEnd
                            for (stackItemI in 0 until excessStackItems) {
                                newCallFrame.stack.pop()
                            }
                            newCallFrame.stack.push(result, name?.copied())
                            newCallFrame.currentLoop = loop.outerLoop

                            this.stackFrame = newCallFrame
                        }
                    }

                    is Instruction.Cause -> {
                        val signal = stack.pop().let {
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
                        val newStack = ValueStack(options.trackValueNames)
                        newStack.push(signal)
                        if (effect != null) {
                            this.stackFrame = StackFrame.Cause(
                                causeParent = stackFrame,
                                existsInFrame = effect.existsInFrame,
                                stack = newStack,
                                file = effect.file,
                                procedure = effect.procedure,
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
                        val newStack = ValueStack(options.trackValueNames)
                        newStack.push(signal)
                        if (nextEffect != null) {
                            this.stackFrame = StackFrame.Cause(
                                causeParent = stackFrame.causeParent,
                                existsInFrame = nextEffect.existsInFrame,
                                file = nextEffect.file,
                                procedure = nextEffect.procedure,
                                stack = newStack,
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
                        val (value, name) = stack.popWithName()

                        val causeParent = stackFrame.causeParent

                        val valuesLeftOnStack = stackFrame.stack.size - stackFrame.stackStart
                        assert(valuesLeftOnStack == 1) { throw InternalVmError("Expected 1 value (the original signal) on the stack after effect, but there were $valuesLeftOnStack.") }

                        val pendingSignalType =
                            (causeParent.pendingSignal!!.typeDescriptor as CanonicalLangType.SignalCanonicalLangType)
                        if (!value.isAssignableTo(pendingSignalType.result)) {
                            throw VmError("I tried to resolve a ${pendingSignalType.name} signal with a value of ${value.debug()}, but it needs to be ${pendingSignalType.result}")
                        }

                        causeParent.stack.push(value, name?.copied())
                        causeParent.pendingSignal = null
                        this.stackFrame = causeParent
                    }

                    is Instruction.Return -> {
                        val (value, name) = stack.popWithName()

                        val returnFromFrame = run {
                            var current = stackFrame
                            while (current is StackFrame.Cause) {
                                current = current.existsInFrame
                            }
                            current
                        }

                        when (returnFromFrame) {
                            is StackFrame.Call -> {
                                val functionScopeLength = (returnFromFrame.stack.size - returnFromFrame.stackStart)
                                for (i in 0 until functionScopeLength) {
                                    returnFromFrame.stack.pop()
                                }

                                returnFromFrame.parent.stack.push(value, name?.copied())
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

    private fun debugStackValue(value: RuntimeValue, name: ValueStack.ValueName?): String {
        val result = run {
            val valueString = value.debugMini()
            if (name != null) {
                if (name.original && name.variable) {
                    "let variable ${name.name} = $valueString"
                } else if (name.original) {
                    "let ${name.name} = $valueString"
                } else {
                    "${name.name} = $valueString"
                }
            } else {
                valueString
            }
        }
        return result
    }

    fun getExecutionTrace(): String {
        val builder = StringBuilder()
        builder.appendLine("Traceback (most recent call last):")
        val calls = mutableListOf<StackFrame>()
        var frame = stackFrame
        while (frame != null) {
            calls.add(frame)
            frame = frame.executionParent
        }
        for (call in calls.reversed()) {
            val instructionMapping = call.procedure.sourceMap?.let {
                it[call.instruction]
            }
            val line = instructionMapping?.position?.line
            builder.append("\t${call.info()}")
            if (line != null) {
                builder.append(" at line $line")
            }
            builder.appendLine()
        }
        return builder.toString()
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