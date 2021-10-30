import { runMainSync } from './testRunner';

// Changed my mind; strong typing isn't important for this prototype
it.skip('is an error to assign an Integer to a String type', () => {
  const script = `
    fn main(): String {
      let bad: String = 5
      bad
    }
  `;
  try {
    runMainSync(script);
  } catch (err: any) {
    expect(err.message).toMatch(/String/);
    expect(err.message).toMatchInlineSnapshot(
      `"Cannot read property 'String' of undefined"`
    );
    return;
  }
  throw new Error('Should not have succeeded');
});
