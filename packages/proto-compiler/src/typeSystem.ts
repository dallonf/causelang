export type TypeReference =
  | PendingInferenceTypeReference
  | ValueTypeReference
  | FailedToResolveTypeReference
  | TypeNameTypeReference;

export interface PendingInferenceTypeReference {
  kind: 'pendingInferenceTypeReference';
}

export interface FailedToResolveTypeReference {
  kind: 'failedToResolveTypeReference';
  name: string;
}

export interface ValueTypeReference {
  kind: 'valueTypeReference';
  id: string;
}

export interface TypeNameTypeReference {
  kind: 'typeNameTypeReference';
  id: string;
}

export type CauseType =
  | PrimitiveType
  | ObjectType
  | EffectType
  | FunctionType
  | CoreFunctionType;

export interface PrimitiveType {
  kind: 'primitiveType';
  id: string;
}

export interface ObjectType {
  kind: 'objectType';
  id: string;
  fields: Record<string, TypeReference>;
}

export interface EffectType {
  kind: 'effectType';
  id: string;
  parameters: Record<string, TypeReference>;
  returnType: TypeReference;
}

export interface ObjectType {
  kind: 'objectType';
  id: string;
  fields: Record<string, TypeReference>;
}

export interface FunctionType {
  kind: 'functionType';
  id: string;
  parameters: Record<string, TypeReference>;
  returnType: TypeReference;
}

export interface CoreFunctionType {
  kind: 'coreFunctionType';
  id: string;
  parameters: Record<string, TypeReference>;
  returnType: TypeReference;
}

export type TypeMap = Map<string, CauseType>;
