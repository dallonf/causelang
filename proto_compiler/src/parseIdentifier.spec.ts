import { parse } from 'path';
import parseIdentifier from './parseIdentifier';

describe('parseIdentifier', () => {
  const expectIdentifier = (identifier: string, suffix?: string) => {
    expect(parseIdentifier(identifier + (suffix ?? ''))).toEqual([
      identifier.length,
      identifier,
    ]);
  };

  it('should parse a normal identifier', () => {
    expectIdentifier('fooBar');
  });

  it('should stop parsing when it reaches whitespace', () => {
    expectIdentifier('fooBar', ' ');
  });

  it('should stop parsing when it reaches a symbol', () => {
    expectIdentifier('foo', '+bar');
  });

  it('should skip whitespace', () => {
    const identifier = 'fooBar';
    const testString = '   ' + identifier;
    expect(parseIdentifier(testString)).toEqual([
      testString.length,
      identifier,
    ]);
  });

  it('should not recognize numbers at the start of an identifier', () => {
    expect(parseIdentifier('2cool4u')).toEqual([0, null]);
  });
});
