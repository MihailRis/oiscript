package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.OiObject;

import java.util.HashMap;
import java.util.Map;

public class MapValue extends Value {
    private final Map<Value, Value> values;

    public MapValue(Map<Value, Value> values) {
        this.values = values;
    }

    @Override
    public Object eval(Context context) {
        Map<Object, Object> values = new HashMap<>();
        for (Map.Entry<Value, Value> entry : this.values.entrySet()) {
            values.put(entry.getKey().eval(context), entry.getValue().eval(context));
        }
        return new OiObject(values);
    }

    @Override
    public String toString() {
        return String.valueOf(values);
    }
}
