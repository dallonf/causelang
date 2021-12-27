import { Library } from './library';
import { EffectHandler } from './runtime';
import {
  EffectType,
  isConcrete,
  ObjectType,
  TypeMap,
  TypeReference,
  ValueTypeReference,
} from './typeSystem';
import { exhaustiveCheck } from './utils';
import * as context from './context';

type LibraryType = ObjectType | EffectType;

interface ObjectTypeLibraryItem {
  type: 'objectType';
  name: string;
  fields: Record<string, ValueTypeReference>;
}

interface EffectLibraryItem {
  type: 'effect';
  name: string;
  params: Record<string, ValueTypeReference>;
  returnType: ValueTypeReference;
  handler: (e: any) => Promise<any> | any;
}

interface NativeFnLibraryItem {
  type: 'nativeFn';
  name: string;
  params: Record<string, ValueTypeReference>;
  returnType: ValueTypeReference;
  handler: (...params: any[]) => any;
}

export type LibraryItem =
  | ObjectTypeLibraryItem
  | EffectLibraryItem
  | NativeFnLibraryItem;

export interface RuntimeLibrary {
  libraryData: Library;
  handleEffects: EffectHandler;
  scopeValues: Record<string, unknown>;
}

export default function makeLibrary(
  libraryName: string,
  ...items: LibraryItem[]
): RuntimeLibrary {
  const types: TypeMap = new Map(
    items
      .map((item) => {
        const id = `${item.name}$${libraryName}`;
        let value: LibraryType | null;
        if (item.type === 'objectType') {
          value = {
            kind: 'objectType',
            id,
            name: item.name,
            fields: item.fields,
          };
        } else if (item.type === 'effect') {
          value = {
            kind: 'effectType',
            id,
            name: item.name,
            parameters: item.params,
            returnType: item.returnType,
          };
        } else if (item.type === 'nativeFn') {
          return null;
        } else {
          return exhaustiveCheck(item);
        }
        return value;
      })
      .filter((x): x is LibraryType => Boolean(x))
      .map((x) => [x.id, x])
  );

  type ScopeItem = {
    symbol: context.ScopeSymbol;
    value: unknown;
  };
  const wrapNativeFn = (x: NativeFnLibraryItem['handler']) =>
    function* (...args: any[]) {
      return x(...args);
    };
  const scope: Record<string, ScopeItem> = Object.fromEntries(
    ([...types.values()] as LibraryType[])
      .map((type): [string, ScopeItem] => {
        switch (type.kind) {
          case 'effectType':
            return [
              type.name,
              {
                symbol: {
                  kind: 'typeReference',
                  name: type.name,
                  type: {
                    kind: 'typeNameTypeReference',
                    id: type.id,
                  },
                },
                value: {
                  kind: 'typeNameTypeReference',
                  id: type.id,
                } as TypeReference,
              },
            ];
          case 'objectType':
            return [
              type.name,
              {
                symbol: {
                  kind: 'typeReference',
                  name: type.name,
                  type: {
                    kind: 'typeNameTypeReference',
                    id: type.id,
                  },
                },
                value: {
                  kind: 'typeNameTypeReference',
                  id: type.id,
                } as TypeReference,
              },
            ];
          default:
            return exhaustiveCheck(type);
        }
      })
      .concat(
        items
          .filter(
            (item): item is NativeFnLibraryItem => item.type === 'nativeFn'
          )
          .map((item): [string, ScopeItem] => {
            return [
              item.name,
              {
                symbol: {
                  kind: 'namedValue',
                  variable: false,
                  name: item.name,
                  type: {
                    kind: 'valueTypeReference',
                    valueType: {
                      kind: 'functionTypeReference',
                      name: item.name,
                      params: item.params,
                      returnType: item.returnType,
                    },
                  },
                },
                value: wrapNativeFn(item.handler),
              },
            ];
          })
      )
  );

  const effectHandlers = new Map(
    items
      .filter((item): item is EffectLibraryItem => item.type === 'effect')
      .map((item) => {
        const type = ([...types.values()] as EffectType[]).find(
          (x) => x.name === item.name
        )!;
        return [type.id, item.handler];
      })
  );
  const handleEffects: EffectHandler = (e: any) => {
    const effectHandler = effectHandlers.get(e.type);
    if (effectHandler) {
      return {
        handled: true,
        value: effectHandler(e),
      };
    }
  };

  const scopeSymbols = Object.fromEntries(
    Object.entries(scope).map(([k, v]) => [k, v.symbol])
  );
  const scopeValues = Object.fromEntries(
    Object.entries(scope).map(([k, v]) => [k, v.value])
  );

  return {
    libraryData: {
      name: libraryName,
      types,
      scope: scopeSymbols,
    },
    handleEffects,
    scopeValues,
  };
}

export const idFromLibrary = (
  name: string,
  library: RuntimeLibrary
): string => {
  const data = library.libraryData;
  const type = data.scope[name];
  if (
    type?.kind === 'typeReference' &&
    type.type.kind === 'typeNameTypeReference'
  ) {
    return type.type.id;
  } else if (type?.kind === 'namedValue') {
    const valueType = type.type.valueType;
    if (valueType.kind === 'typeNameTypeReference') {
      return valueType.id;
    }
  } else if (type?.kind === 'symbol') {
    return type.id;
  }

  throw new Error(`Couldn't find an ID for ${name} in ${data.name}`);
};
