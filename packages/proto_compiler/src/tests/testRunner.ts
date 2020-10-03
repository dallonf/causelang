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

export function runMainSync(
  script: string,

  opts = { library: [] } as {
    library?: RuntimeLibraryValueType[];
    effectHandler?: (e: any) => any;
  }
) {
  const logs: string[] = [];

  const effectHandler = (e: any): any => {
    if (e.type === LogEffectSymbol) {
      logs.push(e.value);
      return;
    } else {
      return effectHandler(e);
    }
  };

  const jsSource = compileToJs(script, opts.library ?? []);
  const runtime = new CauseRuntime(jsSource, 'test.cau', {
    library: opts.library,
  });
  const generator = runtime.invokeFnAsGenerator('main', []);
  let next = generator.next();
  while (!next.done) {
    const effect = next.value;
    next = generator.next(effectHandler(effect));
  }
  return { result: next.value, logs };
}
