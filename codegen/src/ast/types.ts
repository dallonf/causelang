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
      kind: "list";
      type: string;
    }
  | {
      kind: "primitive";
      type: "string";
    };

export function listOf(type: string): NodeFieldType {
  return {
    kind: "list",
    type,
  };
}

export const stringPrimitive: NodeFieldType = {
  kind: "primitive",
  type: "string",
};
