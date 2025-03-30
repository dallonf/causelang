export type LangTypeDeclaration = {
  name: string;
} & (
  | { kind?: undefined }
  | { kind: "simple"; type: FieldType }
  | {
      kind: "complex";
      fields: Record<string, FieldType>;
    }
);

export type FieldType =
  | {
      kind: "langType";
    }
  | {
      kind: "canonicalTypeId";
    }
  | {
      kind: "string";
    }
  | {
      kind: "optional";
      type: FieldType;
    }
  | {
      kind: "list";
      type: FieldType;
    }
  | {
      kind: "object";
      name: string;
      fields: Record<string, FieldType>;
    }
  | {
      kind: "primitiveEnum";
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
