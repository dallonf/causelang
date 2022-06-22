import { isEqual } from 'lodash';
import { Breadcrumbs, Scope } from './context';
import { exhaustiveCheck } from './utils';

export const typeNameTypeReference = (id: string): TypeNameTypeReference => ({
  kind: 'typeNameTypeReference',
  id,
});

export const valueOfTypeId = (id: string): ValueTypeReference => ({
  kind: 'valueTypeReference',
  valueType: typeNameTypeReference(id),
});

export type TypeReference =
  | PendingInferenceTypeReference
  | TypeErrorTypeReference
  | ConcreteTypeReference;

export type ConcreteTypeReference =
  | ValueTypeReference
  | FunctionTypeReference
  | TypeNameTypeReference
  | OptionTypeReference;

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
  valueType: TypeReference;
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

export interface OptionTypeReference {
  kind: 'optionTypeReference';
  name?: string;
  options: TypeReference[];
  children?: Scope;
}

export type NominalType = PrimitiveType | ObjectType | EffectType | SymbolType;

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

export type TypeMap = Map<string, NominalType>;

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
    case 'optionTypeReference':
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
