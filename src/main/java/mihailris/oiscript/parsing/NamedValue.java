package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.SemanticContext;
import mihailris.oiscript.exceptions.NameException;
import mihailris.oiscript.exceptions.ParsingException;

public class NamedValue extends Value {
    private final String name;
    public NamedValue(String token) {
        this.name = token;
    }

    @Override
    public Value build(SemanticContext context) throws ParsingException {
        int index = context.getFunction().getLocalIndex(name);
        if (index != -1)
            return new LocalName(name, index);
        return super.build(context);
    }

    @Override
    public Object eval(Context context) {
        Object object = context.get(name);
        if (object == null)
            throw new NameException(name);
        return object;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
