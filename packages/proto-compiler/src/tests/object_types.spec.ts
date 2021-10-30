import { runMainSync } from './testRunner';

it('Can define a type and instantiate it', () => {
  const script = `
    type Card(
      suit: String,
      rank: Int,
    )

    fn main() {
      let card = Card("hearts", 3)
      cause Debug(card)
      card
    }
  `;

  const { result, output } = runMainSync(script, {
    debugJsOutput: true,
  });

  expect(result).toBeTruthy();
  expect(result).toMatchInlineSnapshot();
  expect(output).toMatchInlineSnapshot();
});
