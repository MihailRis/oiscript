package mihailris.oiscript.stdlib;

import mihailris.oiscript.OiUtils;
import mihailris.oiscript.runtime.OiModule;

import static mihailris.oiscript.OiUtils.customFunc;
import static mihailris.oiscript.OiUtils.requreArgCount;

public class LibMath extends OiModule {
    public LibMath() {
        set("sqrt", customFunc("sqrt", (context, args) -> {
            double arg = ((Number)args[0]).doubleValue();
            return Math.sqrt(arg);
        }, 1));
        set("sin", customFunc("sin", (context, args) -> {
            double arg = ((Number)args[0]).doubleValue();
            return Math.sin(arg);
        }, 1));
        set("cos", customFunc("cos", (context, args) -> {
            double arg = ((Number)args[0]).doubleValue();
            return Math.cos(arg);
        }, 1));
        set("tan", customFunc("tan", (context, args) -> {
            double arg = ((Number)args[0]).doubleValue();
            return Math.tan(arg);
        }, 1));
        set("atan", customFunc("atan", (context, args) -> {
            double arg = ((Number)args[0]).doubleValue();
            return Math.atan(arg);
        }, 1));
        set("asin", customFunc("asin", (context, args) -> {
            double arg = ((Number)args[0]).doubleValue();
            return Math.asin(arg);
        }, 1));
        set("ascos", customFunc("acos", (context, args) -> {
            double arg = ((Number)args[0]).doubleValue();
            return Math.acos(arg);
        }, 1));
        set("abs", customFunc("abs", (context, args) -> {
            if (OiUtils.isFloatingPoint(args[0])){
                return Math.abs(((Number)args[0]).doubleValue());
            }
            return Math.abs(((Number)args[0]).longValue());
        }, 1));
        set("PI", Math.PI);
        set("E", Math.E);
        set("min", customFunc("min", (context, args) -> {
            if (args.length <= 1)
                throw new IllegalArgumentException("at least two arguments required");
            if (args[0] instanceof Iterable) {
                Iterable<?> iterable = (Iterable<?>) args[0];
                requreArgCount("min", 1, args);
                Number min = null;
                for (Object arg : iterable){
                    Number other = (Number) arg;
                    if (min == null || other.doubleValue() < min.doubleValue()) {
                        min = other;
                    }
                }
                return min;
            }
            Number min = (Number) args[0];
            for (int i = 1; i < args.length; i++) {
                Number other = (Number) args[i];
                if (other.doubleValue() < min.doubleValue()) {
                    min = other;
                }
            }
            return min;
        }, -1));

        set("max", customFunc("max", (context, args) -> {
            if (args.length <= 1)
                throw new IllegalArgumentException("at least two arguments required");
            if (args[0] instanceof Iterable) {
                Iterable<?> iterable = (Iterable<?>) args[0];
                requreArgCount("max", 1, args);
                Number max = null;
                for (Object arg : iterable){
                    Number other = (Number) arg;
                    if (max == null || other.doubleValue() > max.doubleValue()) {
                        max = other;
                    }
                }
                return max;
            }
            Number max = (Number) args[0];
            for (int i = 1; i < args.length; i++) {
                Number other = (Number) args[i];
                if (other.doubleValue() > max.doubleValue()) {
                    max = other;
                }
            }
            return max;
        }, -1));

        set("sum", customFunc("sum", (context, args) -> {
            double sum = 0.0;
            Iterable<?> iterable = (Iterable<?>) args[0];
            for (Object object : iterable) {
                Number number = (Number) object;
                sum += number.doubleValue();
            }
            return sum;
        }, 1));

        set("avg", customFunc("avg", (context, args) -> {
            double sum = 0.0;
            int count = 0;
            Iterable<?> iterable = (Iterable<?>) args[0];
            for (Object object : iterable) {
                Number number = (Number) object;
                sum += number.doubleValue();
                count++;
            }
            return sum / count;
        }, 1));
    }
}
