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
