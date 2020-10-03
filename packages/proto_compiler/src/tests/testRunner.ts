import compileAndInvoke from '../compileAndInvoke';
import { LibraryItem, LogSymbol, LogEffect } from '../runtime';

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

  const result = await compileAndInvoke(
    { source: script, filename: 'test.cau' },
    'main',
    [],
    { library }
  );

  return {
    result,
    logs,
  };
}
