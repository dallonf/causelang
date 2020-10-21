import { runMainSync } from './testRunner';

it('changes the value of a variable', () => {
  const script = `
    fn main() {
      let var x = 1
      x = 2
      cause Print(x)
    }
`;

  const { output } = runMainSync(script, { debugJsOutput: true });
  expect(output).toEqual([2]);
});
