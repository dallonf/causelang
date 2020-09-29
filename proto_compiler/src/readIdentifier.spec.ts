import { SourceStream, makeSourceStream, nextChar } from './sourceStream';
import readIdentifier from './readIdentifier';

describe('readIdentifier', () => {
  const expectIdentifier = (identifier: string, suffix?: string) => {
    const fullString = identifier + (suffix ?? '');
    expect(readIdentifier(makeSourceStream(fullString))).toEqual({
      identifier,
      cursor: { source: fullString, index: [...identifier].length },
    });
  };

  it('should read a normal identifier', () => {
    expectIdentifier('fooBar');
  });

  it('should stop reading when it reaches whitespace', () => {
    expectIdentifier('fooBar', ' ');
  });

  it('should stop reading when it reaches a symbol', () => {
    expectIdentifier('foo', '+bar');
  });

  it('should skip whitespace', () => {
    const identifier = 'fooBar';
    const testString = '   ' + identifier + '(';
    const result = readIdentifier(makeSourceStream(testString));
    expect(result?.identifier).toEqual(identifier);
    expect(result && nextChar(result.cursor)?.char).toBe('(');
  });

  it('should not recognize numbers at the start of an identifier', () => {
    expect(readIdentifier(makeSourceStream('2cool4u'))).toEqual(null);
  });
});
