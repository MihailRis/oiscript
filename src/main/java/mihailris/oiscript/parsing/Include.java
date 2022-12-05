package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.OiObject;

public class Include extends Command {
    private final Value value;
    public Include(Position position, Value value) {
        super(position);
        this.value = value;
    }

    @Override
    public void execute(Context context) {
        context.included.extend((OiObject) value.eval(context));
    }

    @Override
    public String toString() {
        return "include "+value;
    }
}
