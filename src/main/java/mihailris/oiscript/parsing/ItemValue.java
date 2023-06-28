package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.OiObject;
import mihailris.oiscript.SemanticContext;
import mihailris.oiscript.exceptions.ParsingException;

import java.util.List;

public class ItemValue extends Value {
    private Value source;
    private Value key;

    public ItemValue(Value source, Value key) {
        this.source = source;
        this.key = key;
    }

    public Value getSource() {
        return source;
    }

    public Value getKey() {
        return key;
    }

    @Override
    public Value build(SemanticContext context) throws ParsingException {
        source = source.build(context);
        key = key.build(context);
        return super.build(context);
    }

    @Override
    public Object eval(Context context) {
        Object sourceObject = source.eval(context);
        Object keyObject = key.eval(context);
        if (sourceObject instanceof String) {
            String string = (String) sourceObject;
            Number number = (Number) keyObject;
            int index = number.intValue();
            return string.charAt(index);
        } else if (sourceObject instanceof OiObject) {
            OiObject oiObject = (OiObject) sourceObject;
            return oiObject.get(keyObject);
        } else if (sourceObject instanceof List) {
            List<?> list = (List<?>) sourceObject;
            Number number = (Number) keyObject;
            int index = number.intValue();
            return list.get(index);
        }
        throw new IllegalArgumentException("[..] is not applicable to "+sourceObject.getClass().getSimpleName());
    }

    @Override
    public String toString() {
        return source+"["+key+"]";
    }
}
