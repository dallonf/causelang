import compileAndInvoke from '../compileAndInvoke';
import compileToJs from '../compileToJs';
import { DebugEffectID, PrintEffectID } from '../coreLibrary';
import { CauseRuntime, EffectHandler } from '../runtime';
import { RuntimeLibrary } from '../runtimeLibrary';

type Output = string | { type: 'debug'; value: any };
function makeEffectOverrides() {
  const output: Output[] = [];
  const printOverrideHandler: EffectHandler = (e) => {
    if (e.type === PrintEffectID) {
      output.push(e.value.message);
      return { handled: true };
    } else if (e.type === DebugEffectID) {
      output.push({ type: 'debug', value: e.value.value });
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
  const { printOverrideHandler, output } = makeEffectOverrides();

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
  const { printOverrideHandler, output } = makeEffectOverrides();

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
