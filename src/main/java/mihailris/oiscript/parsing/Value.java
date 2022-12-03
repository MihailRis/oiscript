package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;

public class Value {
    public Value optimize() {
        return this;
    }

    public Object eval(Context context) {
        throw new IllegalStateException("not implemented in "+getClass().getSimpleName());
    }
}
