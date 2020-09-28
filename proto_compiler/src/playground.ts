import * as path from 'path';
import * as fs from 'fs';
import * as ast from './ast';

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
  const root = {
    type: 'Module' as const,
    body: [],
  };

  return root;
};

const parseDeclaration = (source: string, ctx: Context) => {
  let cursor = 0;
};

const tryReadIdentifier = (source: string) => {};

const parsedAst = parseModule(source, {});

console.log(JSON.stringify(parsedAst, null, 2));

const identifierHeadRegex = new RegExp('(?:[a-zA-Z_]|' + ')');
