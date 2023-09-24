import { generateAst } from "./src/ast/gen.ts";
import { generateInstructions } from "./src/ast/instructions/gen.ts";

await Promise.all([generateAst(), generateInstructions()]);
