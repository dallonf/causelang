import * as ast from './ast';
import {
  Breadcrumbs,
  EffectScopeSymbol,
  findInScope,
  findScope,
  Scope,
  ScopeMap,
  ScopeSymbol,
  toKey,
} from './context';
import { allCoreLibraries } from './coreLibrary';
import { Library } from './library';
import { CoreFunctionType, TypeMap, TypeReference } from './typeSystem';
import { exhaustiveCheck } from './utils';

export interface AnalyzerContext {
  declarationSuffix: string;
  typesOfExpressions: Map<string, TypeReference>;
  typeMap: TypeMap;
  scopes: ScopeMap;
}

export const analyzeModule = (
  module: ast.Module,
  breadcrumbs: Breadcrumbs,
  libraries: Library[],
  opts: {
    declarationSuffix: string;
  }
): AnalyzerContext => {
  const allLibraries = allCoreLibraries.concat(...libraries);
  const typeMap = new Map(
    allLibraries.flatMap((lib) => [...lib.types.entries()])
  );
  const scope = Object.fromEntries(
    allLibraries.flatMap((lib) => Object.entries(lib.scope))
  );

  const ctx: AnalyzerContext = {
    declarationSuffix: opts.declarationSuffix,
    scopes: new Map(),
    typesOfExpressions: new Map(),
    typeMap,
  };

  // First, superficially check all the declarations to see what's hoisted
  // into scope
  const newScope: Scope = { ...scope };
  module.body.forEach((declaration, i) => {
    switch (declaration.type) {
      case 'FunctionDeclaration': {
        newScope[declaration.id.name] = {
          kind: 'namedValue',
          name: declaration.id.name,
          variable: false,
          valueType: {
            kind: 'functionTypeReference',
            name: declaration.id.name,
            // TODO
            params: {},
            // TODO: use scope
            returnType: {
              kind: 'pendingInferenceTypeReference',
            },
          },
        };
        break;
      }
      case 'EffectDeclaration':
        const type: EffectScopeSymbol = {
          kind: 'effect',
          id: `${declaration.id.name}$${ctx.declarationSuffix}`,
          name: declaration.id.name,
        };
        newScope[declaration.id.name] = type;
        break;
      default:
        return exhaustiveCheck(declaration);
    }
  });
  ctx.scopes.set(toKey(breadcrumbs), newScope);

  module.body.forEach((declaration, i) => {
    switch (declaration.type) {
      case 'FunctionDeclaration':
        analyzeFunctionDeclaration(
          declaration,
          [...breadcrumbs, 'body', i],
          ctx
        );
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
    case 'Identifier': {
      const scope = findScope(breadcrumbs, ctx.scopes);
      if (!scope[node.name]) {
        throw new Error(
          `I can't find anything called ${node.name} in the current scope.`
        );
      }
      break;
    }
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
        Object.entries(findScope(breadcrumbs, ctx.scopes)).filter(
          ([k, v]) => !(v.kind === 'namedValue' && v.variable)
        )
      );
      ctx.scopes.set(toKey(breadcrumbs), filteredScope);
      analyzeExpression(node.body, [...breadcrumbs, 'body'], ctx);
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
  const parentScope = findScope(breadcrumbs, ctx.scopes);
  let scope = { ...parentScope };
  node.body.forEach((a: ast.Statement, i) => {
    const statementBreadcrumbs = [...breadcrumbs, 'body', i];
    ctx.scopes.set(toKey(statementBreadcrumbs), scope);
    switch (a.type) {
      case 'ExpressionStatement':
        analyzeExpression(
          a.expression,
          [...statementBreadcrumbs, 'expression'],
          ctx
        );
        break;
      case 'NameDeclarationStatement': {
        analyzeExpression(a.value, [...statementBreadcrumbs, 'value'], ctx);

        let type: TypeReference;
        if (a.typeAnnotation) {
          const typeSymbol = scope[a.typeAnnotation.name];
          if (
            typeSymbol &&
            (typeSymbol.kind === 'objectType' || typeSymbol.kind === 'effect')
          ) {
            type = { kind: 'valueTypeReference', id: typeSymbol.id };
          } else {
            type = {
              kind: 'typeErrorTypeReference',
              error: {
                kind: 'failedToResolveTypeError',
                name: a.typeAnnotation.name,
              },
            };
          }
        } else {
          type = { kind: 'pendingInferenceTypeReference' };
        }

        const scopeSymbol: ScopeSymbol = {
          kind: 'namedValue',
          name: a.name.name,
          variable: a.variable,
          valueType: type,
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
      patternScope = parentScope;
    }

    const handlerExpressionBreadcrumbs = [
      ...breadcrumbs,
      'handlers',
      i,
      'body',
    ];
    ctx.scopes.set(toKey(handlerExpressionBreadcrumbs), patternScope);
    analyzeExpression(a.body, handlerExpressionBreadcrumbs, ctx);
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
      let valueType: TypeReference;

      const typePattern: ast.TypePattern | undefined =
        node.valueType?.type === 'Identifier'
          ? {
              type: 'TypePattern',
              typeName: { type: 'Identifier', name: node.valueType.name },
            }
          : (node.valueType as ast.TypePattern);
      if (typePattern) {
        const typeInScope = findInScope(
          typePattern.typeName.name,
          breadcrumbs,
          ctx.scopes
        );
        if (!typeInScope || !(typeInScope.kind === 'effect')) {
          valueType = {
            kind: 'typeErrorTypeReference',
            error: {
              kind: 'failedToResolveTypeError',
              name: typePattern.typeName.name,
            },
          };
        } else {
          valueType = { kind: 'valueTypeReference', id: typeInScope.id };
        }
      } else {
        valueType = { kind: 'pendingInferenceTypeReference' };
      }

      const symbolToAddToScope: ScopeSymbol = {
        kind: 'namedValue',
        name,
        valueType: valueType,
        variable: false,
      };

      if (findInScope(name, breadcrumbs, ctx.scopes)) {
        throw new Error(
          `I'm trying to add ${name} to the scope, but this pattern is already adding another value with the same name`
        );
      }

      const newScope = {
        ...findScope(breadcrumbs, ctx.scopes),
        [name]: symbolToAddToScope,
      };
      ctx.scopes.set(toKey([...breadcrumbs, 'valueType']), newScope);

      return typePattern
        ? getPatternScope(typePattern, [...breadcrumbs, 'valueType'], ctx)
        : newScope;
    }
    case 'TypePattern': {
      return findScope(breadcrumbs, ctx.scopes);
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
  switch (callee.type) {
    case 'Identifier': {
      const symbol: ScopeSymbol | undefined = findInScope(
        callee.name,
        breadcrumbs,
        ctx.scopes
      );
      if (symbol?.kind === 'effect' || symbol?.kind === 'objectType') {
        ctx.typesOfExpressions.set(toKey([...breadcrumbs, 'callee']), {
          kind: 'typeNameTypeReference',
          id: symbol.id,
        });
      } else if (symbol?.kind === 'namedValue') {
        ctx.typesOfExpressions.set(
          toKey([...breadcrumbs, 'callee']),
          symbol.valueType
        );

        let returnType: TypeReference;
        switch (symbol.valueType.kind) {
          case 'pendingInferenceTypeReference':
            returnType = {
              kind: 'pendingInferenceTypeReference',
            };
            break;
          case 'typeNameTypeReference':
            throw new Error(
              `TODO: handle indirect references to type names at ${breadcrumbs.join(
                '.'
              )}`
            );
          case 'typeErrorTypeReference':
            returnType = {
              kind: 'typeErrorTypeReference',
              error: {
                kind: 'referenceTypeError',
                breadcrumbs: [...breadcrumbs, 'callee'],
              },
            };
            break;
          case 'valueTypeReference': {
            const valueType = ctx.typeMap.get(symbol.valueType.id)!;
            if (valueType.kind === 'coreFunctionType') {
              returnType = valueType.returnType;
            } else {
              returnType = {
                kind: 'typeErrorTypeReference',
                error: {
                  kind: 'notCallableTypeError',
                },
              };
            }
            break;
          }
          case 'functionTypeReference':
            returnType = symbol.valueType.returnType;
            break;
          default:
            return exhaustiveCheck(symbol.valueType);
        }
        ctx.typesOfExpressions.set(
          toKey([...breadcrumbs, 'callee']),
          symbol.valueType
        );
        ctx.typesOfExpressions.set(toKey(breadcrumbs), returnType);
      } else {
        throw new Error(
          `I was expecting "${callee.name}" to be a function or type in scope; maybe it's not spelled correctly.`
        );
      }
      break;
    }
    default:
      throw new Error(
        `I don't know how to analyze function calls like this yet. The technical name for this sort of callee is ${callee.type}`
      );
  }

  node.parameters.forEach((a, i) =>
    analyzeExpression(a, [...breadcrumbs, 'parameters', i], ctx)
  );
};
