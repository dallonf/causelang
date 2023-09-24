import { handlebars, path } from "../../deps.ts";

export function dirname(importMetaUrl: string) {
  return path.dirname(path.fromFileUrl(importMetaUrl));
}

export const projectRoot = path.resolve(dirname(import.meta.url), "../../../");

export async function compileTemplate(
  templatePath: string,
  importMetaUrl: string
) {
  return handlebars.compile(
    await Deno.readTextFile(
      path.join(dirname(importMetaUrl), "templates", templatePath)
    ),
    {
      strict: true,
      noEscape: true,
    }
  );
}
