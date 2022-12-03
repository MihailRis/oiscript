package mihailris.oiscript.parsing;

import java.util.List;

public class CForLoop extends Command {
    private final Command initCommand;
    private final Value condition;
    private final Command repeatCommand;
    private final List<Command> commands;
    public CForLoop(Position position, Command initCommand, Value condition, Command repeatCommand, List<Command> commands) {
        super(position);
        this.initCommand = initCommand;
        this.condition = condition;
        this.repeatCommand = repeatCommand;
        this.commands = commands;
    }

    @Override
    public String toString() {
        return super.toString()+"<for "+initCommand+"; "+condition+"; "+repeatCommand+">";
    }

    @Override
    public List<Command> getCommands() {
        return commands;
    }
}
