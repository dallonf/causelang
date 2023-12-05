import { path } from "../../deps.ts";
import { compileTemplate } from "../utils/templates.ts";
import { errorTypes } from "./errorTypes.ts";

const dirname = path.dirname(path.fromFileUrl(import.meta.url));
const projectRoot = path.resolve(dirname, "../../../");

export async function generateErrors() {
  await Promise.all([generateRustErrorTypes()]);
}

async function generateRustErrorTypes() {
  const template = await compileTemplate(
    "error_types.rs.handlebars",
    import.meta.url
  );

  const errorTypesForTemplate = errorTypes.map((error) => {
    return {
      name: error.name,
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
