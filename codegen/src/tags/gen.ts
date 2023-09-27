import { changeCase, path } from "../../deps.ts";
import { compileTemplate, projectRoot } from "../utils/templates.ts";
import { tags } from "./tags.ts";
import { NodeTagParam } from "./types.ts";

export async function generateTags() {
  const template = await compileTemplate("tags.rs.handlebars", import.meta.url);

  const templateTags = tags.flatMap((tag) => {
    switch (tag.kind) {
      case "single": {
        return [
          {
            name: tag.name,
            params: Object.entries(tag.params).map(([paramName, param]) => {
              return {
                name: changeCase.snakeCase(paramName),
                type: getParamType(param),
              };
            }),
          },
        ];
      }
      case "two-way": {
        const extraParams = Object.entries(tag.extraParams).map(
          ([paramName, param]) => {
            return {
              name: changeCase.snakeCase(paramName),
              type: getParamType(param),
            };
          }
        );
        return [
          {
            name: tag.interface.forwardName,
            params: [
              {
                name: changeCase.snakeCase(tag.interface.breadcrumb2),
                type: "Breadcrumbs",
              },
            ].concat(extraParams),
            inverse: true,
            inverseParam: changeCase.snakeCase(tag.interface.breadcrumb1),
            inverseExtraParams: extraParams,
            inverseName: tag.interface.inverseName,
          },
          {
            name: tag.interface.inverseName,
            params: [
              {
                name: changeCase.snakeCase(tag.interface.breadcrumb1),
                type: "Breadcrumbs",
              },
            ].concat(extraParams),
            inverse: true,
            inverseParam: changeCase.snakeCase(tag.interface.breadcrumb2),
            inverseExtraParams: extraParams,
            inverseName: tag.interface.forwardName,
          },
        ];
      }
      default:
        return tag satisfies never;
    }
  });

  const output = template({
    tags: templateTags,
  });

  await Deno.writeTextFile(
    path.join(projectRoot, "rscause/rscause_compiler/src/gen/tags.rs"),
    output
  );
}

function getParamType(param: NodeTagParam) {
  let type;
  switch (param.type) {
    case "string":
      type = "String";
      break;
    case "uint":
      type = "u32";
      break;
    case "breadcrumbs":
      type = "Breadcrumbs";
      break;
    default:
      return param.type satisfies never;
  }
  if (param.nullable) type = `Option<${type}>`;

  return type;
}
