package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.OiObject;
import mihailris.oiscript.OiUtils;
import mihailris.oiscript.exceptions.AttributeException;

import java.util.Collection;

public class AttributeValue extends Value {
    private final Value source;
    private final String name;

    public AttributeValue(Value source, String name) {
        this.source = source;
        this.name = name;
    }

    @Override
    public Object eval(Context context) {
        Object object = source.eval(context);
        if (name.equals("len")) {
            return OiUtils.length(object);
        }
        OiObject oiobject = (OiObject) object;
        Object value = oiobject.get(name);
        if (value == null) {
            throw new AttributeException(name);
        }
        return value;
    }

    @Override
    public String toString() {
        return source+"."+name;
    }
}
