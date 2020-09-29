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
    return {
      char,
      cursor: {
        source: cursor.source,
        index: cursor.index + char.length,
      },
    };
  } else {
    return null;
  }
};
