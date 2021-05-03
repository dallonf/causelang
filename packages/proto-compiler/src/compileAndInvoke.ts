import compileToJs from './compileToJs';
import { Library } from './library';
import { CauseRuntime, CauseRuntimeOptions } from './runtime';

export interface CompileAndInvokeOptions extends CauseRuntimeOptions {
  libraries?: Library[];
}

export default async function compileAndInvoke(
  file: { source: string; filename?: string },
  fn: string,
  params: unknown[],
  opts = {} as CompileAndInvokeOptions
) {
  const jsSource = compileToJs(file.source, opts.libraries ?? []);
  const runtime = new CauseRuntime(
    jsSource,
    file.filename ?? '<inline script>',
    opts
  );
  return runtime.invokeFn(fn, params);
}
