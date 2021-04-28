import * as analyzer from './analyzer';
import * as context from './context';
import { EffectHandler, LibraryIDMap } from './runtime';

interface TypeLibraryItem {
  type: 'type';
  name: string;
}

interface EffectLibraryItem {
  type: 'effect';
  name: string;
  handler: (e: any) => Promise<any> | any;
}

export type LibraryItem = TypeLibraryItem | EffectLibraryItem;

export interface Library {
  name: string;
  ids: LibraryIDMap;
  analyzerScope: Record<string, context.LibraryScopeSymbol>;
  handleEffects: EffectHandler;
}

export default function makeLibrary(
  libraryName: string,
  ...items: LibraryItem[]
): Library {
  const itemsAndIds = items.map((item) => ({
    item,
    id: `${item.name}$${libraryName}`,
  }));

  const ids = Object.fromEntries(
    itemsAndIds.map(({ item, id }) => [item.name, id])
  );
  const analyzerScope = Object.fromEntries(
    itemsAndIds.map(({ item, id }) => [
      item.name,
      {
        kind: item.type,
        name: item.name,
        id,
      },
    ])
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
    ids,
    analyzerScope,
    handleEffects,
  };
}
