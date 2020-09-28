import { Identifier } from './ast';

// TODO: support emoji and foreign language characters
//
// see:
// https://docs.swift.org/swift-book/ReferenceManual/LexicalStructure.html#grammar_identifier
// https://docs.swift.org/swift-book/LanguageGuide/TheBasics.html#ID313
// https://www.regextester.com/106421

const whitespaceRegex = new RegExp('^\\s*');
const identifierHeadRegex = new RegExp('[a-zA-Z_]');
const identifierAdditionalCharacterRegex = new RegExp('[0-9]');

export default function parseIdentifier(
  source: string
): [charsRead: number, identifier: string | null] {
  let cursor = 0;
  let startPoint = 0;

  const leadingWhitespace = whitespaceRegex.exec(source);
  if (leadingWhitespace) {
    startPoint = cursor = leadingWhitespace[0].length;
  }

  if (identifierHeadRegex.test(source.charAt(cursor))) {
    cursor++;
    while (cursor < source.length) {
      const char = source[cursor];
      if (
        identifierHeadRegex.test(char) ||
        identifierAdditionalCharacterRegex.test(char)
      ) {
        cursor++;
      } else {
        break;
      }
    }
    return [cursor, source.slice(startPoint, cursor)];
  }

  return [0, null];
}
