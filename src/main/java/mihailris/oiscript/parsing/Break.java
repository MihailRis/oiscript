package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.SemanticContext;
import mihailris.oiscript.exceptions.BreakSignal;
import mihailris.oiscript.exceptions.ParsingException;

public class Break extends Command {
    private static final BreakSignal signal = new BreakSignal();
    public Break(Position position) {
        super(position);
    }

    @Override
    public void execute(Context context) {
        throw signal;
    }

    @Override
    public String toString() {
        return super.toString()+"break";
    }

    @Override
    public Command build(SemanticContext context) throws ParsingException {
        if (!context.isLoop()) {
            throw new ParsingException(null, position, "break outside of loop");
        }
        return super.build(context);
    }
}
