import { RuntimeLibraryValueType } from './analyzer';

export const LogEffectSymbol = Symbol('Log');
export const PanicEffectSymbol = Symbol('Panic');

const coreLibrary: RuntimeLibraryValueType[] = [
  {
    kind: 'effect',
    name: 'Log',
    symbol: LogEffectSymbol,
  },
  {
    kind: 'effect',
    name: 'Panic',
    symbol: PanicEffectSymbol,
  },
];

export default coreLibrary;
