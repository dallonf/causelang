import { changeCase, path } from "../../deps.ts";
import { compileTemplate, projectRoot } from "../utils/templates.ts";
import { instructions } from "./instructions.ts";

export async function generateInstructions() {
  const template = await compileTemplate(
    "instruction_types.rs.handlebars",
    import.meta.url
  );

  const templateInstructions = Object.entries(instructions).map(
    ([name, instruction]) => {
      return {
        name,
        description: instruction.description,
        fields:
          instruction.params &&
          Object.entries(instruction.params).map(([paramName, instruction]) => {
            let type;
            switch (instruction.type) {
              case "int":
                type = "i32";
                break;
              case "uint":
                type = "u32";
                break;
              case "boolean":
                type = "bool";
                break;
              default:
                return instruction.type satisfies never;
            }
            if (instruction.nullable) type = `Option<${type}>`;

            return {
              name: changeCase.snakeCase(paramName),
              type,
            };
          }),
      };
    }
  );

  const output = template({
    instructions: templateInstructions,
  });
  await Deno.writeTextFile(
    path.join(
      projectRoot,
      "rscause/rscause_compiler/src/gen/instruction_types.rs"
    ),
    output
  );
}
