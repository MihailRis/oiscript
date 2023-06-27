package mihailris.oiscript.parsing;

import mihailris.oiscript.SemanticContext;
import mihailris.oiscript.exceptions.ParsingException;

import java.util.List;

public class Elif extends Command {
    private Value condition;
    private final List<Command> commands;
    private If ifnode;
    public Elif(Position position, Value condition, List<Command> commands) {
        super(position);
        this.condition = condition;
        this.commands = commands;
    }

    @Override
    public Command build(SemanticContext context) throws ParsingException {
        condition = condition.build(context);
        return super.build(context);
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
