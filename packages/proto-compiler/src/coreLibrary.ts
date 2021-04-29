import makeLibrary, { idFromLibrary } from './library';
import { CauseError } from './runtime';

export const STRING_ID = 'core$String';
export const INTEGER_ID = 'core$Integer';
export const ACTION_ID = 'core$Action';
export const NEVER_ID = 'core$NEVER';
export const BOOLEAN_ID = 'core$Boolean';

export const coreOperationsLibrary = makeLibrary(
  'core',
  {
    type: 'effect',
    name: 'Print',
    params: {
      value: {
        kind: 'valueTypeReference',
        id: STRING_ID,
      },
    },
    returnType: {
      kind: 'valueTypeReference',
      id: ACTION_ID,
    },
    handler(effect: any) {
      console.log(effect.value);
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
      throw new CauseError('Error while running Cause file: ' + effect.value);
    },
  },
  {
    type: 'coreFn',
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
    type: 'coreFn',
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
      return x + y;
    },
  }
);

export const allCoreLibraries = [coreOperationsLibrary];

/**
 * @deprecated
 */
export const PrintEffectID = idFromLibrary('Print', coreOperationsLibrary);
/**
 * @deprecated
 */
export const PanicEffectID = idFromLibrary('Panic', coreOperationsLibrary);
