package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.SemanticContext;
import mihailris.oiscript.exceptions.ParsingException;
import mihailris.oiscript.jit.OiType;

public class Value {
    protected Value() {
    }

    public Value optimize() {
        return this;
    }

    public OiType getType() {
        return OiType.OBJECT;
    }

    public Object eval(Context context) {
        throw new IllegalStateException("not implemented in "+getClass().getSimpleName());
    }

    public Value build(SemanticContext context) throws ParsingException {
        return this;
    }
}
