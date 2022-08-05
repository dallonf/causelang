import com.dallonf.ktcause.Instruction
import com.dallonf.ktcause.ResolvedFile
import com.dallonf.ktcause.Resolver
import com.dallonf.ktcause.ast.Breadcrumbs
import com.dallonf.ktcause.types.*

data class CompiledFile(
    val path: String,
    val types: Map<CanonicalLangTypeId, CanonicalLangType>,
    val chunks: List<InstructionChunk>,
    val exports: Map<String, CompiledExport>,
    // TODO: I don't love that we need this for error reporting;
    // ideally a CompiledFile would be a standalone artifact distinct from the AST and resolver
    val resolved: ResolvedFile?
) {
    fun toFileDescriptor(): Resolver.ExternalFileDescriptor {
        val exportDescriptors = mutableMapOf<String, ValueLangType>()
        for ((exportName, export) in exports) {
            when (export) {
                is CompiledExport.Type -> {
                    val canonicalType = types[export.typeId]
                    requireNotNull(canonicalType) { "$path describes a type (${export.typeId}) but doesn't define it" }
                    exportDescriptors[exportName] = canonicalType
                }

                is CompiledExport.Function -> TODO()
                is CompiledExport.Value -> TODO()
            }
        }

        return Resolver.ExternalFileDescriptor(exportDescriptors)
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

        fun writeInstruction(instruction: Instruction) {
            instructions.add(instruction)
        }

        fun writeLiteral(constant: CompiledConstant) {
            val index = addConstant(constant)
            writeInstruction(Instruction.Literal(index))
        }
    }

    sealed interface CompiledConstant {
        data class StringConst(val value: String) : CompiledConstant
        data class IntegerConst(val value: Long) : CompiledConstant
        data class FloatConst(val value: Double) : CompiledConstant
        data class ErrorConst(val filePath: String, val breadcrumbs: Breadcrumbs, val error: ErrorValueLangType) :
            CompiledConstant
    }

    sealed interface CompiledExport {
        data class Type(val typeId: CanonicalLangTypeId) : CompiledExport
        data class Function(val chunkIndex: Int, val type: ValueLangType) : CompiledExport
        data class Value(val constant: CompiledConstant, val type: ValueLangType) : CompiledExport
    }
}