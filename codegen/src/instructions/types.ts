export interface Instruction {
  params?: {
    [name: string]: InstructionParam;
  };
  description?: string;
}

export interface InstructionParam {
  type: "int" | "uint" | "boolean";
  nullable?: boolean;
}
