package mihailris.oiscript.parsing;

import mihailris.oiscript.Arithmetics;
import mihailris.oiscript.Context;
import mihailris.oiscript.SemanticContext;
import mihailris.oiscript.exceptions.ParsingException;
import mihailris.oiscript.jit.OiType;

public class BitInverse extends Value {
    private Value value;

    public BitInverse(Value value) {
        this.value = value;
    }

    @Override
    public Value build(SemanticContext context) throws ParsingException {
        value = value.build(context);
        return super.build(context);
    }

    @Override
    public OiType getType() {
        return OiType.LONG;
    }

    public Value getValue() {
        return value;
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
