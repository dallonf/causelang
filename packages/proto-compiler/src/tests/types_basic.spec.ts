import { runMainSync } from './testRunner';

it('is an error to assign an Integer to a String type', () => {
  const script = `
    fn main(): String {
      let bad: String = 5
      bad
    }
  `;
  try {
    runMainSync(script);
  } catch (err) {
    expect(err.message).toMatch(/String/);
    expect(err.message).toMatchInlineSnapshot();
    return;
  }
  throw new Error('Should not have succeeded');
});
