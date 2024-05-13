package com.dallonf.ktcause.gen

import kotlinx.serialization.json.*
import com.dallonf.ktcause.Instruction

object InstructionRustSerialization {
  fun deserializeInstruction(instruction: JsonElement): Instruction {
    if (instruction is JsonObject) {
      instruction["NoOp"]?.let {
        return deserializeNoOpInstruction(it)
      }
      instruction["Pop"]?.let {
        return deserializePopInstruction(it)
      }
      instruction["Swap"]?.let {
        return deserializeSwapInstruction(it)
      }
      instruction["PopScope"]?.let {
        return deserializePopScopeInstruction(it)
      }
      instruction["RegisterEffect"]?.let {
        return deserializeRegisterEffectInstruction(it)
      }
      instruction["PopEffects"]?.let {
        return deserializePopEffectsInstruction(it)
      }
      instruction["PushAction"]?.let {
        return deserializePushActionInstruction(it)
      }
      instruction["Literal"]?.let {
        return deserializeLiteralInstruction(it)
      }
      instruction["Import"]?.let {
        return deserializeImportInstruction(it)
      }
      instruction["ImportSameFile"]?.let {
        return deserializeImportSameFileInstruction(it)
      }
      instruction["DefineFunction"]?.let {
        return deserializeDefineFunctionInstruction(it)
      }
      instruction["ReadLocal"]?.let {
        return deserializeReadLocalInstruction(it)
      }
      instruction["WriteLocal"]?.let {
        return deserializeWriteLocalInstruction(it)
      }
      instruction["ReadLocalThroughEffectScope"]?.let {
        return deserializeReadLocalThroughEffectScopeInstruction(it)
      }
      instruction["WriteLocalThroughEffectScope"]?.let {
        return deserializeWriteLocalThroughEffectScopeInstruction(it)
      }
      instruction["Construct"]?.let {
        return deserializeConstructInstruction(it)
      }
      instruction["CallFunction"]?.let {
        return deserializeCallFunctionInstruction(it)
      }
      instruction["GetMember"]?.let {
        return deserializeGetMemberInstruction(it)
      }
      instruction["NameValue"]?.let {
        return deserializeNameValueInstruction(it)
      }
      instruction["IsAssignableTo"]?.let {
        return deserializeIsAssignableToInstruction(it)
      }
      instruction["Jump"]?.let {
        return deserializeJumpInstruction(it)
      }
      instruction["JumpIfFalse"]?.let {
        return deserializeJumpIfFalseInstruction(it)
      }
      instruction["StartLoop"]?.let {
        return deserializeStartLoopInstruction(it)
      }
      instruction["ContinueLoop"]?.let {
        return deserializeContinueLoopInstruction(it)
      }
      instruction["BreakLoop"]?.let {
        return deserializeBreakLoopInstruction(it)
      }
      instruction["Cause"]?.let {
        return deserializeCauseInstruction(it)
      }
      instruction["RejectSignal"]?.let {
        return deserializeRejectSignalInstruction(it)
      }
      instruction["FinishEffect"]?.let {
        return deserializeFinishEffectInstruction(it)
      }
      instruction["Return"]?.let {
        return deserializeReturnInstruction(it)
      }
    }

    throw AssertionError("Can't parse as an instruction: $instruction")
  }


  fun deserializeNoOpInstruction(instruction: JsonElement): Instruction.NoOp {
    require(instruction is JsonObject)
    return Instruction.NoOp
  }

  fun deserializePopInstruction(instruction: JsonElement): Instruction.Pop {
    require(instruction is JsonObject)
    return Instruction.Pop(
        (instruction["number"] as JsonPrimitive).int,
    )
  }

  fun deserializeSwapInstruction(instruction: JsonElement): Instruction.Swap {
    require(instruction is JsonObject)
    return Instruction.Swap
  }

  fun deserializePopScopeInstruction(instruction: JsonElement): Instruction.PopScope {
    require(instruction is JsonObject)
    return Instruction.PopScope(
        (instruction["values"] as JsonPrimitive).int,
    )
  }

  fun deserializeRegisterEffectInstruction(instruction: JsonElement): Instruction.RegisterEffect {
    require(instruction is JsonObject)
    return Instruction.RegisterEffect(
        (instruction["procedure_index"] as JsonPrimitive).int,
    )
  }

  fun deserializePopEffectsInstruction(instruction: JsonElement): Instruction.PopEffects {
    require(instruction is JsonObject)
    return Instruction.PopEffects(
        (instruction["number"] as JsonPrimitive).int,
    )
  }

  fun deserializePushActionInstruction(instruction: JsonElement): Instruction.PushAction {
    require(instruction is JsonObject)
    return Instruction.PushAction
  }

  fun deserializeLiteralInstruction(instruction: JsonElement): Instruction.Literal {
    require(instruction is JsonObject)
    return Instruction.Literal(
        (instruction["constant"] as JsonPrimitive).int,
    )
  }

  fun deserializeImportInstruction(instruction: JsonElement): Instruction.Import {
    require(instruction is JsonObject)
    return Instruction.Import(
        (instruction["file_path_constant"] as JsonPrimitive).int,
        (instruction["export_name_constant"] as JsonPrimitive).int,
    )
  }

