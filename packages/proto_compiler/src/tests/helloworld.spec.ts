import { runMain } from './testRunner';

describe('simple Hello World script', () => {
  const script = ` 
  fn main() {
    cause(Log("Hello World"))
  }
  `;

  it('logs hello world', async () => {
    const { result, logs } = await runMain(script);

    expect(result).toBe(undefined);
    expect(logs).toEqual(['Hello World']);
  });
});
