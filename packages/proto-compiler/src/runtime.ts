import * as vm from 'vm';
import * as analyzer from './analyzer';
import coreLibrary, { LogEffectSymbol, PanicEffectSymbol } from './coreLibrary';

export type EffectHandler = (
  effect: any
) => Promise<undefined | { handled: true; value?: any }>;

const coreLibraryEffectHandler: EffectHandler = async (effect) => {
  switch (effect.type) {
    case LogEffectSymbol:
      console.log(effect.value);
      return { handled: true };
    case PanicEffectSymbol:
      throw new CauseError('Error while running Cause file: ' + effect.value);
  }
};

export class CauseError extends Error {
  constructor(message?: string) {
    super(message);
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

export interface CauseRuntimeOptions {
  library?: analyzer.RuntimeLibraryValueType[];
  effectHandler?: EffectHandler;
}

export interface CauseRuntimeInvokeOptions {
  additionalEffectHandler?: EffectHandler;
}

export class CauseRuntime {
  script: vm.Script;
  typeMap: Record<string, symbol>;
  effectHandler: EffectHandler;

  constructor(jsSource: string, filename: string, opts?: CauseRuntimeOptions) {
    this.script = new vm.Script(jsSource, {
      filename,
    });
    const library = coreLibrary.concat(opts?.library ?? []);
    this.typeMap = Object.fromEntries(library.map((x) => [x.name, x.symbol]));
    this.effectHandler =
      opts?.effectHandler ?? ((e) => Promise.resolve(undefined));
  }

  async invokeFn(
    name: string,
    params: unknown[],
    opts = {} as CauseRuntimeInvokeOptions
  ): Promise<any> {
    const generator = this.invokeFnAsGenerator(name, params);
    let next = generator.next();
    while (!next.done) {
      const effect = next.value;

      next = generator.next(
        await this.handleEffect(effect, opts.additionalEffectHandler)
      );
    }
    return next.value;
  }

  invokeFnAsGenerator(name: string, params: unknown[]): Generator {
    const context = { ...this.typeMap };

    this.script.runInNewContext(context, {
      breakOnSigint: true,
    });

    const fn = (context[name] as unknown) as (...args: any) => Generator;
    if (typeof fn !== 'function') {
      throw new Error(`${name} is not a function in this Cause module`);
    }

    const generator = fn(...params);
    return generator;
  }

  async handleEffect(effect: any, extraHandler?: EffectHandler) {
    const handlers = [
      extraHandler,
      this.effectHandler,
      coreLibraryEffectHandler,
    ].filter((a) => a) as EffectHandler[];
    let effectResult;
    for (const handler of handlers) {
      effectResult = await handler(effect);
      if (effectResult?.handled) {
        break;
      }
    }

    if (!effectResult?.handled) {
      throw new CauseError(
        `I don't know how to handle a ${(effect as any).type.toString()} effect`
      );
    }

    return effectResult.value;
  }
}
