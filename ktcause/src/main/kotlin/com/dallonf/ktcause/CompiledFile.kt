package com.dallonf.ktcause

import com.dallonf.ktcause.ast.SourcePosition
import com.dallonf.ktcause.types.*

data class CompiledFile(
    val path: String,
    val types: Map<CanonicalLangTypeId, CanonicalLangType>,
    val chunks: List<InstructionChunk>,
    val exports: Map<String, CompiledExport>,
) {
    fun toFileDescriptor(): Resolver.ExternalFileDescriptor {
        val exportDescriptors = mutableMapOf<String, LangType>()
        for ((exportName, export) in exports) {
            when (export) {
                is CompiledExport.Type -> {
                    val canonicalType = types[export.typeId]
                    requireNotNull(canonicalType) { "$path describes a type (${export.typeId}) but doesn't define it" }
                    exportDescriptors[exportName] = TypeReferenceConstraintLangType(canonicalType)
                }

                is CompiledExport.Error -> {
                    exportDescriptors[exportName] = export.error
                }

                is CompiledExport.Function -> TODO()
                is CompiledExport.Value -> TODO()
            }
        }

        return Resolver.ExternalFileDescriptor(exportDescriptors, types)
    }

    data class InstructionChunk(val constantTable: List<CompiledConstant>, val instructions: List<Instruction>)

    data class MutableInstructionChunk(
        val constantTable: MutableList<CompiledConstant> = mutableListOf(),
        val instructions: MutableList<Instruction> = mutableListOf(),
    ) {
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

        fun writeInstruction(instruction: Instruction): Int {
            instructions.add(instruction)
            return instructions.lastIndex
        }

        fun writeLiteral(constant: CompiledConstant): Int {
            val index = addConstant(constant)
            writeInstruction(Instruction.Literal(index))
            return instructions.lastIndex
        }
    }

    sealed interface CompiledConstant {
        data class StringConst(val value: String) : CompiledConstant
        data class IntegerConst(val value: Long) : CompiledConstant
        data class FloatConst(val value: Double) : CompiledConstant
        data class ErrorConst(val sourcePosition: SourcePosition, val error: ErrorLangType) :
            CompiledConstant
    }

    sealed interface CompiledExport {
        data class Error(val error: ErrorLangType) : CompiledExport
        data class Type(val typeId: CanonicalLangTypeId) : CompiledExport
        data class Function(val chunkIndex: Int, val type: LangType) : CompiledExport
        data class Value(val constant: CompiledConstant, val type: LangType) : CompiledExport
    }
}