package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.OiObject;

import java.util.List;

public class ItemValue extends Value {
    private final Value source;
    private final Value key;

    public ItemValue(Value source, Value key) {
        this.source = source;
        this.key = key;
    }

    @SuppressWarnings("unchecked")
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
