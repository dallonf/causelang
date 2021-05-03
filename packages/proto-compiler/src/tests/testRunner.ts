import compileAndInvoke from '../compileAndInvoke';
import compileToJs from '../compileToJs';
import { PrintEffectID } from '../coreLibrary';
import { CauseRuntime, EffectHandler } from '../runtime';
import { RuntimeLibrary } from '../runtimeLibrary';

function makePrintOverride() {
  const output: string[] = [];
  const printOverrideHandler: EffectHandler = (e) => {
    if (e.type === PrintEffectID) {
      output.push(e.value);
      return { handled: true };
    }
  };

  return { printOverrideHandler, output };
}

export async function runMain(
  script: string,
  opts = {} as {
    libraries?: RuntimeLibrary[];
  }
) {
  const { printOverrideHandler, output } = makePrintOverride();

  const result = await compileAndInvoke(
    { source: script, filename: 'test.cau' },
    'main',
    [],
    { libraries: opts.libraries, additionalEffectHandler: printOverrideHandler }
  );

  return {
    result,
    output,
  };
}

export type SyncEffectHandler = (
  effect: any
) => undefined | { handled: true; value: any };

export function runMainSync(
  script: string,

  opts = {} as {
    libraries?: RuntimeLibrary[];
    debugJsOutput?: boolean;
  }
) {
  const { printOverrideHandler, output } = makePrintOverride();

  const jsSource = compileToJs(
    script,
    opts.libraries?.map((x) => x.libraryData) ?? []
  );
  if (opts.debugJsOutput) {
    console.log(jsSource);
  }
  const runtime = new CauseRuntime(jsSource, 'test.cau', {
    libraries: opts.libraries,
    additionalEffectHandler: printOverrideHandler,
  });

  const result = runtime.invokeFnSync('main', []);
  return { result, output };
}
