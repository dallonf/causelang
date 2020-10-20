export interface RuntimeEffect {
  type: string;
  value: any;
}

type RuntimeHandler = (
  effect: RuntimeEffect
) => Generator<RuntimeEffect, undefined | { handled: true; value: any }, any>;

export function* cauRuntime$handleEffects(
  iter: Generator<any>,
  ...handlers: RuntimeHandler[]
) {
  let next;
  let value: any = undefined;
  effects: while (((next = iter.next(value)), !next.done)) {
    value = undefined;
    for (const handler of handlers) {
      const result = yield* handler(next.value);
      if (result?.handled) {
        value = result.value;
        continue effects;
      }
    }
    yield next.value;
  }
  return next.value;
}
