import * as ast from './ast';
import { nextChar, SourceStream } from './sourceStream';
import {
  advanceLine,
  expectCursor,
  consumeSequence,
  expectWhitespace,
  readIdentifier,
  skipWhitespace,
  assertCursor,
} from './readToken';
import CompilerError from './CompilerError';

export interface Context {}

export const parseModule = (cursor: SourceStream, ctx: Context): ast.Module => {
  const root: ast.Module = {
    type: 'Module',
    body: [],
  };

  let current;
  while (
    ((cursor = skipWhitespace(cursor)), (current = nextChar(cursor)), current)
  ) {
    const declaration = parseDeclaration(cursor, ctx);
    if (declaration) {
      cursor = declaration.cursor;
      root.body.push(declaration.result);
    } else {
      throw new CompilerError(
        'I\'m looking for declarations here in the root of the module; stuff like "let", "fn", etc.',
        cursor
      );
    }
  }

  return root;
};

const parseDeclaration = (
  cursor: SourceStream,
  ctx: Context
): null | { result: ast.Declaration; cursor: SourceStream } => {
  const keyword = readIdentifier(cursor);
  if (keyword) {
    if (keyword.identifier === 'fn') {
      return parseFunctionDeclaration(keyword.cursor, ctx);
    }
  }
  return null;
};

const parseFunctionDeclaration = (
  cursor: SourceStream,
  ctx: Context
): { result: ast.FunctionDeclaration; cursor: SourceStream } => {
  const idRead = readIdentifier(cursor);
  if (!idRead) {
    throw new CompilerError(
      'I\'m looking for a function name after the "fn" declaration, but I can\'t find one',
      cursor
    );
  }
  cursor = idRead.cursor;

  cursor = skipWhitespace(cursor);
  const argOpenParen = nextChar(cursor);
  if (!argOpenParen || argOpenParen.char !== '(') {
    throw new CompilerError(
      `The next part of a function declaration should be a "(" to list the parameters. I found this instead: ${argOpenParen?.char}`,
      cursor
    );
  }
  cursor = argOpenParen.cursor;

  cursor = skipWhitespace(cursor);
  const argCloseParen = nextChar(cursor);
  if (!argCloseParen || argCloseParen.char !== ')') {
    throw new CompilerError(
      `The next part of a function declaration should be a ")" to close out the parameter list. I found this instead: ${argCloseParen?.char}`,
      cursor
    );
  }
  cursor = argCloseParen.cursor;

  const body = parseExpression(cursor, ctx);
  if (!body) {
    throw new CompilerError(
      `Your function body should be an expression, like a block: {}`,
      cursor
    );
  }
  cursor = body.cursor;

  const result: ast.FunctionDeclaration = {
    type: 'FunctionDeclaration',
    id: {
      type: 'Identifier',
      name: idRead.identifier,
    },
    parameters: [],
    body: body.result,
  };

  return { result, cursor };
};

const parseExpression = (
  cursor: SourceStream,
  ctx: Context
): null | { result: ast.Expression; cursor: SourceStream } => {
  cursor = skipWhitespace(cursor);
  const char = nextChar(cursor);
  if (!char) return null;

  if (char.char === '{') {
    return parseBlockExpression(cursor, ctx);
  } else {
    let initialExpression;
    let readAttempt;
    if (
      ((readAttempt = parsePrefixOperatorExpression(cursor, ctx)), readAttempt)
    ) {
      initialExpression = readAttempt.result;
      cursor = readAttempt.cursor;
    } else if (((readAttempt = parseStringLiteral(cursor, ctx)), readAttempt)) {
      initialExpression = readAttempt.result;
      cursor = readAttempt.cursor;
    } else if (((readAttempt = parseIntLiteral(cursor, ctx)), readAttempt)) {
      initialExpression = readAttempt.result;
      cursor = readAttempt.cursor;
    } else if (
      ((readAttempt = readIdentifier(cursor)),
      readAttempt &&
        !ast.keywordSet.has(readAttempt.identifier as ast.KeywordValue))
    ) {
      initialExpression = {
        type: 'Identifier' as const,
        name: readAttempt!.identifier,
      };
      cursor = readAttempt!.cursor;
    } else {
      return null;
    }

    // Look for suffixes that change the meaning of this initial expression
    const suffixStart = nextChar(cursor);
    if (suffixStart) {
      if (suffixStart.char === '(') {
        return parseCallExpression(initialExpression, cursor, ctx);
      }
    }

    return { result: initialExpression, cursor };
  }
};

