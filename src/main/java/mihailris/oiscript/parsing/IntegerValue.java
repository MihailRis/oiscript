package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;

public class IntegerValue extends Value {
    final long value;

    public IntegerValue(long value) {
        this.value = value;
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
