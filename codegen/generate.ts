import { fs, path } from "./deps.ts";
import { generateAst } from "./src/ast/gen.ts";
import { generateErrors } from "./src/errors/gen.ts";
import { generateInstructions } from "./src/instructions/gen.ts";
import { generateTags } from "./src/tags/gen.ts";

async function generate() {
  await Promise.all([
    generateAst(),
    generateInstructions(),
    generateTags(),
    generateErrors(),
  ]);
}

await generate();

const WATCH_DEBOUNCE = 300;

const templateDirs = fs.walkSync("./src", {
  match: [new RegExp(`\\${path.SEP}templates$`)],
  includeDirs: true,
  includeFiles: false,
});
const watcher = Deno.watchFs([...templateDirs].map((it) => it.path));
console.log("watching for template changes...");
let lastChange = Date.now();
for await (const event of watcher) {
  const currentChange = Date.now();
  if (currentChange - lastChange < WATCH_DEBOUNCE) continue;
  console.log(`${event.paths[0]} changed, regenerating...`);
  lastChange = currentChange;
  try {
    await generate();
  } catch (e) {
    console.error(e);
  }
}
