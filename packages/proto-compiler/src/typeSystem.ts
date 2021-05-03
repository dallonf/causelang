import { Breadcrumbs } from './context';

export type TypeReference =
  | PendingInferenceTypeReference
  | TypeErrorTypeReference
  | ValueTypeReference
  | FunctionTypeReference
  | TypeNameTypeReference;

export interface PendingInferenceTypeReference {
  kind: 'pendingInferenceTypeReference';
}

export interface TypeErrorTypeReference {
  kind: 'typeErrorTypeReference';
  error: FailedToResolveTypeError | ReferenceTypeError | NotCallableTypeError;
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

export type CauseType =
  | PrimitiveType
  | ObjectType
  | EffectType;

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

export interface ObjectType {
  kind: 'objectType';
  id: string;
  fields: Record<string, TypeReference>;
}

export type TypeMap = Map<string, CauseType>;
