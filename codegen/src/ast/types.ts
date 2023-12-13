export interface CategoryDeclaration {
  name: string;
}

export interface NodeDeclaration {
  name: string;
  category?: string;
  fields: Record<string, NodeFieldType>;
}

export type NodeFieldType =
  | string
  | {
      kind: "optional";
      type: NodeFieldType;
    }
  | {
      kind: "list";
      type: NodeFieldType;
    }
  | {
      kind: "primitive";
      type: "string" | "boolean";
    };

export function listOf(type: NodeFieldType): NodeFieldType {
  return {
    kind: "list",
    type,
  };
}

export function optional(type: NodeFieldType): NodeFieldType {
  return {
    kind: "optional",
    type,
  };
}

export const stringPrimitive: NodeFieldType = {
  kind: "primitive",
  type: "string",
};

export const booleanPrimitive: NodeFieldType = {
  kind: "primitive",
  type: "boolean",
};
