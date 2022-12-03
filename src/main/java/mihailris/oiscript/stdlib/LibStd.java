package mihailris.oiscript.stdlib;

import mihailris.oiscript.OiNone;
import mihailris.oiscript.OiObject;
import mihailris.oiscript.OiUtils;
import mihailris.oiscript.OiVector;

import java.util.Collection;
import java.util.Random;

import static mihailris.oiscript.OiUtils.customFunc;

public class LibStd extends OiObject {
    public static final Random random = new Random();

    public LibStd() {
        set("int", customFunc("int", (context, args) -> {
            Object arg = args[0];
            if (arg instanceof String) {
                return Long.parseLong((String) arg);
            } else if (arg instanceof Number) {
                return ((Number)arg).longValue();
            } else if (arg instanceof Character) {
                return (int)(char)arg;
            }
            throw new IllegalArgumentException("unable to cast "+arg.getClass().getSimpleName()+" to int");
        }, 1));
        set("rand", customFunc("rand", (context, args) -> random.nextFloat(), 0));
        set("len", customFunc("len", (context, args) -> {
            Object arg = args[0];
            if (arg instanceof String) {
                return ((String) arg).length();
            } else if (arg instanceof Collection) {
                return ((Collection<?>) arg).size();
            }
            return -1;
        }, 1));
        set("vector", customFunc("vector", (context, args) -> {
            Object arg = args[0];

            OiVector vector = new OiVector();
            if (arg instanceof String) {
                String text = (String)arg;
                for (char c : text.toCharArray()){
                    vector.add(c);
                }
            } else if (arg instanceof Collection) {
                vector.addAll((Collection<?>) arg);
            } else if (arg instanceof Iterable) {
                for (Object object : (Iterable<?>)arg) {
                    vector.add(object);
                }
            }
            return vector;
        }, 1));
        set("chr", customFunc("chr", (context, args) -> {
            Object arg = args[0];
            if (arg instanceof Number) {
                return (char)((Number)arg).shortValue();
            }
            throw new IllegalArgumentException("unable to cast "+arg.getClass().getSimpleName()+" to char");
        }, 1));
        set("cat", customFunc("cat", (context, args) -> {
            Object arg = args[0];
            if (arg instanceof String) {
                return '"'+ OiUtils.cat((String) arg)+'"';
            }
            return arg.toString();
        }, 1));
        set("keys", customFunc("keys", (context, args) -> {
            Object arg = args[0];
            if (arg instanceof OiObject) {
                OiObject object = (OiObject) arg;
                return object.keys();
            }
            return new OiVector();
        }, 1));
        set("endl", System.lineSeparator());
        set("print", customFunc("print", (context, args) -> {
            for (int i = 0; i < args.length; i++) {
                System.out.print(args[i]);
                if (i+1 < args.length) {
                    System.out.print(' ');
                }
            }
            System.out.println();
            return OiNone.NONE;
        }, -1));
    }
}
