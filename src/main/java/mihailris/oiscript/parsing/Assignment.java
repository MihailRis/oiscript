package mihailris.oiscript.parsing;

import mihailris.oiscript.Arithmetics;
import mihailris.oiscript.Context;

public class Assignment extends Command {
    private final String name;
    private final String operator;
    private final Value value;
    public Assignment(Position position, String name, String operator, Value value) {
        super(position);
        this.name = name;
        this.operator = operator;
        this.value = value;
    }

    @Override
    public void execute(Context context) {
        switch (operator) {
            case "=": context.namespace.put(name, value.eval(context)); break;
            case "-=": context.namespace.put(name, Arithmetics.subtract(context.get(name), value.eval(context))); break;
            case "+=": context.namespace.put(name, Arithmetics.add(context.get(name), value.eval(context))); break;
            case "*=": context.namespace.put(name, Arithmetics.multiply(context.get(name), value.eval(context))); break;
            case "/=": context.namespace.put(name, Arithmetics.divide(context.get(name), value.eval(context))); break;
            default:
                throw new IllegalStateException("not implemented for '"+operator+"'");
        }
    }

    @Override
    public String toString() {
        return super.toString()+name+" "+operator+" "+value;
    }
}
