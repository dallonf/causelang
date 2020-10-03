import * as vm from 'vm';
import * as analyzer from './analyzer';

type EffectHandler = (effect: any) => Promise<any>;
export type LibraryItem =
  | (analyzer.EffectDeclarationValueType & {
      symbol: symbol;
      handler: EffectHandler;
    })
  | (analyzer.TypeDeclarationValueType & {
      symbol: symbol;
    });

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

export const defaultLibrary: LibraryItem[] = [
  {
    kind: 'effect',
    name: 'Log',
    symbol: LogSymbol,
    handler: async (effect: LogEffect) => {
      console.log(effect.value);
    },
  },
  {
    kind: 'effect',
    name: 'Panic',
    symbol: PanicSymbol,
    handler: async (effect: PanicEffect) => {
      throw new CauseError(effect.value);
    },
  },
];

export type Effect = LogEffect | PanicEffect;

export class CauseError extends Error {
  constructor(message?: string) {
    super(message);
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

interface CauseRuntimeOptions {
  library: LibraryItem[];
}

function dumbAssert(condition: any): asserts condition {
  if (!condition) {
    throw new Error('assertion failed');
  }
}

export class CauseRuntime {
  script: vm.Script;
  effectMap: Map<symbol, EffectHandler>;
  typeMap: Record<string, symbol>;

  constructor(jsSource: string, filename: string, opts?: CauseRuntimeOptions) {
    this.script = new vm.Script(jsSource, {
      filename,
    });
    const library = defaultLibrary.concat(opts?.library ?? []);
    this.effectMap = new Map(
      library
        .filter((x) => x.kind === 'effect')
        .map((x) => {
          dumbAssert(x.kind === 'effect');
          return [x.symbol, x.handler];
        })
    );
    this.typeMap = Object.fromEntries(library.map((x) => [x.name, x.symbol]));
  }

  async invokeFn(
    name: string,
    params: unknown[],
    alternateEffectHandlers?: Map<symbol, EffectHandler>
  ): Promise<any> {
    // TODO: symbols don't seem to play right with Object.entries()
    const context = { ...this.typeMap };

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
      const effect = next.value as { type: symbol };

      const handler = this.effectMap.get(effect.type);
      if (!handler) {
        throw new CauseError(
          `I don't know how to handle a ${effect.type.toString()} effect`
        );
      }

      const result = await handler(effect);
      next = generator.next(result);
    }
    return next.value;
  }
}
