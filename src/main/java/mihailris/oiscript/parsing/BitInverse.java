package mihailris.oiscript.parsing;

import mihailris.oiscript.Arithmetics;
import mihailris.oiscript.Context;

public class BitInverse extends Value {
    private final Value value;

    public BitInverse(Value value) {
        this.value = value;
    }

    @Override
    public Object eval(Context context) {
        return Arithmetics.bitInverse(value.eval(context));
    }

    @Override
    public String toString() {
        return "~"+value;
    }
}
