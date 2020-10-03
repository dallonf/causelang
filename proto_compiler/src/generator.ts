import * as path from 'path';
import * as fs from 'fs';
import generate from '@babel/generator';
import * as jsAst from '@babel/types';
import * as ast from './ast';
import * as analyzer from './analyzer';
import { cursorPosition, makeSourceStream, remainder } from './sourceStream';
import CompilerError from './CompilerError';
import { parseModule } from './parser';
import { exhaustiveCheck } from './utils';

type Breadcrumbs = analyzer.Breadcrumbs;

interface GeneratorContext {
  expressionTypes: Map<string, analyzer.ValueType>;
}

export const generateModule = (
  module: ast.Module,
  breadcrumbs: Breadcrumbs,
  ctx: GeneratorContext
) => {
  const program = jsAst.program([]);

  const statements = module.body.map((a, i) =>
    generateDeclaration(a, [...breadcrumbs, 'body', i], ctx)
  );
  program.body.push(...statements);

  // runtime call to invoke the main function
  const entryExpression = jsAst.expressionStatement(
    jsAst.callExpression(
      jsAst.memberExpression(
        jsAst.identifier('CauseRuntime'),
        jsAst.identifier('invokeEntry')
      ),
      [jsAst.identifier('main')]
    )
  );
  program.body.push(entryExpression);

  return generate(program).code;
};

const generateDeclaration = (
  node: ast.FunctionDeclaration,
  breadcrumbs: Breadcrumbs,
  ctx: GeneratorContext
) => {
  let bodyStatements;
  if (node.body.type === 'BlockExpression') {
    const cauStatements = node.body.body;
    bodyStatements = cauStatements.map((a, i) => {
      const statement = generateStatement(
        a,
        [...breadcrumbs, 'body', 'body', i],
        ctx
      );
      if (
        statement.type === 'ExpressionStatement' &&
        i === cauStatements.length - 1
      ) {
        return jsAst.returnStatement(statement.expression);
      } else {
        return statement;
      }
    });
  } else {
    bodyStatements = [
      jsAst.returnStatement(
        generateExpression(node.body, [...breadcrumbs, 'body'], ctx)
      ),
    ];
  }

  return jsAst.functionDeclaration(
    jsAst.identifier(node.id.name),
    [],
    jsAst.blockStatement(bodyStatements),
    true
  );
};

const generateStatement = (
  node: ast.Statement,
  breadcrumbs: Breadcrumbs,
  ctx: GeneratorContext
) => {
  return jsAst.expressionStatement(
    generateExpression(node.expression, [...breadcrumbs, 'expression'], ctx)
  );
};

const generateExpression = (
  node: ast.Expression,
  breadcrumbs: Breadcrumbs,
  ctx: GeneratorContext
): jsAst.Expression => {
  switch (node.type) {
    case 'Identifier': {
      return jsAst.identifier(node.name);
    }
    case 'StringLiteral': {
      return jsAst.stringLiteral(node.value);
    }
    case 'IntLiteral': {
      return jsAst.numericLiteral(0);
    }
    case 'CallExpression': {
      if (node.callee.type === 'Keyword' && node.callee.keyword === 'cause') {
        if (node.parameters.length !== 1) {
          throw new Error('"cause" should only have one parameter');
        }

        return jsAst.yieldExpression(
          generateExpression(
            node.parameters[0],
            [...breadcrumbs, 'parameters', 0],
            ctx
          )
        );
      }

      const type = ctx.expressionTypes.get(
        [...breadcrumbs, 'callee'].join('.')
      );
      if (!type) {
        throw new Error(
          `I'm confused. I'm trying to figure out the type of this function call, but I don't know what it is. This probably isn't your fault! Here's the technical breadcrumb to the call in question: ${breadcrumbs.join(
            '.'
          )}`
        );
      }
      if (type.kind === 'effect') {
        if (node.parameters.length !== 1) {
          throw new Error('Effects can only have one parameter for now');
        }
        return jsAst.objectExpression([
          jsAst.objectProperty(
            jsAst.identifier('type'),
            // TODO: The type name might be out of scope...
            jsAst.identifier(type.name)
          ),
          jsAst.objectProperty(
            jsAst.identifier('value'),
            generateExpression(
              node.parameters[0],
              [...breadcrumbs, 'parameters', 0],
              ctx
            )
          ),
        ]);
      }

      return jsAst.callExpression(
        generateExpression(node.callee, [...breadcrumbs, 'callee'], ctx),
        node.parameters.map((a, i) =>
          generateExpression(a, [...breadcrumbs, 'parameters', i], ctx)
        )
      );
    }
    case 'BlockExpression': {
      throw new Error(
        "I don't know how to compile inline block expressions yet"
      );
    }
    case 'Keyword': {
      throw new Error(
        `${node.keyword} is a keyword, which you can't use here; if this is a variable, try renaming it`
      );
    }
    default:
      return exhaustiveCheck(node);
  }
};