const parseStringLiteral = (
  cursor: SourceStream,
  ctx: Context
): null | { result: ast.StringLiteral; cursor: SourceStream } => {
  const openingQuote = nextChar(cursor);
  if (!openingQuote || openingQuote.char !== '"') return null;
  cursor = openingQuote.cursor;

  const stringChars = [];
  while (true) {
    const char = nextChar(cursor);
    if (!char) {
      throw new CompilerError(
        'This string literal never ended; are you missing an end quote (") somewhere?',
        cursor
      );
    }
    cursor = char.cursor;

    // TODO: escaping with \"
    if (char.char === '"') break;

    stringChars.push(char.char);
  }

  return {
    cursor,
    result: {
      type: 'StringLiteral',
      value: stringChars.join(''),
    },
  };
};

const parseIntLiteral = (
  cursor: SourceStream,
  ctx: Context
): null | { result: ast.IntLiteral; cursor: SourceStream } => {
  // TODO: negative numbers with minus signs

  const intChars = [];
  let char;
  while (((char = nextChar(cursor)), char && char.char.match(/[0-9]/))) {
    intChars.push(char!.char);
    cursor = char!.cursor;
  }
  if (intChars.length === 0) {
    return null;
  }

  const int = parseInt(intChars.join(''), 10);
  return {
    cursor,
    result: {
      type: 'IntLiteral',
      value: int,
    },
  };
};

const prefixOperators = new Set(['cause'] as const);
type PrefixSupportedOperator = typeof prefixOperators extends Set<infer T>
  ? T
  : never;
const parsePrefixOperatorExpression = (
  cursor: SourceStream,
  ctx: Context
): null | { result: ast.PrefixOperatorExpression; cursor: SourceStream } => {
  const keyword = readIdentifier(cursor);
  if (
    keyword &&
    prefixOperators.has(keyword.identifier as PrefixSupportedOperator)
  ) {
    cursor = keyword.cursor;
    let tmp;
    if (((tmp = consumeSequence(cursor, ' ')), !tmp)) {
      return null;
    }
    cursor = tmp;
    skipWhitespace(cursor);

    let expression = parseExpression(cursor, ctx);
    if (expression) {
      return {
        result: {
          type: 'PrefixOperatorExpression',
          operator: {
            type: 'Keyword',
            keyword: keyword.identifier as PrefixSupportedOperator,
          },
          expression: expression.result,
        },
        cursor: expression.cursor,
      };
    }
  }
  return null;
};

const parseCallExpression = (
  callee: ast.Expression,
  cursor: SourceStream,
  ctx: Context
): {
  result: ast.CallExpression;
  cursor: SourceStream;
} => {
  const openBrace = nextChar(cursor);
  if (!openBrace || openBrace.char !== '(') {
    throw new CompilerError(
      "I'm confused; I'm looking for a function call, but I don't even see a starting \"(\". This probably isn't your fault!",
      cursor
    );
  }
  cursor = openBrace.cursor;

  const args: ast.Expression[] = [];

  let parameter;
  while (
    ((cursor = skipWhitespace(cursor)),
    (parameter = parseExpression(cursor, ctx)),
    parameter)
  ) {
    args.push(parameter.result);
    cursor = skipWhitespace(parameter.cursor);

    const comma = nextChar(cursor);
    if (!comma || comma.char !== ',') {
      break;
    } else {
      cursor = comma.cursor;
    }
  }

  const closeBrace = nextChar(cursor);
  if (!closeBrace || closeBrace.char !== ')') {
    throw new CompilerError(
      'I\'m looking for a ")" to close the parameter list',
      cursor
    );
  }
  cursor = closeBrace.cursor;

  return {
    result: {
      type: 'CallExpression',
      callee: callee,
      parameters: args,
    },
    cursor,
  };
};

