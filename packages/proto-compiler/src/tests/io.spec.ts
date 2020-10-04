import { RuntimeLibraryValueType } from '../analyzer';
import { runMainSync, SyncEffectHandler } from './testRunner';

it('can receive a value from an input effect and return it', () => {
  const script = `
    fn main() {
      cause Log("What is your name?")
      cause Log(cause Prompt())
    }
  `;

  const promptSymbol = Symbol('Prompt');
  const library: RuntimeLibraryValueType[] = [
    {
      kind: 'effect',
      name: 'Prompt',
      symbol: promptSymbol,
    },
  ];
  const effectHandler: SyncEffectHandler = (e) => {
    if (e.type === promptSymbol) {
      return { handled: true, value: 'Batman' };
    }
  };

  const { result, logs } = runMainSync(script, {
    library,
    effectHandler,
  });
  expect(result).toBe(undefined);
  expect(logs).toEqual(['What is your name?', 'Batman']);
});
