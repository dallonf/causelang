export function exhaustiveCheck(param: never): never {
  throw new Error(
    `Exhaustive type check failed for param: ${JSON.stringify(param)}`
  );
}
