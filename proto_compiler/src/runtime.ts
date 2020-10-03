import * as vm from 'vm';
import { exhaustiveCheck } from './utils';

export type EffectMap = Record<
  string | symbol,
  { topLevelName: string; handler: (effect: unknown) => Promise<unknown> }
>;
export type TypeMap = Record<string | symbol, string>;

export const LogSymbol = Symbol('Log');
export type LogEffect = {
  type: typeof LogSymbol;
  value: string;
};

export const PanicSymbol = Symbol('Panic');
export type PanicEffect = {
  type: typeof PanicSymbol;
  value: string;
};

export type Effect = LogEffect | PanicEffect;

export class CauseError extends Error {
  constructor(message?: string) {
    super(message);
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

export function invokeEntry(fn: () => Generator<Effect, any, any>) {
  const generator = fn();

  let next;
  while (((next = generator.next()), !next.done)) {
    const effect = next.value;
    if (effect.type === LogSymbol) {
      console.log(effect.value);
    } else if (effect.type === PanicSymbol) {
      throw new CauseError(effect.value);
    } else {
      exhaustiveCheck(effect);
    }
  }
  return next.value;
}

export const defaultEffects: EffectMap = {
  [LogSymbol]: {
    topLevelName: 'Log',
    handler: async (effect: LogEffect) => {
      console.log(effect.value);
    },
  },
  [PanicSymbol]: {
    topLevelName: 'Panic',
    handler: async (effect: PanicEffect) => {
      throw new CauseError(effect.value);
    },
  },
};

interface CauseRuntimeOptions {
  effectHandlers?: EffectMap;
  types?: TypeMap;
}

export class CauseRuntime {
  script: vm.Script;
  effectMap: EffectMap;
  typeMap: TypeMap;

  constructor(jsSource: string, filename: string, opts?: CauseRuntimeOptions) {
    this.script = new vm.Script(jsSource, {
      filename,
    });
    this.effectMap = { ...defaultEffects, ...opts?.effectHandlers };
    this.typeMap = opts?.types || {};
  }

  async invokeFn(name: string, params: unknown[]): Promise<any> {
    // TODO: symbols don't seem to play right with Object.entries()
    const context = Object.fromEntries(
      Object.entries(this.effectMap)
        .map(([key, value]) => [value.topLevelName, key])
        .concat(
          Object.entries(this.typeMap).map(([key, value]) => [value, key])
        )
    );

    this.script.runInNewContext(context, {
      breakOnSigint: true,
    });
    const fn = (context[name] as unknown) as (...args: any) => Generator;
    if (typeof fn !== 'function') {
      throw new Error(`${name} is not a function in this Cause module`);
    }

    const generator = fn(...params);

    let next = generator.next();
    while (!next.done) {
      const effect = next.value as { type: string };

      const handler = this.effectMap[effect.type];
      if (!handler) {
        throw new CauseError(
          `I don't know how to handle a ${effect.type} effect`
        );
      }

      const result = await handler.handler(effect);
      next = generator.next(result);
    }
    return next.value;
  }
}
