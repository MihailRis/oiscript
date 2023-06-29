package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.Logics;
import mihailris.oiscript.SemanticContext;
import mihailris.oiscript.exceptions.ParsingException;
import mihailris.oiscript.jit.OiType;

public class Not extends Value {
    private Value value;

    public Not(Value value) {
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public Value build(SemanticContext context) throws ParsingException {
        value = value.build(context);
        return super.build(context);
    }

    @Override
    public OiType getType() {
        return OiType.BOOL;
    }

    @Override
    public Object eval(Context context) {
        return !Logics.isTrue(value.eval(context));
    }

    @Override
    public String toString() {
        return "not "+value;
    }
}
