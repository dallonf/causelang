import { RuntimeLibraryValueType } from '../analyzer';
import compileAndInvoke from '../compileAndInvoke';
import { LogEffectSymbol } from '../coreLibrary';
import { EffectHandler } from '../runtime';

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
