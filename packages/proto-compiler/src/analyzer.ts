import * as ast from './ast';
import { coreFunctions } from './coreLibrary';
import { Library } from './makeLibrary';
import { exhaustiveCheck } from './utils';

export type ScopeSymbol = DeclarationScopeSymbol | CoreFunctionScopeSymbol;

export type DeclarationScopeSymbol =
  | EffectScopeSymbol
  | FunctionScopeSymbol
  | TypeScopeSymbol
  | NamedValueScopeSymbol;

export type LibraryScopeSymbol =
  | EffectScopeSymbol
  | TypeScopeSymbol;

export interface EffectScopeSymbol {
  kind: 'effect';
  name: string;
  id: string;
}

interface FunctionScopeSymbol {
  kind: 'fn';
  name?: string;
}

export interface TypeScopeSymbol {
  kind: 'type';
  name: string;
  id: string;
}

export interface NamedValueScopeSymbol {
  kind: 'namedValue';
  name: string;
  valueType?: ScopeSymbol;
  variable: boolean;
}

export interface CoreFunctionScopeSymbol {
  kind: 'coreFn';
  name: string;
}

export type Scope = Record<string, ScopeSymbol>;

export interface AnalyzerContext {
  declarationSuffix: string;
  scope: Scope;
  typesOfExpressions: Map<string, ScopeSymbol>;
}

export type Breadcrumbs = (string | number)[];

export const getAnalyzerScope = (
  ...libraries: Library[]
): Record<string, LibraryScopeSymbol> => {
  return Object.fromEntries([
    ...Object.entries(coreFunctions).map(([k, v]) => [
      k,
      {
        kind: 'coreFn',
        name: k,
      } as CoreFunctionScopeSymbol,
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
        const type: EffectScopeSymbol = {
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
          ([k, v]) => !(v.kind === 'namedValue' && v.variable)
        )
      );
      analyzeExpression(node.body, [...breadcrumbs, 'body'], {
        ...ctx,
        scope: filteredScope,
      });
      break;
    }
    case 'BranchExpression':
      node.conditions.forEach((condition, i) => {
        if (condition.guard) {
          analyzeExpression(
            condition.guard,
            [...breadcrumbs, 'branch', 'conditions', i, 'guard'],
            ctx
          );
        }

        analyzeExpression(
          condition.body,
          [...breadcrumbs, 'branch', 'conditions', i, 'body'],
          ctx
        );
      });
      break;
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
        const scopeSymbol: ScopeSymbol = {
          kind: 'namedValue',
          name: a.name.name,
          variable: a.variable,
        };
        scope = { ...scope, [a.name.name]: scopeSymbol };
        break;
      }
      case 'AssignmentStatement': {
        const possibleVariable = scope[a.name.name];
        if (!possibleVariable || possibleVariable.kind !== 'namedValue') {
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

      const symbolToAddToScope: ScopeSymbol = {
        kind: 'namedValue',
        name,
        valueType: valueType,
        variable: false,
      };

      if (ctx.scope[name]) {
        throw new Error(
          `I'm trying to add ${name} to the scope, but this pattern is already adding another value with the same name`
        );
      }

      const newScope = { ...ctx.scope, [name]: symbolToAddToScope };

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
  let calleeType: ScopeSymbol;
  switch (callee.type) {
    case 'Identifier': {
      const type: ScopeSymbol | undefined = ctx.scope[callee.name];
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
