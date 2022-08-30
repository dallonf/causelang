package com.dallonf.ktcause

import com.dallonf.ktcause.ast.SourcePosition
import com.dallonf.ktcause.types.*
import java.math.BigDecimal

data class CompiledFile(
    val path: String,
    val types: Map<CanonicalLangTypeId, CanonicalLangType>,
    val chunks: List<InstructionChunk>,
    val exports: Map<String, CompiledExport>,
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

    data class InstructionChunk(val constantTable: List<CompiledConstant>, val instructions: List<Instruction>)

    data class MutableInstructionChunk(
        val constantTable: MutableList<CompiledConstant> = mutableListOf(),
        val instructions: MutableList<Instruction> = mutableListOf(),
    ) {

        data class JumpPlaceholder(
            private val chunk: MutableInstructionChunk,
            private val index: Int,
            private val makeInstruction: (Int) -> Instruction
        ) {
            fun fill(jumpTo: Int? = null) {
                chunk.instructions[index] = makeInstruction(jumpTo ?: chunk.instructions.size)
            }
        }

        fun toInstructionChunk() = InstructionChunk(constantTable.toList(), instructions.toList())

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
    }

    sealed interface CompiledConstant {
        data class StringConst(val value: String) : CompiledConstant
        data class NumberConst(val value: BigDecimal) : CompiledConstant
        data class WholeNumberConst(val value: Long) : CompiledConstant
        data class ErrorConst(val sourcePosition: SourcePosition, val error: ErrorLangType) : CompiledConstant

        data class TypeConst(val type: ValueLangType) : CompiledConstant
    }

    sealed interface CompiledExport {
        data class Error(val error: ErrorLangType) : CompiledExport
        data class Constraint(val constraint: ConstraintReference) : CompiledExport
        data class Function(val chunkIndex: Int, val type: ValueLangType) : CompiledExport
        data class NativeFunction internal constructor(
            val type: FunctionValueLangType,
            val function: (List<RuntimeValue>) -> RuntimeValue,
        ) : CompiledExport

        data class Value(val constant: CompiledConstant?, val type: ValueLangType) : CompiledExport
    }
}