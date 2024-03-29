package mihailris.oiscript;

import mihailris.oiscript.parsing.Range;

import java.util.Collection;

public class Logics {
    public static boolean isTrue(Object object) {
        if ((object instanceof Boolean)) {
            return (Boolean) object;
        } else if (object instanceof Number) {
            Number number = (Number) object;
            return number.doubleValue() != 0.0;
        } else if (object instanceof CharSequence) {
            return !((String)object).isEmpty();
        } else if (object instanceof Collection) {
            return !((Collection<?>)object).isEmpty();
        } else return object != OiNone.NONE;
    }

    public static boolean in(Object leftValue, Object rightValue){
        if (rightValue instanceof Collection) {
            Collection<?> collection = (Collection<?>) rightValue;
            return collection.contains(leftValue);
        }
        if (rightValue instanceof String) {
            String string = (String) rightValue;
            return string.contains(String.valueOf(leftValue));
        }
        if (rightValue instanceof Range) {
            Range range = (Range) rightValue;
            long value = ((Number) leftValue).longValue();
            return range.contains(value);
        }
        if (rightValue instanceof OiObject) {
            OiObject object = (OiObject) rightValue;
            return object.has(leftValue);
        }
        return false;
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

    public static Object and(Object leftValue, Object rightValue) {
        if (!isTrue(leftValue))
            return leftValue;
        return rightValue;
    }

    public static Object or(Object leftValue, Object rightValue) {
        if (isTrue(leftValue))
            return leftValue;
        return rightValue;
    }
}
