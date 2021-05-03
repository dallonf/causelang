import * as context from './context';
import { TypeMap } from './typeSystem';

export interface Library {
  name: string;
  types: TypeMap;
  scope: Record<string, context.ScopeSymbol>;
}
