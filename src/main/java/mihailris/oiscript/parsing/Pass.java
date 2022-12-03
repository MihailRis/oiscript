package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;

public class Pass extends Command {
    public Pass(Position position) {
        super(position);
    }

    @Override
    public void execute(Context context) {
    }

    @Override
    public String toString() {
        return "<pass>";
    }
}
