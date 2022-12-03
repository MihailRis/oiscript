package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.exceptions.BreakSignal;

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
}
