const Print = 'Print$test';

function* main() {
  return yield* cauRuntime$handleEffects(
    (function* () {
      return yield {
        type: Print,
        value: 'Howdy world!',
      };
    })(),
    function* (e) {
      return {
        handled: true,
        value: yield { type: Print, value: 'Intercepted a Print effect' },
      };
    }
  );
}

function* cauRuntime$handleEffects(iter, ...handlers) {
  let next;
  let value = undefined;
  effects: while (((next = iter.next(value)), !next.done)) {
    value = undefined;
    for (handler of handlers) {
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

[...main()]; // ?