  fun deserializeImportSameFileInstruction(instruction: JsonElement): Instruction.ImportSameFile {
    require(instruction is JsonObject)
    return Instruction.ImportSameFile(
        (instruction["export_name_constant"] as JsonPrimitive).int,
    )
  }

  fun deserializeDefineFunctionInstruction(instruction: JsonElement): Instruction.DefineFunction {
    require(instruction is JsonObject)
    return Instruction.DefineFunction(
        (instruction["procedure_index"] as JsonPrimitive).int,
        (instruction["type_constant"] as JsonPrimitive).int,
        (instruction["captured_values"] as JsonPrimitive).int,
    )
  }

  fun deserializeReadLocalInstruction(instruction: JsonElement): Instruction.ReadLocal {
    require(instruction is JsonObject)
    return Instruction.ReadLocal(
        (instruction["index"] as JsonPrimitive).int,
    )
  }

  fun deserializeWriteLocalInstruction(instruction: JsonElement): Instruction.WriteLocal {
    require(instruction is JsonObject)
    return Instruction.WriteLocal(
        (instruction["index"] as JsonPrimitive).int,
    )
  }

  fun deserializeReadLocalThroughEffectScopeInstruction(instruction: JsonElement): Instruction.ReadLocalThroughEffectScope {
    require(instruction is JsonObject)
    return Instruction.ReadLocalThroughEffectScope(
        (instruction["effect_depth"] as JsonPrimitive).int,
        (instruction["index"] as JsonPrimitive).int,
    )
  }

  fun deserializeWriteLocalThroughEffectScopeInstruction(instruction: JsonElement): Instruction.WriteLocalThroughEffectScope {
    require(instruction is JsonObject)
    return Instruction.WriteLocalThroughEffectScope(
        (instruction["effect_depth"] as JsonPrimitive).int,
        (instruction["index"] as JsonPrimitive).int,
    )
  }

  fun deserializeConstructInstruction(instruction: JsonElement): Instruction.Construct {
    require(instruction is JsonObject)
    return Instruction.Construct(
        (instruction["arity"] as JsonPrimitive).int,
    )
  }

  fun deserializeCallFunctionInstruction(instruction: JsonElement): Instruction.CallFunction {
    require(instruction is JsonObject)
    return Instruction.CallFunction(
        (instruction["arity"] as JsonPrimitive).int,
    )
  }

  fun deserializeGetMemberInstruction(instruction: JsonElement): Instruction.GetMember {
    require(instruction is JsonObject)
    return Instruction.GetMember(
        (instruction["index"] as JsonPrimitive).int,
    )
  }

  fun deserializeNameValueInstruction(instruction: JsonElement): Instruction.NameValue {
    require(instruction is JsonObject)
    return Instruction.NameValue(
        (instruction["name_constant"] as JsonPrimitive).int,
        (instruction["variable"] as JsonPrimitive).boolean,
        (instruction["local_index"] as JsonPrimitive).intOrNull,
    )
  }

  fun deserializeIsAssignableToInstruction(instruction: JsonElement): Instruction.IsAssignableTo {
    require(instruction is JsonObject)
    return Instruction.IsAssignableTo
  }

  fun deserializeJumpInstruction(instruction: JsonElement): Instruction.Jump {
    require(instruction is JsonObject)
    return Instruction.Jump(
        (instruction["instruction"] as JsonPrimitive).int,
    )
  }

  fun deserializeJumpIfFalseInstruction(instruction: JsonElement): Instruction.JumpIfFalse {
    require(instruction is JsonObject)
    return Instruction.JumpIfFalse(
        (instruction["instruction"] as JsonPrimitive).int,
    )
  }

  fun deserializeStartLoopInstruction(instruction: JsonElement): Instruction.StartLoop {
    require(instruction is JsonObject)
    return Instruction.StartLoop(
        (instruction["end_instruction"] as JsonPrimitive).int,
    )
  }

  fun deserializeContinueLoopInstruction(instruction: JsonElement): Instruction.ContinueLoop {
    require(instruction is JsonObject)
    return Instruction.ContinueLoop
  }

  fun deserializeBreakLoopInstruction(instruction: JsonElement): Instruction.BreakLoop {
    require(instruction is JsonObject)
    return Instruction.BreakLoop(
        (instruction["levels"] as JsonPrimitive).int,
    )
  }

  fun deserializeCauseInstruction(instruction: JsonElement): Instruction.Cause {
    require(instruction is JsonObject)
    return Instruction.Cause
  }

  fun deserializeRejectSignalInstruction(instruction: JsonElement): Instruction.RejectSignal {
    require(instruction is JsonObject)
    return Instruction.RejectSignal
  }

  fun deserializeFinishEffectInstruction(instruction: JsonElement): Instruction.FinishEffect {
    require(instruction is JsonObject)
    return Instruction.FinishEffect
  }

  fun deserializeReturnInstruction(instruction: JsonElement): Instruction.Return {
    require(instruction is JsonObject)
    return Instruction.Return
  }
}