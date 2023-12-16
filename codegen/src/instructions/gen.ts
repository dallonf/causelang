import { changeCase, path } from "../../deps.ts";
import { compileTemplate, projectRoot } from "../utils/templates.ts";
import { instructions } from "./instructions.ts";

export async function generateInstructions() {
  await Promise.all([generateInstructionTypes(), generateInstructionMapping()]);
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

async function generateInstructionMapping() {
  const template = await compileTemplate(
    "instruction_mapping.rs.handlebars",
    import.meta.url
  );

  const templateInstructions = Object.entries(instructions).map(
    ([name, instruction]) => {
      const constructorParamTypes = Object.entries(
        instruction.params ?? {}
      ).map(([, param]) => {
        switch (param.type) {
          case "int":
          case "uint":
            if (param.nullable) {
              return "Ljava/lang/Integer;";
            } else {
              return "I";
            }
          case "boolean":
            return "Z";
          default:
            return param.type satisfies never;
        }
      });
      const constructorSignature = `(${constructorParamTypes.join("")})V`;
      const className = `com/dallonf/ktcause/Instruction$${name}`;
      return {
        name,
        class: className,
        constructorSignature,
        params: Object.entries(instruction.params ?? {}).map(
          ([paramName, param]) => ({
            name: changeCase.snakeCase(paramName),
            convertToOptionalInt: param.nullable && param.type === "uint",
            convertToInt: param.type === "uint",
          })
        ),
      };
    }
  );

  const output = template({
    instructions: templateInstructions,
  });
  await Deno.writeTextFile(
    path.join(
      projectRoot,
      "rscause/rscause_jni/src/mapping/gen/instruction_mapping.rs"
    ),
    output
  );
}
