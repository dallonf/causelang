import { changeCase, path } from "../../deps.ts";
import { compileTemplate, projectRoot } from "../utils/templates.ts";
import { tags } from "./tags.ts";
import { NodeTagParam } from "./types.ts";

export async function generateTags() {
  await Promise.all([generateTagTypes(), generateTagsRustSerializationKt()]);
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

async function generateTagsRustSerializationKt() {
  const template = await compileTemplate(
    "TagsRustSerialization.kt.handlebars",
    import.meta.url
  );

  function getSerializeExpression(param: NodeTagParam, name: string): string {
    if (param.nullable && param.type !== "string" && param.type !== "uint") {
      const inner = getSerializeExpression({ ...param, nullable: false }, "it");
      return `${name}?.let { ${inner} } ?: JsonNull`;
    }

    switch (param.type) {
      case "string":
        return name;
      case "uint":
        return `${name}.toInt()`;
      case "breadcrumbs":
        return `RustSerialization.serializeBreadcrumbs(${name})`;
      default:
        return param.type satisfies never;
    }
  }

  const templateTags = flattenTags().map((tag) => {
    return {
      ...tag,
      params: tag.params.map((param) => {
        return {
          ...param,
          serializeExpression: getSerializeExpression(
            param.type,
            `tag.${param.camelCaseName}`
          ),
        };
      }),
    };
  });

  const output = template({
    tags: templateTags,
  });

  await Deno.writeTextFile(
    path.join(
      projectRoot,
      "ktcause/src/main/kotlin/com/dallonf/ktcause/gen/TagsRustSerialization.kt"
    ),
    output
  );
}

interface FlattenedTag {
  name: string;
  params: FlattenedTagParam[];

  inverse?: boolean;
  inverseParam?: string;
  inverseExtraParams?: FlattenedTagParam[];
  inverseName?: string;
}

interface FlattenedTagParam {
  rustSyntaxName: string;
  snakeCaseName: string;
  camelCaseName: string;
  type: NodeTagParam;
  rustType: string;
}

function flattenTags(): FlattenedTag[] {
  return tags.flatMap((tag) => {
    switch (tag.kind) {
      case "single": {
        return [
          {
            name: tag.name,
            params: Object.entries(tag.params).map(([paramName, param]) => {
              return {
                snakeCaseName: changeCase.snakeCase(paramName),
                rustSyntaxName: getRustSyntaxName(paramName),
                camelCaseName: paramName,
                type: param,
                rustType: getParamRustType(param),
              };
            }),
          },
        ];
      }
      case "two-way": {
        const extraParams: FlattenedTagParam[] = Object.entries(
          tag.extraParams
        ).map(([paramName, param]) => {
          return {
            snakeCaseName: changeCase.snakeCase(paramName),
            rustSyntaxName: getRustSyntaxName(paramName),
            camelCaseName: paramName,
            type: param,
            rustType: getParamRustType(param),
          };
        });
        const breadcrumb2Param: FlattenedTagParam = {
          rustSyntaxName: getRustSyntaxName(tag.interface.breadcrumb2),
          snakeCaseName: changeCase.snakeCase(tag.interface.breadcrumb2),
          camelCaseName: tag.interface.breadcrumb2,
          type: { type: "breadcrumbs" as const },
          rustType: "Breadcrumbs",
        };
        const breadcrumb1Param: FlattenedTagParam = {
          rustSyntaxName: getRustSyntaxName(tag.interface.breadcrumb1),
          snakeCaseName: changeCase.snakeCase(tag.interface.breadcrumb1),
          camelCaseName: tag.interface.breadcrumb1,
          type: { type: "breadcrumbs" as const },
          rustType: "Breadcrumbs",
        };
        return [
          {
            name: tag.interface.forwardName,
            params: [breadcrumb2Param].concat(extraParams),
            inverse: true,
            inverseParam: getRustSyntaxName(tag.interface.breadcrumb1),
            inverseExtraParams: extraParams,
            inverseName: tag.interface.inverseName,
          },
          {
            name: tag.interface.inverseName,
            params: [breadcrumb1Param].concat(extraParams),
            inverse: true,
            inverseParam: getRustSyntaxName(tag.interface.breadcrumb2),
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

function getRustSyntaxName(camelCaseName: string) {
  const snakeCase = changeCase.snakeCase(camelCaseName);

  // deal with reserved words
  if (snakeCase === "loop") {
    return `r#${snakeCase}`;
  }

  return snakeCase;
}
