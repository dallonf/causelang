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
  handlers?: HandlerBlockSuffix[];
}

export interface HandlerBlockSuffix {
  type: 'HandlerBlockSuffix';
  pattern?: Pattern;
  body: Expression;
}

export interface MemberExpression {
  type: 'MemberExpression';
  object: Expression;
  property: Identifier;
}

export type Expression =
  | Identifier
  | Literal
  | CallExpression
  | BlockExpression
  | PrefixOperatorExpression
  | MemberExpression;

export interface ExpressionStatement {
  type: 'ExpressionStatement';
  expression: Expression;
}

export interface NameDeclarationStatement {
  type: 'NameDeclarationStatement';
  name: Identifier;
  value: Expression;
  variable: boolean;
}

export interface AssignmentStatement {
  type: 'AssignmentStatement';
  name: Identifier;
  value: Expression;
}

export type Statement =
  | ExpressionStatement
  | NameDeclarationStatement
  | AssignmentStatement;

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

export type Pattern = TypePattern | NamePattern;

export interface TypePattern {
  type: 'TypePattern';
  typeName: Identifier;
}

export interface NamePattern {
  type: 'NamePattern';
  name: Identifier;
  valueType?: Identifier | TypePattern;
}
