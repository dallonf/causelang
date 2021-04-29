import { cursorPosition, makeSourceStream } from './sourceStream';
import * as parser from './parser';
import * as analyzer from './analyzer';
import * as generator from './generator';
import * as context from './context';
import CompilerError from './CompilerError';
import { allCoreLibraries } from './coreLibrary';

export default function compileToJs(
  source: string,
  analyzerScope?: context.Scope
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
    scope: {
      ...Object.fromEntries(
        allCoreLibraries.flatMap((x) => Object.entries(x.scope))
      ),
      ...analyzerScope,
    },
  });

  const outputSource = generator.generateModule(parsedAst, ['main'], {
    typesOfExpressions: analyzerContext.typesOfExpressions,
    scopes: analyzerContext.scopes,
    // TODO
    types: new Map(),
  });

  return outputSource;
}
