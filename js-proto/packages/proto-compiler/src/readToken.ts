import CompilerError from './CompilerError';
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

// TODO: also skip comments
export function skipWhitespace(
  cursor: SourceStream,
  { stopAtNewline } = { stopAtNewline: false }
): SourceStream {
  while (true) {
    const char = nextChar(cursor);
    if (!char) {
      return cursor;
    }
    if (stopAtNewline && char.char === '\n') {
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

export function expectWhitespace(
  cursor: SourceStream
): undefined | SourceStream {
  const char = nextChar(cursor);
  if (!char) return;
  if (!whitespaceRegex.test(char.char)) return;
  cursor = char.cursor;
  return skipWhitespace(cursor);
}

export function advanceLine(cursor: SourceStream) {
  cursor = skipWhitespace(cursor, { stopAtNewline: true });
  const newline = nextChar(cursor);
  if (!newline) {
    return cursor;
  }
  if (newline.char !== '\n') {
    throw new CompilerError(
      'I was expecting this to be the end of a line',
      cursor
    );
  }
  cursor = newline.cursor;
  return skipWhitespace(cursor);
}

export function consumeSequence(
  cursor: SourceStream,
  expected: string
): null | SourceStream {
  const expectedChars = [...expected];
  let readChars = 0;
  while (readChars < expectedChars.length) {
    const next = nextChar(cursor);
    if (next && next.char === expectedChars[readChars]) {
      readChars += 1;
      cursor = next.cursor;
    } else {
      return null;
    }
  }
  return cursor;
}

export function expectCursor(
  prevCursor: SourceStream,
  newCursor: SourceStream | null | undefined,
  error: string
): SourceStream {
  if (!newCursor) {
    throw new CompilerError(error, prevCursor);
  }
  return newCursor;
}

export function assertCursor(
  prevCursor: SourceStream,
  newCursor: SourceStream | null | undefined,
  error: string
): asserts newCursor is SourceStream {
  if (!newCursor) {
    throw new CompilerError(error, prevCursor);
  }
}
