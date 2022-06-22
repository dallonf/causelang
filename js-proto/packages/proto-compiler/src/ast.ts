const KEYWORDS = [
  'cause',
  'effect',
  'fn',
  'type',
  'option',
  'let',
  'var',
  'symbol',
] as const;
export const keywordSet = new Set(KEYWORDS);
export type KeywordValue = typeof KEYWORDS extends readonly (infer T)[]
  ? T
  : never;

export function isKeyword(input: string): input is KeywordValue {
  return (keywordSet as Set<string>).has(input);
}

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

export interface IntegerLiteral {
  type: 'IntLiteral';
  value: number;
}

export type TypeReference = Identifier;

export type Literal = StringLiteral | IntegerLiteral;

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

export interface FunctionExpression {
  type: 'FunctionExpression';
  parameters: void[];
  body: Expression;
}

export interface BranchExpression {
  type: 'BranchExpression';
  conditions: BranchCondition[];
}

export interface BranchCondition {
  type: 'BranchCondition';
  guard?: Expression;
  body: Expression;
}

export type Expression =
  | Identifier
  | Literal
  | CallExpression
  | BlockExpression
  | PrefixOperatorExpression
  | MemberExpression
  | FunctionExpression
  | BranchExpression;

export interface ExpressionStatement {
  type: 'ExpressionStatement';
  expression: Expression;
}

export interface NameDeclarationStatement {
  type: 'NameDeclarationStatement';
  name: Identifier;
  typeAnnotation?: TypeReference;
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
  returnType?: TypeReference;
  body: Expression;
}

export interface EffectDeclaration {
  type: 'EffectDeclaration';
  id: Identifier;
  parameters: ParameterDescriptor[];
  returnType?: TypeReference;
}
export interface ParameterDescriptor {
  type: 'ParameterDescriptor';
  name: Identifier;
  valueType: TypeReference;
}

export interface ObjectTypeDeclaration {
  type: 'TypeDeclaration';
  id: Identifier;
  fields: FieldDescriptor[];
}

export interface FieldDescriptor {
  type: 'FieldDescriptor';
  name: Identifier;
  valueType: TypeReference;
}

export interface SymbolDeclaration {
  type: 'SymbolDeclaration';
  id: Identifier;
}

export interface OptionDeclaration {
  type: 'OptionDeclaration';
  name: Identifier;
  options: (TypeReference | TypeDeclaration)[];
}

export type TypeDeclaration =
  | EffectDeclaration
  | ObjectTypeDeclaration
  | SymbolDeclaration
  | OptionDeclaration;

export type Declaration = FunctionDeclaration | TypeDeclaration;

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
  typeName: TypeReference;
}

export interface NamePattern {
  type: 'NamePattern';
  name: Identifier;
  valueType?: Identifier | TypePattern;
}
