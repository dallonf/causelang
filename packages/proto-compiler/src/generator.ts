import generate from '@babel/generator';
import * as jsAst from '@babel/types';
import * as ast from './ast';
import * as analyzer from './analyzer';
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
    case 'CallExpression':
    case 'UnaryCallExpression':
      return generateCallExpression(node, breadcrumbs, ctx);
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

const generateCallExpression = (
  node: ast.CallExpression | ast.UnaryCallExpression,
  breadcrumbs: Breadcrumbs,
  ctx: GeneratorContext
): jsAst.Expression => {
  let parameters: { node: ast.Expression; breadcrumbs: Breadcrumbs }[];
  if (node.type === 'UnaryCallExpression') {
    parameters = [
      { node: node.parameter, breadcrumbs: [...breadcrumbs, 'parameter'] },
    ];
  } else {
    parameters = node.parameters.map((a, i) => ({
      node: a,
      breadcrumbs: [...breadcrumbs, 'parameters', i],
    }));
  }

  if (node.callee.type === 'Keyword' && node.callee.keyword === 'cause') {
    if (parameters.length !== 1) {
      throw new Error('"cause" should only have one parameter');
    }

    return jsAst.yieldExpression(
      generateExpression(parameters[0].node, parameters[0].breadcrumbs, ctx)
    );
  }

  const type = ctx.expressionTypes.get([...breadcrumbs, 'callee'].join('.'));
  if (!type) {
    throw new Error(
      `I'm confused. I'm trying to figure out the type of this function call, but I don't know what it is. This probably isn't your fault! Here's the technical breadcrumb to the call in question: ${breadcrumbs.join(
        '.'
      )}`
    );
  }
  if (type.kind === 'effect' || type.kind === 'type') {
    if (parameters.length !== 1) {
      throw new Error('Effects and types can only have one parameter for now');
    }
    return jsAst.objectExpression([
      jsAst.objectProperty(
        jsAst.identifier('type'),
        generateExpression(node.callee, [...breadcrumbs, 'callee'], ctx)
      ),
      jsAst.objectProperty(
        jsAst.identifier('value'),
        generateExpression(parameters[0].node, parameters[0].breadcrumbs, ctx)
      ),
    ]);
  } else if (type.kind === 'fn') {
    return jsAst.yieldExpression(
      jsAst.callExpression(
        generateExpression(node.callee, [...breadcrumbs, 'callee'], ctx),
        parameters.map((a) => generateExpression(a.node, a.breadcrumbs, ctx))
      ),
      true
    );
  } else {
    throw new Error(
      `I don't know how to compile this kind of function call yet. The type of the callee is ${JSON.stringify(
        type
      )}`
    );
  }
};
