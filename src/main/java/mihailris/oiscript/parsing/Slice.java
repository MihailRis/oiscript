package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.OiUtils;
import mihailris.oiscript.OiVector;
import mihailris.oiscript.SemanticContext;
import mihailris.oiscript.exceptions.ParsingException;

import java.util.ArrayList;
import java.util.List;

public class Slice extends Value {
    private Value value;
    private Value start;
    private Value end;
    private Value step;

    public Slice(Value value, Value start, Value end, Value step) {
        this.value = value;
        this.start = start;
        this.end = end;
        this.step = step;
    }

    public Value getValue() {
        return value;
    }

    public Value getStart() {
        return start;
    }

    public Value getEnd() {
        return end;
    }

    public Value getStep() {
        return step;
    }

    @Override
    public Value build(SemanticContext context) throws ParsingException {
        value = value.build(context);
        if (start != null) start = start.build(context);
        if (end != null) end = end.build(context);
        if (step != null) step = step.build(context);
        return super.build(context);
    }

    @Override
    public Object eval(Context context) {
        Object object = value.eval(context);

        Object start = null;
        Object end = null;
        Object step = null;
        if (this.start != null) start = this.start.eval(context);
        if (this.end != null) end = this.end.eval(context);
        if (this.step != null) step = this.step.eval(context);

        return slice(object, start, end, step);
    }

    public static Object slice(Object object, Object startObject, Object endObject, Object stepObject) {
        int length = (int) OiUtils.length(object);
        int start = 0;
        int end = length;
        int step = 1;
        if (startObject != null) start = OiUtils.asInt(startObject);
        if (endObject != null) end = OiUtils.asInt(endObject);
        if (stepObject != null) step = OiUtils.asInt(stepObject);
        if (start < 0) start += length;
        if (end < 0) end += length;
        return _slice(object, start, end, step);
    }

    public static Object _slice(Object object, int start, int end, int step) {
        if (object instanceof String) {
            String string = (String) object;
            if (step == 1) {
                return string.substring(start, end);
            } else {
                StringBuilder builder = new StringBuilder();
                if (step < 0) {
                    for (int i = end-1; i >= 0; i += step) {
                        builder.append(string.charAt(i));
                    }
                } else {
                    for (int i = start; i < end; i += step) {
                        builder.append(string.charAt(i));
                    }
                }
                return builder.toString();
            }
        } else if (object instanceof List) {
            List<?> list = (List<?>) object;
            List<Object> dest;
            if (object instanceof OiVector) {
                dest = new OiVector();
            } else {
                dest = new ArrayList<>();
            }
            if (step < 0) {
                for (int i = end-1; i >= 0; i += step) {
                    dest.add(list.get(i));
                }
            } else {
                for (int i = start; i < end; i += step) {
                    dest.add(list.get(i));
                }
            }
            return dest;
        }
        throw new IllegalArgumentException("not implemented for "+object.getClass());
    }

    @Override
    public String toString() {
        String string = value+"[";
        if (start != null)
            string += start;
        string += ":";
        if (end != null)
            string += end;
        if (step != null) {
            string += ":"+step;
        }
        return string + "]";
    }
}
