export type LangTypeDeclaration = {
  name: string;
} & (
  | { kind?: undefined }
  | { kind: "simple"; type: FieldType }
  | {
      kind: "complex";
      customHashImplementation?: boolean;
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

export function getTypeHasSubtypes(type: FieldType): boolean {
  switch (type.kind) {
    case "canonicalTypeId":
      // technically a reference to another type,
      // but it's indirect so doesn't matter for our purposes
      return false;
    case "langType":
      return true;

    case "list":
    case "optional":
      return getTypeHasSubtypes(type.type);

    case "object":
      return Object.values(type.fields).some(getTypeHasSubtypes);

    case "primitiveEnum":
    case "string":
      return false;
    default:
      return type satisfies never;
  }
}
