import * as path from 'path';
import * as fs from 'fs';
import * as analyzer from './analyzer';
import * as runtime from './runtime';
import { cursorPosition, makeSourceStream, remainder } from './sourceStream';
import CompilerError from './CompilerError';
import * as parser from './parser';
import * as generator from './generator';
import { assert } from 'console';

const source = fs.readFileSync(
  path.join(__dirname, 'fixtures/01_helloworld.cau'),
  'utf-8'
);

let parsedAst;
try {
  parsedAst = parser.parseModule(makeSourceStream(source), {});
} catch (e) {
  if (e instanceof CompilerError) {
    const position = cursorPosition(e.cursor);
    console.error(
      `Compiler error at Line ${position.line}, Column ${position.column}`
    );
    console.error('Remainder of code:\n', remainder(e.cursor));
  }
  throw e;
}

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

const analyzerContext = analyzer.analyzeModule(parsedAst, ['main'], {
  scope: analyzer.scopeFromLibrary(library),
  expressionTypes: new Map(),
});

const outputSource = generator.generateModule(parsedAst, ['main'], {
  expressionTypes: analyzerContext.expressionTypes,
});

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
