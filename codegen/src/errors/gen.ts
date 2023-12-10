import { changeCase, path } from "../../deps.ts";
import { compileTemplate } from "../utils/templates.ts";
import { errorTypes } from "./errorTypes.ts";
import { FieldType } from "./types.ts";

const dirname = path.dirname(path.fromFileUrl(import.meta.url));
const projectRoot = path.resolve(dirname, "../../../");

export async function generateErrors() {
  await Promise.all([generateRustErrorTypes(), generateMappings()]);
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
          name: rustFieldName(name),
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

async function generateMappings() {
  const template = await compileTemplate(
    "error_types_mapping.rs.handlebars",
    import.meta.url
  );

  const errorTypesForTemplate = errorTypes.map((error) => {
    return {
      name: error.name,
      hasFields:
        error.fields !== undefined && Object.keys(error.fields).length > 0,
      fields: Object.entries(error.fields ?? {}).map(([name, type]) => {
        return {
          name: rustFieldName(name),
          rustType: rustFieldType(type),
          getterName: changeCase.camelCase("get_" + name),
          isInt: rustFieldType(type) === "u32",
          jniType: jniFieldType(type),
        };
      }),
      constructorParams: Object.entries(error.fields ?? {})
        .map(([, type]) => jniFieldType(type))
        .join(""),
    };
  });

  const output = template({
    errorTypes: errorTypesForTemplate,
  });

  await Deno.writeTextFile(
    path.join(
      projectRoot,
      "rscause/rscause_jni/src/mapping/gen/error_types.rs"
    ),
    output
  );
}

function rustFieldName(name: string): string {
  if (name === "type") return "r#type";
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
    default: {
      return type satisfies never;
    }
  }
}

const jniTypeMap: Record<string, string> = {
  u32: "I",
};

function jniFieldType(type: FieldType): string {
  if (typeof type === "string") {
    if (type === "string") return "Ljava/lang/String;";
    if (type in jniTypeMap) return jniTypeMap[type];
    throw new Error(`Unknown JNI type ${type}`);
  }
  switch (type.kind) {
    case "list":
      return "Ljava/util/List;";
    case "optional":
      return jniFieldType(type.type);
    case "diverged":
      return type.kotlin;
    default: {
      return type satisfies never;
    }
  }
}
