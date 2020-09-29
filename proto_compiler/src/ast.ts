const KEYWORDS = ['cause', 'fn'] as const;
export const keywordSet = new Set(KEYWORDS);
export type KeywordLiteral = typeof KEYWORDS extends readonly (infer T)[]
  ? T
  : never;

export interface Keyword {
  type: 'Keyword';
  keyword: KeywordLiteral;
}

export interface Identifier {
  type: 'Identifier';
  name: string;
}

export interface StringLiteral {
  type: 'StringLiteral';
  value: string;
}

export interface IntLiteral {
  type: 'IntLiteral';
  value: number;
}

export type Literal = StringLiteral | IntLiteral;

export interface UnaryCallExpression {
  type: 'UnaryCallExpression';
  callee: Expression;
  argument: Expression;
}

export interface CallExpression {
  type: 'CallExpression';
  callee: Expression;
  arguments: Expression[];
}

export interface BlockExpression {
  type: 'BlockExpression';
  body: Statement[];
}

export type Expression =
  | Keyword
  | Identifier
  | Literal
  | UnaryCallExpression
  | CallExpression
  | BlockExpression;

export interface ExpressionStatement {
  type: 'ExpressionStatement';
  expression: Expression;
}

export type Statement = ExpressionStatement;

export interface FunctionDeclaration {
  type: 'FunctionDeclaration';
  id: Identifier;
  arguments: void[];
  body: Expression;
}

export type Declaration = FunctionDeclaration;

export interface Module {
  type: 'Module';
  body: Declaration[];
}

export type ASTRoot = Module;
