package mihailris.oiscript.parsing;

import java.util.List;

public class Elif extends Command {
    private final Value condition;
    private final List<Command> commands;
    private If ifnode;
    public Elif(Position position, Value condition, List<Command> commands) {
        super(position);
        this.condition = condition;
        this.commands = commands;
    }

    @Override
    public String toString() {
        return super.toString()+"elif "+condition;
    }

    @Override
    public List<Command> getCommands() {
        return commands;
    }

    public void setIfNode(If ifnode) {
        this.ifnode = ifnode;
    }

    public If getIf() {
        return ifnode;
    }

    public Value getCondition() {
        return condition;
    }
}
