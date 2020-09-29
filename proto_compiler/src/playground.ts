import * as path from 'path';
import * as fs from 'fs';
import * as ast from './ast';
import {
  cursorPosition,
  makeSourceStream,
  nextChar,
  remainder,
  SourceStream,
} from './sourceStream';
import { advanceLine, readIdentifier, skipWhitespace } from './readToken';
import CompilerError from './CompilerError';

const sampleAst: ast.ASTRoot = {
  type: 'Module',
  body: [
    {
      type: 'FunctionDeclaration',
      id: {
        type: 'Identifier',
        name: 'main',
      },
      arguments: [],
      body: {
        type: 'BlockExpression',
        body: [
          {
            type: 'ExpressionStatement',
            expression: {
              type: 'UnaryCallExpression',
              callee: {
                type: 'Keyword',
                keyword: 'cause',
              },
              argument: {
                type: 'CallExpression',
                callee: {
                  type: 'Identifier',
                  name: 'Log',
                },
                arguments: [
                  {
                    type: 'StringLiteral',
                    value: 'Hello World',
                  },
                ],
              },
            },
          },
          {
            type: 'ExpressionStatement',
            expression: {
              type: 'CallExpression',
              callee: {
                type: 'Identifier',
                name: 'ExitCode',
              },
              arguments: [
                {
                  type: 'IntLiteral',
                  value: 0,
                },
              ],
            },
          },
        ],
      },
    },
  ],
};

interface Context {}

const source = fs.readFileSync(
  path.join(__dirname, 'fixtures/01_helloworld.cau'),
  'utf-8'
);

const parseModule = (cursor: SourceStream, ctx: Context): ast.Module => {
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
      `The next part of a function declaration should be a "(" to list the arguments. I found this instead: ${argOpenParen?.char}`,
      cursor
    );
  }
  cursor = argOpenParen.cursor;

  cursor = skipWhitespace(cursor);
  const argCloseParen = nextChar(cursor);
  if (!argCloseParen || argCloseParen.char !== ')') {
    throw new CompilerError(
      `The next part of a function declaration should be a ")" to close out the argument list. I found this instead: ${argCloseParen?.char}`,
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
    arguments: [],
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
    if (((readAttempt = parseStringLiteral(cursor, ctx)), readAttempt)) {
      initialExpression = readAttempt.result;
      cursor = readAttempt.cursor;
    } else if (((readAttempt = parseIntLiteral(cursor, ctx)), readAttempt)) {
      initialExpression = readAttempt.result;
      cursor = readAttempt.cursor;
    } else if (((readAttempt = readIdentifier(cursor)), readAttempt)) {
      if (ast.keywordSet.has(readAttempt.identifier as ast.KeywordLiteral)) {
        initialExpression = {
          type: 'Keyword' as const,
          keyword: readAttempt.identifier as ast.KeywordLiteral,
        };
      } else {
        initialExpression = {
          type: 'Identifier' as const,
          name: readAttempt.identifier,
        };
      }
      cursor = readAttempt.cursor;
    } else {
      return null;
    }

    // Look for suffixes that change the meaning of this initial expression
    const openBrace = nextChar(cursor);
    if (openBrace && openBrace.char === '(') {
      return parseCallExpression(initialExpression, cursor, ctx);
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

  let argument;
  while (
    ((cursor = skipWhitespace(cursor)),
    (argument = parseExpression(cursor, ctx)),
    argument)
  ) {
    args.push(argument.result);
    cursor = skipWhitespace(argument.cursor);

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
      'I\'m looking for a ")" to close the argument list',
      cursor
    );
  }
  cursor = closeBrace.cursor;

  return {
    result: {
      type: 'CallExpression',
      callee: callee,
      arguments: args,
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

    const expression = parseExpression(cursor, ctx);
    if (expression) {
      body.push({
        type: 'ExpressionStatement',
        expression: expression.result,
      });
      cursor = expression.cursor;
      cursor = advanceLine(cursor);
    } else {
      throw new CompilerError(
        "I'm looking for statements in this block.",
        cursor
      );
    }
  }

  return {
    result: {
      type: 'BlockExpression',
      body,
    },
    cursor,
  };
};

let parsedAst;
try {
  parsedAst = parseModule(makeSourceStream(source), {});
} catch (e) {
  if (e instanceof CompilerError) {
    const position = cursorPosition(e.cursor);
    console.error(
      `Compiler error at Line ${position.line}, Column ${position.column}`
    );
    console.error('Remainder of code:\n', remainder(e.cursor));
  }
  throw e;
}

console.log(JSON.stringify(parsedAst, null, 2));
