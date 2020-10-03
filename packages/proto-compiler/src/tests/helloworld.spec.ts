import { RuntimeLibraryValueType } from '../analyzer';
import { runMain, runMainSync } from './testRunner';

describe('basic hello world', () => {
  const script = ` 
    fn main() {
      cause(Log("Hello World"))
    }
  `;

  it('logs hello world', () => {
    const { result, logs } = runMainSync(script);

    expect(result).toBe(undefined);
    expect(logs).toEqual(['Hello World']);
  });

  it('logs hello world async', async () => {
    const { result, logs } = await runMain(script);

    expect(result).toBe(undefined);
    expect(logs).toEqual(['Hello World']);
  });
});

it('returns a hello world value', () => {
  const script = ` 
    fn main() {
      Greeting("Hello World")
    }
  `;

  const GreetingSymbol = Symbol('Greeting');
  const greetingType: RuntimeLibraryValueType = {
    kind: 'type',
    name: 'Greeting',
    symbol: GreetingSymbol,
  };
  const { result, logs } = runMainSync(script, { library: [greetingType] });

  expect(result).toEqual({ type: GreetingSymbol, value: 'Hello World' });
  expect(logs).toEqual([]);
});

it('supports unary call syntax', () => {
  const script = `
    fn main() {
      cause Log("Hello World")
    }
  `;
  const { result, logs } = runMainSync(script);
  expect(result).toBe(undefined);
  expect(logs).toEqual(['Hello World']);
});
