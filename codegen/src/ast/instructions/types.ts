export interface Instruction {
  params?: {
    [name: string]: InstructionParam;
  };
  description?: string;
}

export interface InstructionParam {
  type: "int" | "boolean";
  nullable?: boolean;
}
