package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.OiVector;
import mihailris.oiscript.SemanticContext;
import mihailris.oiscript.exceptions.ParsingException;

import java.util.ArrayList;
import java.util.List;

public class ListValue extends Value {
    private final List<Value> values;

    public ListValue(List<Value> values) {
        this.values = values;
    }

    public List<Value> getValues() {
        return values;
    }

    @Override
    public Value build(SemanticContext context) throws ParsingException {
        List<Value> temp = new ArrayList<>(values);
        values.clear();
        for (Value value : temp) {
            values.add(value.build(context));
        }
        return super.build(context);
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
