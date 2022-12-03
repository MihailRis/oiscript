package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;

import java.util.List;

public abstract class Command {
    protected final Position position;

    public Command(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "";
    }

    public List<Command> getCommands(){
        return null;
    }

    public void execute(Context context) {
        throw new IllegalStateException("not implemented in "+getClass().getSimpleName());
    }
}
