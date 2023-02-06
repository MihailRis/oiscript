package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.OiObject;

import java.util.List;

public class ItemAssignment extends Command {
    private final Value source;
    private final Value key;
    private final String operator;
    private final Value value;

    public ItemAssignment(Position position, Value source, Value key, String operator, Value value) {
        super(position);
        this.source = source;
        this.key = key;
        this.operator = operator;
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(Context context) {
        Object source = this.source.eval(context);
        if (source instanceof OiObject) {
            ((OiObject)source).set(key.eval(context), value.eval(context));
        } else if (source instanceof List) {
            List<Object> list = (List<Object>) source;
            list.set(((Number)key.eval(context)).intValue(), value.eval(context));
        } else {
            throw new RuntimeException("unable to change item of "+source);
        }
    }

    @Override
    public String toString() {
        return super.toString()+source+"["+key+"] "+operator+" "+value;
    }
}
