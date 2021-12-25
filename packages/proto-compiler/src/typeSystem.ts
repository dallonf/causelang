import { isEqual } from 'lodash';
import { Breadcrumbs } from './context';
import { exhaustiveCheck } from './utils';

export type TypeReference =
  | PendingInferenceTypeReference
  | TypeErrorTypeReference
  | ConcreteTypeReference;

export type ConcreteTypeReference =
  | ValueTypeReference
  | FunctionTypeReference
  | TypeNameTypeReference;

export interface PendingInferenceTypeReference {
  kind: 'pendingInferenceTypeReference';
}

export interface TypeErrorTypeReference {
  kind: 'typeErrorTypeReference';
  error:
    | FailedToResolveTypeError
    | ReferenceTypeError
    | NotCallableTypeError
    | MismatchedTypeError;
}

export interface FailedToResolveTypeError {
  kind: 'failedToResolveTypeError';
  name: string;
}

export interface ReferenceTypeError {
  kind: 'referenceTypeError';
  breadcrumbs: Breadcrumbs;
}

export interface NotCallableTypeError {
  kind: 'notCallableTypeError';
}

export interface MismatchedTypeError {
  kind: 'mismatchedTypeError';
  expectedType?: TypeReference;
  actualType: TypeReference;
}

export interface ValueTypeReference {
  kind: 'valueTypeReference';
  id: string;
}

export interface FunctionTypeReference {
  kind: 'functionTypeReference';
  name?: string;
  params: Record<string, TypeReference>;
  returnType: TypeReference;
}

export interface TypeNameTypeReference {
  kind: 'typeNameTypeReference';
  id: string;
}

export type CauseType = PrimitiveType | ObjectType | EffectType | SymbolType;

export interface PrimitiveType {
  kind: 'primitiveType';
  id: string;
}

export interface ObjectType {
  kind: 'objectType';
  id: string;
  name: string;
  fields: Record<string, TypeReference>;
}

export interface EffectType {
  kind: 'effectType';
  id: string;
  name: string;
  parameters: Record<string, TypeReference>;
  returnType: TypeReference;
}

export interface SymbolType {
  kind: 'symbolType';
  id: string;
  name: string;
}

export type TypeMap = Map<string, CauseType>;

export const isConcrete = (
  type: TypeReference
): type is ConcreteTypeReference => {
  switch (type.kind) {
    case 'pendingInferenceTypeReference':
    case 'typeErrorTypeReference':
      return false;
    case 'valueTypeReference':
    case 'typeNameTypeReference':
    case 'functionTypeReference':
      return true;
    default:
      return exhaustiveCheck(type);
  }
};

export const isAssignableTo = (
  actual: ConcreteTypeReference,
  expecting: ConcreteTypeReference,
  typeMap: TypeMap
): boolean => {
  return isEqual(actual, expecting);
};
