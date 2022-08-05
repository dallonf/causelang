package com.dallonf.ktcause

sealed interface Instruction {
    object Pop : Instruction
    object PushAction : Instruction
    data class Literal(val constant: Int) : Instruction
    data class Import(val filePathConstant: Int, val exportNameConstant: Int) : Instruction
    data class ReadLocal(val index: Int) : Instruction
    data class Construct(val arity: Int) : Instruction
    data class CallFunction(val arity: Int) : Instruction
    object Cause : Instruction
    object Return : Instruction
}