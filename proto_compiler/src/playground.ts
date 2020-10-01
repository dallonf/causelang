import * as path from 'path';
import * as fs from 'fs';
import generate from '@babel/generator';
import * as jsAst from '@babel/types';
import * as ast from './ast';
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

const generateModule = (module: ast.Module) => {
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

const generateDeclaration = (node: ast.FunctionDeclaration) => {
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

const generateStatement = (node: ast.Statement) => {
  return jsAst.expressionStatement(generateExpression(node.expression));
};

const generateExpression = (node: ast.Expression): jsAst.Expression => {
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
      const args = node.arguments.map(generateExpression);
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
  }
  return jsAst.stringLiteral('hi');
};

const program = generateModule(parsedAst);

const outputSource = generate(program).code;

console.log(outputSource);
