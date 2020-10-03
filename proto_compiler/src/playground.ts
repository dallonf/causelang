import * as path from 'path';
import * as fs from 'fs';
import * as analyzer from './analyzer';
import { cursorPosition, makeSourceStream, remainder } from './sourceStream';
import CompilerError from './CompilerError';
import * as parser from './parser';
import * as generator from './generator';

type Breadcrumbs = analyzer.Breadcrumbs;

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

const rootScope: analyzer.Scope = {
  Log: {
    kind: 'effect',
    name: 'Log',
  },
  ExitCode: {
    kind: 'effect',
    name: 'ExitCode',
  },
};

const analyzerContext = analyzer.analyzeModule(parsedAst, ['main'], {
  scope: rootScope,
  expressionTypes: new Map(),
});

const outputSource = generator.generateModule(parsedAst, ['main'], {
  expressionTypes: analyzerContext.expressionTypes,
});

console.log(outputSource);
