import * as ast from './ast';
import {
  Breadcrumbs,
  findInScope,
  findScope,
  Scope,
  ScopeMap,
  ScopeItem,
  SymbolScopeItem,
  toKey,
  TypeReferenceScopeItem,
} from './context';
import { allCoreLibraries, INTEGER_ID, STRING_ID } from './coreLibrary';
import { Library } from './library';
import {
  ConcreteTypeReference,
  EffectType,
  isAssignableTo,
  isConcrete,
  ObjectType,
  OptionTypeReference,
  SymbolType,
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
          type: {
            kind: 'valueTypeReference',
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
          },
        };
        break;
      }
      case 'EffectDeclaration':
      case 'TypeDeclaration':
      case 'SymbolDeclaration':
      case 'OptionDeclaration': {
        const newType = analyzeTypeDeclaration(
          declaration,
          [...breadcrumbs, 'body', i],
          ctx
        );
        newScope[newType.name] = newType.scopeItem;
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
      case 'TypeDeclaration':
      case 'SymbolDeclaration':
      case 'OptionDeclaration':
        break;
      default:
        return exhaustiveCheck(declaration);
    }
  });

  return ctx;
};

interface TypeDeclarationResult {
  scopeItem: TypeReferenceScopeItem | SymbolScopeItem;
  name: string;
}

