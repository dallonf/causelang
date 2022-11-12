package com.dallonf.ktcause

import com.dallonf.ktcause.ast.*
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

    data class Procedure(
        val identity: ProcedureIdentity,
        val constantTable: List<CompiledConstant>,
        val instructions: List<Instruction>,
        val sourceMap: List<InstructionMapping?>?
    ) {
        sealed class ProcedureIdentity {
            abstract val declaration: NodeInfo

            data class Function(val name: String?, override val declaration: NodeInfo) : ProcedureIdentity()
            data class Effect(val matchesType: ConstraintReference, override val declaration: NodeInfo) : ProcedureIdentity()
        }

        enum class InstructionPhase {
            SETUP, EXECUTE, PLUMBING, CLEANUP,
        }

        data class InstructionMapping(val nodeInfo: NodeInfo, val phase: InstructionPhase = InstructionPhase.EXECUTE) {
            val position: DocumentPosition
                get() = when (phase) {
                    InstructionPhase.CLEANUP -> nodeInfo.position.end
                    else -> nodeInfo.position.start
                }
        }
    }

    data class MutableProcedure(
        val identity: Procedure.ProcedureIdentity,
        val constantTable: MutableList<CompiledConstant> = mutableListOf(),
        val instructions: MutableList<Instruction> = mutableListOf(),
        val sourceMap: MutableList<Procedure.InstructionMapping?> = mutableListOf(),
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

        fun toProcedure() = Procedure(identity, constantTable.toList(), instructions.toList(), sourceMap.toList())

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

        fun writeInstruction(
            instruction: Instruction,
            nodeInfo: NodeInfo?,
            phase: Procedure.InstructionPhase = Procedure.InstructionPhase.EXECUTE
        ) {
            if ((instruction is Instruction.Pop && instruction.number == 0) || (instruction is Instruction.PopEffects && instruction.number == 0) || (instruction is Instruction.PopScope && instruction.values == 0)) {
                // don't write no-op instructions
                return
            }

            instructions.add(instruction)
            sourceMap.add(nodeInfo?.let { Procedure.InstructionMapping(nodeInfo, phase) })
        }

        fun writeLiteral(
            constant: CompiledConstant,
            nodeInfo: NodeInfo?,
            phase: Procedure.InstructionPhase = Procedure.InstructionPhase.EXECUTE
        ) {
            val index = addConstant(constant)
            writeInstruction(Instruction.Literal(index), nodeInfo, phase)
        }

        fun writeJumpPlaceholder(
            nodeInfo: NodeInfo?, phase: Procedure.InstructionPhase = Procedure.InstructionPhase.EXECUTE
        ): JumpPlaceholder {
            instructions.add(Instruction.NoOp)
            sourceMap.add(nodeInfo?.let { Procedure.InstructionMapping(nodeInfo, phase) })
            return JumpPlaceholder(this, instructions.lastIndex) { Instruction.Jump(it) }
        }

        fun writeJumpIfFalsePlaceholder(
            nodeInfo: NodeInfo?, phase: Procedure.InstructionPhase = Procedure.InstructionPhase.EXECUTE
        ): JumpPlaceholder {
            instructions.add(Instruction.NoOp)
            sourceMap.add(nodeInfo?.let { Procedure.InstructionMapping(nodeInfo, phase) })
            return JumpPlaceholder(this, instructions.lastIndex) { Instruction.JumpIfFalse(it) }
        }

        fun writeStartLoopPlaceholder(
            nodeInfo: NodeInfo?, phase: Procedure.InstructionPhase = Procedure.InstructionPhase.EXECUTE
        ): JumpPlaceholder {
            instructions.add(Instruction.NoOp)
            sourceMap.add(nodeInfo?.let { Procedure.InstructionMapping(nodeInfo, phase) })
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