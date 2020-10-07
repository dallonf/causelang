const KEYWORDS = ['cause', 'fn'] as const;
export const keywordSet = new Set(KEYWORDS);
export type KeywordValue = typeof KEYWORDS extends readonly (infer T)[]
  ? T
  : never;

export interface Keyword {
  type: 'Keyword';
  keyword: KeywordValue;
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

export interface CallExpression {
  type: 'CallExpression';
  callee: Expression;
  parameters: Expression[];
}

export interface PrefixOperatorExpression {
  type: 'PrefixOperatorExpression';
  operator: Keyword;
  expression: Expression;
}

export interface BlockExpression {
  type: 'BlockExpression';
  body: Statement[];
}

export type Expression =
  | Identifier
  | Literal
  | CallExpression
  | BlockExpression
  | PrefixOperatorExpression;

export interface ExpressionStatement {
  type: 'ExpressionStatement';
  expression: Expression;
}

export interface NameDeclarationStatement {
  type: 'NameDeclarationStatement';
  name: Identifier;
  value: Expression;
}

export type Statement = ExpressionStatement | NameDeclarationStatement;

export interface FunctionDeclaration {
  type: 'FunctionDeclaration';
  id: Identifier;
  parameters: void[];
  body: Expression;
}

export type Declaration = FunctionDeclaration;

export interface Module {
  type: 'Module';
  body: Declaration[];
}

export type ASTRoot = Module;

export type Node =
  | Module
  | Declaration
  | Statement
  | Expression
  | Literal
  | Identifier;
