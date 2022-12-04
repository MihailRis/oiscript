package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.exceptions.OiRuntimeException;

public class ValueWrapper extends Command {
    private final Value value;
    public ValueWrapper(Position position, Value value) {
        super(position);
        this.value = value;
    }

    @Override
    public String toString() {
        return super.toString()+value.toString();
    }

    @Override
    public void execute(Context context) {
        try {
            value.eval(context);
        } catch (RuntimeException e) {
            throw new OiRuntimeException(position, e);
        }
    }
}
