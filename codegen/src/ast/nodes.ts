import { NodeDeclaration, listOf, optional, stringPrimitive } from "./types.ts";

export const categories = [
  { name: "TypeReference" },
  { name: "Declaration" },
  { name: "Body" },
  { name: "Statement" },
  { name: "Expression" },
];

export const nodes: NodeDeclaration[] = [
  {
    name: "Identifier",
    fields: {
      text: stringPrimitive,
    },
  },

  {
    name: "IdentifierTypeReference",
    category: "TypeReference",
    fields: {
      identifier: "Identifier",
    },
  },

  {
    name: "FunctionSignatureParameter",
    fields: {
      name: "Identifier",
      typeReference: optional("TypeReference"),
    },
  },
  {
    name: "FunctionCallParameter",
    fields: {
      value: "Expression",
    },
  },

  {
    name: "File",
    fields: {
      declarations: listOf("Declaration"),
    },
  },

  {
    name: "Import",
    category: "Declaration",
    fields: {
      path: "ImportPath",
      mappings: listOf("ImportMapping"),
    },
  },
  {
    name: "ImportPath",
    fields: {
      path: stringPrimitive,
    },
  },
  {
    name: "ImportMapping",
    fields: {
      sourceName: "Identifier",
      rename: optional("Identifier"),
    },
  },
  {
    name: "Function",
    category: "Declaration",
    fields: {
      name: "Identifier",
      params: listOf("FunctionSignatureParameter"),
      body: "Body",
      returnType: optional("TypeReference"),
    },
  },

  {
    name: "BlockBody",
    category: "Body",
    fields: {
      statements: listOf("Statement"),
    },
  },

  {
    name: "ExpressionStatement",
    category: "Statement",
    fields: {
      expression: "Expression",
    },
  },

  {
    name: "CauseExpression",
    category: "Expression",
    fields: {
      signal: "Expression",
    },
  },
  {
    name: "CallExpression",
    category: "Expression",
    fields: {
      callee: "Expression",
      parameters: listOf("FunctionCallParameter"),
    },
  },
  {
    name: "IdentifierExpression",
    category: "Expression",
    fields: {
      identifier: "Identifier",
    },
  },
  {
    name: "StringLiteralExpression",
    category: "Expression",
    fields: {
      text: stringPrimitive,
    },
  },
];
