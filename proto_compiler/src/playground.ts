import * as path from 'path';
import * as fs from 'fs';
import generate from '@babel/generator';
import * as jsAst from '@babel/types';
import * as ast from './ast';
import * as analyzer from './analyzer';
import {
  cursorPosition,
  makeSourceStream,
  nextChar,
  remainder,
  SourceStream,
} from './sourceStream';
import { advanceLine, readIdentifier, skipWhitespace } from './readToken';
import CompilerError from './CompilerError';
import { parseModule } from './parser';
import { exhaustiveCheck } from './utils';

const source = fs.readFileSync(
  path.join(__dirname, 'fixtures/01_helloworld.cau'),
  'utf-8'
);

let parsedAst;
try {
  parsedAst = parseModule(makeSourceStream(source), {});
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

interface Scope {
  [name: string]: analyzer.ValueType;
}

interface AnalyzerContext {
  scope: Scope;
  expressionTypes: Map<string, analyzer.ValueType>;
}

const rootScope: Scope = {
  Log: {
    kind: 'effect',
    name: 'Log',
  },
  ExitCode: {
    kind: 'effect',
    name: 'ExitCode',
  },
};

type Breadcrumbs = (string | number)[];

const analyzeModule = (
  module: ast.Module,
  breadcrumbs: Breadcrumbs,
  ctx: AnalyzerContext
): AnalyzerContext => {
  // First, superficially check all the declarations to see what's hoisted
  // into scope
  const newScope: Scope = { ...ctx.scope };
  for (const declaration of module.body) {
    if (declaration.type === 'FunctionDeclaration') {
      newScope[declaration.id.name] = {
        kind: 'fn',
        name: declaration.id.name,
      };
    }
  }

  module.body.forEach((declaration, i) => {
    analyzeFunctionDeclaration(declaration, [...breadcrumbs, 'body', i], {
      ...ctx,
      scope: newScope,
    });
  });

  return ctx;
};

const analyzeFunctionDeclaration = (
  node: ast.FunctionDeclaration,
  breadcrumbs: Breadcrumbs,
  ctx: AnalyzerContext
): void => {
  analyzeExpression(node.body, [...breadcrumbs, 'body'], ctx);
};

const analyzeExpression = (
  node: ast.Expression,
  breadcrumbs: Breadcrumbs,
  ctx: AnalyzerContext
): void => {
  switch (node.type) {
    case 'BlockExpression': {
      // TODO: this is gonna get tricky with scope when variable declarations
      // are a thing
      node.body.forEach((a: ast.ExpressionStatement, i) => {
        analyzeExpression(
          a.expression,
          [...breadcrumbs, 'body', i, 'expression'],
          ctx
        );
      });
      break;
    }
    case 'CallExpression': {
      const { callee } = node;
      let calleeType: analyzer.ValueType;
      switch (callee.type) {
        case 'Identifier': {
          const type: analyzer.ValueType | undefined = ctx.scope[callee.name];
          if (!type) {
            throw new Error(
              `I was expecting "${callee.name}" to be a type in scope; maybe it's not spelled correctly.`
            );
          }
          calleeType = type;
          break;
        }
        case 'Keyword':
          calleeType = {
            kind: 'keyword',
            keyword: callee.keyword,
          };
          break;
        default:
          throw new Error(
            `I don't know how to analyze function calls like this yet. The technical name for this sort of callee is ${callee.type}`
          );
      }

      ctx.expressionTypes.set([...breadcrumbs, 'callee'].join('.'), calleeType);

      node.parameters.forEach((a, i) =>
        analyzeExpression(a, [...breadcrumbs, 'parameters', i], ctx)
      );
    }
  }
};

interface GeneratorContext {
  expressionTypes: Map<string, analyzer.ValueType>;
}

const generateModule = (
  module: ast.Module,
  breadcrumbs: Breadcrumbs,
  ctx: GeneratorContext
) => {
  const program = jsAst.program([]);

  const statements = module.body.map((a, i) =>
    generateDeclaration(a, [...breadcrumbs, 'body', i], ctx)
  );
  program.body.push(...statements);

  // runtime call to invoke the main function
  const entryExpression = jsAst.expressionStatement(
    jsAst.callExpression(
      jsAst.memberExpression(
        jsAst.identifier('CauseRuntime'),
        jsAst.identifier('invokeEntry')
      ),
      [jsAst.identifier('main')]
    )
  );
  program.body.push(entryExpression);

  return program;
};

const generateDeclaration = (
  node: ast.FunctionDeclaration,
  breadcrumbs: Breadcrumbs,
  ctx: GeneratorContext
) => {
  let bodyStatements;
  if (node.body.type === 'BlockExpression') {
    const cauStatements = node.body.body;
    bodyStatements = cauStatements.map((a, i) => {
      const statement = generateStatement(
        a,
        [...breadcrumbs, 'body', 'body', i],
        ctx
      );
      if (
        statement.type === 'ExpressionStatement' &&
        i === cauStatements.length - 1
      ) {
        return jsAst.returnStatement(statement.expression);
      } else {
        return statement;
      }
    });
  } else {
    bodyStatements = [
      jsAst.returnStatement(
        generateExpression(node.body, [...breadcrumbs, 'body'], ctx)
      ),
    ];
  }

  return jsAst.functionDeclaration(
    jsAst.identifier(node.id.name),
    [],
    jsAst.blockStatement(bodyStatements),
    true
  );
};

const generateStatement = (
  node: ast.Statement,
  breadcrumbs: Breadcrumbs,
  ctx: GeneratorContext
) => {
  return jsAst.expressionStatement(
    generateExpression(node.expression, [...breadcrumbs, 'expression'], ctx)
  );
};

const generateExpression = (
  node: ast.Expression,
  breadcrumbs: Breadcrumbs,
  ctx: GeneratorContext
): jsAst.Expression => {
  switch (node.type) {
    case 'Identifier': {
      return jsAst.identifier(node.name);
    }
    case 'StringLiteral': {
      return jsAst.stringLiteral(node.value);
    }
    case 'IntLiteral': {
      return jsAst.numericLiteral(0);
    }
    case 'CallExpression': {
      if (node.callee.type === 'Keyword' && node.callee.keyword === 'cause') {
        if (node.parameters.length !== 1) {
          throw new Error('"cause" should only have one parameter');
        }

        return jsAst.yieldExpression(
          generateExpression(
            node.parameters[0],
            [...breadcrumbs, 'parameters', 0],
            ctx
          )
        );
      }

      const type = ctx.expressionTypes.get(
        [...breadcrumbs, 'callee'].join('.')
      );
      if (!type) {
        throw new Error(
          `I'm confused. I'm trying to figure out the type of this function call, but I don't know what it is. This probably isn't your fault! Here's the technical breadcrumb to the call in question: ${breadcrumbs.join(
            '.'
          )}`
        );
      }
      if (type.kind === 'effect') {
        if (node.parameters.length !== 1) {
          throw new Error('Effects can only have one parameter for now');
        }
        return jsAst.objectExpression([
          jsAst.objectProperty(
            jsAst.identifier('type'),
            // TODO: The type name might be out of scope...
            jsAst.identifier(type.name)
          ),
          jsAst.objectProperty(
            jsAst.identifier('value'),
            generateExpression(
              node.parameters[0],
              [...breadcrumbs, 'parameters', 0],
              ctx
            )
          ),
        ]);
      }

      return jsAst.callExpression(
        generateExpression(node.callee, [...breadcrumbs, 'callee'], ctx),
        node.parameters.map((a, i) =>
          generateExpression(a, [...breadcrumbs, 'parameters', i], ctx)
        )
      );
    }
    case 'BlockExpression': {
      throw new Error(
        "I don't know how to compile inline block expressions yet"
      );
    }
    case 'Keyword': {
      throw new Error(
        `${node.keyword} is a keyword, which you can't use here; if this is a variable, try renaming it`
      );
    }
    default:
      return exhaustiveCheck(node);
  }
};

const analyzerContext = analyzeModule(parsedAst, ['main'], {
  scope: rootScope,
  expressionTypes: new Map(),
});

const program = generateModule(parsedAst, ['main'], {
  expressionTypes: analyzerContext.expressionTypes,
});

const outputSource = generate(program).code;

console.log(outputSource);
