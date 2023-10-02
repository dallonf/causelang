import { changeCase, path } from "../../deps.ts";
import { compileTemplate, projectRoot } from "../utils/templates.ts";
import { tags } from "./tags.ts";
import { NodeTagParam } from "./types.ts";

export async function generateTags() {
  await Promise.all([generateTagTypes(), generateTagMappings()]);
}

async function generateTagTypes() {
  const template = await compileTemplate("tags.rs.handlebars", import.meta.url);

  const templateTags = flattenTags();

  const output = template({
    tags: templateTags,
  });

  await Deno.writeTextFile(
    path.join(projectRoot, "rscause/rscause_compiler/src/gen/tags.rs"),
    output
  );
}

async function generateTagMappings() {
  const template = await compileTemplate(
    "tag_mappings.rs.handlebars",
    import.meta.url
  );

  const templateTags = flattenTags().map((tag) => {
    return {
      ...tag,
      params: tag.params.map((param) => {
        let getterName = `get${changeCase.pascalCase(param.camelCaseName)}`;
        if (getterName === "getIndex") getterName = "getIndex-pVg5ArA";
        return { ...param, getterName };
      }),
    };
  });

  const output = template({
    tags: templateTags,
  });

  await Deno.writeTextFile(
    path.join(
      projectRoot,
      "rscause/rscause_jni/src/mapping/gen/tag_mappings.rs"
    ),
    output
  );
}

function flattenTags() {
  return tags.flatMap((tag) => {
    switch (tag.kind) {
      case "single": {
        return [
          {
            name: tag.name,
            params: Object.entries(tag.params).map(([paramName, param]) => {
              return {
                snakeCaseName: changeCase.snakeCase(paramName),
                camelCaseName: paramName,
                rustType: getParamRustType(param),
                javaType: getParamJavaType(param),
              };
            }),
          },
        ];
      }
      case "two-way": {
        const extraParams = Object.entries(tag.extraParams).map(
          ([paramName, param]) => {
            return {
              snakeCaseName: changeCase.snakeCase(paramName),
              camelCaseName: paramName,
              rustType: getParamRustType(param),
              javaType: getParamJavaType(param),
            };
          }
        );
        return [
          {
            name: tag.interface.forwardName,
            params: [
              {
                snakeCaseName: changeCase.snakeCase(tag.interface.breadcrumb2),
                camelCaseName: tag.interface.breadcrumb2,
                rustType: "Breadcrumbs",
                javaType: getParamJavaType({ type: "breadcrumbs" }),
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
                snakeCaseName: changeCase.snakeCase(tag.interface.breadcrumb1),
                camelCaseName: tag.interface.breadcrumb1,
                rustType: "Breadcrumbs",
                javaType: getParamJavaType({ type: "breadcrumbs" }),
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
}

function getParamRustType(param: NodeTagParam) {
  let type;
  switch (param.type) {
    case "string":
      type = "Arc<String>";
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

function getParamJavaType(param: NodeTagParam) {
  let type;
  switch (param.type) {
    case "string":
      type = "Ljava/lang/String;";
      break;
    case "uint":
      type = "I";
      break;
    case "breadcrumbs":
      type = "Lcom/dallonf/ktcause/ast/Breadcrumbs;";
      break;
    default:
      return param.type satisfies never;
  }

  return type;
}
