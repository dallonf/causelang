import { changeCase, path } from "../../deps.ts";
import { compileTemplate } from "../utils/templates.ts";
import { errorTypes } from "./errorTypes.ts";
import { FieldType } from "./types.ts";

const dirname = path.dirname(path.fromFileUrl(import.meta.url));
const projectRoot = path.resolve(dirname, "../../../");

export async function generateErrors() {
  await Promise.all([
    generateRustErrorTypes(),
    generateLangErrorRustSerializationKt(),
  ]);
}

async function generateRustErrorTypes() {
  const template = await compileTemplate(
    "error_types.rs.handlebars",
    import.meta.url
  );

  const errorTypesForTemplate = errorTypes.map((error) => {
    return {
      name: error.name,
      hasFields:
        error.fields !== undefined && Object.keys(error.fields).length > 0,
      fields: Object.entries(error.fields ?? {}).map(([name, type]) => {
        return {
          name: rustSyntaxFieldName(name),
          type: rustFieldType(type),
        };
      }),
    };
  });

  const output = template({
    errorTypes: errorTypesForTemplate,
  });

  await Deno.writeTextFile(
    path.join(projectRoot, "rscause/rscause_compiler/src/gen/error_types.rs"),
    output
  );
}

async function generateLangErrorRustSerializationKt() {
  const template = await compileTemplate(
    "LangErrorRustSerialization.kt.handlebars",
    import.meta.url
  );

  function getDeserializeExpression(
    field: FieldType,
    name: string,
    { ktNullable = true } = {}
  ): string {
    if (typeof field === "string") {
      const assertedName = ktNullable ? `${name}!!` : name;
      switch (field) {
        case "string":
          return `(${name} as JsonPrimitive).content`;
        case "u32":
          return `(${name} as JsonPrimitive).int`;
        default:
          return `deserialize${field.replace(/\./g, "")}(${assertedName})`;
      }
    }

    switch (field.kind) {
      case "arc":
      case "box":
        return getDeserializeExpression(field.type, name, { ktNullable });
      case "diverged":
        return getDeserializeExpression(field.kotlin, name, { ktNullable });
      case "list": {
        const innerType = getDeserializeExpression(field.type, "it", {
          ktNullable: false,
        });
        return `(${name} as JsonArray).map { ${innerType} }`;
      }
      case "optional": {
        const innerType = getDeserializeExpression(field.type, "it", {
          ktNullable: false,
        });
        return `${name}?.let { if (${name} is JsonNull) { null } else { ${innerType} } }`;
      }
      default:
        return field satisfies never;
    }
  }

  function getSerializeExpression(field: FieldType, name: string): string {
    if (typeof field === "string") {
      switch (field) {
        case "string":
        case "u32":
          return `JsonPrimitive(${name})`;
        default:
          return `serialize${field.replace(/^.+::/g, "")}(${name})`;
      }
    }

    switch (field.kind) {
      case "arc":
      case "box":
        return getSerializeExpression(field.type, name);
      case "diverged":
        return getSerializeExpression(field.rust, name);
      case "list": {
        const innerType = getSerializeExpression(field.type, "it");
        return `JsonArray(${name}.map { ${innerType} })`;
      }
      case "optional": {
        const innerType = getSerializeExpression(field.type, "it");
        return `${name}?.let { ${innerType} } ?: JsonNull`;
      }
      default:
        return field satisfies never;
    }
  }

  const errorTypesForTemplate = errorTypes.map((error) => {
    return {
      ...error,
      hasFields: Object.keys(error.fields ?? {}).length > 0,
      fields: Object.entries(error.fields ?? {}).map(([name, type]) => {
        const rsName = rustFieldName(name);
        return {
          rsName,
          deserializeExpression: getDeserializeExpression(
            type,
            `error["${rsName}"]`
          ),
          serializeExpression: getSerializeExpression(
            type,
            `errorLangType.${name}`
          ),
        };
      }),
    };
  });

  const output = template({
    errorTypes: errorTypesForTemplate,
  });

  await Deno.writeTextFile(
    path.join(
      projectRoot,
      "ktcause/src/main/kotlin/com/dallonf/ktcause/gen/LangErrorRustSerialization.kt"
    ),
    output
  );
}

/**
 * Escapes reserved words with r#. Use this when outputting Rust syntax.
 * @param name
 * @returns
 */
function rustSyntaxFieldName(name: string): string {
  if (name === "type") return "r#type";
  return rustFieldName(name);
}

function rustFieldName(name: string): string {
  return changeCase.snakeCase(name);
}

function rustFieldType(type: FieldType): string {
  if (typeof type === "string") {
    if (type === "string") return "String";
    return type;
  }
  switch (type.kind) {
    case "list":
      return `Vec<${rustFieldType(type.type)}>`;
    case "optional":
      return `Option<${rustFieldType(type.type)}>`;
    case "diverged":
      return type.rust;
    case "box":
      return `Box<${rustFieldType(type.type)}>`;
    case "arc":
      return `Arc<${rustFieldType(type.type)}>`;
    default: {
      return type satisfies never;
    }
  }
}
