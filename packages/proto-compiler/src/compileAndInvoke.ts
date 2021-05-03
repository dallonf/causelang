import compileToJs from './compileToJs';
import { CauseRuntime, CauseRuntimeOptions } from './runtime';
import { RuntimeLibrary } from './runtimeLibrary';

export interface CompileAndInvokeOptions extends CauseRuntimeOptions {
  libraries?: RuntimeLibrary[];
}

export default async function compileAndInvoke(
  file: { source: string; filename?: string },
  fn: string,
  params: unknown[],
  opts = {} as CompileAndInvokeOptions
) {
  const jsSource = compileToJs(
    file.source,
    opts.libraries?.map((x) => x.libraryData) ?? []
  );
  const runtime = new CauseRuntime(
    jsSource,
    file.filename ?? '<inline script>',
    opts
  );
  return runtime.invokeFn(fn, params);
}
