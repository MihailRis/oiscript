package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.OiObject;

public class AttributeValue extends Value {
    private final Value source;
    private final String name;

    public AttributeValue(Value source, String name) {
        this.source = source;
        this.name = name;
    }

    @Override
    public Object eval(Context context) {
        OiObject object = (OiObject) source.eval(context);
        return object.get(name);
    }

    @Override
    public String toString() {
        return source+"."+name;
    }
}
