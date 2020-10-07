import { CoreFunctionValueType, LibraryValueType } from './analyzer';
import makeLibrary, { Library, mergeLibraries } from './makeLibrary';
import { CauseError, EffectHandler } from './runtime';

const coreLibrary = makeLibrary(
  {
    type: 'effect',
    name: 'Log',
    handler(effect: any) {
      console.log(effect.value);
    },
  },
  {
    type: 'effect',
    name: 'Panic',
    handler(effect: any) {
      throw new CauseError('Error while running Cause file: ' + effect.value);
    },
  }
);

export const LogEffectSymbol = coreLibrary.symbols['Log'];
export const PanicEffectSymbol = coreLibrary.symbols['Panic'];

type CoreFnMap = { [name: string]: Function };

const coreFunctions: CoreFnMap = {
  append: (x: string, y: string) => {
    return x + y;
  },
};

export interface CoreLibrary {
  runtimeScope: Record<string, symbol | Function>;
  analyzerScope: Record<string, LibraryValueType | CoreFunctionValueType>;
  handleEffects: EffectHandler;
}

export function mergeCoreLibrary(...libraries: Library[]): CoreLibrary {
  const extensionLibrary = mergeLibraries(coreLibrary, ...libraries);
  return {
    runtimeScope: {
      ...coreFunctions,
      ...extensionLibrary.symbols,
    },
    analyzerScope: {
      ...Object.fromEntries(
        Object.entries(coreFunctions).map(([k, v]) => [
          k,
          {
            kind: 'coreFn',
            name: k,
          } as CoreFunctionValueType,
        ])
      ),
      ...extensionLibrary.analyzerScope,
    },
    handleEffects: extensionLibrary.handleEffects,
  };
}

export default mergeCoreLibrary();
