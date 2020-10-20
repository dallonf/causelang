import * as ast from './ast';
import { coreFunctions } from './coreLibrary';
import { Library } from './makeLibrary';

export type ValueType =
  | KeywordValueType
  | DeclarationValueType
  | InstanceValueType
  | CoreFunctionValueType;

export type DeclarationValueType =
  | EffectDeclarationValueType
  | FunctionDeclarationValueType
  | TypeDeclarationValueType
  | NameDeclarationValueType;

export type LibraryValueType =
  | EffectDeclarationValueType
  | TypeDeclarationValueType;

interface KeywordValueType {
  kind: 'keyword';
  keyword: ast.KeywordValue;
}

export interface EffectDeclarationValueType {
  kind: 'effect';
  name: string;
  id: string;
}

interface FunctionDeclarationValueType {
  kind: 'fn';
  name?: string;
}

export interface TypeDeclarationValueType {
  kind: 'type';
  name: string;
  id: string;
}

export interface NameDeclarationValueType {
  kind: 'name';
  name: string;
  valueType?: ValueType;
}

export interface CoreFunctionValueType {
  kind: 'coreFn';
  name: string;
}

interface InstanceValueType {
  kind: 'instance';
  name?: string;
  type: DeclarationValueType;
}

export type Scope = Record<string, ValueType>;

export interface AnalyzerContext {
  scope: Scope;
  expressionTypes: Map<string, ValueType>;
}

export type Breadcrumbs = (string | number)[];

export const getAnalyzerScope = (
  ...libraries: Library[]
): Record<string, LibraryValueType> => {
  return Object.fromEntries([
    ...Object.entries(coreFunctions).map(([k, v]) => [
      k,
      {
        kind: 'coreFn',
        name: k,
      } as CoreFunctionValueType,
    ]),
    ...libraries.flatMap((l) => Object.entries(l.analyzerScope)),
  ]);
};

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
      analyzeBlockExpression(node, breadcrumbs, ctx);
      break;
    }
    case 'CallExpression':
      analyzeCallExpression(node, breadcrumbs, ctx);
      break;
    case 'PrefixOperatorExpression': {
      analyzeExpression(node.expression, [...breadcrumbs, 'expression'], ctx);
      break;
    }
  }
};

const analyzeBlockExpression = (
  node: ast.BlockExpression,
  breadcrumbs: Breadcrumbs,
  ctx: AnalyzerContext
) => {
  let scope = { ...ctx.scope };
  node.body.forEach((a: ast.Statement, i) => {
    const statementBreadcrumbs = [...breadcrumbs, 'body', i];
    if (a.type === 'ExpressionStatement') {
      analyzeExpression(a.expression, [...statementBreadcrumbs, 'expression'], {
        ...ctx,
        scope,
      });
    } else if (a.type === 'NameDeclarationStatement') {
      analyzeExpression(a.value, [...statementBreadcrumbs, 'value'], {
        ...ctx,
        scope,
      });
      const valueType: ValueType = {
        kind: 'name',
        name: a.name.name,
      };
      scope = { ...scope, [a.name.name]: valueType };
    }
  });
};

const analyzeCallExpression = (
  node: ast.CallExpression,
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
          `I was expecting "${callee.name}" to be a function or type in scope; maybe it's not spelled correctly.`
        );
      }
      calleeType = type;
      break;
    }
    default:
      throw new Error(
        `I don't know how to analyze function calls like this yet. The technical name for this sort of callee is ${callee.type}`
      );
  }
  ctx.expressionTypes.set([...breadcrumbs, 'callee'].join('.'), calleeType);

  node.parameters.forEach((a, i) =>
    analyzeExpression(a, [...breadcrumbs, 'parameters', i], ctx)
  );
};
