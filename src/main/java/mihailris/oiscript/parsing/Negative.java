package mihailris.oiscript.parsing;

import mihailris.oiscript.Arithmetics;
import mihailris.oiscript.Context;
import mihailris.oiscript.SemanticContext;
import mihailris.oiscript.exceptions.ParsingException;
import mihailris.oiscript.jit.OiType;

public class Negative extends Value {
    private Value value;

    public Negative(Value value) {
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public OiType getType() {
        return value.getType();
    }

    @Override
    public Value build(SemanticContext context) throws ParsingException {
        value = value.build(context);
        return super.build(context);
    }

    @Override
    public Value optimize() {
        if (value instanceof IntegerValue) {
            IntegerValue integerValue = (IntegerValue) value;
            integerValue.setValue(-integerValue.getValue());
            return integerValue;
        }
        return super.optimize();
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
