package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;

public class ScriptRef extends Value {
    public static final Value INSTANCE = new ScriptRef();

    private ScriptRef() {
    }

    @Override
    public Object eval(Context context) {
        return context.script;
    }

    @Override
    public String toString() {
        return "script";
    }
}
