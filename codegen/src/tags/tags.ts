import { NodeTag, singleNodeTag, twoWayNodeTag } from "./types.ts";

export const tags: NodeTag[] = [
  singleNodeTag("ReferencesFile", {
    path: { type: "string" },
    exportName: { type: "string", nullable: true },
  }),
  singleNodeTag("CanonicalIdInfo", {
    parentName: { type: "string", nullable: true },
    index: { type: "uint" },
  }),
  singleNodeTag("BadFileReference", {}),
  singleNodeTag("TopLevelDeclaration", {
    name: { type: "string" },
  }),
  twoWayNodeTag(["source", "ValueGoesTo", "destination"], {
    inverseName: "ValueComesFrom",
  }),
  twoWayNodeTag(["statement", "SetsVariable", "variable"], {
    inverseName: "VariableSetBy",
  }),
  twoWayNodeTag(
    ["function", "FunctionCanReturnTypeOf", "returnExpressionValue"],
    {
      inverseName: "ReturnsFromFunction",
    }
  ),
  twoWayNodeTag(["function", "FunctionCanReturnAction", "returnExpression"], {
    inverseName: "ActionReturn",
  }),
  twoWayNodeTag(["declaration", "DeclarationForScope", "scope"], {
    inverseName: "ScopeContainsDeclaration",
  }),
  twoWayNodeTag(["value", "ValueCapturedByFunction", "function"], {
    inverseName: "FunctionCapturesValue",
  }),
];
