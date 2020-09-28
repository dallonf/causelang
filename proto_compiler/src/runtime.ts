export const LogSymbol = Symbol('Log');
export type LogEffect = {
    type: typeof LogSymbol;
    value: string;
}

export const PanicSymbol = Symbol('Panic');
export type PanicEffect = {
    type: typeof PanicSymbol;
    value: string;
}

export const ExitCodeSymbol = Symbol('ExitCode');
export type ExitCodeType = {
    type: typeof ExitCodeSymbol;
    value: number;
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

export function invokeEntry(fn: () => Generator<Effect, any, any>) {
    const generator = fn();

    let next;
    while (next = generator.next(), !next.done) {
        const effect = next.value;
        if (effect.type === LogSymbol) {
            console.log(effect.value);
        } else if (effect.type === PanicSymbol) {
            throw new CauseError(effect.value);
        } else {
            exhaustiveCheck(effect);
        }
    }
    return next.value;
}
