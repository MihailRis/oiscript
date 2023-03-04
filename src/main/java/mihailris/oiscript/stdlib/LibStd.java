package mihailris.oiscript.stdlib;

import mihailris.oiscript.*;
import mihailris.oiscript.runtime.Function;
import mihailris.oiscript.runtime.OiModule;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static mihailris.oiscript.OiUtils.*;

public class LibStd extends OiModule {
    public static final Random random = new Random();

    public LibStd() {
        set("_version", OI.VERSION_STRING);
        set("_out", System.out);
        set("_err", System.err);
        set("_scripts", new OiObject());
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
        set("bool", customFunc("bool", (context, args) -> {
            Object arg = args[0];
            return Logics.isTrue(arg);
        }, 1));
        set("str", customFunc("str", (context, args) -> {
            Object arg = args[0];
            return String.valueOf(arg);
        }, 1));
        set("rand", customFunc("rand", (context, args) -> random.nextFloat(), 0));
        set("shuffle", customFunc("shuffle", (context, args) -> {
            List<?> list = (List<?>) args[0];
            Collections.shuffle(list, random);
            return list;
        }, 1));
        set("len", customFunc("len", (context, args) -> {
            Object arg = args[0];
            return OiUtils.length(arg);
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
            PrintStream out = (PrintStream) get("_out");
            for (int i = 0; i < args.length; i++) {
                out.print(args[i]);
                if (i+1 < args.length) {
                    out.print(' ');
                }
            }
            out.println();
            return OiNone.NONE;
        }, -1));

        set("nanotime", customFunc("nanotime", (context, args) -> System.nanoTime() / 1000000000.0, 0));

        set("_sync", customFunc("_sync", (context, args) -> {
            System.out.println("LibStd.LibStd SYNC "+Thread.currentThread());
            return OiNone.NONE;
        }, 0, true));

        set("$new", customFunc("$new", (context, args) -> {
            OiObject prototype = (OiObject) args[0];
            OiObject object = new OiObject().extend(prototype);
            object.set("_proto", prototype);
            Function initMethod = object.getMethod("_init");
            if (initMethod != null) {
                args[0] = object;
                initMethod.execute(context, args);
            }
            return object;
        }, -1));
    }
}
