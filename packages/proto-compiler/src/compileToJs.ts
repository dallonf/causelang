import { cursorPosition, makeSourceStream } from './sourceStream';
import * as parser from './parser';
import * as analyzer from './analyzer';
import * as generator from './generator';
import CompilerError from './CompilerError';
import coreLibrary from './coreLibrary';

export default function compileToJs(
  source: string,
  analyzerScope?: analyzer.Scope
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
    declarationSuffix: 'main',
    scope: { ...coreLibrary.analyzerScope, ...analyzerScope },
    typesOfExpressions: new Map(),
    resolvedSymbols: new Map(),
  });

  const outputSource = generator.generateModule(parsedAst, ['main'], {
    typesOfExpressions: analyzerContext.typesOfExpressions,
    resolvedSymbols: analyzerContext.resolvedSymbols,
  });

  return outputSource;
}
