export function doit<T>(cb: () => T): T {
  return cb();
}

export function withit<T>(value: T): Pipe<T> {
  return new Pipe(value);
}

class Pipe<T> {
  constructor(public value: T) {}

  doit<Next>(cb: (it: T) => Next): Pipe<Next> {
    return new Pipe(cb(this.value));
  }

  done(): T {
    return this.value;
  }
}

export function pipeitNotNull<T, Result>(
  value: T,
  cb: (value: NonNullable<T>) => Result
): Result | null | undefined {
  if (value != null) {
    return cb(value);
  } else {
    return value as null | undefined;
  }
}
