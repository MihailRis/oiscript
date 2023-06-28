package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.jit.OiType;

public class IntegerValue extends Value {
    long value;

    public IntegerValue(long value) {
        this.value = value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public OiType getType() {
        return OiType.LONG;
    }

    @Override
    public Object eval(Context context) {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public long getValue() {
        return value;
    }
}
