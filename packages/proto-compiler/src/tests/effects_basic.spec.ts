import makeLibrary from '../makeLibrary';
import { runMainSync } from './testRunner';

it('Can intercept an effect', () => {
  const script = `
    fn main() {
      {
        cause Print("Howdy world!")
      } handle => {
        cause Print("Intercepted a Print effect")
      }
    }
  `;

  const { result, output } = runMainSync(script);
  expect(result).toBe(undefined);
  expect(output).toEqual(['Intercepted a Print effect']);
});

it('Can identify the type of effect', () => {
  const library = makeLibrary('test', {
    type: 'effect',
    name: 'InterceptThis',
    handler: () => {
      throw new Error('This effect should have been intercepted!');
    },
  });

  const script = `
    fn main() {
      {
        cause InterceptThis()
        cause Print("Howdy world!")
      } handle InterceptThis() => {
        cause Print("Intercepted an InterceptThis effect")
      }
    }
  `;

  const { result, output } = runMainSync(script, {
    libraries: [library],
  });

  expect(result).toBe(undefined);
  expect(output).toEqual([
    'Intercepted an InterceptThis effect',
    'Howdy world!',
  ]);
});
