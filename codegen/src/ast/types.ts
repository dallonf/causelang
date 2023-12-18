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
  | OptionalFieldType
  | ListFieldType
  | PrimitiveFieldType;

export interface OptionalFieldType {
  kind: "optional";
  type: NodeFieldType;
}

export interface ListFieldType {
  kind: "list";
  type: NodeFieldType;
}

export interface PrimitiveFieldType {
  kind: "primitive";
  type: "string" | "boolean" | "int" | "bigdecimal";
}

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

export function primitive(type: PrimitiveFieldType["type"]): NodeFieldType {
  return {
    kind: "primitive",
    type,
  };
}
