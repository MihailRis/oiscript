package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.OiObject;

public class AttributeAssignment extends Command {
    private final Value source;
    private final String name;
    private final String operator;
    private final Value value;
    public AttributeAssignment(Position position, Value source, String name, String operator, Value value) {
        super(position);
        this.source = source;
        this.name = name;
        this.operator = operator;
        this.value = value;
    }

    @Override
    public void execute(Context context) {
        OiObject object = (OiObject) source.eval(context);
        object.set(name, value.eval(context));
    }

    @Override
    public String toString() {
        return super.toString()+source+"."+name+" "+operator+" "+value;
    }
}
