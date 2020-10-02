import * as path from 'path';
import * as fs from 'fs';
import generate from '@babel/generator';
import * as jsAst from '@babel/types';
import * as ast from './ast';
import * as typedAst from './typedAst';
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
  [name: string]: typedAst.ValueType;
}

interface CrawlerContext {
  scope: Scope;
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

const crawlModule = (
  module: ast.Module,
  ctx: CrawlerContext
): typedAst.Typed<ast.Module> => {
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

  return {
    ...module,
    body: module.body.map((declaration) => {
      return crawlFunctionDeclaration(declaration, {
        ...ctx,
        scope: newScope,
      });
    }),
  };
};

const crawlFunctionDeclaration = (
  node: ast.FunctionDeclaration,
  ctx: CrawlerContext
): typedAst.Typed<ast.FunctionDeclaration> => {
  return {
    ...node,
    body: crawlExpression(node.body, ctx),
  };
};

const crawlExpression = (
  node: ast.Expression,
  ctx: CrawlerContext
): typedAst.Typed<ast.Expression> => {
  switch (node.type) {
    case 'BlockExpression': {
      // TODO: this is gonna get tricky with scope when variable declarations
      // are a thing
      return {
        ...node,
        body: node.body.map((a: ast.ExpressionStatement) => ({
          ...a,
          expression: crawlExpression(a.expression, ctx),
        })),
      };
    }
    case 'CallExpression': {
      const { callee } = node;
      let typedCallee: typedAst.TypedExpression;
      switch (callee.type) {
        case 'Identifier': {
          const type: typedAst.ValueType | undefined = ctx.scope[callee.name];
          if (!type) {
            `I was expecting "${callee.name}" to be a type in scope; maybe it's not spelled correctly.`;
          }
          typedCallee = {
            ...crawlExpression(callee, ctx),
            returnType: type,
          };
          break;
        }
        case 'Keyword':
          typedCallee = {
            ...crawlExpression(callee, ctx),
            returnType: {
              kind: 'keyword',
              keyword: callee.keyword,
            },
          };
          break;
        default:
          throw new Error(
            `I don't know how to crawl function calls like this yet. The technical name for this sort of callee is ${callee.type}`
          );
      }
      const args = node.arguments.map((a) => crawlExpression(a, ctx));

      return {
        ...node,
        arguments: args,
        callee: typedCallee,
      };
    }
    default:
      return node;
  }
};

const generateModule = (module: typedAst.Typed<ast.Module>) => {
  const program = jsAst.program([]);

  const statements = module.body.map((a, i) => generateDeclaration(a));
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

const generateDeclaration = (node: typedAst.Typed<ast.FunctionDeclaration>) => {
  let bodyStatements;
  if (node.body.type === 'BlockExpression') {
    const cauStatements = node.body.body;
    bodyStatements = cauStatements.map((a, i) => {
      const statement = generateStatement(a);
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
    bodyStatements = [jsAst.returnStatement(generateExpression(node.body))];
  }

  return jsAst.functionDeclaration(
    jsAst.identifier(node.id.name),
    [],
    jsAst.blockStatement(bodyStatements),
    true
  );
};

const generateStatement = (node: typedAst.Typed<ast.Statement>) => {
  return jsAst.expressionStatement(generateExpression(node.expression));
};

const generateExpression = (
  node: typedAst.Typed<ast.Expression>
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
        if (node.arguments.length !== 1) {
          throw new Error('"cause" should only have one parameter');
        }

        return jsAst.yieldExpression(generateExpression(node.arguments[0]));
      }

      return jsAst.callExpression(
        generateExpression(node.callee),
        node.arguments.map(generateExpression)
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

const crawledAst = crawlModule(parsedAst, {
  scope: rootScope,
});

const program = generateModule(crawledAst);

const outputSource = generate(program).code;

console.log(outputSource);
