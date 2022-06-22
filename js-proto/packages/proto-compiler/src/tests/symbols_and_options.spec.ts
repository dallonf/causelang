import { runMainSync } from './testRunner';

it('defines symbol types', () => {
  const script = `
    symbol One
    symbol Two
    symbol Three

    fn main() {
      let one = One
      let two = Two
      let three = Three

      cause Debug(three)
      two
    }
  `;

  const { result, output } = runMainSync(script);

  expect(result).toBeTruthy();
  expect(result).toEqual({
    type: '$symbol',
    id: expect.stringContaining('Two'),
  });
  expect(output).toMatchInlineSnapshot(`
    Array [
      Object {
        "type": "debug",
        "value": Object {
          "id": "Three",
          "type": "$symbol",
        },
      },
    ]
  `);
});

it('defines option types', () => {
  const script = `
    symbol Hearts
    symbol Diamonds
    symbol Clubs
    symbol Spades

    option Suit(
      Hearts,
      Diamonds,
      Clubs,
      Spades
    )

    fn main() {
      let cardSuit: Suit = Hearts
      cardSuit
    }
  `;

  const { result } = runMainSync(script);
  expect(result).toMatchInlineSnapshot(`
    Object {
      "id": "Hearts",
      "type": "$symbol",
    }
  `);
});

it.skip('defines option types with shorthand symbols/types', () => {
  const script = `
    option MaybeInt(
      symbol None,
      type Some(value: Int)
    )

    fn main() {
      let value = MaybeInt.Some(4)
      value
    }
  `;

  const { result } = runMainSync(script);
  expect(result).toMatchInlineSnapshot();
});
