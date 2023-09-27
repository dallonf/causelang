import { generateAst } from "./src/ast/gen.ts";
import { generateInstructions } from "./src/instructions/gen.ts";
import { generateTags } from "./src/tags/gen.ts";

await Promise.all([generateAst(), generateInstructions(), generateTags()]);
