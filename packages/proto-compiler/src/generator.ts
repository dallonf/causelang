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
  if (node.body.type === 'BlockExpression' && !node.body.handlers) {
    bodyStatements = generateBlockExpressionStatements(
      node.body,
      [...breadcrumbs, 'body'],
      ctx
    );
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
  switch (node.type) {
    case 'NameDeclarationStatement':
      return jsAst.variableDeclaration(node.variable ? 'let' : 'const', [
        jsAst.variableDeclarator(
          jsAst.identifier(node.name.name),
          generateExpression(node.value, [...breadcrumbs, 'value'], ctx)
        ),
      ]);
    case 'ExpressionStatement':
      return jsAst.expressionStatement(
        generateExpression(node.expression, [...breadcrumbs, 'expression'], ctx)
      );
    case 'AssignmentStatement': {
      return jsAst.expressionStatement(
        jsAst.assignmentExpression(
          '=',
          jsAst.identifier(node.name.name),
          generateExpression(node.value, [...breadcrumbs, 'value'], ctx)
        )
      );
    }
    default:
      return exhaustiveCheck(node);
  }
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
      return jsAst.numericLiteral(node.value);
    }
    case 'CallExpression':
      return generateCallExpression(node, breadcrumbs, ctx);
    case 'BlockExpression': {
      return generateBlockExpression(node, breadcrumbs, ctx);
    }
    case 'PrefixOperatorExpression':
      if (node.operator.keyword === 'cause') {
        return jsAst.yieldExpression(
          generateExpression(
            node.expression,
            [...breadcrumbs, 'expression'],
            ctx
          )
        );
      } else {
        throw new Error(
          `I'm not sure how to compile a ${node.operator.keyword} operator here`
        );
      }
    case 'MemberExpression':
      return jsAst.memberExpression(
        generateExpression(node.object, [...breadcrumbs, 'object'], ctx),
        jsAst.identifier(node.property.name)
      );
    default:
      return exhaustiveCheck(node);
  }
};

const generateCallExpression = (
  node: ast.CallExpression,
  breadcrumbs: Breadcrumbs,
  ctx: GeneratorContext
): jsAst.Expression => {
  const type = ctx.expressionTypes.get([...breadcrumbs, 'callee'].join('.'));
  if (!type) {
    throw new Error(
      `I'm confused. I'm trying to figure out the type of this function call, but I don't know what it is. This probably isn't your fault! Here's the technical breadcrumb to the call in question: ${breadcrumbs.join(
        '.'
      )}`
    );
  }
  if (type.kind === 'effect' || type.kind === 'type') {
    if (node.parameters.length > 1) {
      throw new Error('Effects and types can only have one parameter for now');
    }
    return jsAst.objectExpression([
      jsAst.objectProperty(
        jsAst.identifier('type'),
        generateExpression(node.callee, [...breadcrumbs, 'callee'], ctx)
      ),
      ...(node.parameters.length
        ? [
            jsAst.objectProperty(
              jsAst.identifier('value'),
              generateExpression(
                node.parameters[0],
                [...breadcrumbs, 'parameters', 0],
                ctx
              )
            ),
          ]
        : []),
    ]);
  } else if (type.kind === 'fn') {
    return jsAst.yieldExpression(
      jsAst.callExpression(
        generateExpression(node.callee, [...breadcrumbs, 'callee'], ctx),
        node.parameters.map((a, i) =>
          generateExpression(a, [...breadcrumbs, 'parameters', i], ctx)
        )
      ),
      true
    );
  } else if (type.kind === 'coreFn') {
    return jsAst.callExpression(
      generateExpression(node.callee, [...breadcrumbs, 'callee'], ctx),
      node.parameters.map((a, i) =>
        generateExpression(a, [...breadcrumbs, 'parameters', i], ctx)
      )
    );
  } else {
    throw new Error(
      `I don't know how to compile this kind of function call yet. The type of the callee is ${JSON.stringify(
        type
      )}`
    );
  }
};
function generateBlockExpressionStatements(
  node: ast.BlockExpression,
  breadcrumbs: analyzer.Breadcrumbs,
  ctx: GeneratorContext
) {
  const cauStatements = node.body;
  return cauStatements.map((a, i) => {
    const statement = generateStatement(a, [...breadcrumbs, 'body', i], ctx);
    if (
      statement.type === 'ExpressionStatement' &&
      i === cauStatements.length - 1
    ) {
      return jsAst.returnStatement(statement.expression);
    } else {
      return statement;
    }
  });
}

