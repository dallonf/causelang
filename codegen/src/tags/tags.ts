import { NodeTag, singleNodeTag, twoWayNodeTag } from "./types.ts";

export const tags: NodeTag[] = [
  singleNodeTag("ReferencesFile", {
    path: { type: "string" },
    exportName: { type: "string", nullable: true },
  }),
  singleNodeTag("BadFileReference", {}),
  twoWayNodeTag(["source", "ValueComesFrom", "destination"], {
    inverseName: "ValuesGoesTo",
  }),
  twoWayNodeTag(["function", "FunctionCanReturnTypeOf", "returnExpression"], {
    inverseName: "ReturnsFromFunction",
  }),
  twoWayNodeTag(["function", "FunctionCanReturnAction", "returnExpression"], {
    inverseName: "ActionReturn",
  }),
];
