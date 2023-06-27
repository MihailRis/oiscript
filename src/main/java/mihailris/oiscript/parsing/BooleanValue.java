package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;

public class BooleanValue extends Value {
    private final boolean value;

    public BooleanValue(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public Object eval(Context context) {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
