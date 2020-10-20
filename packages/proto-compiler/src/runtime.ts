import * as vm from 'vm';
import coreLibrary, { coreFunctions } from './coreLibrary';
import { Library } from './makeLibrary';

export type EffectHandler = (
  effect: any
) => undefined | { handled: true; value?: Promise<any> | any };

export type LibraryIDMap = Record<string, string>;

export class CauseError extends Error {
  constructor(message?: string) {
    super(message);
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

export interface CauseRuntimeOptions {
  libraries?: Library[];
  additionalEffectHandler?: EffectHandler;
}

export interface CauseRuntimeInvokeOptions {
  additionalEffectHandler?: EffectHandler;
}

export class CauseRuntime {
  script: vm.Script;
  library: RuntimeLibrary;
  additionalEffectHandler?: EffectHandler;

  constructor(jsSource: string, filename: string, opts?: CauseRuntimeOptions) {
    this.script = new vm.Script(jsSource, {
      filename,
    });
    this.additionalEffectHandler = opts?.additionalEffectHandler;

    this.library = makeRuntimeLibrary(...(opts?.libraries ?? []));
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

  invokeFnSync(
    name: string,
    params: unknown[],
    opts = {} as CauseRuntimeInvokeOptions
  ): any {
    const generator = this.invokeFnAsGenerator(name, params);
    let next = generator.next();
    while (!next.done) {
      const effect = next.value as any;
      const result = this.handleEffect(effect, opts.additionalEffectHandler);
      if (typeof result?.then === 'function') {
        throw new CauseError(
          `invokeFnSync: ${effect.type.toString()} effect was handled asynchronously!`
        );
      }
      next = generator.next(result);
    }
    return next.value;
  }

  invokeFnAsGenerator(name: string, params: unknown[]): Generator {
    const context = this.library.runtimeScope;

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

  handleEffect(effect: any, extraHandler?: EffectHandler) {
    const compositeHandler = mergeEffectHandlers(
      ...([
        this.library.handleEffects,
        this.additionalEffectHandler,
        extraHandler,
      ].filter((x) => x) as EffectHandler[])
    );

    const effectResult = compositeHandler(effect);

    if (!effectResult?.handled) {
      throw new CauseError(
        `I don't know how to handle a ${(effect as any).type.toString()} effect`
      );
    }

    return effectResult.value;
  }
}

export const mergeEffectHandlers = (
  ...handlers: EffectHandler[]
): EffectHandler => {
  const reverseHandlerList = [...handlers].reverse();
  return (e: any) => {
    for (const handler of reverseHandlerList) {
      const result = handler(e);
      if (result?.handled) {
        return result;
      }
    }
  };
};

interface RuntimeLibrary {
  runtimeScope: Record<string, string | Function>;
  handleEffects: EffectHandler;
}

function makeRuntimeLibrary(...libraries: Library[]): RuntimeLibrary {
  const nameSet = new Set<string>([coreLibrary.name]);
  for (const lib of libraries) {
    if (nameSet.has(lib.name)) {
      throw new Error(
        `I can't include two libraries with the same name! The duplicate name is ${lib.name}`
      );
    }
  }

  const allLibraries = [coreLibrary, ...libraries];

  return {
    runtimeScope: {
      ...coreFunctions,
      ...Object.fromEntries(allLibraries.flatMap((a) => Object.entries(a.ids))),
    },
    handleEffects: mergeEffectHandlers(
      ...allLibraries.map((l) => l.handleEffects)
    ),
  };
}
