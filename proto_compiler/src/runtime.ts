export const Log = Symbol('Log');
export type LogEffect = {
    type: typeof Log;
    value: string;
}

export const Panic = Symbol('Panic');
export type PanicEffect = {
    type: typeof Panic;
    value: string;
}

export type Effect = LogEffect | PanicEffect;

export class CauseError extends Error {
    constructor(message?: string) {
        super(message);
        Object.setPrototypeOf(this, new.target.prototype);
    }
}


function exhaustiveCheck(param: never) {
    throw new Error(`Exhaustive type check failed for param: ${(param as any).toString()}/${JSON.stringify(param)}`);
};

export function invokeMain(fn: () => Generator<Effect, void, any>) {
    const generator = fn();

    let next;
    while (next = generator.next(), !next.done) {
        const effect = next.value;
        if (effect.type === Log) {
            console.log(effect.value);
        } else if (effect.type === Panic) {
            throw new CauseError(effect.value);
        } else {
            exhaustiveCheck(effect);
        }
    }
}
