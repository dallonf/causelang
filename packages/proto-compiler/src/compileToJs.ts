import { RuntimeLibraryValueType } from './analyzer';
import { cursorPosition, makeSourceStream } from './sourceStream';
import * as parser from './parser';
import * as analyzer from './analyzer';
import * as generator from './generator';
import CompilerError from './CompilerError';

export default function compileToJs(
  source: string,
  library: RuntimeLibraryValueType[]
) {
  let parsedAst;
  try {
    parsedAst = parser.parseModule(makeSourceStream(source), {});
  } catch (e) {
    if (e instanceof CompilerError) {
      const position = cursorPosition(e.cursor);
      throw new Error(
        `Compiler error at Line ${position.line}, Column ${position.column}: ${e.message}`
      );
    } else {
      throw e;
    }
  }

  const analyzerContext = analyzer.analyzeModule(parsedAst, ['main'], {
    scope: analyzer.scopeFromLibrary(library),
    expressionTypes: new Map(),
  });

  const outputSource = generator.generateModule(parsedAst, ['main'], {
    expressionTypes: analyzerContext.expressionTypes,
  });

  return outputSource;
}
