import compileToJs from '../compileToJs';
import { CauseRuntime, LibraryItem, LogSymbol, LogEffect } from '../runtime';

describe('simple Hello World script', () => {
  const script = ` 
  fn main() {
    cause(Log("Hello World"))
  }
  `;

  it('logs hello world', async () => {
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
    const runtime = new CauseRuntime(jsSource, 'helloworld.cau', {
      library: [log],
    });

    const result = await runtime.invokeFn('main', []);

    expect(result).toBe(undefined);
    expect(logs).toEqual(['Hello World']);
  });
});
