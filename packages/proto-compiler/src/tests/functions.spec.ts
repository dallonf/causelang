import { runMainSync } from './testRunner';

it('can call another function in scope and use its value', () => {
  const script = `
    fn main() {
      cause Log getGreeting()
    }

    fn getGreeting() {
      "Hello World"
    }
  `;
  const { result, logs } = runMainSync(script);
  expect(result).toBe(undefined);
  expect(logs).toEqual(['Hello World']);
});

it('can cause effects in a nested function call', () => {
  const script = `
    fn main() {
      greet()
    }

    fn greet() {
      cause Log "Hello World"
    }
  `;
  const { result, logs } = runMainSync(script);
  expect(result).toBe(undefined);
  expect(logs).toEqual(['Hello World']);
});

