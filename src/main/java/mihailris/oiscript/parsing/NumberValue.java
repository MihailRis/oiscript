package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;

public class NumberValue extends Value {
    final double value;

    public NumberValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public static Value choose(double value) {
        if ((long)value == value)
            return new IntegerValue((long) value);
        return new NumberValue(value);
    }

    public static Value choose(long value) {
        return new IntegerValue(value);
    }

    @Override
    public Object eval(Context context) {
        return value;
    }

    @Override
    public String toString() {
        if ((long)value == value)
            return String.valueOf((long)value);
        return String.valueOf(value);
    }
}
