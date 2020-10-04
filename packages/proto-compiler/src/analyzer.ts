import * as ast from './ast';
import coreLibrary from './coreLibrary';

export type ValueType =
  | KeywordValueType
  | DeclarationValueType
  | InstanceValueType;

export type DeclarationValueType =
  | EffectDeclarationValueType
  | FunctionDeclarationValueType
  | TypeDeclarationValueType;

export type RuntimeLibraryValueType =
  | EffectDeclarationValueType
  | TypeDeclarationValueType;

interface KeywordValueType {
  kind: 'keyword';
  keyword: ast.KeywordValue;
}

export interface EffectDeclarationValueType {
  kind: 'effect';
  name: string;
  symbol: symbol;
}

interface FunctionDeclarationValueType {
  kind: 'fn';
  name?: string;
}

export interface TypeDeclarationValueType {
  kind: 'type';
  name: string;
  symbol: symbol;
}

interface InstanceValueType {
  kind: 'instance';
  name?: string;
  type: DeclarationValueType;
}

export interface Scope {
  [name: string]: ValueType;
}

export interface AnalyzerContext {
  scope: Scope;
  expressionTypes: Map<string, ValueType>;
}

export type Breadcrumbs = (string | number)[];

export const scopeFromLibrary = (
  library: (EffectDeclarationValueType | TypeDeclarationValueType)[]
): Scope =>
  Object.fromEntries([...coreLibrary, ...library].map((x) => [x.name, x]));

export const analyzeModule = (
  module: ast.Module,
  breadcrumbs: Breadcrumbs,
  ctx: AnalyzerContext
): AnalyzerContext => {
  // First, superficially check all the declarations to see what's hoisted
  // into scope
  const newScope: Scope = { ...ctx.scope };
  for (const declaration of module.body) {
    if (declaration.type === 'FunctionDeclaration') {
      newScope[declaration.id.name] = {
        kind: 'fn',
        name: declaration.id.name,
      };
    }
  }

  module.body.forEach((declaration, i) => {
    analyzeFunctionDeclaration(declaration, [...breadcrumbs, 'body', i], {
      ...ctx,
      scope: newScope,
    });
  });

  return ctx;
};

const analyzeFunctionDeclaration = (
  node: ast.FunctionDeclaration,
  breadcrumbs: Breadcrumbs,
  ctx: AnalyzerContext
): void => {
  analyzeExpression(node.body, [...breadcrumbs, 'body'], ctx);
};

const analyzeExpression = (
  node: ast.Expression,
  breadcrumbs: Breadcrumbs,
  ctx: AnalyzerContext
): void => {
  switch (node.type) {
    case 'BlockExpression': {
      // TODO: this is gonna get tricky with scope when variable declarations
      // are a thing
      node.body.forEach((a: ast.ExpressionStatement, i) => {
        analyzeExpression(
          a.expression,
          [...breadcrumbs, 'body', i, 'expression'],
          ctx
        );
      });
      break;
    }
    case 'CallExpression':
    case 'UnaryCallExpression':
      analyzeCallExpression(node, breadcrumbs, ctx);
      break;
  }
};

const analyzeCallExpression = (
  node: ast.CallExpression | ast.UnaryCallExpression,
  breadcrumbs: Breadcrumbs,
  ctx: AnalyzerContext
) => {
  const { callee } = node;
  let calleeType: ValueType;
  switch (callee.type) {
    case 'Identifier': {
      const type: ValueType | undefined = ctx.scope[callee.name];
      if (!type) {
        throw new Error(
          `I was expecting "${callee.name}" to be a type in scope; maybe it's not spelled correctly.`
        );
      }
      calleeType = type;
      break;
    }
    case 'Keyword':
      calleeType = {
        kind: 'keyword',
        keyword: callee.keyword,
      };
      break;
    default:
      throw new Error(
        `I don't know how to analyze function calls like this yet. The technical name for this sort of callee is ${callee.type}`
      );
  }
  ctx.expressionTypes.set([...breadcrumbs, 'callee'].join('.'), calleeType);

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
  parameters.forEach((a) => analyzeExpression(a.node, a.breadcrumbs, ctx));
};