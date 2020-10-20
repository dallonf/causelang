import makeLibrary from '../makeLibrary';
import { runMainSync } from './testRunner';

it('can receive a value from an input effect and return it', () => {
  const script = `
    fn main() {
      cause Log("What is your name?")
      cause Log(append("Hello, ", cause Prompt()))
    }
  `;

  const library = makeLibrary('test', {
    type: 'effect',
    name: 'Prompt',
    handler: (e) => 'Batman',
  });

  const { result, logs } = runMainSync(script, {
    libraries: [library],
  });
  expect(result).toBe(undefined);
  expect(logs).toEqual(['What is your name?', 'Hello, Batman']);
});

it('can assign a received value to a name', () => {
  const script = `
  fn main() {
    cause Log("What is your name?")
    let name = cause Prompt()
    cause Log(append("Hello, ", name))
  }
`;

  const library = makeLibrary('test', {
    type: 'effect',
    name: 'Prompt',
    handler: (e) => 'Batman',
  });

  const { result, logs } = runMainSync(script, {
    libraries: [library],
  });
  expect(result).toBe(undefined);
  expect(logs).toEqual(['What is your name?', 'Hello, Batman']);
});
