import { path, changeCase } from "../../deps.ts";
import { compileTemplate } from "../utils/templates.ts";
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
  const template = await compileTemplate(
    "RustCompilerMeta.kt.handlebars",
    import.meta.url
  );

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
  const template = await compileTemplate(
    "ast_nodes.rs.handlebars",
    import.meta.url
  );

  const templateCategories = categories.map((category) => {
    const suffixRegex = new RegExp(`${category.name}$`);
    return {
      name: `${category.name}Node`,
      nodes: nodes
        .filter((node) => node.category === category.name)
        .map((node) => ({
          anyVariantName: node.name,
          variantName: node.name.replace(suffixRegex, ""),
          nodeName: `${node.name}Node`,
        })),
    };
  });

  const templateNodes = nodes.map((node) => {
    return {
      name: `${node.name}Node`,
      variantName: node.name,
      fields: Object.entries(node.fields).map(([name, type]) => {
        return {
          name: changeCase.snakeCase(name),
          type: rsFieldType(type),
          isNode: isNode(type),
        };
      }),
    };
  });

  const output = template({
    breadcrumbNames: breadcrumbNames.map((name) => changeCase.snakeCase(name)),
    categories: templateCategories,
    nodes: templateNodes,
  });

  await Deno.writeTextFile(
    path.join(projectRoot, "rscause/rscause_compiler/src/gen/ast_nodes.rs"),
    output
  );
}

async function generateAstMappingRs() {
  const template = await compileTemplate(
    "ast_mapping.rs.handlebars",
    import.meta.url
  );

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
          const rsType = rsFieldType(type, {
            astNamespace: "ast",
          });
          const javaType = javaFieldType(type);
          return [
            {
              isNode: true,
              name: rsName,
              getterName,
              rsType,
              javaType,
            },
          ];
        }
      ),
    };
  });

  const output = template({
    breadcrumbNames: breadcrumbNames,
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
  opts: {
    /** If true, indicates that the type has no wrapper, like `Vec` or `Box`. Categories will be boxed. */
    bare?: boolean;
    astNamespace?: string;
  } = {}
): string {
  if (typeof type === "string") {
    let name = `${type}Node`;
    if (opts.astNamespace) {
      name = `${opts.astNamespace}::${name}`;
    }
    if (!categories.some((category) => category.name === type)) {
      name = `Arc<${name}>`;
    }
    return name;
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
      return `Vec<${rsFieldType(type.type, { ...opts, bare: false })}>`;
    case "optional":
      return `Option<${rsFieldType(type.type, opts)}>`;
    default:
      return type satisfies never;
  }
}

function isNode(type: NodeFieldType): boolean {
  if (typeof type === "string") return true;
  switch (type.kind) {
    case "primitive":
      return false;
    case "list":
      return isNode(type.type);
    case "optional":
      return isNode(type.type);
    default:
      return type satisfies never;
  }
}

function javaFieldType(type: NodeFieldType): string {
  if (typeof type === "string") {
    return `Lcom/dallonf/ktcause/ast/${type}Node;`;
  }
  switch (type.kind) {
    case "primitive":
      switch (type.type) {
        case "string":
          return "Ljava/lang/String;";
        default:
          return type satisfies never;
      }
    case "list":
      return `Ljava/util/List;`;
    case "optional":
      return javaFieldType(type.type);
    default:
      return type satisfies never;
  }
}

const breadcrumbNames = nodes.flatMap((node) => Object.keys(node.fields));
