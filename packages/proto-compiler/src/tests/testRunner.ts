import compileAndInvoke from '../compileAndInvoke';
import compileToJs from '../compileToJs';
import { LogEffectSymbol } from '../coreLibrary';
import { emptyLibrary, Library, mergeLibraries } from '../makeLibrary';
import { CauseRuntime } from '../runtime';

function makeLogOverride() {
  const logs: string[] = [];
  const logOverrideLibrary: Library = {
    symbols: {},
    analyzerScope: {},
    handleEffects: (e) => {
      if (e.type === LogEffectSymbol) {
        logs.push(e.value);
        return { handled: true };
      }
    },
  };
  return { logOverrideLibrary, logs };
}

export async function runMain(
  script: string,
  opts = {} as {
    library?: Library;
  }
) {
  const {
    logOverrideLibrary,
    logs,
  }: { logOverrideLibrary: Library; logs: string[] } = makeLogOverride();
  const library = mergeLibraries(
    logOverrideLibrary,
    opts.library ?? emptyLibrary
  );

  const result = await compileAndInvoke(
    { source: script, filename: 'test.cau' },
    'main',
    [],
    { library: library }
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
    library?: Library;
    debugJsOutput?: boolean;
  }
) {
  const { logOverrideLibrary, logs } = makeLogOverride();
  const library = mergeLibraries(
    logOverrideLibrary,
    opts.library ?? emptyLibrary
  );

  const jsSource = compileToJs(script, library.analyzerScope);
  if (opts.debugJsOutput) {
    console.log(jsSource);
  }
  const runtime = new CauseRuntime(jsSource, 'test.cau', {
    library: library,
  });

  const result = runtime.invokeFnSync('main', []);
  return { result, logs };
}
