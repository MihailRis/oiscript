package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.OiObject;

public class Include extends Command {
    private final String name;
    public Include(Position position, String name) {
        super(position);
        this.name = name;
    }

    @Override
    public void execute(Context context) {
        context.script.extend((OiObject) context.script.get(name));
    }

    @Override
    public String toString() {
        return "include "+name;
    }
}
