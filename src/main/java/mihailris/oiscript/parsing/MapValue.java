package mihailris.oiscript.parsing;

import java.util.Map;

public class MapValue extends Value {
    private final Map<Value, Value> values;

    public MapValue(Map<Value, Value> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return String.valueOf(values);
    }
}
