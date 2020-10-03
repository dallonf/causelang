import * as path from 'path';
import * as fs from 'fs';
import * as runtime from './runtime';
import { assert } from 'console';
import compileToJs from './compileToJs';

const source = fs.readFileSync(
  path.join(__dirname, 'fixtures/01_helloworld.cau'),
  'utf-8'
);

const ExitCodeSymbol = Symbol('ExitCode');
interface ExitCodeType {
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

const outputSource = compileToJs(source, library);

const execute = async () => {
  const causeRuntime = new runtime.CauseRuntime(
    outputSource,
    '01_helloworld.cau',
    {
      library,
    }
  );
  const exitCode: ExitCodeType = await causeRuntime.invokeFn('main', []);
  assert(exitCode.type === ExitCodeSymbol);

  console.log('Exit code', exitCode.value);
};

execute().catch((err) => {
  console.error(err);
  process.exit(1);
});
