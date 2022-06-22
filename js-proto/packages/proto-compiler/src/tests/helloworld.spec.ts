import { STRING_ID } from '../coreLibrary';
import makeLibrary, { idFromLibrary } from '../runtimeLibrary';
import { valueOfTypeId } from '../typeSystem';
import { runMain, runMainSync } from './testRunner';

describe('basic hello world', () => {
  const script = ` 
    fn main() {
      cause Print("Hello World")
    }
  `;

  it('prints hello world', () => {
    const { result, output } = runMainSync(script);

    expect(result).toBe(undefined);
    expect(output).toEqual(['Hello World']);
  });

  it('print hello world async', async () => {
    const { result, output } = await runMain(script);

    expect(result).toBe(undefined);
    expect(output).toEqual(['Hello World']);
  });
});

it('returns a hello world value', () => {
  const script = ` 
    fn main() {
      Greeting("Hello World")
    }
  `;

  const library = makeLibrary('test', {
    type: 'objectType',
    name: 'Greeting',
    fields: {
      message: valueOfTypeId(STRING_ID),
    },
  });

  const { result, output } = runMainSync(script, {
    libraries: [library],
  });

  expect(result).toEqual({
    type: idFromLibrary('Greeting', library),
    value: { message: 'Hello World' },
  });
  expect(output).toEqual([]);
});
