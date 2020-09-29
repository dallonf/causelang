import * as path from 'path';
import * as fs from 'fs';
import * as ast from './ast';
import { makeSourceStream, nextChar, SourceStream } from './sourceStream';
import { readIdentifier, skipWhitespace } from './readToken';
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
  while (((current = nextChar(cursor)), current)) {
    const declaration = parseDeclaration(cursor, ctx);
    if (declaration) {
      cursor = declaration.cursor;
      root.body.push(declaration.result);
    } else {
      console.log(
        'Not sure what to do with the rest of the source file',
        `\`${cursor.source.slice(cursor.index)}\``
      );
      break;
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

  const result: ast.FunctionDeclaration = {
    type: 'FunctionDeclaration',
    id: {
      type: 'Identifier',
      name: idRead.identifier,
    },
    arguments: [],
    body: {
      type: 'Identifier',
      name: 'TMP',
    },
  };

  return { result, cursor };
};

const parsedAst = parseModule(makeSourceStream(source), {});

console.log(JSON.stringify(parsedAst, null, 2));
