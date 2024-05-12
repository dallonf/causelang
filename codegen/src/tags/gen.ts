import { changeCase, path } from "../../deps.ts";
import { compileTemplate, projectRoot } from "../utils/templates.ts";
import { tags } from "./tags.ts";
import { NodeTagParam } from "./types.ts";

export async function generateTags() {
  await Promise.all([
    generateTagTypes(),
    generateTagMappings(),
    generateTagsRustSerializationKt(),
  ]);
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
        return name;
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
  snakeCaseName: string;
  camelCaseName: string;
  type: NodeTagParam;
  rustType: string;
  javaType: string;
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
                camelCaseName: paramName,
                type: param,
                rustType: getParamRustType(param),
                javaType: getParamJavaType(param),
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
            camelCaseName: paramName,
            type: param,
            rustType: getParamRustType(param),
            javaType: getParamJavaType(param),
          };
        });
        const breadcrumb2Param: FlattenedTagParam = {
          snakeCaseName: changeCase.snakeCase(tag.interface.breadcrumb2),
          camelCaseName: tag.interface.breadcrumb2,
          type: { type: "breadcrumbs" as const },
          rustType: "Breadcrumbs",
          javaType: getParamJavaType({ type: "breadcrumbs" }),
        };
        const breadcrumb1Param: FlattenedTagParam = {
          snakeCaseName: changeCase.snakeCase(tag.interface.breadcrumb1),
          camelCaseName: tag.interface.breadcrumb1,
          type: { type: "breadcrumbs" as const },
          rustType: "Breadcrumbs",
          javaType: getParamJavaType({ type: "breadcrumbs" }),
        };
        return [
          {
            name: tag.interface.forwardName,
            params: [breadcrumb2Param].concat(extraParams),
            inverse: true,
            inverseParam: changeCase.snakeCase(tag.interface.breadcrumb1),
            inverseExtraParams: extraParams,
            inverseName: tag.interface.inverseName,
          },
          {
            name: tag.interface.inverseName,
            params: [breadcrumb1Param].concat(extraParams),
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
