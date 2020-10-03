import compileToJs from '../compileToJs';
import { CauseRuntime, LibraryItem, LogSymbol, LogEffect } from '../runtime';

export async function runMain(script: string, library = [] as LibraryItem[]) {
  const logs: string[] = [];

  const log: LibraryItem = {
    kind: 'effect',
    name: 'Log',
    symbol: LogSymbol,
    handler: async (effect: LogEffect) => {
      logs.push(effect.value);
    },
  };

  library = [log, ...library];

  const jsSource = compileToJs(script, library);
  const runtime = new CauseRuntime(jsSource, 'test.cau', {
    library,
  });

  const result = await runtime.invokeFn('main', []);

  return {
    result,
    logs,
  };
}
