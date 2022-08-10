package com.dallonf.ktcause

sealed interface Instruction {
    object NoOp : Instruction
    data class Pop(val number: Int = 1) : Instruction

    /**
     * Pops a number of values while preserving the top of the stack
     */
    data class PopScope(val values: Int) : Instruction

    data class RegisterEffect(val chunk: Int) : Instruction

    data class PopEffects(val number: Int) : Instruction

    object PushAction : Instruction
    data class Literal(val constant: Int) : Instruction
    data class Import(val filePathConstant: Int, val exportNameConstant: Int) : Instruction

    data class ImportSameFile(val exportNameConstant: Int) : Instruction
    data class ReadLocal(val index: Int) : Instruction
    data class Construct(val arity: Int) : Instruction
    data class CallFunction(val arity: Int) : Instruction

    object IsAssignableTo : Instruction

    data class Jump(val instruction: Int) : Instruction
    data class JumpIfFalse(val instruction: Int) : Instruction

    object Cause : Instruction
    object RejectSignal : Instruction
    object FinishEffect : Instruction
    object Return : Instruction
}