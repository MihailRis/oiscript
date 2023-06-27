package mihailris.oiscript.jit;

import mihailris.oiscript.runtime.Function;

public class CompilerContext {
    private final Function function;
    private int localsCount;

    private Type lastType = Type.OBJECT;

    public CompilerContext(Function function) {
        this.function = function;
        this.localsCount = function.getLocals().size();
    }

    public Type getLastType() {
        return lastType;
    }

    public void setLastType(Type lastType) {
        this.lastType = lastType;
    }

    public int newLocal() {
        return localsCount++;
    }

    public Function getFunction() {
        return function;
    }

    public enum Type {
        OBJECT,
        BOOL,
    }
}
