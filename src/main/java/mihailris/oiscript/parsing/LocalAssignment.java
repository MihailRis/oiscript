package mihailris.oiscript.parsing;

import mihailris.oiscript.Arithmetics;
import mihailris.oiscript.Context;
import mihailris.oiscript.SemanticContext;
import mihailris.oiscript.exceptions.ParsingException;

public class LocalAssignment extends Command {
    private final String name;
    private final int index;
    private final String operator;
    private Value value;
    public LocalAssignment(Position position, String name, int index, String operator, Value value) {
        super(position);
        this.name = name;
        this.index = index;
        this.operator = operator;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public Value getValue() {
        return value;
    }

    public String getOperator() {
        return operator;
    }

    @Override
    public void execute(Context context) {
        Object object = value.eval(context);
        switch (operator) {
            case "=": context.locals[index] = object; break;
            case "-=": context.locals[index] = Arithmetics.subtract(context.get(name), object); break;
            case "+=": context.locals[index] = Arithmetics.add(context.get(name), object); break;
            case "*=": context.locals[index] = Arithmetics.multiply(context.get(name), object); break;
            case "/=": context.locals[index] = Arithmetics.divide(context.get(name), object); break;
            default:
                throw new IllegalStateException("not implemented for '"+operator+"'");
        }
    }

    @Override
    public String toString() {
        return super.toString()+name+"<"+index+"> "+operator+" "+value;
    }
}
