import { TypeReference } from './typeSystem';

export type ScopeSymbol = DeclarationScopeSymbol | CoreFunctionScopeSymbol;

export type DeclarationScopeSymbol =
  | EffectScopeSymbol
  | FunctionScopeSymbol
  | TypeScopeSymbol
  | NamedValueScopeSymbol;

export type LibraryScopeSymbol =
  | TypeScopeSymbol
  | EffectScopeSymbol
  | CoreFunctionScopeSymbol;

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
  kind: 'objectType';
  name: string;
  id: string;
}

export interface NamedValueScopeSymbol {
  kind: 'namedValue';
  name: string;
  valueType: TypeReference;
  variable: boolean;
}

export interface CoreFunctionScopeSymbol {
  kind: 'coreFn';
  name: string;
  id: string;
}

export type Scope = Record<string, ScopeSymbol>;

export type Breadcrumbs = (string | number)[];
export type ScopeMap = Map<string, Readonly<Scope>>;

export const toKey = (breadcrumbs: Breadcrumbs) => breadcrumbs.join('.');

export const findScope = (
  breadcrumbs: Breadcrumbs,
  scopeMap: ScopeMap
): Readonly<Scope> => {
  if (breadcrumbs.length == 0) {
    throw new Error('could not find a scope');
  }
  const potentialScope = scopeMap.get(toKey(breadcrumbs));
  if (potentialScope) {
    // cache this value at the current key to speed up future requests
    scopeMap.set(toKey(breadcrumbs), potentialScope);
    return potentialScope;
  } else {
    return findScope(breadcrumbs.slice(0, -1), scopeMap);
  }
};

export const findInScope = (
  identifier: string,
  breadcrumbs: Breadcrumbs,
  scopeMap: ScopeMap
): ScopeSymbol | undefined => {
  const scope = findScope(breadcrumbs, scopeMap);
  return scope[identifier];
};
