import compileToJs from '../compileToJs';
import { CauseRuntime, LibraryItem, LogSymbol, LogEffect } from '../runtime';

export async function runMain(script: string) {
  const logs: string[] = [];

  const log: LibraryItem = {
    kind: 'effect',
    name: 'Log',
    symbol: LogSymbol,
    handler: async (effect: LogEffect) => {
      logs.push(effect.value);
    },
  };

  const jsSource = compileToJs(script, [log]);
  const runtime = new CauseRuntime(jsSource, 'test.cau', {
    library: [log],
  });

  const result = await runtime.invokeFn('main', []);

  return {
    result,
    logs,
  };
}
