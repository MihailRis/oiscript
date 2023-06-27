package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.exceptions.LocalNameException;

public class LocalName extends Value {
    private final String name;
    private final int index;

    public LocalName(String name, int index) {
        this.name = name;
        this.index = index;
    }

    @Override
    public Object eval(Context context) {
        Object object = context.get(index);
        if (object == null)
            throw new LocalNameException(name);
        return object;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return name+"#"+index;
    }
}
