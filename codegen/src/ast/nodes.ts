import {
  NodeDeclaration,
  booleanPrimitive,
  listOf,
  optional,
  primitive,
  stringPrimitive,
} from "./types.ts";

export const categories = [
  { name: "TypeReference" },
  { name: "Declaration" },
  { name: "Body" },
  { name: "Statement" },
  { name: "Expression" },
  { name: "BranchOption" },
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
    name: "Pattern",
    fields: {
      name: optional("Identifier"),
      typeReference: "TypeReference",
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
    name: "NamedValue",
    category: "Declaration",
    fields: {
      name: "Identifier",
      typeAnnotation: optional("TypeReference"),
      value: "Expression",
      isVariable: booleanPrimitive,
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
    name: "SingleStatementBody",
    category: "Body",
    fields: {
      statement: "Statement",
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
    name: "DeclarationStatement",
    category: "Statement",
    fields: {
      declaration: "Declaration",
    },
  },

  {
    name: "BranchExpression",
    category: "Expression",
    fields: {
      withValue: optional("Expression"),
      branches: listOf("BranchOption"),
    },
  },
  {
    name: "IfBranchOption",
    category: "BranchOption",
    fields: {
      condition: "Expression",
      body: "Body",
    },
  },
  {
    name: "IsBranchOption",
    category: "BranchOption",
    fields: {
      pattern: "Pattern",
      body: "Body",
    },
  },
  {
    name: "ElseBranchOption",
    category: "BranchOption",
    fields: {
      body: "Body",
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
  {
    name: "NumberLiteralExpression",
    category: "Expression",
    fields: {
      value: primitive("bigdecimal"),
    },
  },
];
