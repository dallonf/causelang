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
    manualMapping: true,
    fields: {
      expected: diverged({
        rust: "old_resolving_lang_types::OldResolvingLangType",
        kotlin: "ConstraintValueLangType",
      }),
      actual: arc(
        diverged({
          rust: "old_resolving_lang_types::OldResolvingLangType",
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
    fields: {
      options: optional(
        diverged({
          rust: "old_resolving_lang_types::OneOfOldResolvingLangType",
          kotlin: "OptionValueLangType",
        })
      ),
    },
  },
  {
    name: "UnreachableBranch",
    fields: {
      options: optional(
        diverged({
          rust: "old_resolving_lang_types::OneOfOldResolvingLangType",
          kotlin: "OptionValueLangType",
        })
      ),
    },
  },
  {
    name: "ActionIncompatibleWithValueTypes",
    manualMapping: true, // the ValueType struct is hard to translate
    fields: {
      actions: listOf(
        diverged({ rust: "SourcePosition", kotlin: "SourcePosition.Source" })
      ),
      types: optional(
        listOf(
          diverged({
            rust: "ActionIncompatibleWithValueTypesValueType",
            kotlin: "ActionIncompatibleWithValueTypes.ValueType",
          })
        )
      ),
    },
  },
  {
    name: "ConstraintUsedAsValue",
    fields: {
      type: diverged({
        rust: "old_resolving_lang_types::OldResolvingLangType",
        kotlin: "ConstraintValueLangType",
      }),
    },
  },
  {
    name: "ValueUsedAsConstraint",
    fields: {
      type: diverged({
        rust: "old_resolving_lang_types::AnyOldResolvingLangType",
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
