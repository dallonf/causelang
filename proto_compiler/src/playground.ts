const keywords = ["cause", "fn"] as const;
const keywordSet = new Set(keywords);
type KeywordLiteral = typeof keywords extends readonly (infer T)[] ? T : never;

interface Keyword {
  type: "Keyword";
  keyword: KeywordLiteral;
}

interface Identifier {
  type: "Identifier";
  name: string;
}

interface StringLiteral {
  type: "StringLiteral";
  value: string;
}

interface IntLiteral {
  type: "IntLiteral";
  value: number;
}

type Literal = StringLiteral | IntLiteral;

interface UnaryCallExpression {
  type: "UnaryCallExpression";
  callee: Expression;
  argument: Expression;
}

interface CallExpression {
  type: "CallExpression";
  callee: Expression;
  arguments: Expression[];
}

interface BlockExpression {
  type: "BlockExpression";
  body: Statement[];
}

type Expression =
  | Keyword
  | Identifier
  | Literal
  | UnaryCallExpression
  | CallExpression
  | BlockExpression;

interface ExpressionStatement {
  type: "ExpressionStatement";
  expression: Expression;
}

type Statement = ExpressionStatement;

interface FunctionDeclaration {
  type: "FunctionDeclaration";
  id: Identifier;
  body: Expression;
}

type Declaration = FunctionDeclaration;

interface Module {
  type: "Module";
  body: Declaration[];
}

const sampleAst: Module = {
  type: "Module",
  body: [
    {
      type: "FunctionDeclaration",
      id: {
        type: "Identifier",
        name: "main",
      },
      body: {
        type: "BlockExpression",
        body: [
          {
            type: "ExpressionStatement",
            expression: {
              type: "UnaryCallExpression",
              callee: {
                type: "Keyword",
                keyword: "cause",
              },
              argument: {
                type: "CallExpression",
                callee: {
                  type: "Identifier",
                  name: "Log",
                },
                arguments: [
                  {
                    type: "StringLiteral",
                    value: "Hello World",
                  },
                ],
              },
            },
          },
          {
            type: "ExpressionStatement",
            expression: {
              type: "CallExpression",
              callee: {
                type: "Identifier",
                name: "ExitCode",
              },
              arguments: [
                {
                  type: "IntLiteral",
                  value: 0,
                },
              ],
            },
          },
        ],
      },
    },
  ],
};

console.log("hello world");
