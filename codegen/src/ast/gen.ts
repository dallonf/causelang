import { handlebars, path, changeCase } from "../../deps.ts";
import { checkData } from "./check.ts";
import { categories, nodes } from "./nodes.ts";
import { NodeFieldType } from "./types.ts";

const dirname = path.dirname(path.fromFileUrl(import.meta.url));
const projectRoot = path.resolve(dirname, "../../../");

export async function generateAst() {
  if (!checkData()) return;

  await Promise.all([
    generateRustCompilerMetaKt(),
    generateAstNodesRs(),
    generateAstMappingRs(),
  ]);
}

async function generateRustCompilerMetaKt() {
  const template = await compileTemplate("RustCompilerMeta.kt.handlebars");

  const output = template({
    nodes,
  });

  await Deno.writeTextFile(
    path.join(
      projectRoot,
      "ktcause/src/main/kotlin/com/dallonf/ktcause/gen/RustCompilerMeta.kt"
    ),
    output
  );
}

async function generateAstNodesRs() {
  const template = await compileTemplate("ast_nodes.rs.handlebars");

  const templateCategories = categories.map((category) => {
    const suffixRegex = new RegExp(`${category.name}$`);
    return {
      name: `${category.name}Node`,
      nodes: nodes
        .filter((node) => node.category === category.name)
        .map((node) => ({
          variantName: node.name.replace(suffixRegex, ""),
          nodeName: `${node.name}Node`,
        })),
    };
  });

  const templateNodes = nodes.map((node) => {
    return {
      name: `${node.name}Node`,
      fields: Object.entries(node.fields).map(([name, type]) => {
        return {
          name: changeCase.snakeCase(name),
          type: rsFieldType(type),
        };
      }),
    };
  });

  const output = template({
    categories: templateCategories,
    nodes: templateNodes,
  });

  await Deno.writeTextFile(
    path.join(projectRoot, "rscause/rscause_compiler/src/gen/ast_nodes.rs"),
    output
  );
}

async function generateAstMappingRs() {
  const template = await compileTemplate("ast_mapping.rs.handlebars");

  const templateCategories = categories.map((category) => {
    const suffixRegex = new RegExp(`${category.name}$`);
    const name = `${category.name}Node`;
    return {
      name,
      nodes: nodes
        .filter((node) => node.category === category.name)
        .map((node) => ({
          categoryName: name,
          variantName: node.name.replace(suffixRegex, ""),
          nodeName: `${node.name}Node`,
        })),
    };
  });

  const templateNodes = nodes.map((node) => {
    return {
      name: `${node.name}Node`,
      fields: Object.entries(node.fields).flatMap(
        ([fieldName, type]): Record<string, unknown>[] => {
          const rsName = changeCase.snakeCase(fieldName);
          const getterName = `get${changeCase.pascalCase(fieldName)}`;
          if (typeof type === "string") {
            return [
              {
                isNode: true,
                name: rsName,
                getterName,
                type: `${type}Node`,
                needsBoxing: categories.some(
                  (category) => type === category.name
                ),
              },
            ];
          }
          if (type.kind === "list" && typeof type.type === "string") {
            return [
              {
                isList: true,
                name: rsName,
                getterName,
                type: `${type.type}Node`,
              },
            ];
          }
          if (type.kind === "primitive") {
            if (type.type === "string") {
              return [
                {
                  isString: true,
                  name: rsName,
                  getterName,
                },
              ];
            }
          }
          return [];
        }
      ),
    };
  });

  const output = template({
    categories: templateCategories,
    nodes: templateNodes,
  });
  await Deno.writeTextFile(
    path.join(
      projectRoot,
      "rscause/rscause_jni/src/mapping/gen/ast_mapping.rs"
    ),
    output
  );
}

function rsFieldType(
  type: NodeFieldType,
  {
    bare = true,
  }: {
    /** If true, indicates that the type has no wrapper, like `Vec` or `Box`. Categories will be boxed. */
    bare?: boolean;
  } = {}
): string {
  if (typeof type === "string") {
    if (bare && categories.some((category) => category.name === type))
      return `Box<${type}Node>`;
    return `${type}Node`;
  }
  switch (type.kind) {
    case "primitive":
      switch (type.type) {
        case "string":
          return "Arc<String>";
        default:
          return type satisfies never;
      }
    case "list":
      return `Vec<${rsFieldType(type.type, { bare: false })}>`;
    case "optional":
      return `Option<${rsFieldType(type.type)}>`;
    default:
      return type satisfies never;
  }
}

async function compileTemplate(templatePath: string) {
  return handlebars.compile(
    await Deno.readTextFile(path.join(dirname, "templates", templatePath)),
    {
      strict: true,
      noEscape: true,
    }
  );
}
