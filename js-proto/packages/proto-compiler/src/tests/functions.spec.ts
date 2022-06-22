import { runMainSync } from './testRunner';

it('can call another function in scope and use its value', () => {
  const script = `
    fn main() {
      cause Print(getGreeting())
    }

    fn getGreeting() {
      "Hello World"
    }
  `;
  const { result, output } = runMainSync(script);
  expect(result).toBe(undefined);
  expect(output).toEqual(['Hello World']);
});

it('can cause effects in a nested function call', () => {
  const script = `
    fn main() {
      greet()
    }

    fn greet() {
      cause Print("Hello World")
    }
  `;
  const { result, output } = runMainSync(script);
  expect(result).toBe(undefined);
  expect(output).toEqual(['Hello World']);
});