function generateBlockExpression(
  node: ast.BlockExpression,
  breadcrumbs: Breadcrumbs,
  ctx: GeneratorContext
) {
  const iife = jsAst.callExpression(
    jsAst.functionExpression(
      null,
      [],
      jsAst.blockStatement(
        generateBlockExpressionStatements(node, breadcrumbs, ctx)
      ),
      true
    ),
    []
  );

  if (node.handlers?.length) {
    const handlers = node.handlers.map((h, i) =>
      generateHandlerFunction(h, [...breadcrumbs, 'handlers', i], ctx)
    );

    return jsAst.yieldExpression(
      jsAst.callExpression(jsAst.identifier('cauRuntime$handleEffects'), [
        iife,
        ...handlers,
      ]),
      true
    );
  } else {
    return jsAst.yieldExpression(iife, true);
  }
}

function generatePatternHandlingStatements(
  matchingExpression: jsAst.Expression,
  node: ast.Pattern,
  breadcrumbs: Breadcrumbs,
  ctx: GeneratorContext
): jsAst.Statement[] {
  switch (node.type) {
    case 'TypePattern': {
      return [
        jsAst.ifStatement(
          jsAst.binaryExpression(
            '!==',
            jsAst.memberExpression(
              matchingExpression,
              jsAst.identifier('type')
            ),
            jsAst.identifier(node.typeName.name)
          ),
          jsAst.returnStatement()
        ),
      ];
    }
    case 'NamePattern': {
      const varName = jsAst.identifier(node.name.name);
      const result: jsAst.Statement[] = [
        jsAst.variableDeclaration('const', [
          jsAst.variableDeclarator(varName, matchingExpression),
        ]),
      ];
      if (node.valueType) {
        // Using an identifier as a type (`Effect`) is
        // syntax sugar for a basic TypePattern (`Effect()`)
        const typePattern: ast.TypePattern =
          node.valueType.type === 'Identifier'
            ? { type: 'TypePattern', typeName: node.valueType }
            : node.valueType;
        result.push(
          ...generatePatternHandlingStatements(
            varName,
            typePattern,
            [...breadcrumbs, 'valueType'],
            ctx
          )
        );
      }
      return result;
    }
    default: {
      return exhaustiveCheck(node);
    }
  }
}

function generateHandlerFunction(
  node: ast.HandlerBlockSuffix,
  breadcrumbs: Breadcrumbs,
  ctx: GeneratorContext
) {
  const returnHandled = jsAst.returnStatement(
    jsAst.objectExpression([
      jsAst.objectProperty(
        jsAst.identifier('handled'),
        jsAst.booleanLiteral(true)
      ),
      jsAst.objectProperty(
        jsAst.identifier('value'),
        generateExpression(node.body, [...breadcrumbs, 'body'], ctx)
      ),
    ])
  );

  let statements;
  if (node.pattern) {
    statements = [
      ...generatePatternHandlingStatements(
        jsAst.identifier('$effect'),
        node.pattern,
        [...breadcrumbs, 'pattern'],
        ctx
      ),
      returnHandled,
    ];
  } else {
    statements = [returnHandled];
  }

  return jsAst.functionExpression(
    null,
    node.pattern ? [jsAst.identifier('$effect')] : [],
    jsAst.blockStatement(statements),
    true
  );
}
