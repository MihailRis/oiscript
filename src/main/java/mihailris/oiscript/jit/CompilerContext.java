package mihailris.oiscript.jit;

import mihailris.oiscript.runtime.Function;

public class CompilerContext {
    private final Function function;
    private int localsCount;

    public CompilerContext(Function function) {
        this.function = function;
        this.localsCount = function.getLocals().size();
    }

    public int newLocal() {
        return localsCount++;
    }

    public Function getFunction() {
        return function;
    }
}
