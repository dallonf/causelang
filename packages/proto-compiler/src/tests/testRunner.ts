import { getAnalyzerScope } from '../analyzer';
import compileAndInvoke from '../compileAndInvoke';
import compileToJs from '../compileToJs';
import { LogEffectID } from '../coreLibrary';
import { Library } from '../makeLibrary';
import { CauseRuntime, EffectHandler } from '../runtime';

function makeLogOverride() {
  const logs: string[] = [];
  const logOverrideHandler: EffectHandler = (e) => {
    if (e.type === LogEffectID) {
      logs.push(e.value);
      return { handled: true };
    }
  };

  return { logOverrideHandler, logs };
}

export async function runMain(
  script: string,
  opts = {} as {
    libraries?: Library[];
  }
) {
  const { logOverrideHandler, logs } = makeLogOverride();

  const result = await compileAndInvoke(
    { source: script, filename: 'test.cau' },
    'main',
    [],
    { libraries: opts.libraries, additionalEffectHandler: logOverrideHandler }
  );

  return {
    result,
    logs,
  };
}

export type SyncEffectHandler = (
  effect: any
) => undefined | { handled: true; value: any };

export function runMainSync(
  script: string,

  opts = {} as {
    libraries?: Library[];
    debugJsOutput?: boolean;
  }
) {
  const { logOverrideHandler, logs } = makeLogOverride();

  const jsSource = compileToJs(
    script,
    getAnalyzerScope(...(opts.libraries ?? []))
  );
  if (opts.debugJsOutput) {
    console.log(jsSource);
  }
  const runtime = new CauseRuntime(jsSource, 'test.cau', {
    libraries: opts.libraries,
    additionalEffectHandler: logOverrideHandler,
  });

  const result = runtime.invokeFnSync('main', []);
  return { result, logs };
}
