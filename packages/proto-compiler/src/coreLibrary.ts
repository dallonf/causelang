import makeLibrary from './makeLibrary';
import { CauseError } from './runtime';

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

export default coreLibrary;
