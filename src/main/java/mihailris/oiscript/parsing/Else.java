package mihailris.oiscript.parsing;

import java.util.List;

public class Else extends Command {
    private final List<Command> commands;
    public Else(Position position, List<Command> commands) {
        super(position);
        this.commands = commands;
    }

    @Override
    public String toString() {
        return super.toString()+"else";
    }

    @Override
    public List<Command> getCommands() {
        return commands;
    }
}
