import { path } from "../../deps.ts";
import { compileTemplate } from "../utils/templates.ts";

const dirname = path.dirname(path.fromFileUrl(import.meta.url));
const projectRoot = path.resolve(dirname, "../../../");

export async function generateErrors() {
  await Promise.all([generateRustErrorTypes()]);
}

async function generateRustErrorTypes() {
  const template = await compileTemplate(
    "errors.rs.handlebars",
    import.meta.url
  );

  const output = template({});

  await Deno.writeTextFile(
    path.join(projectRoot, "rscause/rscause_compiler/src/gen/errors.rs"),
    output
  );
}
