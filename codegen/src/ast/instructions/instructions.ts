import { Instruction } from "./types.ts";

export const instructions: Record<string, Instruction> = {
  NoOp: {},
  Pop: {
    params: {
      number: {
        type: "int",
      },
    },
  },
  Swap: {
    description: "Reverses the top two items on the stack",
  },
  PopScope: {
    description:
      "Pops a number of values while preserving the top of the stack",
    params: {
      values: {
        type: "int",
      },
    },
  },
  RegisterEffect: {
    params: {
      procedureIndex: {
        type: "int",
      },
    },
  },
  PopEffects: {
    params: {
      number: {
        type: "int",
      },
    },
  },
  PushAction: {},
  Literal: {
    params: {
      constant: {
        type: "int",
      },
    },
  },
  Import: {
    params: {
      filePathConstant: {
        type: "int",
      },
      exportNameConstant: {
        type: "int",
      },
    },
  },
  ImportSameFile: {
    params: {
      exportNameConstant: {
        type: "int",
      },
    },
  },
  DefineFunction: {
    params: {
      procedureIndex: {
        type: "int",
      },
      typeConstant: {
        type: "int",
      },
      capturedValues: {
        type: "int",
      },
    },
  },
  ReadLocal: {
    params: {
      index: {
        type: "int",
      },
    },
  },
  WriteLocal: {
    params: {
      index: {
        type: "int",
      },
    },
  },
  ReadLocalThroughEffectScope: {
    params: {
      effectDepth: {
        type: "int",
      },
      index: {
        type: "int",
      },
    },
  },
  WriteLocalThroughEffectScope: {
    params: {
      effectDepth: {
        type: "int",
      },
      index: {
        type: "int",
      },
    },
  },
  Construct: {
    params: {
      arity: {
        type: "int",
      },
    },
  },
  CallFunction: {
    params: {
      arity: {
        type: "int",
      },
    },
  },
  GetMember: {
    params: {
      index: {
        type: "int",
      },
    },
  },
  NameValue: {
    params: {
      nameConstant: {
        type: "int",
      },
      variable: {
        type: "boolean",
        nullable: true,
      },
      localIndex: {
        type: "int",
        nullable: true,
      },
    },
  },
  IsAssignableTo: {},
  Jump: {
    params: {
      instruction: {
        type: "int",
      },
    },
  },
  JumpIfFalse: {
    params: {
      instruction: {
        type: "int",
      },
    },
  },
  StartLoop: {
    params: {
      endInstruction: {
        type: "int",
      },
    },
  },
  ContinueLoop: {},
  BreakLoop: {
    params: {
      levels: {
        type: "int",
      },
    },
  },
  Cause: {},
  RejectSignal: {},
  FinishEffect: {},
  Return: {},
};
