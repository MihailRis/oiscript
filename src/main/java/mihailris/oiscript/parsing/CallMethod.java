package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.OiNone;
import mihailris.oiscript.exceptions.NameException;

import java.util.Collection;
import java.util.List;

public class CallMethod extends Value {
    private final Value source;
    private final String methodName;
    private final List<Value> values;
    public CallMethod(Value source, String methodName, List<Value> args) {
        this.source = source;
        this.methodName = methodName;
        this.values = args;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object eval(Context context) {
        Object object = source.eval(context);
        if (object instanceof Collection) {
            Collection<Object> collection = (Collection<Object>) object;
            switch (methodName) {
                case "add":
                    for (Value value : values) {
                        collection.add(value.eval(context));
                    }
                    return OiNone.NONE;
                case "remove":
                    for (Value value : values) {
                        collection.remove(value.eval(context));
                    }
                    return OiNone.NONE;
            }
        }
        throw new NameException(object.getClass().getSimpleName()+"."+methodName);
    }

    @Override
    public String toString() {
        String valuesStr = values.toString();
        return source.toString()+"."+methodName+"("+valuesStr.substring(1, valuesStr.length()-1)+")";
    }


    public List<Value> getValues() {
        return values;
    }
}