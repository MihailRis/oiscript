package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;

public class ValueBlock extends Value {
    private final Value value;

    public ValueBlock(Value value) {
        this.value = value;
    }

    @Override
    public Object eval(Context context) {
        return value.eval(context);
    }

    @Override
    public String toString() {
        return "("+value+")";
    }
}
