export interface SourceStream {
  readonly source: string;
  readonly index: number;
}

export interface SourceStreamValue {
  readonly char: string;
  readonly cursor: SourceStream;
}

export const makeSourceStream = (source: string): SourceStream => {
  return {
    source,
    index: 0,
  };
};

export const nextChar = (cursor: SourceStream): null | SourceStreamValue => {
  const codePoint = cursor.source.codePointAt(cursor.index);
  if (codePoint) {
    const char = String.fromCodePoint(codePoint);
    const newCursor = {
      source: cursor.source,
      index: cursor.index + char.length,
    };
    return {
      char,
      cursor: newCursor,
    };
  } else {
    return null;
  }
};

export const remainder = (cursor: SourceStream): string => {
  return cursor.source.slice(cursor.index);
};

export const cursorPosition = (
  cursor: SourceStream
): { line: number; column: number } => {
  const before = cursor.source.slice(0, cursor.index);
  const lines = [...before.matchAll(/\n/g)].length;
  const lastLineIndex = before.lastIndexOf('\n');
  const lastLine = before.slice(lastLineIndex);

  return {
    line: lines + 1,
    column: [...lastLine].length,
  };
};
