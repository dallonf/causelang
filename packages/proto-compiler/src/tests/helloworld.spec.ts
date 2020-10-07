import makeLibrary from '../makeLibrary';
import { runMain, runMainSync } from './testRunner';

describe('basic hello world', () => {
  const script = ` 
    fn main() {
      cause Log("Hello World")
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

  const library = makeLibrary({
    type: 'type',
    name: 'Greeting',
  });

  const { result, logs } = runMainSync(script, { library });

  expect(result).toEqual({
    type: library.symbols.Greeting,
    value: 'Hello World',
  });
  expect(logs).toEqual([]);
});
