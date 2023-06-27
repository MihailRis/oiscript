package mihailris.oiscript.parsing;

import mihailris.oiscript.Arithmetics;
import mihailris.oiscript.Context;
import mihailris.oiscript.SemanticContext;
import mihailris.oiscript.exceptions.ParsingException;

public class Assignment extends Command {
    private final String name;
    private final String operator;
    private Value value;
    public Assignment(Position position, String name, String operator, Value value) {
        super(position);
        this.name = name;
        this.operator = operator;
        this.value = value;
    }

    @Override
    public Command build(SemanticContext context) throws ParsingException {
        value = value.build(context);
        int index = context.getFunction().getLocalIndex(name);
        if (index != -1) {
            return new LocalAssignment(position, name, index, operator, value);
        } else {
            return new LocalAssignment(position, name, context.getFunction().defineLocal(name), operator, value);
        }
        //return super.build(context);
    }

    @Override
    public void execute(Context context) {
        Object object = value.eval(context);
        switch (operator) {
            case "=": context.namespace.put(name, object); break;
            case "-=": context.namespace.put(name, Arithmetics.subtract(context.get(name), object)); break;
            case "+=": context.namespace.put(name, Arithmetics.add(context.get(name), object)); break;
            case "*=": context.namespace.put(name, Arithmetics.multiply(context.get(name), object)); break;
            case "/=": context.namespace.put(name, Arithmetics.divide(context.get(name), object)); break;
            default:
                throw new IllegalStateException("not implemented for '"+operator+"'");
        }
    }

    @Override
    public String toString() {
        return super.toString()+name+" "+operator+" "+value;
    }
}
