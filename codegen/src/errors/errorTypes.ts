import {
  ErrorTypeDeclaration,
  arc,
  diverged,
  listOf,
  optional,
} from "./types.ts";

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
      actualError: arc(
        diverged({
          rust: "LangError",
          kotlin: "ErrorLangType",
        })
      ),
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
        rust: "lang_types::LangType",
        kotlin: "ConstraintValueLangType",
      }),
      actual: arc(
        diverged({
          rust: "lang_types::LangType",
          kotlin: "ResolvedValueLangType",
        })
      ),
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
        diverged({
          rust: "lang_types::OneOfLangType",
          kotlin: "OptionValueLangType",
        })
      ),
    },
  },
  {
    name: "ActionIncompatibleWithValueTypes",
    fields: {
      actions: listOf(
        diverged({ rust: "SourcePosition", kotlin: "SourcePosition.Source" })
      ),
      types: listOf(
        diverged({ rust: "lang_types::LangType", kotlin: "ValueType" })
      ),
    },
  },
  {
    name: "ConstraintUsedAsValue",
    fields: {
      type: diverged({
        rust: "lang_types::LangType",
        kotlin: "ConstraintValueLangType",
      }),
    },
  },
  {
    name: "ValueUsedAsConstraint",
    fields: {
      type: diverged({
        rust: "lang_types::AnyInferredLangType",
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
