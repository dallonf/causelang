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

  const { result, output } = runMainSync(script);

  expect(result).toBeTruthy();
  expect(result).toEqual({
    type: expect.stringContaining('Card'),
    value: {
      suit: 'hearts',
      rank: 3,
    },
  });
  expect(output).toMatchInlineSnapshot(`
    Array [
      Object {
        "type": "debug",
        "value": Object {
          "type": "Card$main",
          "value": Object {
            "rank": 3,
            "suit": "hearts",
          },
        },
      },
    ]
  `);
});
