import * as vm from 'vm';
import coreLibrary from './coreLibrary';
import { emptyLibrary, Library, mergeLibraries } from './makeLibrary';

export type EffectHandler = (
  effect: any
) => undefined | { handled: true; value?: Promise<any> | any };

export type LibrarySymbolMap = Record<string, symbol>;

export class CauseError extends Error {
  constructor(message?: string) {
    super(message);
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

export interface CauseRuntimeOptions {
  library?: Library;
}

export interface CauseRuntimeInvokeOptions {
  additionalEffectHandler?: EffectHandler;
}

export class CauseRuntime {
  script: vm.Script;
  library: Library;

  constructor(jsSource: string, filename: string, opts?: CauseRuntimeOptions) {
    this.script = new vm.Script(jsSource, {
      filename,
    });

    this.library = mergeLibraries(coreLibrary, opts?.library ?? emptyLibrary);
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
    const context = this.library.symbols;

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
    if (extraHandler) {
      const result = extraHandler(effect);
      if (result?.handled) {
        return result.value;
      }
    }

    const effectResult = this.library.handleEffects(effect);

    if (!effectResult?.handled) {
      throw new CauseError(
        `I don't know how to handle a ${(effect as any).type.toString()} effect`
      );
    }

    return effectResult.value;
  }
}
