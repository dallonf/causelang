import { RuntimeLibraryValueType } from '../analyzer';
import compileAndInvoke from '../compileAndInvoke';
import compileToJs from '../compileToJs';
import { LogEffectSymbol } from '../coreLibrary';
import { CauseRuntime, EffectHandler } from '../runtime';

export async function runMain(
  script: string,
  opts = { library: [] } as {
    library?: RuntimeLibraryValueType[];
    effectHandler?: EffectHandler;
  }
) {
  const logs: string[] = [];

  const logOverrideEffectHandler: EffectHandler = async (e) => {
    if (e.type === LogEffectSymbol) {
      logs.push(e.value);
      return { handled: true };
    } else {
      return opts.effectHandler?.(e);
    }
  };

  const result = await compileAndInvoke(
    { source: script, filename: 'test.cau' },
    'main',
    [],
    { library: opts.library, effectHandler: logOverrideEffectHandler }
  );

  return {
    result,
    logs,
  };
}

export type SyncEffectHandler = (
  effect: any
) => undefined | { handled: true; value: any };

export function runMainSync(
  script: string,

  opts = { library: [] } as {
    library?: RuntimeLibraryValueType[];
    effectHandler?: SyncEffectHandler;
    debugJsOutput?: boolean;
  }
) {
  const logs: string[] = [];

  const innerHandler = (e: any) => {
    if (e.type === LogEffectSymbol) {
      logs.push(e.value);
      return { handled: true };
    }
  };

  const handlers = [opts.effectHandler, innerHandler].filter(
    (a) => a
  ) as SyncEffectHandler[];

  const jsSource = compileToJs(script, opts.library ?? []);
  if (opts.debugJsOutput) {
    console.log(jsSource);
  }
  const runtime = new CauseRuntime(jsSource, 'test.cau', {
    library: opts.library,
  });
  const generator = runtime.invokeFnAsGenerator('main', []);
  let next = generator.next();
  while (!next.done) {
    const effect = next.value;
    let effectResult;
    for (const handler of handlers) {
      effectResult = handler(effect);
      if (effectResult?.handled) {
        break;
      }
    }

    if (!effectResult?.handled) {
      throw new Error(
        `I don't know how to handle a ${(effect as any).type.toString()} effect`
      );
    }
    next = generator.next(effectResult.value);
  }
  return { result: next.value, logs };
}
