package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.SemanticContext;
import mihailris.oiscript.exceptions.ParsingException;

public class Wait extends Command {
    private final Value durationValue;

    public Wait(Position position, Value durationValue) {
        super(position);
        this.durationValue = durationValue;
    }

    @Override
    public void execute(Context context) {
        context.runHandle.waitDelay(((Number)durationValue.eval(context)).longValue());
    }

    @Override
    public String toString() {
        return super.toString()+"wait "+durationValue;
    }
}
