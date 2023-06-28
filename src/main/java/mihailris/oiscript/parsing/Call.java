package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.SemanticContext;
import mihailris.oiscript.exceptions.ParsingException;
import mihailris.oiscript.runtime.Function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Call extends Value {
    private final Value source;
    private final List<Value> values;

    public Call(Value source, List<Value> values) {
        this.source = source;
        this.values = values;
    }

    public List<Value> getValues() {
        return values;
    }

    public Value getSource() {
        return source;
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
        Function function = (Function)source.eval(context);
        Object[] args = new Object[values.size()];
        int index = 0;
        for (Value argValue : values) {
            args[index++] = argValue.eval(context);
        }
        return context.script.execute(function, context.runHandle, args);
    }

    @Override
    public String toString() {
        String valuesStr = String.valueOf(values);
        return source+"("+valuesStr.substring(1, valuesStr.length()-1)+")";
    }
}
