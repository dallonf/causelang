import * as runtime from 'causelang-proto-compiler/src/runtime';

export const ExitCodeSymbol = Symbol('ExitCode');
export interface ExitCodeType {
  type: typeof ExitCodeSymbol;
  value: number;
}

const library: runtime.LibraryItem[] = [
  {
    kind: 'type',
    name: 'ExitCode',
    symbol: ExitCodeSymbol,
  },
];
export default library;
