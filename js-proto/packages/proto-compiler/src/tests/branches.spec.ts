import { runMainSync } from './testRunner';

it('is an expression', () => {
  const script = `
    fn main() {
      branch {
        if equals("red", "blue") => "nope"
        if equals("red", "red") => "yup"
        default => "wut"
      }
    }
  `;

  const { result } = runMainSync(script);
  expect(result).toBe('yup');
});