function analyzeTypeDeclaration(
  node: ast.TypeDeclaration,
  breadcrumbs: Breadcrumbs,
  ctx: AnalyzerContext
): TypeDeclarationResult {
  switch (node.type) {
    case 'EffectDeclaration': {
      const id = `${node.id.name}$${ctx.declarationSuffix}`;
      const type: EffectType = {
        kind: 'effectType',
        id,
        name: node.id.name,
        parameters: Object.fromEntries(
          node.parameters.map((param): [string, TypeReference] => [
            param.name.name,
            {
              kind: 'pendingInferenceTypeReference',
            },
          ])
        ),
        returnType: { kind: 'pendingInferenceTypeReference' },
      };
      const scopeItem: TypeReferenceScopeItem = {
        kind: 'typeReference',
        name: node.id.name,
        type: {
          kind: 'typeNameTypeReference',
          id,
        },
      };
      ctx.typeMap.set(id, type);
      return {
        scopeItem: scopeItem,
        name: node.id.name,
      };
    }
    case 'TypeDeclaration': {
      const id = `${node.id.name}$${ctx.declarationSuffix}`;
      const type: ObjectType = {
        kind: 'objectType',
        id,
        name: node.id.name,
        fields: Object.fromEntries(
          node.fields.map((param): [string, TypeReference] => [
            param.name.name,
            {
              kind: 'pendingInferenceTypeReference',
            },
          ])
        ),
      };
      const scopeItem: TypeReferenceScopeItem = {
        kind: 'typeReference',
        name: node.id.name,
        type: {
          kind: 'typeNameTypeReference',
          id,
        },
      };
      ctx.typeMap.set(id, type);
      return {
        scopeItem: scopeItem,
        name: node.id.name,
      };
    }
    case 'SymbolDeclaration': {
      const id = `${node.id.name}$${ctx.declarationSuffix}`;
      const type: SymbolType = {
        kind: 'symbolType',
        id,
        name: node.id.name,
      };
      const scopeItem: SymbolScopeItem = {
        kind: 'symbol',
        id,
        name: node.id.name,
      };
      ctx.typeMap.set(id, type);
      return {
        scopeItem: scopeItem,
        name: node.id.name,
      };
    }
    case 'OptionDeclaration': {
      interface ChildType {
        name?: string;
        scopeItem: TypeReferenceScopeItem | SymbolScopeItem;
      }
      const resolvedOptions = node.options.map(
        (optionNode, i): ChildType => {
          if (optionNode.type === 'Identifier') {
            return {
              scopeItem: {
                kind: 'typeReference',
                type: {
                  kind: 'pendingInferenceTypeReference',
                },
              },
            };
          } else {
            return analyzeTypeDeclaration(
              optionNode,
              [...breadcrumbs, 'options', i],
              ctx
            );
          }
        }
      );
      const children = Object.fromEntries(
        resolvedOptions
          .filter((it): it is ChildType & { name: string } => 'name' in it)
          .map((it) => [it.name, it.scopeItem])
      );
      const optionTypes = resolvedOptions.map(
        (it): TypeReference => {
          switch (it.scopeItem.kind) {
            case 'typeReference':
              return it.scopeItem.type;
            case 'symbol':
              return {
                kind: 'typeNameTypeReference',
                id: it.scopeItem.id,
              };
            default: {
              const exhaustive: never = it.scopeItem;
              return exhaustive;
            }
          }
        }
      );
      const type: OptionTypeReference = {
        kind: 'optionTypeReference',
        name: node.name.name,
        options: optionTypes,
        children,
      };
      return {
        name: node.name.name,
        scopeItem: {
          kind: 'typeReference',
          type,
        },
      };
    }
    default: {
      const exhaustive: never = node;
      return exhaustive;
    }
  }
}

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
        valueType: {
          kind: 'typeNameTypeReference',
          id: INTEGER_ID,
        },
      });
      break;
    case 'StringLiteral':
      ctx.typesOfExpressions.set(toKey(breadcrumbs), {
        kind: 'valueTypeReference',
        valueType: {
          kind: 'typeNameTypeReference',
          id: STRING_ID,
        },
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
          case 'typeReference':
            type = item.type;
            break;
          case 'namedValue':
            type = item.type;
            break;
          case 'symbol':
            type = {
              kind: 'valueTypeReference',
              valueType: {
                kind: 'typeNameTypeReference',
                id: item.id,
              },
            };
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
        setTypeOfExpression(ctx, breadcrumbs, {
          kind: 'pendingInferenceTypeReference',
        });
      } else if (parentType.kind === 'optionTypeReference') {
        const child = parentType.children?.[node.property.name];
        if (child?.kind === 'typeReference') {
          setTypeOfExpression(ctx, breadcrumbs, child.type);
        } else if (child?.kind === 'symbol') {
          setTypeOfExpression(ctx, breadcrumbs, {
            kind: 'valueTypeReference',
            valueType: {
              kind: 'typeNameTypeReference',
              id: child.id,
            },
          });
        } else {
          setTypeOfExpression(ctx, breadcrumbs, {
            kind: 'typeErrorTypeReference',
            error: {
              kind: 'failedToResolveTypeError',
              name: node.property.name,
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
          if (typeSymbol?.kind === 'typeReference') {
            const typeAnnotationReference = typeSymbol.type;
            if (typeOfValue.kind === 'typeErrorTypeReference') {
              type = {
                kind: 'typeErrorTypeReference',
                error: {
                  kind: 'referenceTypeError',
                  breadcrumbs: [...statementBreadcrumbs, 'value'],
                },
              };
            } else if (!isConcrete(typeAnnotationReference)) {
              type = { kind: 'pendingInferenceTypeReference' };
            } else if (
              typeOfValue.kind !== 'pendingInferenceTypeReference' &&
              isAssignableTo(
                typeOfValue,
                typeAnnotationReference,
                ctx.typeMap
              ) &&
              (typeAnnotationReference.kind === 'typeNameTypeReference' ||
                typeAnnotationReference.kind === 'optionTypeReference')
            ) {
              type = {
                kind: 'valueTypeReference',
                valueType: typeAnnotationReference,
              };
            } else {
              type = {
                kind: 'typeErrorTypeReference',
                error: {
                  kind: 'mismatchedTypeError',
                  expectedType: typeSymbol.type,
                  actualType: typeAnnotationReference,
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

        const scopeSymbol: ScopeItem = {
          kind: 'namedValue',
          name: a.name.name,
          variable: a.variable,
          type: {
            kind: 'valueTypeReference',
            valueType: type,
          },
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
    const handlerBreadcrumbs = [...breadcrumbs, 'handlers', i];
    let patternScope;
    if (a.pattern) {
      patternScope = getPatternScope(
        a.pattern,
        [...handlerBreadcrumbs, 'pattern'],
        ctx
      );
    } else {
      patternScope = parentScope;
    }

    const handlerExpressionBreadcrumbs = [...handlerBreadcrumbs, 'body'];
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
        if (!typeInScope || !(typeInScope.kind === 'typeReference')) {
          valueType = {
            kind: 'typeErrorTypeReference',
            error: {
              kind: 'failedToResolveTypeError',
              name: typePattern.typeName.name,
            },
          };
        } else {
          valueType = typeInScope.type;
        }
      } else {
        valueType = { kind: 'pendingInferenceTypeReference' };
      }

      const symbolToAddToScope: ScopeItem = {
        kind: 'namedValue',
        name,
        type: {
          kind: 'valueTypeReference',
          valueType,
        },
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
  analyzeExpression(node.callee, [...breadcrumbs, 'callee'], ctx);
  const calleeType = ctx.typesOfExpressions.get(
    toKey([...breadcrumbs, 'callee'])
  );

  if (calleeType) {
    let returnType: TypeReference;
    switch (calleeType.kind) {
      case 'pendingInferenceTypeReference':
        returnType = {
          kind: 'pendingInferenceTypeReference',
        };
        break;
      case 'typeNameTypeReference':
        returnType = calleeType;
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
      case 'optionTypeReference':
        returnType = {
          kind: 'typeErrorTypeReference',
          error: {
            kind: 'notCallableTypeError',
          },
        };
        break;
      case 'functionTypeReference':
        returnType = calleeType.returnType;
        break;
      default:
        return exhaustiveCheck(calleeType);
    }
    ctx.typesOfExpressions.set(toKey(breadcrumbs), returnType);
  } else {
    throw new Error(
      `I was expecting this to be a function or type in scope, since it's being called; maybe it's not spelled correctly.`
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

  const effectType =
    valueType.valueType.kind === 'typeNameTypeReference'
      ? ctx.typeMap.get(valueType.valueType.id)
      : null;
  if (effectType?.kind !== 'effectType') {
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
