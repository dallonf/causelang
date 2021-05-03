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
import { allCoreLibraries, INTEGER_ID, STRING_ID } from './coreLibrary';
import { Library } from './library';
import {
  ConcreteTypeReference,
  EffectType,
  isAssignableTo,
  TypeMap,
  TypeReference,
} from './typeSystem';
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
  const allLibraries = allCoreLibraries
    .map((x) => x.libraryData)
    .concat(...libraries);
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
      case 'EffectDeclaration': {
        const id = `${declaration.id.name}$${ctx.declarationSuffix}`;
        const type: EffectType = {
          kind: 'effectType',
          id,
          name: declaration.id.name,
          parameters: Object.fromEntries(
            declaration.parameters.map((param): [string, TypeReference] => [
              param.name.name,
              {
                kind: 'pendingInferenceTypeReference',
              },
            ])
          ),
          returnType: { kind: 'pendingInferenceTypeReference' },
        };
        const symbol: EffectScopeSymbol = {
          kind: 'effect',
          id,
          name: declaration.id.name,
        };
        newScope[declaration.id.name] = symbol;
        ctx.typeMap.set(id, type);
        break;
      }
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
      ctx.typesOfExpressions.set(toKey(breadcrumbs), {
        kind: 'valueTypeReference',
        id: INTEGER_ID,
      });
      break;
    case 'StringLiteral':
      ctx.typesOfExpressions.set(toKey(breadcrumbs), {
        kind: 'valueTypeReference',
        id: STRING_ID,
      });
      break;
    case 'Identifier': {
      const item = findInScope(node.name, breadcrumbs, ctx.scopes);
      if (!item) {
        setTypeOfExpression(ctx, breadcrumbs, {
          kind: 'typeErrorTypeReference',
          error: {
            kind: 'failedToResolveTypeError',
            name: node.name,
          },
        });
      } else {
        let type: TypeReference;
        switch (item.kind) {
          case 'effect':
          case 'objectType':
            type = {
              kind: 'typeNameTypeReference',
              id: item.id,
            };
            break;
          case 'namedValue':
            type = item.valueType;
            break;
          default:
            type = exhaustiveCheck(item);
        }
        setTypeOfExpression(ctx, breadcrumbs, type);
      }
      break;
    }
    case 'MemberExpression': {
      analyzeExpression(node.object, [...breadcrumbs, 'object'], ctx);
      const parentType = getTypeOfExpression([...breadcrumbs, 'object'], ctx);
      if (parentType.kind === 'valueTypeReference') {
        const parentValueType = ctx.typeMap.get(parentType.id)!;
        if (parentValueType.kind === 'objectType') {
          const field = parentValueType.fields[node.property.name];
          if (!field) {
            setTypeOfExpression(ctx, breadcrumbs, {
              kind: 'typeErrorTypeReference',
              error: {
                kind: 'failedToResolveTypeError',
                name: node.property.name,
              },
            });
          }
          setTypeOfExpression(ctx, breadcrumbs, field);
        } else {
          setTypeOfExpression(ctx, breadcrumbs, {
            kind: 'typeErrorTypeReference',
            error: {
              kind: 'mismatchedTypeError',
              actualType: parentType,
            },
          });
        }
      } else {
        setTypeOfExpression(ctx, breadcrumbs, {
          kind: 'typeErrorTypeReference',
          error: {
            kind: 'mismatchedTypeError',
            actualType: parentType,
          },
        });
      }
      break;
    }
    case 'CallExpression':
      analyzeCallExpression(node, breadcrumbs, ctx);
      break;
    case 'PrefixOperatorExpression': {
      analyzeExpression(node.expression, [...breadcrumbs, 'expression'], ctx);
      if (node.operator.keyword === 'cause') {
        analyzeCauseExpression(node, breadcrumbs, ctx);
      }
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

      ctx.typesOfExpressions.set(toKey(breadcrumbs), {
        kind: 'functionTypeReference',
        params: {},
        returnType: getTypeOfExpression([...breadcrumbs, 'body'], ctx),
      });
      break;
    }
    case 'BranchExpression':
      node.conditions.forEach((condition, i) => {
        if (condition.guard) {
          // TODO: this needs to wind up being a boolean
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

      const firstType = getTypeOfExpression(
        [...breadcrumbs, 'branch', 'conditions', 0, 'body'],
        ctx
      );
      const mismatch = node.conditions
        .map((condition, i) => {
          const thisType = getTypeOfExpression(
            [...breadcrumbs, 'branch', 'conditions', 0, 'body'],
            ctx
          );
          if (
            isAssignableTo(
              thisType as ConcreteTypeReference,
              firstType as ConcreteTypeReference,
              ctx.typeMap
            )
          ) {
            return thisType;
          } else {
            return null;
          }
        })
        .find((x) => Boolean(x));
      if (mismatch) {
        setTypeOfExpression(ctx, breadcrumbs, {
          kind: 'typeErrorTypeReference',
          error: {
            kind: 'mismatchedTypeError',
            expectedType: firstType,
            actualType: mismatch,
          },
        });
      } else {
        setTypeOfExpression(ctx, breadcrumbs, firstType);
      }
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
        ctx.typesOfExpressions.set(
          toKey(statementBreadcrumbs),
          getTypeOfExpression([...statementBreadcrumbs, 'expression'], ctx)
        );
        break;
      case 'NameDeclarationStatement': {
        analyzeExpression(a.value, [...statementBreadcrumbs, 'value'], ctx);
        const typeOfValue = getTypeOfExpression(
          [...statementBreadcrumbs, 'value'],
          ctx
        );

        let type: TypeReference;
        if (a.typeAnnotation) {
          const typeSymbol = scope[a.typeAnnotation.name];
          if (
            typeSymbol &&
            (typeSymbol.kind === 'objectType' || typeSymbol.kind === 'effect')
          ) {
            const typeAnnotationReference = {
              kind: 'valueTypeReference',
              id: typeSymbol.id,
            } as const;
            if (typeOfValue.kind === 'typeErrorTypeReference') {
              type = {
                kind: 'typeErrorTypeReference',
                error: {
                  kind: 'referenceTypeError',
                  breadcrumbs: [...statementBreadcrumbs, 'value'],
                },
              };
            } else if (
              typeOfValue.kind !== 'pendingInferenceTypeReference' &&
              isAssignableTo(typeOfValue, typeAnnotationReference, ctx.typeMap)
            ) {
              type = { kind: 'valueTypeReference', id: typeSymbol.id };
            } else {
              type = {
                kind: 'typeErrorTypeReference',
                error: {
                  kind: 'mismatchedTypeError',
                  expectedType: {
                    kind: 'valueTypeReference',
                    id: typeSymbol.id,
                  },
                  actualType: {
                    kind: 'valueTypeReference',
                    id: typeAnnotationReference.id,
                  },
                },
              };
            }
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
          type = typeOfValue;
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
        // TODO: check for mismatches here
        analyzeExpression(a.value, [...statementBreadcrumbs, 'value'], ctx);
        setTypeOfExpression(
          ctx,
          statementBreadcrumbs,
          getTypeOfExpression([...statementBreadcrumbs, 'value'], ctx)
        );
        break;
      }
      default:
        return exhaustiveCheck(a);
    }
  });

  // return type is the last value
  ctx.typesOfExpressions.set(
    toKey(breadcrumbs),
    getTypeOfExpression([...breadcrumbs, 'body', node.body.length - 1], ctx)
  );

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
        setTypeOfExpression(ctx, [...breadcrumbs, 'callee'], {
          kind: 'typeNameTypeReference',
          id: symbol.id,
        });
        setTypeOfExpression(ctx, breadcrumbs, {
          kind: 'valueTypeReference',
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
          case 'valueTypeReference':
            returnType = {
              kind: 'typeErrorTypeReference',
              error: {
                kind: 'notCallableTypeError',
              },
            };
            break;
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

const analyzeCauseExpression = (
  node: ast.PrefixOperatorExpression,
  breadcrumbs: Breadcrumbs,
  ctx: AnalyzerContext
) => {
  const valueType = getTypeOfExpression([...breadcrumbs, 'expression'], ctx);
  if (valueType.kind !== 'valueTypeReference') {
    setTypeOfExpression(ctx, breadcrumbs, {
      kind: 'typeErrorTypeReference',
      error: {
        kind: 'mismatchedTypeError',
        actualType: valueType,
      },
    });
    return;
  }

  const effectType = ctx.typeMap.get(valueType.id)!;
  if (effectType.kind !== 'effectType') {
    setTypeOfExpression(ctx, breadcrumbs, {
      kind: 'typeErrorTypeReference',
      error: {
        kind: 'mismatchedTypeError',
        actualType: valueType,
      },
    });
    return;
  }

  setTypeOfExpression(ctx, breadcrumbs, effectType.returnType);
};

const getTypeOfExpression = (
  breadcrumbs: Breadcrumbs,
  ctx: AnalyzerContext
) => {
  const result = ctx.typesOfExpressions.get(toKey(breadcrumbs));
  if (!result) {
    throw new Error(
      `The expression at ${breadcrumbs.join('.')} hasn't been analyzed yet`
    );
  }
  return result;
};

const setTypeOfExpression = (
  ctx: AnalyzerContext,
  breadcrumbs: Breadcrumbs,
  type: TypeReference
) => {
  ctx.typesOfExpressions.set(toKey(breadcrumbs), type);
};
