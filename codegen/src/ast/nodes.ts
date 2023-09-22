import { NodeDeclaration, listOf } from "./types.ts";

export const categories = [
  {
    name: "Declaration",
  },
];

export const nodes: NodeDeclaration[] = [
  {
    name: "File",
    fields: {
      declarations: listOf("Declaration"),
    },
  },
  {
    name: "Function",
    category: "Declaration",
    fields: {
      name: "string",
      params: listOf("FunctionSignatureParameter"),
      body: "Body",
      returnType: "TypeReference",
    },
  },
];
