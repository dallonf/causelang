import { changeCase, path } from "../../deps.ts";
import { compileTemplate, projectRoot } from "../utils/templates.ts";
import { instructions } from "./instructions.ts";
import { InstructionParam } from "./types.ts";

export async function generateInstructions() {
  await Promise.all([
    generateInstructionTypes(),
    generateInstructionRustSerializationKt(),
  ]);
}

async function generateInstructionTypes() {
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

async function generateInstructionRustSerializationKt() {
  const template = await compileTemplate(
    "InstructionRustSerialization.kt.handlebars",
    import.meta.url
  );

  function getDeserializeExpression(
    param: InstructionParam,
    name: string
  ): string {
    switch (param.type) {
      case "int":
      case "uint":
        if (param.nullable) {
          return `(${name} as JsonPrimitive).intOrNull`;
        } else {
          return `(${name} as JsonPrimitive).int`;
        }
      case "boolean":
        if (param.nullable) {
          return `(${name} as JsonPrimitive).booleanOrNull`;
        } else {
          return `(${name} as JsonPrimitive).boolean`;
        }
      default:
        return param.type satisfies never;
    }
  }

  const templateInstructions = Object.entries(instructions).map(
    ([name, instruction]) => ({
      ...instruction,
      name,
      hasParams: Object.entries(instruction.params ?? {}).length > 0,
      params: Object.entries(instruction.params ?? {}).map(
        ([paramName, param]) => {
          return {
            ...param,
            name: paramName,
            deserializeExpression: getDeserializeExpression(
              param,
              `instruction["${changeCase.snakeCase(paramName)}"]`
            ),
          };
        }
      ),
    })
  );

  const output = template({
    instructions: templateInstructions,
  });

  await Deno.writeTextFile(
    path.join(
      projectRoot,
      "ktcause/src/main/kotlin/com/dallonf/ktcause/gen/InstructionRustSerialization.kt"
    ),
    output
  );
}
