package mihailris.oiscript.parsing;

import mihailris.oiscript.Arithmetics;
import mihailris.oiscript.Context;

public class Negative extends Value {
    private final Value value;

    public Negative(Value value) {
        this.value = value;
    }

    @Override
    public Object eval(Context context) {
        return Arithmetics.negative(value.eval(context));
    }

    @Override
    public String toString() {
        return "-"+value;
    }
}
