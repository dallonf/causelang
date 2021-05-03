import { cursorPosition, makeSourceStream } from './sourceStream';
import * as parser from './parser';
import * as analyzer from './analyzer';
import * as generator from './generator';
import CompilerError from './CompilerError';
import { Library } from './library';

export default function compileToJs(source: string, libraries: Library[]) {
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

  const analyzerContext = analyzer.analyzeModule(
    parsedAst,
    ['main'],
    libraries,
    {
      declarationSuffix: 'main',
    }
  );

  const outputSource = generator.generateModule(parsedAst, ['main'], {
    typesOfExpressions: analyzerContext.typesOfExpressions,
    scopes: analyzerContext.scopes,
    types: new Map(analyzerContext.typeMap.entries()),
  });

  return outputSource;
}
