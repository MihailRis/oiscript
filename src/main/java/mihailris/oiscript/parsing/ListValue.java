package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.OiVector;

import java.util.List;

public class ListValue extends Value {
    private final List<Value> values;

    public ListValue(List<Value> values) {
        this.values = values;
    }

    @Override
    public Object eval(Context context) {
        List<Object> objects = new OiVector();
        for (Value value : values) {
            objects.add(value.eval(context));
        }
        return objects;
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
