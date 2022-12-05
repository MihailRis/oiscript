package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.Logics;

public class Not extends Value {
    private final Value value;

    public Not(Value value) {
        this.value = value;
    }

    @Override
    public Object eval(Context context) {
        return !Logics.isTrue(value.eval(context));
    }

    @Override
    public String toString() {
        return "not "+value;
    }
}
