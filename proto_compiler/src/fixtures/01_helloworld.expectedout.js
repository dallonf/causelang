function* main() {
    yield { type: CauseRuntime.LogSymbol, value: "Hello World" };
    return { type: CauseRuntime.ExitCodeSymbol, value: 0 };
}

CauseRuntime.invokeEntry(main);
