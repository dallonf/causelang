import * as path from 'path';
import * as fs from 'fs';
import * as ast from './ast';
import parseIdentifier from './parseIdentifier';

const sampleAst: ast.ASTRoot = {
  type: 'Module',
  body: [
    {
      type: 'FunctionDeclaration',
      id: {
        type: 'Identifier',
        name: 'main',
      },
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

const parseModule = (source: string, ctx: Context): ast.Module => {
  let cursor = 0;
  const root: ast.Module = {
    type: 'Module',
    body: [],
  };

  while (cursor < source.length) {
    const [charsRead, declaration] = parseDeclaration(
      source.slice(cursor),
      ctx
    );
    if (declaration) {
      cursor += charsRead;
      root.body.push(declaration);
    } else {
      console.log(
        'Not sure what to do with the rest of the source file',
        `\`${source.slice(cursor + 1)}\``
      );
      break;
    }
  }

  return root;
};

const parseDeclaration = (
  source: string,
  ctx: Context
): [charsRead: number, declaration: null | ast.Declaration] => {
  const [idCursor, identifier] = parseIdentifier(source);
  if (identifier) {
    if (identifier === 'fn') {
      const [newCursor, fnDefinition] = parseFunctionDeclaration(
        source.slice(idCursor + 1),
        ctx
      );
      return [idCursor + newCursor, fnDefinition];
    } else {
      // throw new Error(
      //   `Expected a declaration here, but I got ${identifier} instead!`
      // );
      return [0, null];
    }
  } else {
    return [0, null];
  }
};

const parseFunctionDeclaration = (
  source: string,
  ctx: Context
): [charsRead: number, result: ast.FunctionDeclaration] => {
  return [
    0,
    {
      type: 'FunctionDeclaration',
      id: {
        type: 'Identifier',
        name: 'TMP',
      },
      body: {
        type: 'Identifier',
        name: 'TMP',
      },
    },
  ];
};

const parsedAst = parseModule(source, {});

console.log(JSON.stringify(parsedAst, null, 2));
