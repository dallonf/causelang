import { SourceStream, nextChar } from './sourceStream';

// TODO: support emoji and foreign language characters
//
// see:
// https://docs.swift.org/swift-book/ReferenceManual/LexicalStructure.html#grammar_identifier
// https://docs.swift.org/swift-book/LanguageGuide/TheBasics.html#ID313
// https://www.regextester.com/106421
// https://www.npmjs.com/package/grapheme-splitter

const whitespaceRegex = new RegExp('^\\s+$');
const identifierHeadRegex = new RegExp('^[a-zA-Z_]$');
const identifierAdditionalCharacterRegex = new RegExp('[0-9]');

export function readIdentifier(
  cursor: SourceStream
): null | { identifier: string; cursor: SourceStream } {
  while (true) {
    const char = nextChar(cursor);
    if (!char) {
      return null;
    }
    if (whitespaceRegex.test(char.char)) {
      cursor = char.cursor;
    } else {
      break;
    }
  }

  let headChar = nextChar(cursor);
  let identifierChars = [];
  if (headChar && identifierHeadRegex.test(headChar.char)) {
    identifierChars.push(headChar.char);
    cursor = headChar.cursor;
    let currentChar;
    while (((currentChar = nextChar(cursor)), currentChar)) {
      if (
        identifierHeadRegex.test(currentChar.char) ||
        identifierAdditionalCharacterRegex.test(currentChar.char)
      ) {
        identifierChars.push(currentChar.char);
        cursor = currentChar.cursor;
      } else {
        break;
      }
    }
    return {
      cursor,
      identifier: identifierChars.join(''),
    };
  }

  return null;
}

export function skipWhitespace(cursor: SourceStream): SourceStream {
  while (true) {
    const char = nextChar(cursor);
    if (!char) {
      return cursor;
    }
    if (whitespaceRegex.test(char.char)) {
      cursor = char.cursor;
    } else {
      break;
    }
  }
  return cursor;
}
