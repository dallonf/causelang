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
