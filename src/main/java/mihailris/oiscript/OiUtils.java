package mihailris.oiscript;

import mihailris.oiscript.parsing.Range;
import mihailris.oiscript.runtime.Function;

import java.util.ArrayList;
import java.util.Collection;

import static mihailris.oiscript.OiNone.NONE;

public class OiUtils {
    public static void requreArgCount(String funcname, int count, Object... args) {
        if (count != args.length) {
            throw new IllegalArgumentException("func "+funcname+": "+count+" arguments excepted, got "+args.length);
        }
    }

    public interface Callback {
        Object call(Context context, Object... args);
    }

    public static Function customFunc(String name, Callback callback, int argc, boolean mainThread) {
        if (mainThread){
            return new Function(name, new ArrayList<>()) {
                @Override
                public Object execute(Context context, Object... args) {
                    if (argc >= 0)
                        OiUtils.requreArgCount(getName(), argc, args);

                    if (context.runHandle.thread == Thread.currentThread()) {
                        return context.runHandle.callOutside(this, context, args);
                    }
                    return callback.call(context, args);
                }
            };
        } else {
            return new Function(name, new ArrayList<>()) {
                @Override
                public Object execute(Context context, Object... args) {
                    if (argc >= 0)
                        OiUtils.requreArgCount(getName(), argc, args);
                    return callback.call(context, args);
                }
            };
        }
    }

    public static Function customFunc(String name, Callback callback, int argc) {
        return customFunc(name, callback, argc, false);
    }

    public static String cat(String string) {
        StringBuilder builder = new StringBuilder();
        for (char c : string.toCharArray()){
            switch (c) {
                case '\n': builder.append("\\n"); break;
                case '\r': builder.append("\\r"); break;
                case '\b': builder.append("\\b"); break;
                case '\f': builder.append("\\f"); break;
                case '\t': builder.append("\\t"); break;
                case '\'': builder.append("\\'"); break;
                case '\"': builder.append("\\\""); break;
                case '\0': builder.append("\\0"); break;
                default:
                    builder.append(c);
                    break;
            }
        }
        return builder.toString();
    }

    public static float asFloat(Object object) {
        return ((Number)object).floatValue();
    }

    public static double asDouble(Object object) {
        return ((Number)object).doubleValue();
    }

    public static int asInt(Object object) {
        return ((Number)object).intValue();
    }

    public static long asLong(Object object) {
        return ((Number)object).longValue();
    }

    public static String asString(Object object) {
        if (object == NONE)
            return null;
        return String.valueOf(object);
    }

    public static long length(Object object) {
        if (object instanceof Collection) {
            Collection<?> collection = (Collection<?>) object;
            return collection.size();
        }
        if (object instanceof CharSequence) {
            return ((CharSequence)object).length();
        }
        if (object instanceof Range) {
            return ((Range) object).length();
        }
        return -1;
    }

    public static boolean isFloatingPoint(Object object) {
        return object instanceof Double || object instanceof Float;
    }
}
