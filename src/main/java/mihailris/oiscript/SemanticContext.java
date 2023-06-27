package mihailris.oiscript;

import mihailris.oiscript.runtime.Function;

public class SemanticContext {
    private final SemanticContext parent;
    private final Function function;
    private final boolean loop;

    public SemanticContext(Function function) {
        this(function, null, false);
    }

    private SemanticContext(Function function, SemanticContext parent, boolean loop) {
        this.parent = parent;
        this.function = function;
        this.loop = loop;
    }

    public SemanticContext childLoop() {
        return new SemanticContext(function, this, true);
    }

    public Function getFunction() {
        return function;
    }

    public SemanticContext getParent() {
        return parent;
    }

    public boolean isLoop() {
        return loop;
    }
}
