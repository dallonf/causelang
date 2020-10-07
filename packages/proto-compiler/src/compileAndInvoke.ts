import compileToJs from './compileToJs';
import { Library } from './makeLibrary';
import { CauseRuntime, CauseRuntimeOptions } from './runtime';

export interface CompileAndInvokeOptions {
  library?: Library;
}

export default async function compileAndInvoke(
  file: { source: string; filename?: string },
  fn: string,
  params: unknown[],
  opts = {} as CompileAndInvokeOptions
) {
  const jsSource = compileToJs(file.source, opts.library?.analyzerScope);
  const runtime = new CauseRuntime(
    jsSource,
    file.filename ?? '<inline script>',
    {
      library: opts.library,
    }
  );
  return runtime.invokeFn(fn, params);
}
