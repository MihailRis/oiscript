package mihailris.oiscript;

public class Arithmetics {
    public static Object subtract(Object a, Object b) {
        if (OiUtils.isFloatingPoint(a) || OiUtils.isFloatingPoint(b)){
            return ((Number)a).doubleValue() -  ((Number)a).doubleValue();
        }
        long da;
        long db;
        if (a instanceof Character)
            da = (char)a;
        else
            da = ((Number) a).longValue();
        if (b instanceof Character)
            db = (char)b;
        else
            db = ((Number) b).longValue();
        return da - db;
    }

    public static Object add(Object a, Object b) {
        if (a instanceof String || b instanceof String) {
            return String.valueOf(a) + b;
        }
        if (OiUtils.isFloatingPoint(a) || OiUtils.isFloatingPoint(b)){
            return ((Number)a).doubleValue() + ((Number)b).doubleValue();
        }
        long da;
        long db;
        if (a instanceof Character)
            da = (char)a;
        else
            da = ((Number) a).longValue();
        if (b instanceof Character)
            db = (char)b;
        else
            db = ((Number) b).longValue();
        return da + db;
    }

    public static Object multiply(Object a, Object b) {
        Number nb = (Number) b;
        if (a instanceof String) {
            long multiplier = nb.longValue();
            if (multiplier == 0)
                return "";
            else if (multiplier == 1) {
                return a;
            }
            StringBuilder builder = new StringBuilder((String)a);
            for (int i = 1; i < multiplier; i++) {
                builder.append(a);
            }
            return builder.toString();
        }
        Number na = (Number) a;
        return na.doubleValue() * nb.doubleValue();
    }

    public static Object divide(Object a, Object b) {
        Number na = (Number) a;
        Number nb = (Number) b;
        return na.doubleValue() / nb.doubleValue();
    }

    public static Object modulo(Object a, Object b) {
        Number na = (Number) a;
        Number nb = (Number) b;
        return na.doubleValue() % nb.doubleValue();
    }

    public static Object negative(Object a) {
        Number number = (Number) a;
        if (number.doubleValue() == number.longValue())
            return -number.longValue();
        return -number.doubleValue();
    }
}
