// import CauseRuntime from '@causelang/runtime';

function* main() {
    return yield { type: CauseRuntime.Log, value: "Hello World" }
}

CauseRuntime.invokeMain(main);
