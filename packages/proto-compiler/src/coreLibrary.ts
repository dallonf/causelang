import { CauseError } from './runtime';
import makeLibrary, { idFromLibrary } from './runtimeLibrary';

export const STRING_ID = 'core$String';
export const INTEGER_ID = 'core$Integer';
export const ACTION_ID = 'core$Action';
export const NEVER_ID = 'core$Never';
export const BOOLEAN_ID = 'core$Boolean';
export const UNKNOWN_ID = 'core$Unknown';

export const coreOperationsLibrary = makeLibrary(
  'core',
  {
    type: 'effect',
    name: 'Print',
    params: {
      message: {
        kind: 'valueTypeReference',
        id: STRING_ID,
      },
    },
    returnType: {
      kind: 'valueTypeReference',
      id: ACTION_ID,
    },
    handler(effect: any) {
      console.log(effect.value.message);
    },
  },
  {
    type: 'effect',
    name: 'Debug',
    params: {
      value: {
        kind: 'valueTypeReference',
        id: UNKNOWN_ID,
      },
    },
    returnType: {
      kind: 'valueTypeReference',
      id: ACTION_ID,
    },
    handler(effect: any) {
      console.dir(effect.value.value);
    },
  },
  {
    type: 'effect',
    name: 'Panic',
    params: {
      message: {
        kind: 'valueTypeReference',
        id: STRING_ID,
      },
    },
    returnType: {
      kind: 'valueTypeReference',
      id: NEVER_ID,
    },
    handler(effect: any) {
      throw new CauseError(
        'Error while running Cause file: ' + effect.value.message
      );
    },
  },
  {
    type: 'nativeFn',
    name: 'append',
    params: {
      x: {
        kind: 'valueTypeReference',
        id: STRING_ID,
      },
      y: {
        kind: 'valueTypeReference',
        id: STRING_ID,
      },
    },
    returnType: {
      kind: 'valueTypeReference',
      id: STRING_ID,
    },
    handler: (x: string, y: string) => {
      return x + y;
    },
  },
  {
    type: 'nativeFn',
    name: 'equals',
    params: {
      // TODO: this might wind up needing to be generic!
      x: {
        kind: 'valueTypeReference',
        id: STRING_ID,
      },
      y: {
        kind: 'valueTypeReference',
        id: STRING_ID,
      },
    },
    returnType: {
      kind: 'valueTypeReference',
      id: BOOLEAN_ID,
    },
    handler: (x: string, y: string) => {
      return x === y;
    },
  }
);

export const allCoreLibraries = [coreOperationsLibrary];

export const PrintEffectID = idFromLibrary('Print', coreOperationsLibrary);
export const DebugEffectID = idFromLibrary('Debug', coreOperationsLibrary);
export const PanicEffectID = idFromLibrary('Panic', coreOperationsLibrary);
