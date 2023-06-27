package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.OiNone;
import mihailris.oiscript.SemanticContext;
import mihailris.oiscript.exceptions.ParsingException;

public class Return extends Command {
    private Value value;
    public Return(Position position, Value value) {
        super(position);
        this.value = value;
    }

    @Override
    public void execute(Context context) {
        context.returned = true;
        if (value == null) {
            context.returnValue = OiNone.NONE;
        } else {
            context.returnValue = value.eval(context);
        }
    }

    @Override
    public Command build(SemanticContext context) throws ParsingException {
        value = value.build(context);
        return super.build(context);
    }

    @Override
    public String toString() {
        if (value == null)
            return super.toString()+"return";
        return super.toString()+"return "+value;
    }

    public Value getValue() {
        return value;
    }
}
