import { STRING_ID } from '../coreLibrary';
import makeLibrary from '../runtimeLibrary';
import { valueOfTypeId } from '../typeSystem';
import { runMainSync } from './testRunner';

it('can receive a value from an input effect and return it', () => {
  const script = `
    fn main() {
      cause Print("What is your name?")
      cause Print(append("Hello, ", cause Prompt()))
    }
  `;

  const library = makeLibrary('test', {
    type: 'effect',
    name: 'Prompt',
    params: {},
    returnType: valueOfTypeId(STRING_ID),
    handler: (e) => 'Batman',
  });

  const { result, output } = runMainSync(script, {
    libraries: [library],
  });
  expect(result).toBe(undefined);
  expect(output).toEqual(['What is your name?', 'Hello, Batman']);
});

it('can assign a received value to a name', () => {
  const script = `
  fn main() {
    cause Print("What is your name?")
    let name = cause Prompt()
    cause Print(append("Hello, ", name))
  }
`;

  const library = makeLibrary('test', {
    type: 'effect',
    name: 'Prompt',
    params: {},
    returnType: valueOfTypeId(STRING_ID),
    handler: (e) => 'Batman',
  });

  const { result, output: output } = runMainSync(script, {
    libraries: [library],
  });
  expect(result).toBe(undefined);
  expect(output).toEqual(['What is your name?', 'Hello, Batman']);
});

it('can use an inline block expression to return a result', () => {
  const script = `
    fn main() {
      cause Print("What is your name?")
      let greeting = {
        let name = cause Prompt()
        append("Hello, ", name)
      }
      cause Print(greeting)
    }
  `;

  const library = makeLibrary('test', {
    type: 'effect',
    name: 'Prompt',
    params: {},
    returnType: valueOfTypeId(STRING_ID),
    handler: (e) => 'Superman',
  });

  const { result, output: output } = runMainSync(script, {
    libraries: [library],
  });
  expect(result).toBe(undefined);
  expect(output).toEqual(['What is your name?', 'Hello, Superman']);
});
