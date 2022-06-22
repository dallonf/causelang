import { runMainSync } from './testRunner';

it('changes the value of a variable', () => {
  const script = `
    fn main() {
      let var x = 1
      x = 2
      cause Print(x)
    }
  `;

  const { output } = runMainSync(script);
  expect(output).toEqual([2]);
});

it('cannot set a name that is not "var"', () => {
  const script = `
    fn main() {
      let x = 1
      x = 2
    }
  `;

  expect(() => runMainSync(script)).toThrowErrorMatchingInlineSnapshot(
    `"x isn't a variable; it's just a name. You could try adding var to its declaration: \\"let var x...\\""`
  );
});

it('cannot update a variable in a closure', () => {
  const script = `
    fn main() {
      let var x = 1

      let update = fn() {
        x = 2
      }

      update()
    }
  `;

  expect(() =>
    runMainSync(script, { debugJsOutput: true })
  ).toThrowErrorMatchingInlineSnapshot(
    `"I can't find a variable called x in the current scope."`
  );
});
