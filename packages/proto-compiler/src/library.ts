import * as context from './context';
import { EffectHandler } from './runtime';
import { CauseType, TypeMap, ValueTypeReference } from './typeSystem';

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

interface CoreFnLibraryItem {
  type: 'coreFn';
  name: string;
  params: Record<string, ValueTypeReference>;
  returnType: ValueTypeReference;
  handler: (...params: any[]) => any;
}

export type LibraryItem =
  | ObjectTypeLibraryItem
  | EffectLibraryItem
  | CoreFnLibraryItem;

export interface Library {
  name: string;
  types: TypeMap;
  scope: Record<string, context.LibraryScopeSymbol>;
  handleEffects: EffectHandler;
}

const exhaustiveCheck = (input: never): never => {
  throw new Error(`unexpected type: ${input}`);
};

export default function makeLibrary(
  libraryName: string,
  ...items: LibraryItem[]
): Library {
  const itemsAndIds = items.map((item) => ({
    item,
    id: `${item.name}$${libraryName}`,
  }));

  const types: TypeMap = new Map(
    itemsAndIds.map(({ item, id }) => {
      const key = id;
      let value: CauseType;
      if (item.type === 'objectType') {
        value = {
          kind: 'objectType',
          id,
          fields: item.fields,
        };
      } else if (item.type === 'effect') {
        value = {
          kind: 'effectType',
          id,
          parameters: item.params,
          returnType: item.returnType,
        };
      } else if (item.type === 'coreFn') {
        value = {
          kind: 'coreFunctionType',
          id,
          parameters: item.params,
          returnType: item.returnType,
        };
      } else {
        return exhaustiveCheck(item);
      }
      return [key, value];
    })
  );

  const scope = Object.fromEntries(
    itemsAndIds.map(({ item, id }) => {
      const key = item.name;
      let value: context.LibraryScopeSymbol;
      if (item.type === 'objectType') {
        value = {
          kind: 'objectType',
          id,
          name: item.name,
        };
      } else if (item.type === 'effect') {
        value = {
          kind: 'effect',
          id,
          name: item.name,
        };
      } else if (item.type === 'coreFn') {
        value = {
          kind: 'coreFn',
          id,
          name: item.name,
        };
      } else {
        return exhaustiveCheck(item);
      }
      return [key, value];
    })
  );
  const handleEffects: EffectHandler = (e: any) => {
    const effectItem = itemsAndIds.find((a) => a.id === e.type);
    if (effectItem && effectItem.item.type === 'effect') {
      return {
        handled: true,
        value: effectItem.item.handler(e),
      };
    }
  };
  return {
    name: libraryName,
    types,
    scope: scope,
    handleEffects,
  };
}

export const idFromLibrary = (name: string, library: Library): string => {
  const type = library.scope[name];
  if (!type) {
    throw new Error(`Couldn't find an ID for ${name} in ${library.name}`);
  }
  return type.id;
};
