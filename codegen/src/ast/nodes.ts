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
    name: "FunctionTypeReference",
    category: "TypeReference",
    fields: {
      params: listOf("FunctionSignatureParameter"),
      returnType: "TypeReference",
    },
  },
  {
    name: "OneOfTypeReference",
    category: "TypeReference",
    fields: {
      options: listOf("TypeReference"),
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
    name: "ObjectType",
    category: "Declaration",
    fields: {
      name: "Identifier",
      fields: listOf("ObjectField"),
    },
  },
  {
    name: "SignalType",
    category: "Declaration",
    fields: {
      name: "Identifier",
      fields: listOf("ObjectField"),
      result: optional("TypeReference"),
    },
  },
  {
    name: "TypeAlias",
    category: "Declaration",
    fields: {
      name: "Identifier",
      type: "TypeReference",
    },
  },
  {
    name: "ObjectField",
    fields: {
      name: "Identifier",
      typeAnnotation: "TypeReference",
    },
  },

  {
    name: "BlockBody",
    category: "Body",
    fields: {
      statements: listOf("Statement"),
      result: optional("Expression"),
    },
  },
  {
    name: "SingleExpressionBody",
    category: "Body",
    fields: {
      expression: "Expression",
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
    name: "EffectStatement",
    category: "Statement",
    fields: {
      pattern: "Pattern",
      body: "Body",
    },
  },

  {
    name: "GroupExpression",
    category: "Expression",
    fields: {
      expression: "Expression",
    },
  },
  {
    name: "BlockExpression",
    category: "Expression",
    fields: {
      block: "BlockBody",
    },
  },
  {
    name: "FunctionExpression",
    category: "Expression",
    fields: {
      params: listOf("FunctionSignatureParameter"),
      body: "Expression",
      returnType: optional("TypeReference"),
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
    name: "LoopExpression",
    category: "Expression",
    fields: {
      body: "Body",
    },
  },
  {
    name: "SetExpression",
    category: "Expression",
    fields: {
      identifier: "Identifier",
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
    name: "PipeCallExpression",
    category: "Expression",
    fields: {
      subject: "Expression",
      callee: "Expression",
      parameters: listOf("FunctionCallParameter"),
    },
  },
  {
    name: "MemberExpression",
    category: "Expression",
    fields: {
      objectExpression: "Expression",
      memberIdentifier: "Identifier",
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
  {
    name: "ReturnExpression",
    category: "Expression",
    fields: {
      value: optional("Expression"),
    },
  },
  {
    name: "BreakExpression",
    category: "Expression",
    fields: {
      withValue: optional("Expression"),
    },
  },
];
