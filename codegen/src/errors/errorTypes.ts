import { ErrorTypeDeclaration, diverged, listOf, optional } from "./types.ts";

export const errorTypes: ErrorTypeDeclaration[] = [
  {
    name: "NeverResolved",
  },
  {
    name: "NotInScope",
  },
  {
    name: "FileNotFound",
  },
  {
    name: "ImportPathInvalid",
  },
  {
    name: "ExportNotFound",
  },
  {
    name: "ProxyError",
    fields: {
      actualError: diverged({
        rust: "LangType",
        kotlin: "ErrorLangType",
      }),
      proxyChain: listOf(
        diverged({ rust: "ErrorPosition", kotlin: "SourcePosition" })
      ),
    },
  },
  {
    name: "NotCallable",
  },
  {
    name: "NotCausable",
  },
  {
    name: "ImplementationTodo",
    fields: {
      description: "string",
    },
  },
  {
    name: "MismatchedType",
    fields: {
      expected: diverged({
        rust: "AnyInferredLangType",
        kotlin: "ConstraintValueLangType",
      }),
      actual: diverged({
        rust: "LangType",
        kotlin: "ResolvedValueLangType",
      }),
    },
  },
  {
    name: "MissingParameters",
    fields: {
      names: listOf("string"),
    },
  },
  {
    name: "ExcessParameters",
    fields: {
      expected: "u32",
    },
  },
  {
    name: "UnknownParameter",
  },
  {
    name: "MissingElseBranch",
  },
  {
    name: "UnreachableBranch",
    fields: {
      options: optional(
        diverged({ rust: "OneOfLangType", kotlin: "OptioNValueLangType" })
      ),
    },
  },
  {
    name: "ActionIncompatibleWithValueTypes",
    fields: {
      actions: listOf(
        diverged({ rust: "SourcePosition", kotlin: "SourcePosition.Source" })
      ),
      types: listOf(diverged({ rust: "LangType", kotlin: "ValueType" })),
    },
  },
  {
    name: "ConstraintUsedAsValue",
    fields: {
      type: diverged({
        rust: "LangType",
        kotlin: "ConstraintValueLangType",
      }),
    },
  },
  {
    name: "ValueUsedAsConstraint",
    fields: {
      type: diverged({
        rust: "AnyInferredLangType",
        kotlin: "ValueLangType",
      }),
    },
  },
  {
    name: "DoesNotHaveAnyMembers",
  },
  {
    name: "DoesNotHaveMember",
  },
  {
    name: "NotVariable",
  },
  {
    name: "OuterVariable",
  },
  {
    name: "CannotBreakHere",
  },
  {
    name: "NotSupportedInRust",
  },
  {
    name: "CompilerBug",
    fields: {
      description: "string",
    },
  },
];
