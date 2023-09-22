import { handlebars, path } from "../../deps.ts";
import { checkData } from "./check.ts";
import { nodes } from "./nodes.ts";

const dirname = path.dirname(path.fromFileUrl(import.meta.url));
const projectRoot = path.resolve(dirname, "../../../");

export async function generateAst() {
  if (!checkData()) return;

  const rustCompilerMetaTemplate = handlebars.compile(
    await Deno.readTextFile(
      path.join(dirname, "./templates/RustCompilerMeta.kt.handlebars")
    ),
    {
      strict: true,
    }
  );

  const output = rustCompilerMetaTemplate({
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
