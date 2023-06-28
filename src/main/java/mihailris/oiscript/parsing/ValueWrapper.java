package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.SemanticContext;
import mihailris.oiscript.exceptions.OiRuntimeException;
import mihailris.oiscript.exceptions.ParsingException;

public class ValueWrapper extends Command {
    private Value value;
    public ValueWrapper(Position position, Value value) {
        super(position);
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public Command build(SemanticContext context) throws ParsingException {
        value = value.build(context);
        return super.build(context);
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
