package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.exceptions.ContinueSignal;

public class Continue extends Command {
    private static final ContinueSignal signal = new ContinueSignal();
    public Continue(Position position) {
        super(position);
    }

    @Override
    public void execute(Context context) {
        throw signal;
    }

    @Override
    public String toString() {
        return super.toString()+"continue";
    }
}
