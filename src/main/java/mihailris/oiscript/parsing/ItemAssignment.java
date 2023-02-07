package mihailris.oiscript.parsing;

import mihailris.oiscript.Arithmetics;
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
            Object newObject = value.eval(context);
            int index = ((Number) key.eval(context)).intValue();
            if (operator.equals("=")) {
                list.set(index, newObject);
                return;
            }
            Object previous = list.get(index);
            switch (operator) {
                case "+=":
                    list.set(index, Arithmetics.add(previous, newObject));
                    break;
                case "-=":
                    list.set(index, Arithmetics.subtract(previous, newObject));
                    break;
                case "*=":
                    list.set(index, Arithmetics.multiply(previous, newObject));
                    break;
                case "/=":
                    list.set(index, Arithmetics.divide(previous, newObject));
                    break;
                case "//":
                    list.set(index, Arithmetics.divideInteger(previous, newObject));
                    break;
                case "%=":
                    list.set(index, Arithmetics.modulo(previous, newObject));
                    break;
                case "**=":
                    list.set(index, Arithmetics.power(previous, newObject));
                    break;
                default:
                    throw new RuntimeException("not implemented for '"+operator+"'");
            }
        } else {
            throw new RuntimeException("unable to change item of "+source);
        }
    }

    @Override
    public String toString() {
        return super.toString()+source+"["+key+"] "+operator+" "+value;
    }
}
