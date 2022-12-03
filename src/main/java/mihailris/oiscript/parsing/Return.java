package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;

public class Return extends Command {
    private final Value value;
    public Return(Position position, Value value) {
        super(position);
        this.value = value;
    }

    @Override
    public void execute(Context context) {
        context.returned = true;
        context.returnValue = value.eval(context);
    }

    @Override
    public String toString() {
        if (value == null)
            return super.toString()+"return";
        return super.toString()+"return "+value;
    }
}
