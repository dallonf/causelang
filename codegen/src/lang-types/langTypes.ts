import { LangTypeDeclaration, listOf, optional } from "./types.ts";

export const langTypes: LangTypeDeclaration[] = [
  { name: "TypeReference", kind: "simple", type: { kind: "langType" } },
  {
    name: "Instance",
    kind: "complex",
    fields: {
      type_id: { kind: "canonicalTypeId" },
    },
  },
  {
    name: "Function",
    kind: "complex",
    fields: {
      name: optional({ kind: "string" }),
      params: listOf({
        kind: "object",
        name: "LangParameter",
        fields: {
          name: { kind: "string" },
          value_type: { kind: "langType" },
        },
      }),
      return_type: { kind: "langType" },
    },
  },

  {
    name: "Primitive",
    kind: "simple",
    type: { kind: "primitiveEnum" },
  },
  { name: "StopgapDictionary" },
  { name: "StopgapList" },

  { name: "Action" },
  { name: "Anything" },
  { name: "AnySignal" },
  { name: "NeverContinues" },

  {
    name: "OneOf",
    kind: "complex",
    customHashImplementation: true,
    fields: {
      options: listOf({ kind: "langType" }),
    },
  },

  // TODO: BadValue should only exist at runtime
  { name: "BadValue" },
];
