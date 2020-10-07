import * as analyzer from './analyzer';
import { EffectHandler, LibrarySymbolMap } from './runtime';

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
  symbols: LibrarySymbolMap;
  analyzerScope: Record<string, analyzer.LibraryValueType>;
  handleEffects: EffectHandler;
}

export default function makeLibrary(...items: LibraryItem[]): Library {
  const itemsAndSymbols = items.map((item) => ({
    item,
    symbol: Symbol(item.name),
  }));

  const symbols = Object.fromEntries(
    itemsAndSymbols.map(({ item, symbol }) => [item.name, symbol])
  );
  const analyzerScope = Object.fromEntries(
    itemsAndSymbols.map(({ item, symbol }) => [
      item.name,
      {
        kind: item.type,
        name: item.name,
        symbol,
      },
    ])
  );
  const handleEffects: EffectHandler = (e: any) => {
    const effectItem = itemsAndSymbols.find((a) => a.symbol === e.type);
    if (effectItem && effectItem.item.type === 'effect') {
      return {
        handled: true,
        value: effectItem.item.handler(e),
      };
    }
  };
  return {
    symbols,
    analyzerScope,
    handleEffects,
  };
}

export function mergeLibraries(...libraries: Library[]): Library {
  return {
    symbols: Object.fromEntries(
      libraries.flatMap((a) => Object.entries(a.symbols))
    ),
    analyzerScope: Object.fromEntries(
      libraries.flatMap((a) => Object.entries(a.analyzerScope))
    ),
    handleEffects: (e) => {
      for (const handler of libraries.map((a) => a.handleEffects).reverse()) {
        const result = handler(e);
        if (result?.handled) {
          return result;
        }
      }
    },
  };
}

export const emptyLibrary: Library = {
  symbols: {},
  analyzerScope: {},
  handleEffects: () => undefined,
};
