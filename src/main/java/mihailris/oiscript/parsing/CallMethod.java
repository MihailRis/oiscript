package mihailris.oiscript.parsing;

import mihailris.oiscript.*;
import mihailris.oiscript.exceptions.MethodException;
import mihailris.oiscript.exceptions.NameException;
import mihailris.oiscript.runtime.Function;
import mihailris.oiscript.runtime.OiModule;
import mihailris.oiscript.stdlib.OiStringMethods;

import java.util.Arrays;
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
        if (object instanceof OiObject) {
            OiObject oiObject = (OiObject) object;
            Function function = oiObject.getMethod(methodName);
            if (function != null) {
                Object[] args = new Object[function.getArgs().size()];
                int index = 0;

                if (!(oiObject instanceof OiModule))
                    args[index++] = oiObject;

                for (Value argValue : values) {
                    if (argValue instanceof RestHolder) {
                        RestHolder restHolder = (RestHolder) argValue;
                        Collection<?> rest = (Collection<?>) restHolder.getValue().eval(context);
                        for (Object arg : rest) {
                            args[index++] = arg;
                        }
                        continue;
                    }
                    args[index++] = argValue.eval(context);
                }
                return function.execute(new Context(context.script, context.runHandle), args);
            } else {
                throw new MethodException(methodName);
            }
        }
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
                case "remove":
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
            return OiStringMethods.call(string, methodName, args);
        } else if (object instanceof Range) {
            Range range = (Range) object;
            switch (methodName) {
                case "fit": {
                    Number value = (Number) args[0];
                    return value.doubleValue() * (range.getEnd()-range.getStart()) + range.getStart();
                }
                case "unfit": {
                    Number value = (Number) args[0];
                    return (value.doubleValue() - range.getStart()) / (range.getEnd()-range.getStart());
                }
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