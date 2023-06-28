package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.OiObject;
import mihailris.oiscript.OiUtils;
import mihailris.oiscript.SemanticContext;
import mihailris.oiscript.exceptions.AttributeException;
import mihailris.oiscript.exceptions.ParsingException;
import mihailris.oiscript.jit.OiType;

public class AttributeValue extends Value {
    private Value source;
    private final String name;
    private OiType type;

    public AttributeValue(Value source, String name) {
        this.source = source;
        this.name = name;
    }

    public Value getSource() {
        return source;
    }

    public String getName() {
        return name;
    }

    @Override
    public OiType getType() {
        return type;
    }

    @Override
    public Value build(SemanticContext context) throws ParsingException {
        source = source.build(context);
        if (name.equals("len")) {
            type = OiType.LONG;
        }
        return super.build(context);
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
