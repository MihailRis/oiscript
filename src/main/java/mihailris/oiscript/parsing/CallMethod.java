package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.OiNone;
import mihailris.oiscript.OiUtils;
import mihailris.oiscript.exceptions.NameException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
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
        Object[] args = new Object[values.size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = values.get(i).eval(context);
        }
        if (object instanceof Collection) {
            Collection<Object> collection = (Collection<Object>) object;
            switch (methodName) {
                case "add":
                    collection.addAll(Arrays.asList(args));
                    break;
                case "rem":
                    for (Object value : args) {
                        collection.remove(value);
                    }
                    break;
                case "clear":
                    OiUtils.requreArgCount("clear", 0, args);
                    collection.clear();
                    break;
                default:
                    throw new NameException(object.getClass().getSimpleName()+"."+methodName);
            }
        } else if (object instanceof String) {
            String string = (String) object;
            switch (methodName) {
                case "join": {
                    StringBuilder builder = new StringBuilder();
                    OiUtils.requreArgCount("string.join", 1, args);
                    Collection<?> collection = (Collection<?>) args[0];
                    Iterator<?> iterator = collection.iterator();
                    for (int i = 0; i < collection.size(); i++) {
                        builder.append(iterator.next());
                        if (i+1 < collection.size()) {
                            builder.append(string);
                        }
                    }
                    return builder.toString();
                }
                default:
                    throw new NameException(object.getClass().getSimpleName()+"."+methodName);
            }
        }
        return OiNone.NONE;
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