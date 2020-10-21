import * as ast from './ast';
import { coreFunctions } from './coreLibrary';
import { Library } from './makeLibrary';
import { exhaustiveCheck } from './utils';

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
  variable: boolean;
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
  declarationSuffix: string;
  scope: Scope;
  typesOfExpressions: Map<string, ValueType>;
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
  module.body.forEach((declaration, i) => {
    switch (declaration.type) {
      case 'FunctionDeclaration':
        newScope[declaration.id.name] = {
          kind: 'fn',
          name: declaration.id.name,
        };
        break;
      case 'EffectDeclaration':
        const type: EffectDeclarationValueType = {
          kind: 'effect',
          id: `${declaration.id.name}$${ctx.declarationSuffix}`,
          name: declaration.id.name,
        };
        newScope[declaration.id.name] = type;
        ctx.typesOfExpressions.set([...breadcrumbs, 'body', i].join('.'), type);
        break;
      default:
        return exhaustiveCheck(declaration);
    }
  });

  module.body.forEach((declaration, i) => {
    switch (declaration.type) {
      case 'FunctionDeclaration':
        analyzeFunctionDeclaration(declaration, [...breadcrumbs, 'body', i], {
          ...ctx,
          scope: newScope,
        });
        break;
      case 'EffectDeclaration':
        break;
      default:
        return exhaustiveCheck(declaration);
    }
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
    case 'IntLiteral':
    case 'StringLiteral':
      break;
    case 'Identifier':
      if (!ctx.scope[node.name]) {
        throw new Error(
          `I can't find anything called ${node.name} in the current scope.`
        );
      }
      break;
    case 'MemberExpression':
      analyzeExpression(node.object, [...breadcrumbs, 'object'], ctx);
      break;
    case 'CallExpression':
      analyzeCallExpression(node, breadcrumbs, ctx);
      break;
    case 'PrefixOperatorExpression': {
      analyzeExpression(node.expression, [...breadcrumbs, 'expression'], ctx);
      break;
    }
    case 'BlockExpression': {
      analyzeBlockExpression(node, breadcrumbs, ctx);
      break;
    }
    case 'FunctionExpression': {
      // Function expressions don't inherit variables from their scope; only constant names
      const filteredScope: Scope = Object.fromEntries(
        Object.entries(ctx.scope).filter(
          ([k, v]) => !(v.kind === 'name' && v.variable)
        )
      );
      analyzeExpression(node.body, [...breadcrumbs, 'body'], {
        ...ctx,
        scope: filteredScope,
      });
      break;
    }
    default:
      return exhaustiveCheck(node);
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
    switch (a.type) {
      case 'ExpressionStatement':
        analyzeExpression(
          a.expression,
          [...statementBreadcrumbs, 'expression'],
          {
            ...ctx,
            scope,
          }
        );
        break;
      case 'NameDeclarationStatement': {
        analyzeExpression(a.value, [...statementBreadcrumbs, 'value'], {
          ...ctx,
          scope,
        });
        const valueType: ValueType = {
          kind: 'name',
          name: a.name.name,
          variable: a.variable,
        };
        scope = { ...scope, [a.name.name]: valueType };
        break;
      }
      case 'AssignmentStatement': {
        const possibleVariable = scope[a.name.name];
        if (!possibleVariable || possibleVariable.kind !== 'name') {
          throw new Error(
            `I can't find a variable called ${a.name.name} in the current scope.`
          );
        } else if (!possibleVariable.variable) {
          throw new Error(
            `${a.name.name} isn't a variable; it's just a name. You could try adding var to its declaration: "let var ${a.name.name}..."`
          );
        }
        break;
      }
      default:
        return exhaustiveCheck(a);
    }
  });

  node.handlers?.forEach((a, i) => {
    let patternScope;
    if (a.pattern) {
      patternScope = getPatternScope(
        a.pattern,
        [...breadcrumbs, 'pattern'],
        ctx
      );
    } else {
      patternScope = ctx.scope;
    }

    analyzeExpression(a.body, [...breadcrumbs, 'handlers', i, 'body'], {
      ...ctx,
      scope: patternScope,
    });
  });
};

const getPatternScope = (
  node: ast.Pattern,
  breadcrumbs: Breadcrumbs,
  ctx: AnalyzerContext
): Scope => {
  switch (node.type) {
    case 'NamePattern': {
      const name = node.name.name;
      let valueType;

      const typePattern: ast.TypePattern | undefined =
        node.valueType?.type === 'Identifier'
          ? {
              type: 'TypePattern',
              typeName: { type: 'Identifier', name: node.valueType.name },
            }
          : (node.valueType as ast.TypePattern);
      if (typePattern) {
        const typeInScope = ctx.scope[typePattern.typeName.name];
        if (!typeInScope || !(typeInScope.kind === 'effect')) {
          throw new Error(
            `I can't find a type or effect called "${typeInScope}" in the current scope`
          );
        }
        valueType = typeInScope;
      }

      const valueTypeToAddToScope: ValueType = {
        kind: 'name',
        name,
        valueType,
        variable: false,
      };

      if (ctx.scope[name]) {
        throw new Error(
          `I'm trying to add ${name} to the scope, but this pattern is already adding another value with the same name`
        );
      }

      const newScope = { ...ctx.scope, [name]: valueTypeToAddToScope };

      return typePattern
        ? getPatternScope(typePattern, [...breadcrumbs, 'valueType'], {
            ...ctx,
            scope: newScope,
          })
        : newScope;
    }
    case 'TypePattern': {
      return ctx.scope;
    }
    default:
      return exhaustiveCheck(node);
  }
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
  ctx.typesOfExpressions.set([...breadcrumbs, 'callee'].join('.'), calleeType);

  node.parameters.forEach((a, i) =>
    analyzeExpression(a, [...breadcrumbs, 'parameters', i], ctx)
  );
};