const parseBlockExpression = (
  cursor: SourceStream,
  ctx: Context
): { result: ast.BlockExpression; cursor: SourceStream } => {
  cursor = skipWhitespace(cursor);
  const openBrace = nextChar(cursor);
  if (!openBrace || openBrace.char !== '{') {
    throw new CompilerError(
      "I'm confused; I'm looking for a block expression, but I don't even see a starting \"{\". This probably isn't your fault!",
      cursor
    );
  }
  cursor = openBrace.cursor;

  const body: ast.Statement[] = [];

  let current;
  while (
    ((cursor = skipWhitespace(cursor)), (current = nextChar(cursor)), current)
  ) {
    if (current.char === '}') {
      cursor = current.cursor;
      break;
    }

    const statement = parseStatement(cursor, ctx);
    if (statement) {
      body.push(statement.result);
      cursor = advanceLine(statement.cursor);
    } else {
      throw new CompilerError(
        "I'm looking for statements in this block.",
        cursor
      );
    }
  }
  if (!current) {
    throw new CompilerError(
      'I\'m looking for a "}" to close this block',
      cursor
    );
  }

  cursor = skipWhitespace(cursor, { stopAtNewline: true });

  const handlers: ast.HandlerBlockSuffix[] = [];
  const handleSuffixKeywordCursor = consumeSequence(cursor, 'handle');
  if (handleSuffixKeywordCursor) {
    cursor = handleSuffixKeywordCursor;
    cursor = skipWhitespace(cursor, { stopAtNewline: true });
    cursor = expectCursor(
      cursor,
      consumeSequence(cursor, '=>'),
      'I\'m looking for an arrow ("=>") to seperate the handler match pattern from the handler itself.'
    );
    cursor = skipWhitespace(cursor);

    const bodyExpression = parseExpression(cursor, ctx);
    assertCursor(
      cursor,
      bodyExpression?.cursor,
      "I'm looking for an expression to handle the effect."
    );
    cursor = bodyExpression.cursor;
    handlers.push({
      type: 'HandlerBlockSuffix',
      body: bodyExpression.result,
    });
  }
  // TODO: multiple handlers

  return {
    result: {
      type: 'BlockExpression',
      body,
      handlers: handlers.length ? handlers : undefined,
    },
    cursor,
  };
};

const parseStatement = (
  cursor: SourceStream,
  ctx: Context
): undefined | { result: ast.Statement; cursor: SourceStream } => {
  const nameDeclaration = parseNameDeclarationStatement(cursor, ctx);
  if (nameDeclaration) {
    return nameDeclaration;
  }

  const expression = parseExpression(cursor, ctx);
  if (expression) {
    return {
      result: {
        type: 'ExpressionStatement',
        expression: expression.result,
      },
      cursor: expression.cursor,
    };
  }
};

const parseNameDeclarationStatement = (
  cursor: SourceStream,
  ctx: Context
):
  | undefined
  | { result: ast.NameDeclarationStatement; cursor: SourceStream } => {
  let tmp;
  tmp = consumeSequence(cursor, 'let');
  if (!tmp) return;
  cursor = tmp;
  tmp = expectWhitespace(cursor);
  if (!tmp) return;
  cursor = tmp;

  const name = readIdentifier(cursor);
  if (!name)
    throw new CompilerError('I was expected to see a name after "let"', cursor);
  cursor = name.cursor;

  cursor = skipWhitespace(cursor);
  tmp = consumeSequence(cursor, '=');
  if (!tmp) throw new CompilerError('I was expecting to see an "="', cursor);
  cursor = tmp;

  cursor = skipWhitespace(cursor);
  const expression = parseExpression(cursor, ctx);
  if (!expression)
    throw new CompilerError(
      `I was expecting an expression after "let ${name.identifier} ="`,
      cursor
    );
  cursor = expression.cursor;

  return {
    result: {
      type: 'NameDeclarationStatement',
      name: { type: 'Identifier', name: name.identifier },
      value: expression.result,
    },
    cursor,
  };
};
