import { LibraryItem } from '../runtime';
import { runMain } from './testRunner';

it('logs hello world', async () => {
  const script = ` 
    fn main() {
      cause(Log("Hello World"))
    }
  `;

  const { result, logs } = await runMain(script);

  expect(result).toBe(undefined);
  expect(logs).toEqual(['Hello World']);
});

it('returns a hello world value', async () => {
  const script = ` 
    fn main() {
      Greeting("Hello World")
    }
  `;

  const GreetingSymbol = Symbol('Greeting');
  const greetingType: LibraryItem = {
    kind: 'type',
    name: 'Greeting',
    symbol: GreetingSymbol,
  };
  const { result, logs } = await runMain(script, [greetingType]);

  expect(result).toEqual({ type: GreetingSymbol, value: 'Hello World' });
  expect(logs).toEqual([]);
});
