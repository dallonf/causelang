import compileToJs from './compileToJs';
import { CauseRuntime, CauseRuntimeOptions } from './runtime';

export default async function compileAndInvoke(
  file: { source: string; filename?: string },
  fn: string,
  params: unknown[],
  opts = {} as CauseRuntimeOptions
) {
  const jsSource = compileToJs(file.source, opts.library ?? []);
  const runtime = new CauseRuntime(
    jsSource,
    file.filename ?? '<inline script>',
    opts
  );
  return runtime.invokeFn(fn, params);
}
