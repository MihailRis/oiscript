package mihailris.oiscript.stdlib;

import mihailris.oiscript.OiObject;

import static mihailris.oiscript.OiUtils.customFunc;

public class LibMath extends OiObject {
    public LibMath() {
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
        set("PI", Math.PI);
        set("E", Math.E);
        set("min", customFunc("min", (context, args) -> {
            if (args.length <= 1)
                throw new IllegalArgumentException("at least two arguments required");
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
