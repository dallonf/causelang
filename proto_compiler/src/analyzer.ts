import * as ast from './ast';

export type ValueType =
  | DynamicValueType
  | KeywordValueType
  | DeclarationValueType
  | InstanceValueType;

export type DeclarationValueType =
  | EffectDeclarationValueType
  | FunctionDeclarationValueType;

/**
 * This is gonna have to be phased out pretty quickly;
 * Cause is not a dynamically typed language
 * and it mostly just exists as a placeholder for an incomplete
 * crawler
 */
interface DynamicValueType {
  kind: 'dynamic';
}

interface KeywordValueType {
  kind: 'keyword';
  keyword: ast.KeywordValue;
}

interface EffectDeclarationValueType {
  kind: 'effect';
  name: string;
}

interface FunctionDeclarationValueType {
  kind: 'fn';
  name?: string;
}

interface InstanceValueType {
  kind: 'instance';
  name?: string;
  type: DeclarationValueType;
}
