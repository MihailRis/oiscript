package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;

public class CharValue extends Value {
    private final char value;

    public CharValue(char value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public Object eval(Context context) {
        return value;
    }
}
