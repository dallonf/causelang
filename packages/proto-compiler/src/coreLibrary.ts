import makeLibrary from './makeLibrary';
import { CauseError } from './runtime';

const coreLibrary = makeLibrary(
  'core',
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
export default coreLibrary;

export const LogEffectID = coreLibrary.ids['Log'];
export const PanicEffectID = coreLibrary.ids['Panic'];

type CoreFnMap = { [name: string]: Function };

export const coreFunctions: CoreFnMap = {
  append: (x: string, y: string) => {
    return x + y;
  },
};
