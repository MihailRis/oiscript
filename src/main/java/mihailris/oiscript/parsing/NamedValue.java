package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.exceptions.NameException;

public class NamedValue extends Value {
    private final String name;
    public NamedValue(String token) {
        this.name = token;
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
