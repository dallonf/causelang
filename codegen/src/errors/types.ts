export interface ErrorDeclaration {
  name: string;
  fields?: Record<string, FieldType>;
}

export type FieldType =
  | string
  | {
      kind: "optional";
      type: FieldType;
    }
  | {
      kind: "list";
      type: FieldType;
    }
  | {
      kind: "diverged";
      kotlin: string;
      rust: string;
    };

export function listOf(input: FieldType): FieldType {
  return {
    kind: "list",
    type: input,
  };
}

export function optional(input: FieldType): FieldType {
  return {
    kind: "optional",
    type: input,
  };
}

export function diverged(input: { kotlin: string; rust: string }): FieldType {
  return {
    kind: "diverged",
    kotlin: input.kotlin,
    rust: input.rust,
  };
}
