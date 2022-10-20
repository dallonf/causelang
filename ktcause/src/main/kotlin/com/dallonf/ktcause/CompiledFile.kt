package com.dallonf.ktcause

import com.dallonf.ktcause.ast.SourcePosition
import com.dallonf.ktcause.types.*
import org.apache.commons.numbers.fraction.BigFraction

data class CompiledFile(
    val path: String,
    val types: Map<CanonicalLangTypeId, CanonicalLangType>,
    val procedures: List<Procedure>,
    val exports: Map<String, CompiledExport>,
    val debugCtx: Debug.DebugContext? = null
) {
    fun toFileDescriptor(): Resolver.ExternalFileDescriptor {
        val exportDescriptors = mutableMapOf<String, ValueLangType>()
        for ((exportName, export) in exports) {
            when (export) {
                is CompiledExport.Constraint -> {
                    exportDescriptors[exportName] = export.constraint.asConstraintValue()
                }

                is CompiledExport.Error -> {
                    exportDescriptors[exportName] = export.error
                }

                is CompiledExport.Function -> {
                    exportDescriptors[exportName] = export.type
                }

                is CompiledExport.NativeFunction -> {
                    exportDescriptors[exportName] = export.type
                }

                is CompiledExport.Value -> {
                    // TODO
                }
            }
        }

        return Resolver.ExternalFileDescriptor(exportDescriptors, types)
    }

    data class Procedure(val constantTable: List<CompiledConstant>, val instructions: List<Instruction>)

    data class MutableProcedure(
        val constantTable: MutableList<CompiledConstant> = mutableListOf(),
        val instructions: MutableList<Instruction> = mutableListOf(),
    ) {

        data class JumpPlaceholder(
            private val procedure: MutableProcedure,
            private val index: Int,
            private val makeInstruction: (Int) -> Instruction
        ) {
            fun fill(jumpTo: Int? = null) {
                procedure.instructions[index] = makeInstruction(jumpTo ?: procedure.instructions.size)
            }
        }

        fun toProcedure() = Procedure(constantTable.toList(), instructions.toList())

        fun addConstant(constant: CompiledConstant): Int {
            val existingIndex = constantTable.asSequence().withIndex()
                .firstNotNullOfOrNull { (i, existing) -> if (existing == constant) i else null }

            return if (existingIndex != null) {
                existingIndex
            } else {
                constantTable.add(constant)
                constantTable.lastIndex
            }
        }

        fun addConstant(constant: String): Int = addConstant(CompiledConstant.StringConst(constant))

        fun writeInstruction(instruction: Instruction) {
            instructions.add(instruction)
        }

        fun writeLiteral(constant: CompiledConstant) {
            val index = addConstant(constant)
            writeInstruction(Instruction.Literal(index))
        }

        fun writeJumpPlaceholder(): JumpPlaceholder {
            instructions.add(Instruction.NoOp)
            return JumpPlaceholder(this, instructions.lastIndex) { Instruction.Jump(it) }
        }

        fun writeJumpIfFalsePlaceholder(): JumpPlaceholder {
            instructions.add(Instruction.NoOp)
            return JumpPlaceholder(this, instructions.lastIndex) { Instruction.JumpIfFalse(it) }
        }

        fun writeStartLoopPlaceholder(): JumpPlaceholder {
            instructions.add(Instruction.NoOp)
            return JumpPlaceholder(this, instructions.lastIndex) { Instruction.StartLoop(it) }
        }
    }

    sealed interface CompiledConstant {
        data class StringConst(val value: String) : CompiledConstant
        data class NumberConst(val value: BigFraction) : CompiledConstant
        data class ErrorConst(val sourcePosition: SourcePosition, val error: ErrorLangType) : CompiledConstant

        data class TypeConst(val type: ResolvedValueLangType) : CompiledConstant
    }

    sealed interface CompiledExport {
        data class Error(val error: ErrorLangType) : CompiledExport
        data class Constraint(val constraint: ConstraintReference) : CompiledExport
        data class Function(val procedureIndex: Int, val type: ValueLangType) : CompiledExport
        data class NativeFunction internal constructor(
            val type: FunctionValueLangType,
            val function: (List<RuntimeValue>) -> RuntimeValue,
        ) : CompiledExport

        data class Value(val constant: CompiledConstant?, val type: ValueLangType) : CompiledExport
    }
}