package mihailris.oiscript;

public class Logics {
    public static boolean isTrue(Object object) {
        if ((object instanceof Boolean)) {
            return (Boolean) object;
        } else if (object instanceof Number) {
            Number number = (Number) object;
            return number.doubleValue() != 0.0;
        } else if (object instanceof String) {
            return !((String)object).isEmpty();
        }
        return true;
    }

    public static boolean equals(Object leftValue, Object rightValue) {
        return leftValue.equals(rightValue);
    }

    public static boolean less(Object leftValue, Object rightValue) {
        if (!(leftValue instanceof Number))
            throw new IllegalArgumentException("'<' is not applicable to "+leftValue.getClass().getSimpleName());
        if (!(rightValue instanceof Number))
            throw new IllegalArgumentException("'<' is not applicable to "+rightValue.getClass().getSimpleName());
        Number lvalue = (Number) leftValue;
        Number rvalue = (Number) rightValue;
        return lvalue.doubleValue() < rvalue.doubleValue();
    }

    public static boolean greather(Object leftValue, Object rightValue) {
        if (!(leftValue instanceof Number))
            throw new IllegalArgumentException("'>' is not applicable to "+leftValue.getClass().getSimpleName());
        if (!(rightValue instanceof Number))
            throw new IllegalArgumentException("'>' is not applicable to "+rightValue.getClass().getSimpleName());
        Number lvalue = (Number) leftValue;
        Number rvalue = (Number) rightValue;
        return lvalue.doubleValue() > rvalue.doubleValue();
    }

    public static boolean lequals(Object leftValue, Object rightValue) {
        if (!(leftValue instanceof Number))
            throw new IllegalArgumentException("'<=' is not applicable to "+leftValue.getClass().getSimpleName());
        if (!(rightValue instanceof Number))
            throw new IllegalArgumentException("'<=' is not applicable to "+rightValue.getClass().getSimpleName());
        Number lvalue = (Number) leftValue;
        Number rvalue = (Number) rightValue;
        return lvalue.doubleValue() <= rvalue.doubleValue();
    }

    public static boolean gequals(Object leftValue, Object rightValue) {
        if (!(leftValue instanceof Number))
            throw new IllegalArgumentException("'>=' is not applicable to "+leftValue.getClass().getSimpleName());
        if (!(rightValue instanceof Number))
            throw new IllegalArgumentException("'>=' is not applicable to "+rightValue.getClass().getSimpleName());
        Number lvalue = (Number) leftValue;
        Number rvalue = (Number) rightValue;
        return lvalue.doubleValue() >= rvalue.doubleValue();
    }
}
