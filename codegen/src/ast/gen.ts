import { handlebars, path, changeCase } from "../../deps.ts";
import { checkData } from "./check.ts";
import { categories, nodes } from "./nodes.ts";
import { NodeFieldType } from "./types.ts";

const dirname = path.dirname(path.fromFileUrl(import.meta.url));
const projectRoot = path.resolve(dirname, "../../../");

export async function generateAst() {
  if (!checkData()) return;

  await Promise.all([generateRustCompilerMetaKt(), generateAstNodesRs()]);
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
    if (bare) return `Box<${type}Node>`;
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
