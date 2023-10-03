import { Instruction } from "./types.ts";

export const instructions: Record<string, Instruction> = {
  NoOp: {},
  Pop: {
    params: {
      number: {
        type: "uint",
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
        type: "uint",
      },
    },
  },
  RegisterEffect: {
    params: {
      procedureIndex: {
        type: "uint",
      },
    },
  },
  PopEffects: {
    params: {
      number: {
        type: "uint",
      },
    },
  },
  PushAction: {},
  Literal: {
    params: {
      constant: {
        type: "uint",
      },
    },
  },
  Import: {
    params: {
      filePathConstant: {
        type: "uint",
      },
      exportNameConstant: {
        type: "uint",
      },
    },
  },
  ImportSameFile: {
    params: {
      exportNameConstant: {
        type: "uint",
      },
    },
  },
  DefineFunction: {
    params: {
      procedureIndex: {
        type: "uint",
      },
      typeConstant: {
        type: "uint",
      },
      capturedValues: {
        type: "uint",
      },
    },
  },
  ReadLocal: {
    params: {
      index: {
        type: "uint",
      },
    },
  },
  WriteLocal: {
    params: {
      index: {
        type: "uint",
      },
    },
  },
  ReadLocalThroughEffectScope: {
    params: {
      effectDepth: {
        type: "uint",
      },
      index: {
        type: "uint",
      },
    },
  },
  WriteLocalThroughEffectScope: {
    params: {
      effectDepth: {
        type: "uint",
      },
      index: {
        type: "uint",
      },
    },
  },
  Construct: {
    params: {
      arity: {
        type: "uint",
      },
    },
  },
  CallFunction: {
    params: {
      arity: {
        type: "uint",
      },
    },
  },
  GetMember: {
    params: {
      index: {
        type: "uint",
      },
    },
  },
  NameValue: {
    params: {
      nameConstant: {
        type: "uint",
      },
      variable: {
        type: "boolean",
      },
      localIndex: {
        type: "uint",
        nullable: true,
      },
    },
  },
  IsAssignableTo: {},
  Jump: {
    params: {
      instruction: {
        type: "uint",
      },
    },
  },
  JumpIfFalse: {
    params: {
      instruction: {
        type: "uint",
      },
    },
  },
  StartLoop: {
    params: {
      endInstruction: {
        type: "uint",
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
