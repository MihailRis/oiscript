package mihailris.oiscript;

import mihailris.oiscript.runtime.Function;

import java.util.ArrayList;

public class OiUtils {
    public static void requreArgCount(String funcname, int count, Object... args) {
        if (count != args.length) {
            throw new IllegalArgumentException("func "+funcname+": "+count+" arguments excepted, got "+args.length);
        }
    }

    public interface Callback {
        Object call(Context context, Object... args);
    }

    public static Function customFunc(String name, Callback callback, int argc) {
        return new Function(name, new ArrayList<>(), null) {
            @Override
            public Object execute(Context context, Object... args) {
                if (argc >= 0)
                    OiUtils.requreArgCount(getName(), argc, args);
                return callback.call(context, args);
            }
        };
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

    public static boolean isFloatingPoint(Object object) {
        return object instanceof Double || object instanceof Float;
    }
}
